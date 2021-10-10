/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.record;

/**
 *
 */
public interface RecordBody {

    byte[] getBodyAsBytes();

    RecordBody parseToBody(byte[] body);
}
