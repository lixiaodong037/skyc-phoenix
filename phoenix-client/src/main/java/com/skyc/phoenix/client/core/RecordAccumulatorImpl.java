package com.skyc.phoenix.client.core;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.skyc.phoenix.client.buffer.BufferPool;
import com.skyc.phoenix.client.exception.PhoenixException;
import com.skyc.phoenix.client.record.PhoenixRecord;

/**
 * aggregate every {@link PhoenixRecord} into the {@link RecordBatch} which hold a {@link java.nio.ByteBuffer}
 * for writing the every record bytes into.
 *
 * @author brucelee
 * @since jdk1.6
 */
public final class RecordAccumulatorImpl implements RecordAccumulator {

    private Deque<RecordBatch> batches;

    private BufferPool bufferPool;

    private AtomicInteger appendsInProgress = new AtomicInteger(0);

    private int batchSize;

    private long totalBufferPoolSisze;

    private volatile boolean closed = false;

    public RecordAccumulatorImpl(int batchSize, long totalBufferPoolSisze) {
        this.batches = new ArrayDeque<RecordBatch>();
        this.bufferPool = new BufferPool(batchSize, totalBufferPoolSisze);
    }

    /**
     * @param record
     * @param maxTimeToBlockMs
     *
     * @return
     */
    @Override
    public RecordAppendResult append(PhoenixRecord record, int maxTimeToBlockMs) throws InterruptedException {
        appendsInProgress.incrementAndGet();

        try {
            synchronized(batches) {
                if (closed) {
                    throw new PhoenixException("the accumulator and sender was closed.");
                }
                RecordAppendResult appendResult = tryAppend(record);
                if (appendResult != null) {
                    return appendResult;
                }
            }

            // computer the maxsize for allocate
            int size = Math.max(this.batchSize, record.size());
            ByteBuffer buffer = this.bufferPool.allocate(size, maxTimeToBlockMs);

            synchronized(batches) {
                // try append first, because other thread may have allocated the buffer
                RecordAppendResult appendResult = tryAppend(record);
                if (appendResult != null) {
                    this.bufferPool.release(buffer);
                    return appendResult;
                }

                RecordBatch rb = new RecordBatch(buffer);
                appendResult = rb.tryAppend(record);
                batches.addLast(rb);

                return appendResult;
            }
        } finally {
            appendsInProgress.decrementAndGet();
        }
    }

    @Override
    public List<RecordBatch> drainBySize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("the drained size is lt 0");
        }
        int currSize = 0;
        List<RecordBatch> ready = new ArrayList<RecordBatch>();
        synchronized(batches) {
            while (currSize < maxSize) {
                RecordBatch rb = batches.peekFirst();
                if (currSize + rb.estimatedSizeInBytes() > maxSize && !ready.isEmpty()) {
                    break;
                }

                rb = batches.pollFirst();
                currSize = currSize + rb.estimatedSizeInBytes();
                ready.add(rb);
            }
        }
        return ready;
    }

    @Override
    public List<RecordBatch> drainByRecordNum(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("the drained num is lt 0");
        }
        int currNum = 0;
        List<RecordBatch> ready = new ArrayList<RecordBatch>();
        synchronized(batches) {
            while (currNum < num) {
                RecordBatch rb = batches.peekFirst();
                rb = batches.pollFirst();
                currNum++;
                ready.add(rb);
            }
        }
        return ready;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    private RecordAppendResult tryAppend(PhoenixRecord record) {
        RecordBatch last = batches.peekLast();
        if (last != null) {
            return last.tryAppend(record);
        }
        return null;
    }

}
