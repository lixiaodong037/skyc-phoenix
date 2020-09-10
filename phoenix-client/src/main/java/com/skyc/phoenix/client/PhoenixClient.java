/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client;

import java.io.IOException;

import com.skyc.phoenix.client.core.RecordAccumulator;
import com.skyc.phoenix.client.network.ByteBufferSend;
import com.skyc.phoenix.client.network.NetworkClient;
import com.skyc.phoenix.client.network.Send;
import com.skyc.phoenix.client.record.PhoenixRecord;

/**
 * An PhoenixClient that send records to the phoenixServer
 */
public final class PhoenixClient {

    private final NetworkClient networkClient;

    public PhoenixClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public void close() throws IOException {
    }

    public void send(PhoenixRecord pr) {
        Send send = new ByteBufferSend();

    }
}
