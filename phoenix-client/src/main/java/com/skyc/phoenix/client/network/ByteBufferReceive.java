package com.skyc.phoenix.client.network;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;

import com.skyc.phoenix.client.buffer.BufferPool;

/**
 * receive from channel into
 */
public class ByteBufferReceive implements Receive {

    private ByteBuffer sizeBuffer;

    private BufferPool bufferPool;

    private ByteBuffer contentBuffer;

    private String sendDest;

    private int maxSize;

    public void ByteBufferReceive(BufferPool bufferPool) {
        this.sizeBuffer = ByteBuffer.allocate(4);
        this.bufferPool = bufferPool;
        this.contentBuffer = null;
    }

    public ByteBufferReceive(String sendDest) {
        this.sendDest = sendDest;
    }

    @Override
    public int readFrom(ScatteringByteChannel channel) throws IOException, InterruptedException {
        int read = 0;
        if (channel == null) {
            throw new IllegalArgumentException("the channel cannot be null!");
        }
        if (sizeBuffer.hasRemaining()) {
            int bytesRead = channel.read(sizeBuffer);
            if (bytesRead < 0) {
                throw new EOFException("cannot read any bytes from channel");
            }
            read = read + bytesRead;
            if (!sizeBuffer.hasRemaining()) {
                sizeBuffer.rewind();
                int contentSize = sizeBuffer.getInt();
                if (contentSize > 0) {
                    contentBuffer = bufferPool.allocate(contentSize, -1);
                    int contentBytesRead = channel.read(contentBuffer);
                    if (contentBytesRead < 0) {
                        throw new EOFException("cannot read any content bytes from channel");
                    }
                    read = read + contentBytesRead;
                }
            }
        }

        return read;
    }

    @Override
    public boolean completed() {
        return !sizeBuffer.hasRemaining() && contentBuffer != null & !contentBuffer.hasRemaining();
    }

    @Override
    public ByteBuffer payload() {
        return this.contentBuffer;
    }

    @Override
    public void close() {
        if (contentBuffer != null) {
            bufferPool.release(contentBuffer);
            contentBuffer = null;
        }
    }

    @Override
    public String sendDest() {
        return this.sendDest;
    }
}
