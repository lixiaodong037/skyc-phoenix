/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

/**
 *
 */
public interface RecordBody {

    byte[] getBodyAsBytes();

    RecordBody parseToBody(byte[] body);
}
