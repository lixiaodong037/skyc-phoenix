/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.server.start;

import com.skyc.phoenix.server.PhoenixServer;
import com.skyc.phoenix.server.config.PhoenixServerConfig;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Server 启动类，引入springboot作为启动方案
 *
 * @author brucelee
 * @since jdk1.6
 */
public class PhoenixServerStart {

    public static void main(String[] args) {
//        new SpringApplicationBuilder().sources(PhoenixServerStart.class).web(WebApplicationType.NONE).
//                registerShutdownHook(true).build().run(args);
        PhoenixServerConfig phoenixServerConfig = new PhoenixServerConfig();
        PhoenixServer phoenixServer = new PhoenixServer(NioServerSocketChannel.class, phoenixServerConfig);
        phoenixServer.start();
        System.out.println("start end....");
    }
}
