/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

public class RecordMetaData {

    private final RecordBatchResult recordBatchResult;
    private final long timestamp;
    private final int recordCount;

    public RecordMetaData(RecordBatchResult recordBatchResult, long timestamp, int recordCount) {
        this.recordBatchResult = recordBatchResult;
        this.timestamp = timestamp;
        this.recordCount = recordCount;
    }

    public RecordBatchResult getRecordBatchResult() {
        return recordBatchResult;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRecordCount() {
        return recordCount;
    }
}
