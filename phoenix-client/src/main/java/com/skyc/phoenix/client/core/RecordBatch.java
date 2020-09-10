package com.skyc.phoenix.client.core;

import java.nio.ByteBuffer;

import com.skyc.phoenix.client.record.PhoenixRecord;

/**
 * Contain several {@link com.skyc.phoenix.client.record.PhoenixRecord} in the RecordBatch
 *
 * @author brucelee
 */
public final class RecordBatch {

    /**
     * a bytebuffer from BufferPool which will return to the pool after send to remote
     */
    private ByteBuffer byteBuffer;

    private int recordNum = 0;

    private int recordSize = 0;

    public RecordBatch(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.recordNum = recordNum;
        this.recordSize = recordSize;
    }

    public ByteBuffer buffers() {
        return byteBuffer;
    }

    public RecordAppendResult tryAppend(PhoenixRecord pr) {
        if (!hasEnoughBuffer(pr.size())) {
            return null;
        }
        pr.appendTo(byteBuffer);

        recordNum++;
        recordSize = recordSize + pr.size();

        return new RecordAppendResult();
    }

    public int estimatedSizeInBytes() {
        return recordSize;
    }

    private boolean hasEnoughBuffer(int inputSize) {
        if (byteBuffer.hasRemaining() && byteBuffer.remaining() < inputSize) {
            return false;
        }
        return true;
    }
}