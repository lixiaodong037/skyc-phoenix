/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.client.network.ByteBufferSend;
import com.skyc.phoenix.client.network.NetworkClient;
import com.skyc.phoenix.client.network.Send;

/**
 * A thread for getting the
 *
 * @since JDK1.6
 */
public class Sender implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    private final NetworkClient networkClient;
    private final RecordAccumulator accumulator;
    private final int maxSizeToSend;
    private final int maxNumToSend;
    private volatile boolean running;
    private volatile boolean forceClose;

    public Sender(NetworkClient networkClient, boolean running, RecordAccumulator accumulator, int maxSizeToSend,
                  int maxNumToSend) {
        this.networkClient = networkClient;
        this.running = running;
        this.accumulator = accumulator;
        this.maxSizeToSend = maxSizeToSend;
        this.maxNumToSend = maxNumToSend;
    }

    @Override
    public void run() {
        log.info("Starting phoenix sender thread.");

        // process the bathces
        while (running) {
            List<RecordBatch> recordBatchList = this.accumulator.drainBySize(maxSizeToSend);
            for(RecordBatch recordBatch : recordBatchList) {
                Send send = new ByteBufferSend(recordBatch.buffers());
                networkClient.send(send);
            }
        }

        // when stop accepting requests but there may still be requests in the accumulator or waiting for
        // acknowledgment, wait until these are completed



    }

    public void close() throws IOException {
        this.accumulator.close();
        running = false;
        networkClient.close();
    }

    /**
     * Wake up the selector associated with this send thread
     */
    public void wakeup() {
        this.networkClient.wakeup();
    }
}
