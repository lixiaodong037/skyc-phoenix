/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.server.config;

import java.util.Properties;

import com.skyc.phoenix.common.config.BaseConfig;

/**
 * The serve config
 *
 * @author brucelee
 */
public class PhoenixServerConfig extends BaseConfig {

    public static final String PHOENIX_SERVER_PORT = "phoenix.server.port";
    public static final String PHOENIX_SERVER_ACCEPTOR_THREAD_NUM = "phoenix.server.acceptor.thread.num";
    public static final String PHOENIX_SERVER_WORKER_THREAD_NUM = "phoenix.server.worker.thread.num";

    /**
     * 用于检测服务端连接通道的读写空闲时间，如果超过空闲时间，通道（tcp连接）会被自动关闭，客户端要做好重连动作
      */
    public static class ConnectionIdle {
        public static final String CONNECTION_IDLE_CHECK_OPEN = "connection.idle.check.open";
        public static final String READER_IDLE_TIME_IN_SECONDS = "reader.idle.time.in.seconds";
        public static final String WRITER_IDLE_TIME_IN_SECONDS = "writer.idle.time.in.seconds";
        public static final String ALL_IDLE_TIME_IN_SECONDS = "all.idle.time.in.seconds";
    }

    /**
     * tcp连接相关配置
     */
    public static class TcpConfig {
        public static final String SO_BACKLOG = "so.backlog";
        public static final String SO_KEEP_ALIVE = "so.keep.alive";
        public static final String SO_REUSE_ADDR = "so.reuse.address";
        public static final String SO_LINGER = "so.linger";
        public static final String SO_SND_BUF = "so.send.buffer";
        public static final String SO_RCV_BUF = "so.receive.buffer";
        public static final String TCP_NO_DELAY = "tcp.no.delay";
        public static final String SO_TIMEOUT = "so.timeout";
    }

    /**
     * rpc请求数据相关配置
     */
    public static class RpcDataConfig {
        public static final String MAX_RPC_DATA_SIZE = "max.rpc.data.size";
    }

    /**
     * 默认配置的properties
     */
    private static Properties DEF_CONFIG;

    static {
        DEF_CONFIG = new Properties();
        /**
         *  base config
          */
        DEF_CONFIG.put(PHOENIX_SERVER_PORT, 9400);
        DEF_CONFIG.put(PHOENIX_SERVER_ACCEPTOR_THREAD_NUM, 1);
        int workerNum = Runtime.getRuntime().availableProcessors() * 2;
        DEF_CONFIG.put(PHOENIX_SERVER_WORKER_THREAD_NUM, workerNum);

        /**
         * connection idle config
          */
        DEF_CONFIG.put(ConnectionIdle.CONNECTION_IDLE_CHECK_OPEN, true);
        DEF_CONFIG.put(ConnectionIdle.READER_IDLE_TIME_IN_SECONDS, 1800);
        DEF_CONFIG.put(ConnectionIdle.WRITER_IDLE_TIME_IN_SECONDS, 1800);
        DEF_CONFIG.put(ConnectionIdle.ALL_IDLE_TIME_IN_SECONDS, 1800);

        /**
         * tcp config
         */
        DEF_CONFIG.put(TcpConfig.SO_BACKLOG, 128);
        DEF_CONFIG.put(TcpConfig.SO_KEEP_ALIVE, true);
        DEF_CONFIG.put(TcpConfig.SO_REUSE_ADDR, true);
        // 值为5s：socket调用了close方法后底层socket过5s后再关闭，因为可能有额外数据要发送；
        // 如果值为0：客户端会丢弃缓冲区中的数据，直接进入close状态，不会有timewait状态，直接发送rst
        DEF_CONFIG.put(TcpConfig.SO_LINGER, 5);
        // 发送和接收缓冲区32KB
        DEF_CONFIG.put(TcpConfig.SO_SND_BUF, 1024 * 32);
        DEF_CONFIG.put(TcpConfig.SO_RCV_BUF, 1024 * 32);
        // 默认不开启nagle算法，这样传输是管道式传输，会快很多
        DEF_CONFIG.put(TcpConfig.TCP_NO_DELAY, true);

        // 3s内没有数据传输就抛出timeout或者interruptedIO异常，在抛出例外后，输入流并未关闭，你可以继续通过read 方法读取数据
        DEF_CONFIG.put(TcpConfig.SO_TIMEOUT, 3000);

        // 每次收到的包大小最大16M
        DEF_CONFIG.put(RpcDataConfig.MAX_RPC_DATA_SIZE, 16 * 1024 * 1024);

    }

    public PhoenixServerConfig() {
        super(DEF_CONFIG);
    }

    public PhoenixServerConfig(Properties properties) {
        super(properties);
    }

    public Boolean isConnIdleCheckOpen() {
        if (getBoolean(ConnectionIdle.CONNECTION_IDLE_CHECK_OPEN) == null) {
            return true;
        }
        return getBoolean(ConnectionIdle.CONNECTION_IDLE_CHECK_OPEN);
    }

    public int readerIdleTimeInSec() {
        return getInt(ConnectionIdle.READER_IDLE_TIME_IN_SECONDS);
    }

    public int writerIdleTimeInSec() {
        return getInt(ConnectionIdle.WRITER_IDLE_TIME_IN_SECONDS);
    }

    public int allIdleTimeInSec() {
        return getInt(ConnectionIdle.ALL_IDLE_TIME_IN_SECONDS);
    }

    public int maxRpcDataSize() {
        return getInt(RpcDataConfig.MAX_RPC_DATA_SIZE);
    }

    public int getTcpSoBackLog() {
        return getInt(TcpConfig.SO_BACKLOG);
    }

    public int getTcpSoLinger() {
        return getInt(TcpConfig.SO_LINGER);
    }

    public boolean getTcpSoKeepAlive() {
        return getBoolean(TcpConfig.SO_KEEP_ALIVE);
    }

    public boolean getTcpReuseAddr() {
        return getBoolean(TcpConfig.SO_REUSE_ADDR);
    }

    public int getSoTimeout() {
        return getInt(TcpConfig.SO_TIMEOUT);
    }
    public boolean getTcpNoDelay() {
        return getBoolean(TcpConfig.TCP_NO_DELAY);
    }

    public int getTcpReceiveBuf() {
        return getInt(TcpConfig.SO_RCV_BUF);
    }

    public int getTcpSendBuf() {
        return getInt(TcpConfig.SO_SND_BUF);
    }

}
