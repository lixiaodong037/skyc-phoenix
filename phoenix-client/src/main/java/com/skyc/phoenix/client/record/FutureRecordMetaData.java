/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureRecordMetaData implements Future<RecordMetaData> {

    private RecordMetaData recordMetaData;

    public FutureRecordMetaData(RecordMetaData recordMetaData) {
        this.recordMetaData = recordMetaData;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return recordMetaData.getRecordBatchResult().completed();
    }

    @Override
    public RecordMetaData get() throws InterruptedException, ExecutionException {
        recordMetaData.getRecordBatchResult().await();
        return recordMetaData;
    }

    @Override
    public RecordMetaData get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            recordMetaData.getRecordBatchResult().await(timeout, unit);
        return recordMetaData;
    }

    public RecordMetaData getRecordMetaData() {
        return recordMetaData;
    }

    public void setRecordMetaData(RecordMetaData recordMetaData) {
        this.recordMetaData = recordMetaData;
    }
}
