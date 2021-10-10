/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.Arrays;

/**
 * The ByteBuffers to send.
 */
public class ByteBufferSend implements Send {

    /**
     * buffers
     */
    private final ByteBuffer[] buffers;
    private final int size;
    private int remaining = 0;
    private String destination;

    public ByteBufferSend(ByteBuffer... buffers) {
        this.buffers = buffers;
        for (ByteBuffer byteBuffer : buffers) {
            this.remaining = this.remaining + byteBuffer.remaining();
        }
        this.size = this.remaining;
    }

    public ByteBuffer[] buffers() {
        return buffers;
    }

    @Override
    public long writeTo(GatheringByteChannel channel) throws IOException {
        long written = channel.write(this.buffers);
        if (written < 0) {
            throw new EOFException("Wrote negative bytes to channel, This shouldn't happen.");
        }
        remaining -= written;
        return written;
    }

    @Override
    public boolean completed() {
        return remaining <= 0;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public String destination() {
        return this.destination;
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
