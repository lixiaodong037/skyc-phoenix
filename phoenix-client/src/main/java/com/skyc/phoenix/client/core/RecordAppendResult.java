package com.skyc.phoenix.client.core;

import com.skyc.phoenix.client.record.FutureRecordMetaData;

public class RecordAppendResult {
    private FutureRecordMetaData futureRecordMetaData;
    private boolean batchIsFull;
    private boolean isNewBatchCreated;

    public RecordAppendResult(FutureRecordMetaData futureRecordMetaData, boolean batchIsFull,
                              boolean isNewBatchCreated) {
        this.futureRecordMetaData = futureRecordMetaData;
        this.batchIsFull = batchIsFull;
        this.isNewBatchCreated = isNewBatchCreated;
    }

    public FutureRecordMetaData getFutureRecordMetaData() {
        return futureRecordMetaData;
    }

    public void setFutureRecordMetaData(FutureRecordMetaData futureRecordMetaData) {
        this.futureRecordMetaData = futureRecordMetaData;
    }

    public boolean isBatchIsFull() {
        return batchIsFull;
    }

    public void setBatchIsFull(boolean batchIsFull) {
        this.batchIsFull = batchIsFull;
    }

    public boolean isNewBatchCreated() {
        return isNewBatchCreated;
    }

    public void setNewBatchCreated(boolean newBatchCreated) {
        isNewBatchCreated = newBatchCreated;
    }
}
