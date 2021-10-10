/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client;

import java.util.List;
import java.util.concurrent.Future;

import com.skyc.phoenix.client.server.DefaultServerAddressParser;
import com.skyc.phoenix.common.exception.PhoenixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.client.config.PhoenixClientConfig;
import com.skyc.phoenix.client.core.RecordAccumulator;
import com.skyc.phoenix.client.core.RecordAccumulatorImpl;
import com.skyc.phoenix.client.core.RecordAppendResult;
import com.skyc.phoenix.client.core.Sender;
import com.skyc.phoenix.client.network.NetworkClient;
import com.skyc.phoenix.client.network.NetworkClientImpl;
import com.skyc.phoenix.common.record.PhoenixRecord;
import com.skyc.phoenix.client.record.RecordCallback;
import com.skyc.phoenix.client.record.RecordMetaData;
import com.skyc.phoenix.common.node.ServerAddressParser;
import com.skyc.phoenix.common.node.ServerNode;

/**
 * An PhoenixClient that send records to the phoenixServer
 *
 * @author brucelee
 * @since jdk1.6
 */
public final class PhoenixClient {

    private static final Logger log = LoggerFactory.getLogger(PhoenixClient.class);

    private final PhoenixClientConfig phoenixClientConfig;

    private final RecordAccumulator recordAccumulator;

    private final NetworkClient networkClient;

    private final Sender sender;

    private final Thread senderThread;

    private final long totalMemorySize;

    private final int batchSize;

    private final int lingMs;

    private final long maxBlockTimeMs;

    private final int maxInflightRequestPerConnection;

    private final String bootstrapServers;

    private final String connectionPickerType;

    private final ServerAddressParser serverAddressParser;

    public PhoenixClient(PhoenixClientConfig phoenixClientConfig) {
        this(phoenixClientConfig, new DefaultServerAddressParser());
    }

    public PhoenixClient(PhoenixClientConfig phoenixClientConfig, ServerAddressParser serverAddressParser) {
        if (phoenixClientConfig == null) {
            throw new PhoenixException("the input client config cannot be null!");
        }
        this.phoenixClientConfig = phoenixClientConfig;
        this.serverAddressParser = serverAddressParser;
        this.totalMemorySize = phoenixClientConfig.getLong(PhoenixClientConfig.BUFFER_MEMORY);
        this.batchSize = phoenixClientConfig.getInt(PhoenixClientConfig.BATCH_SIZE);
        this.bootstrapServers = phoenixClientConfig.getString(PhoenixClientConfig.BOOTSTRAP_SERVERS);
        this.connectionPickerType = phoenixClientConfig.getString(PhoenixClientConfig.CONNECTION_PICKER_TYPE);
        this.lingMs = phoenixClientConfig.getInt(PhoenixClientConfig.LING_MS);
        this.maxBlockTimeMs = phoenixClientConfig.getInt(PhoenixClientConfig.MAX_BLOCK_MS);
        this.maxInflightRequestPerConnection = phoenixClientConfig.getInt(
                PhoenixClientConfig.MAX_INFLIGHT_REQUEST_PER_CONNECTION);
        this.recordAccumulator = new RecordAccumulatorImpl(batchSize, totalMemorySize, lingMs);
        String connectionPickerType = phoenixClientConfig.getString(PhoenixClientConfig.CONNECTION_PICKER_TYPE);

        List<ServerNode> serverNodeList = this.serverAddressParser.parseToServerNodes(this.bootstrapServers);
        boolean initConnectionForce = phoenixClientConfig.getBoolean(PhoenixClientConfig.INIT_CONNECTION_FORCE);
        int sendBufferSize = phoenixClientConfig.getInt(PhoenixClientConfig.SEND_BUFFER);
        int receiveBufferSize = phoenixClientConfig.getInt(PhoenixClientConfig.RECEIVE_BUFFER);
        int retryTimes = phoenixClientConfig.getInt(PhoenixClientConfig.RETRIES);
        this.networkClient =
                new NetworkClientImpl(serverNodeList, initConnectionForce, sendBufferSize, receiveBufferSize,
                        connectionPickerType, retryTimes, maxInflightRequestPerConnection);

        int maxSizeToSend = phoenixClientConfig.getInt(PhoenixClientConfig.MAX_SIZE_TO_SEND);
        int maxNumToSend = phoenixClientConfig.getInt(PhoenixClientConfig.MAX_NUM_TO_SEND);

        int acks = phoenixClientConfig.getInt(PhoenixClientConfig.ACKS);
        this.sender = new Sender(networkClient, recordAccumulator, acks, maxSizeToSend, maxNumToSend);
        this.senderThread = new Thread(sender, "phoenix-sender-thread");
        this.senderThread.start();
    }

    /**
     * Close this producer. This method blocks until all previously sent requests complete.
     */
    public void close() {
        log.info("closing the phoenix client...");
        // check whether the close() function is invoke from the application thread
        boolean invokedFromCallback = Thread.currentThread() == this.senderThread;
        if (this.sender != null && this.senderThread != null && this.senderThread.isAlive()) {
            this.sender.forceClose();
            if (!invokedFromCallback) {
                try {
                    this.senderThread.join();
                } catch (InterruptedException e) {
                    throw new PhoenixException("Failed to close kafka producer", e);
                }
            }
        }
    }

    public Future<RecordMetaData> send(PhoenixRecord pr, RecordCallback callback) {
        try {
            RecordAppendResult result = this.recordAccumulator.append(pr, callback, maxBlockTimeMs);
            if (result.isBatchIsFull() || result.isNewBatchCreated()) {
                log.trace("Waking up the sender since current batch is full or getting a new batch");
                this.sender.wakeup();
            }
            return result.getFutureRecordMetaData();
        } catch (InterruptedException e) {
            log.error("current thread was interrupted during sending...");
        }
        return null;
    }
}
