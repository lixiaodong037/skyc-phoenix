/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.skyc.phoenix.common.record.PhoenixRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.common.node.ServerNode;

/**
 * Contain several {@link PhoenixRecord} in the RecordBatch
 *
 * @author brucelee
 */
public final class RecordBatch {

    private static final Logger log = LoggerFactory.getLogger(RecordBatch.class);

    // the complete info for every sent records
    private final List<RecordComplete> recordCompleteList = new ArrayList<RecordComplete>();
    private final RecordBatchResult recordBatchResult;
    // a bytebuffer from BufferPool which will return to the pool after send to remote
    private ByteBuffer byteBuffer;
    private int recordNum = 0;
    private int recordSize = 0;
    private boolean isFull = false;
    // the time when current recordBatch created
    private long createMs;
    // the time for last append the batch
    private long lastAppendMs;
    private AtomicReference<FinalState> finalState = new AtomicReference<FinalState>(null);

    public RecordBatch(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.recordNum = recordNum;
        this.recordSize = recordSize;
        this.createMs = System.currentTimeMillis();
        this.recordBatchResult = new RecordBatchResult();
        this.lastAppendMs = createMs;
    }

    public ByteBuffer buffer() {
        return byteBuffer;
    }

    public FutureRecordMetaData tryAppend(PhoenixRecord pr, RecordCallback callback) {
        if (isFull || !hasEnoughBuffer(pr.size())) {
            isFull = true;
            return null;
        }
        pr.appendTo(byteBuffer);
        this.lastAppendMs = System.currentTimeMillis();

        recordNum++;
        recordSize = recordSize + pr.size();

        RecordMetaData recordMetaData = new RecordMetaData(this.recordBatchResult, lastAppendMs, recordNum);

        if (callback != null) {
            this.recordCompleteList.add(new RecordComplete(recordMetaData, callback));
        }

        return new FutureRecordMetaData(recordMetaData);
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

    public boolean isFull() {
        return isFull;
    }

    /**
     * The latest time the current record appended to now
     *
     * @param nowMs
     *
     * @return
     */
    public long waitedTimeMs(long nowMs) {
        return nowMs - lastAppendMs;
    }

    public void abort(long doneTimestamp, RuntimeException reason) {
        if (!finalState.compareAndSet(null, FinalState.ABORTED)) {
            throw new IllegalStateException(
                    "Current batch has already been completed in final state " + this.finalState.get());
        }

        log.trace("Aborting current batch.", reason);
        completeBatchAndFireCallback(ServerNode.UNKNOWN_NODE, doneTimestamp, reason);
    }

    public boolean done(ServerNode serverNode, long doneTimestamp, RuntimeException exception) {
        final FinalState finalState;
        if (exception == null) {
            log.trace("Send current batch successful, node:{}, sendResultTime:{}", serverNode.toString(),
                    doneTimestamp);
            finalState = FinalState.SUCCEEDED;
        } else {
            finalState = FinalState.FAILED;
            log.trace("Send current batch failed, node:{}, sendResultTime:{}", serverNode.toString(), doneTimestamp);
        }

        // check whether the final state
        if (!this.finalState.compareAndSet(null, finalState)) {
            if (this.finalState.get() == FinalState.ABORTED) {
                log.debug("Current batch had been aborted before sending.");
            } else {
                throw new IllegalStateException(
                        "Current batch has already been completed in final state " + this.finalState.get());
            }
        }

        completeBatchAndFireCallback(serverNode, doneTimestamp, exception);

        return true;
    }

    private void completeBatchAndFireCallback(ServerNode serverNode, long doneTimestamp, RuntimeException exception) {
        this.recordBatchResult.set(serverNode, doneTimestamp, exception);

        for (RecordComplete recordComplete : recordCompleteList) {
            try {
                if (exception == null) {
                    RecordMetaData recordMetaData = recordComplete.getRecordMetaData();
                    if (recordComplete.getRecordCallback() != null) {
                        recordComplete.getRecordCallback()
                                      .onCompletion(recordMetaData, null);
                    }
                } else {
                    if (recordComplete.getRecordCallback() != null) {
                        recordComplete.getRecordCallback()
                                      .onCompletion(null, exception);
                    }
                }
            } catch (Exception e) {
                log.error("Exception happened when call user-provided callback on message.", e);
            }
        }

        this.recordBatchResult.done();
    }

    private enum FinalState {ABORTED, FAILED, SUCCEEDED}

}