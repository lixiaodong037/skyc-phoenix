/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.client.exception.PhoenixClientException;
import com.skyc.phoenix.client.network.ByteBufferSend;
import com.skyc.phoenix.client.network.ClientRequest;
import com.skyc.phoenix.client.network.ClientResponse;
import com.skyc.phoenix.client.network.NetworkClient;
import com.skyc.phoenix.client.network.RequestCompletionHandler;
import com.skyc.phoenix.client.network.Send;
import com.skyc.phoenix.client.record.RecordBatch;

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
    private final int acks;
    // when the running is false, we will not accept any record from client
    private volatile boolean running = true;
    // when forceClose is true, we will abandon all the records in the accumulator
    private volatile boolean forceClose;

    public Sender(NetworkClient networkClient, RecordAccumulator accumulator, int acks, int maxSizeToSend,
     int maxNumToSend) {
        this.networkClient = networkClient;
        this.running = running;
        this.accumulator = accumulator;
        this.maxSizeToSend = maxSizeToSend;
        this.maxNumToSend = maxNumToSend;
        this.acks = acks;
    }

    @Override
    public void run() {
        log.info("Starting phoenix sender thread.");

        // process the bathces until sender was closed
        while (running) {
            try {
                doRun();
            } catch (Exception e) {
                log.error("Send record batch to client exception.", e);
            }
        }

        // when stop accepting requests but there may still be requests in the accumulator or waiting for
        // acknowledgment, wait until these are completed
        while (!forceClose && this.accumulator.hasUnDrained()) {
            try {
                doRun();
            } catch (Exception e) {
                log.error("Send record batch to client exception.", e);
            }
        }

        // abort the unDrained records
        if (forceClose) {
            log.debug("Aborting incomplete batches due to forced shutdown");
            this.abort(System.currentTimeMillis(), new PhoenixClientException("PhoenixClient was closed forcefully."));
        }

        // close the client
        try {
            this.networkClient.close();
        } catch (Exception e) {
            log.error("Failed to close network client", e);
        }
    }

    private void doRun() {
        AccumulatorReadyCheckResult checkResult = this.accumulator.readyCheck();

        if (checkResult.isReady()) {
            List<RecordBatch> recordBatchList = this.accumulator.drainBySize(maxSizeToSend);
            ByteBuffer[] buffers = new ByteBuffer[recordBatchList.size()];
            List<RequestCompletionHandler> callbacks = new ArrayList<RequestCompletionHandler>(recordBatchList.size());
            for (int i = 0; i < recordBatchList.size(); i++) {
                RecordBatch recordBatch = recordBatchList.get(i);
                System.out.println();
                ByteBuffer byteBuffer = (ByteBuffer) recordBatch.buffer();
                System.out.println(byteBuffer.limit() + "-" + byteBuffer.position());
                buffers[i] = (ByteBuffer) recordBatch.buffer().flip();
//                buffers[i] = (ByteBuffer) recordBatch.buffer().rewind();
                RequestCompletionHandler callback = new RequestCompletionHandler() {
                    public void onComplete(ClientResponse response) {
                        handleClientResponse(response, recordBatch, System.currentTimeMillis());
                    }
                };
                callbacks.add(callback);
            }
            ClientRequest clientRequest = new ClientRequest(new ByteBufferSend(buffers), callbacks, acks != 0);
            networkClient.send(clientRequest);
        }

        try {
            this.networkClient.poll(checkResult.getNextReadyCheckDelayMs());
        } catch (IOException e) {
            log.error("poll the network exception!", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException!", e);
        }
    }

    private void handleClientResponse(ClientResponse clientResponse, RecordBatch recordBatch, long timestamp) {
        System.out.println("we all handling response from " + clientResponse.getDestinationServerNode());
    }

    private void abort(long abortTimestamp, RuntimeException reason) {
        this.accumulator.abortUnDrainedRecords(abortTimestamp, reason);
    }

    public void close() {
        this.accumulator.close();
        this.running = false;
        this.wakeup();
    }

    public void forceClose() {
        this.forceClose = true;
        this.close();
    }

    /**
     * Wake up the selector associated with this send thread
     */
    public void wakeup() {
        this.networkClient.wakeup();
    }
}
