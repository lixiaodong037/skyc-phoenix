/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.skyc.phoenix.common.node.ServerNode;
import com.skyc.phoenix.common.record.PhoenixRecord;

/**
 * The metadata for every {@link PhoenixRecord}, when sending.
 *
 * @author brucelee
 * @since jdk 1.6
 */
public final class RecordBatchResult {
    // every RecordBatch has one RecordResult, and it's complete action will be called by one thread
    private final CountDownLatch latch = new CountDownLatch(1);
    // a exception from remote
    private volatile RuntimeException error;
    private volatile ServerNode node;
    private volatile long resultTimestamp;

    public RecordBatchResult() {
    }

    /**
     * This method will be called when complete the send
     *
     * @param node
     * @param error
     * @param resultTimestamp
     */
    public void set(ServerNode node, long resultTimestamp, RuntimeException error) {
        this.node = node;
        this.error = error;
        this.resultTimestamp = resultTimestamp;
    }

    public ServerNode getNode() {
        if (node == null) {
            return ServerNode.UNKNOWN_NODE;
        }
        return node;
    }

    public long getResultTimestamp() {
        return resultTimestamp;
    }

    public void done() {
        if (this.node == null) {
            throw new IllegalStateException("The method `set` must be invoked before this method.");
        }
        this.latch.countDown();
    }

    /**
     * The thread who call the {@link #await()} function will wait the other thread to call {@link #done()}
     *
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        this.latch.await();
    }

    /**
     * The thread who call the {@link #await()} function will wait the other thread to call {@link #done()}
     *
     * @throws InterruptedException
     */
    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        this.latch.await(timeout, unit);
    }

    public boolean completed() {
        return this.latch.getCount() == 0L;
    }
}
