/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SocketChannel;

/**
 * Bytebuffer to send interface, the function of the class will really send content to remote.
 *
 * @author brucelee
 */
public interface Send {

     /**
     * Is this send complete?
     */
    boolean completed();

    /**
     * the data to channel
     *
     * @param channel the channel to write
     * @return return the size of write
     */
    long writeTo(GatheringByteChannel channel) throws IOException;

    /**
     * Size of the send
     */
    long size();
}
