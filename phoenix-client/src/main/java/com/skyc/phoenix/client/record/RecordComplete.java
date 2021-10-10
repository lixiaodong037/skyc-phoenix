/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

public class RecordComplete {
    private final RecordMetaData recordMetaData;
    private final RecordCallback recordCallback;

    public RecordComplete(RecordMetaData recordMetaData, RecordCallback recordCallback) {
        this.recordMetaData = recordMetaData;
        this.recordCallback = recordCallback;
    }

    public RecordMetaData getRecordMetaData() {
        return recordMetaData;
    }

    public RecordCallback getRecordCallback() {
        return recordCallback;
    }
}
