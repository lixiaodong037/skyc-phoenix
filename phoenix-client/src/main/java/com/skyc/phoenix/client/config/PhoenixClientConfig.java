/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.config;

import java.util.Properties;

import com.skyc.phoenix.common.config.BaseConfig;

/**
 * A Configuration for the Phoenix Client.
 *
 * @author brucelee
 * @since jdk1.6
 */
public class PhoenixClientConfig extends BaseConfig {

    public static final String BUFFER_MEMORY = "buffer.memory";
    public static final String BATCH_SIZE = "batch.size";
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    public static final String SEND_BUFFER = "send.buffer.bytes";
    public static final String RECEIVE_BUFFER = "receive.buffer.bytes";
    public static final String RETRIES = "retries";
    public static final String CONNECTION_PICKER_TYPE = "connection.picker.type";
    public static final String INIT_CONNECTION_FORCE = "init.connection.force";
    public static final String MAX_SIZE_TO_SEND = "max.size.to.send";
    public static final String MAX_NUM_TO_SEND = "max.num.to.send";
    public static final String LING_MS = "ling.ms";
    public static final String MAX_BLOCK_MS = "max.block.ms";
    public static final String MAX_INFLIGHT_REQUEST_PER_CONNECTION = "max.inflight.request.per.connection";
    public static final String ACKS = "acks";

    public static PhoenixClientConfig defaultClientConfig() {
        Properties properties = new Properties();
        properties.put(BUFFER_MEMORY, 32 * 1024 * 1024L);
        properties.put(BATCH_SIZE, 16384);
        properties.put(SEND_BUFFER, 128 * 1024);
        properties.put(RECEIVE_BUFFER, 32 * 1024);
        properties.put(RETRIES, 3);
        properties.put(CONNECTION_PICKER_TYPE, "random");
        properties.put(INIT_CONNECTION_FORCE, true);
        properties.put(MAX_SIZE_TO_SEND, 1 * 1024 * 1024);
        properties.put(MAX_NUM_TO_SEND, 10);
        properties.put(LING_MS, 0);
        properties.put(MAX_BLOCK_MS, 2000);
        properties.put(MAX_INFLIGHT_REQUEST_PER_CONNECTION, 5);
        properties.put(ACKS, 0);

        return new PhoenixClientConfig(properties);
    }

    public PhoenixClientConfig(Properties properties) {
        super(properties);
    }

}
