/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 服务端通道空闲后处理器
 *
 * @author brucelee
 * @since jdk1.6
 */
public class ServerChannelIdleHandler extends ChannelDuplexHandler {

    private int allIdleTimeout;

    public ServerChannelIdleHandler(int allIdleTimeout) {
        this.allIdleTimeout = allIdleTimeout;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (IdleState.ALL_IDLE.equals(idleStateEvent.state())) {
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
