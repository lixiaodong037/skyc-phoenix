/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.server;

import com.skyc.phoenix.common.record.PhoenixRecord;
import com.skyc.phoenix.server.config.PhoenixServerConfig;
import com.skyc.phoenix.server.handler.RpcDataCompressOutBoundHandler;
import com.skyc.phoenix.server.handler.ServerChannelIdleHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端入口类，启动一个tcp服务，并绑定端口
 * 整个服务端底层使用netty，reactor模式
 */
public final class PhoenixServer {

    private static final Logger log = LoggerFactory.getLogger(PhoenixServer.class);

    private PhoenixServerConfig phoenixServerConfig;
    private Class<? extends ServerChannel> serverSocketChannelClass;

    private DefaultThreadFactory bossThreadFacotry;
    private DefaultThreadFactory workerThreadFacotry;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public PhoenixServer(Class<? extends ServerChannel> serverSocketChannelClass,
                         PhoenixServerConfig phoenixServerConfig) {
        this.phoenixServerConfig = phoenixServerConfig;
        this.serverSocketChannelClass = serverSocketChannelClass;
    }

    public void start() {
        if (log.isInfoEnabled()) {
            log.info("PhoenixServer is beginning start at port: {}", getPort());
        }
        bossThreadFacotry = new DefaultThreadFactory("phoenix-server-acceptorThread");
        workerThreadFacotry = new DefaultThreadFactory("phoenix-server-workerThread");
        bossGroup = new NioEventLoopGroup(getAcceptorThreads(), bossThreadFacotry);
        workerGroup = new NioEventLoopGroup(getWorkerThreads(), bossThreadFacotry);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(serverSocketChannelClass)
//                       .option(ChannelOption.SO_BACKLOG, phoenixServerConfig.getTcpSoBackLog())
                .childHandler(new ChildChannelInitializerHandler());

        setChildOptions(serverBootstrap);

        try {
            serverBootstrap.bind(getPort()).sync();
        } catch (InterruptedException e) {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            throw new IllegalStateException("PhoenixServer.serverBootstrap was interrupted!", e);
        }
    }

    /**
     * 设置每个worker中socket的配置信息
     *
     * @param serverBootstrap
     */
    private void setChildOptions(ServerBootstrap serverBootstrap) {
//        serverBootstrap.childOption(ChannelOption.SO_BACKLOG, phoenixServerConfig.getTcpSoBackLog())
        serverBootstrap.childOption(ChannelOption.SO_LINGER, phoenixServerConfig.getTcpSoLinger())
                .childOption(ChannelOption.SO_KEEPALIVE, phoenixServerConfig.getTcpSoKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, phoenixServerConfig.getTcpNoDelay())
                .childOption(ChannelOption.SO_SNDBUF, phoenixServerConfig.getTcpSendBuf())
                .childOption(ChannelOption.SO_RCVBUF, phoenixServerConfig.getTcpReceiveBuf())
                .childOption(ChannelOption.SO_REUSEADDR, phoenixServerConfig.getTcpReuseAddr());
//                .childOption(ChannelOption.SO_TIMEOUT, phoenixServerConfig.getSoTimeout());
    }

    private int getPort() {
        return this.phoenixServerConfig.getInt(PhoenixServerConfig.PHOENIX_SERVER_PORT);
    }

    private int getAcceptorThreads() {
        return this.phoenixServerConfig.getInt(PhoenixServerConfig.PHOENIX_SERVER_ACCEPTOR_THREAD_NUM);
    }

    private int getWorkerThreads() {
        return this.phoenixServerConfig.getInt(PhoenixServerConfig.PHOENIX_SERVER_WORKER_THREAD_NUM);
    }


    private static final String CHANNEL_IDEL_STATE_CHECK_HANDLER = "channel_idle_state_check_handler";
    private static final String CHANNEL_IDEL_HANDLER = "channel_idle_handler";

    /**
     * 每个worker都要拥有独立的NioEventLoop，都要初始化自己的Channel，这里是为每个worker初始化Channel的代码
     * 就是每个worker都会做下面的事情
     */
    private class ChildChannelInitializerHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline channelPipe = ch.pipeline();
            // receive request data
            if (phoenixServerConfig.isConnIdleCheckOpen()) {
                channelPipe.addLast(CHANNEL_IDEL_STATE_CHECK_HANDLER,
                        new IdleStateHandler(phoenixServerConfig.readerIdleTimeInSec(),
                                phoenixServerConfig.writerIdleTimeInSec(), phoenixServerConfig.allIdleTimeInSec()));
                channelPipe.addLast(CHANNEL_IDEL_HANDLER,
                        new ServerChannelIdleHandler(phoenixServerConfig.allIdleTimeInSec()));
            }


            /**
             * 请求
             */
            int messageLengthFieldStart = PhoenixRecord.RECORD_LENGTH_OFFSET;
            int messageLengthFieldWidth = PhoenixRecord.RECORD_LENGTH;
            int lengthAdjustment = 0;
            int initialBytesToStrip = PhoenixRecord.RECORD_OFFSET; //只获取真实的记录长度，所以跳过所有前面的
            // 先按长度方式获取对应的网络包
            channelPipe.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(phoenixServerConfig.maxRpcDataSize(),
                    messageLengthFieldStart, messageLengthFieldWidth, lengthAdjustment, initialBytesToStrip));
            // 把网络包解析成本地的结构
//            RpcDataInBoundDecoder rpcDataInBoundDecoder =
//                    new RpcDataInBoundDecoder();
//            channelPipe.addLast("rpcDataInBoundDecoder", rpcDataInBoundDecoder);
//
//            // 解压网络包体内的内容
//            channelPipe.addLast("rpcDataUnCompressInBoundHandler", new RpcDataUnCompressInBoundHandler());

            // 解压内容进行RpcDataBizHandler处理，这是业务处理，目前暂时用netty推荐的方式：在reactor模式下让每个nioEventLoop
            // 独立处理业务，避免上线文切换，如果线程数不够，就扩大workerGroup的线程数
            channelPipe.addLast("rpcDataBizHandler", new RpcDataBizHandler());

            /**
             * 响应
             */
            // 压缩返回体包体内容
            channelPipe.addFirst("rpcDataCompressOutBoundHandler", new RpcDataCompressOutBoundHandler());
            // 打包
//            channelPipe.addFirst(SERVER_DATA_PACK, new RpcDataPackageEncoder());
        }
    }

    /**
     * 处理真实解压的数据
     * <p>
     * 这里重点是先把数据放入本地磁盘channel里面
     */
    private class RpcDataBizHandler extends SimpleChannelInboundHandler {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                    System.currentTimeMillis()).toString() : "BAD ORDER";
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }
}
