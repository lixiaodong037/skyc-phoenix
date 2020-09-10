/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

import com.skyc.phoenix.client.record.PhoenixRecord;

/**
 * The header of one {@link PhoenixRecord},
 *
 * the value for this Object will be decoded for dispatching in the server.
 */
public interface RecordHeader {

    /**
     * Header Object in bytes
     *
     * @return the bytes array for this object
     */
    byte[] getHeaderAsBytes();

    /**
     * parse the bytes to header
     * @param header
     * @return
     */
    RecordHeader parseToHeader(byte[] header);
}
