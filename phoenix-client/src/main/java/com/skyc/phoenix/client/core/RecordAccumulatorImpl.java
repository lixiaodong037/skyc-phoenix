package com.skyc.phoenix.client.core;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.skyc.phoenix.client.buffer.BufferPool;
import com.skyc.phoenix.client.exception.PhoenixClientException;
import com.skyc.phoenix.client.record.FutureRecordMetaData;
import com.skyc.phoenix.common.record.PhoenixRecord;
import com.skyc.phoenix.client.record.RecordBatch;
import com.skyc.phoenix.client.record.RecordCallback;

/**
 * aggregate every {@link PhoenixRecord} into the {@link RecordBatch} which hold a {@link java.nio.ByteBuffer}
 * for writing the every record bytes into.
 *
 * @author brucelee
 * @since jdk1.6
 */
public class RecordAccumulatorImpl implements RecordAccumulator {

    private Deque<RecordBatch> batches;

    private BufferPool bufferPool;

    private AtomicInteger appendsInProgress = new AtomicInteger(0);

    private int batchSize;

    private long totalBufferPoolSize;

    private long lingMs;

    private volatile boolean closed = false;

    /**
     * Create a new RecordAccumulatorImpl instance.
     *
     * @param batchSize           size of every record batch
     * @param totalBufferPoolSize the total memory for the buffer pool
     * @param lingMs              a delay time to add before declaring a RecordBatch that isn't for sending. This
     *                            allows time for
     *                            more records to arrive. This can trade off some atency for potentially better
     *                            throughput due to more
     *                            batching (and hence fewer, larger requests).
     */
    public RecordAccumulatorImpl(int batchSize, long totalBufferPoolSize, long lingMs) {
        this.batchSize = batchSize;
        this.batches = new ArrayDeque<RecordBatch>();
        this.bufferPool = new BufferPool(batchSize, totalBufferPoolSize);
        this.lingMs = lingMs;
    }

    /**
     * @param record
     * @param maxTimeToBlockMs
     *
     * @return
     */
    @Override
    public RecordAppendResult append(PhoenixRecord record, RecordCallback callback, long maxTimeToBlockMs)
            throws InterruptedException {
        appendsInProgress.incrementAndGet();

        try {
            synchronized(batches) {
                if (closed) {
                    throw new PhoenixClientException("the accumulator and sender was closed.");
                }
                RecordAppendResult appendResult = tryAppend(record, callback);
                if (appendResult != null) {
                    return appendResult;
                }
            }

            // computer the maxsize for allocate
            int size = Math.max(this.batchSize, record.size());
            ByteBuffer buffer = this.bufferPool.allocate(size, maxTimeToBlockMs);

            synchronized(batches) {
                // try append first, because other thread may have allocated the buffer
                RecordAppendResult appendResult = tryAppend(record, callback);
                if (appendResult != null) {
                    // release the buffer
                    this.bufferPool.release(buffer);
                    return appendResult;
                }

                RecordBatch rb = new RecordBatch(buffer);
                FutureRecordMetaData future = rb.tryAppend(record, callback);
                batches.addLast(rb);

                return new RecordAppendResult(future, batches.size() > 1 || rb.isFull(), true);
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
                if (rb != null) {
                    if (currSize + rb.estimatedSizeInBytes() > maxSize && !ready.isEmpty()) {
                        break;
                    }

                    rb = batches.pollFirst();
                    currSize = currSize + rb.estimatedSizeInBytes();
                    ready.add(rb);
                } else {
                    break;
                }
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
                if (rb != null) {
                    currNum++;
                    ready.add(rb);
                } else {
                    break;
                }
            }
        }
        return ready;
    }

    @Override
    public boolean hasUnDrained() {
        return this.batches.size() > 0;
    }

    @Override
    public void abortUnDrainedRecords(long abortTimestamp, RuntimeException abortReason) {
        synchronized(batches) {
            Iterator<RecordBatch> it = batches.iterator();
            while (it.hasNext()) {
                RecordBatch recordBatch = it.next();
                it.remove();
                recordBatch.abort(abortTimestamp, abortReason);
                bufferPool.release(recordBatch.buffer());
            }
        }
    }

    @Override
    public AccumulatorReadyCheckResult readyCheck() {
        long nextReadyCheckDelayMs = Long.MAX_VALUE;
        boolean exhausted = this.bufferPool.queued() > 0;
        AccumulatorReadyCheckResult readyCheckResult = new AccumulatorReadyCheckResult();
        synchronized(batches) {
            RecordBatch batch = batches.peekFirst();
            if (batch != null) {
                long waitedTimeMs = batch.waitedTimeMs(System.currentTimeMillis());
                boolean expired = waitedTimeMs > lingMs;
                boolean canSend = batches.size() > 1 || batch.isFull() || exhausted || closed || expired;
                if (canSend) {
                    readyCheckResult.setReady(true);
                    nextReadyCheckDelayMs = 0;
                } else {
                    readyCheckResult.setReady(false);
                    long timeLeftMs = Math.max(lingMs - waitedTimeMs, 0);
                    nextReadyCheckDelayMs = Math.min(timeLeftMs, nextReadyCheckDelayMs);
                }
            }
        }
        return readyCheckResult;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    private RecordAppendResult tryAppend(PhoenixRecord record, RecordCallback callback) {
        RecordBatch last = batches.peekLast();
        if (last != null) {
            FutureRecordMetaData future = last.tryAppend(record, callback);
            if (future == null) {
                return null;
            } else {
                return new RecordAppendResult(future, batches.size() > 1 || last.isFull(), true);
            }
        }
        return null;
    }
}
