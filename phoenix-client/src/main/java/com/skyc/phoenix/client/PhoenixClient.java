/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client;

import java.io.IOException;

import com.skyc.phoenix.client.config.PhoenixClientConfig;
import com.skyc.phoenix.client.core.RecordAccumulator;
import com.skyc.phoenix.client.core.RecordAccumulatorImpl;
import com.skyc.phoenix.client.network.ByteBufferSend;
import com.skyc.phoenix.client.network.NetworkClient;
import com.skyc.phoenix.client.network.Send;
import com.skyc.phoenix.client.record.PhoenixRecord;

/**
 * An PhoenixClient that send records to the phoenixServer
 */
public final class PhoenixClient {

    private final PhoenixClientConfig phoenixClientConfig;

    private final RecordAccumulator recordAccumulator;

    private final long totalMemorySize;

    private final int batchSize;

    public PhoenixClient(PhoenixClientConfig phoenixClientConfig) {
        this.phoenixClientConfig = phoenixClientConfig;
        this.totalMemorySize = phoenixClientConfig.getLong(PhoenixClientConfig.BUFFER_MEMORY_CONFIG);
        this.batchSize = phoenixClientConfig.getInt(PhoenixClientConfig.BATCH_SIZE_CONFIG);
        this.recordAccumulator = new RecordAccumulatorImpl(batchSize, totalMemorySize);

    }

    public void close() throws IOException {

    }

    public void send(PhoenixRecord pr) {
        Send send = new ByteBufferSend();

    }
}
