/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

/**
 * Callback when sending every record.
 */
public interface RecordCallback {

    void onCompletion(RecordMetaData metadata, Exception exception);
}
