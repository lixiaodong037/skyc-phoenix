package com.skyc.phoenix.client.core;

import java.util.List;

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
public interface RecordAccumulator {

    /**
     * Append the record into memory queue.
     *
     * @param record           the record to append
     * @param maxTimeToBlockMs when appending the record, we need request memory buffer from pool, when pool is empty
     *                         thread need to wait maxTimeToBlockMs for getting the enough buffer.
     * @param callback
     *
     * @return the append result
     *
     * @throws InterruptedException
     */
    RecordAppendResult append(PhoenixRecord record, RecordCallback callback, long maxTimeToBlockMs)
            throws InterruptedException;

    /**
     * Drain the record from RecordAccumulator by size
     *
     * @param maxSize the max size to drain
     *
     * @return
     */
    List<RecordBatch> drainBySize(int maxSize);

    /**
     * Drain the record from RecordAccumulator by record num.
     *
     * @param num
     *
     * @return
     */
    List<RecordBatch> drainByRecordNum(int num);

    /**
     * Has records left int the accumulator.
     *
     * @return
     */
    boolean hasUnDrained();

    /**
     * Abort un drained records
     */
    void abortUnDrainedRecords(long abortTimestamp, RuntimeException abortReason);

    /**
     * check the accumulator is ready for sender to drain
     *
     * @return
     */
    AccumulatorReadyCheckResult readyCheck();

    /**
     * close the accumulator which prevent the record appending to accumulator.
     */
    void close();
}
