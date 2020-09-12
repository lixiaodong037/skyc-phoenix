/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.skyc.phoenix.client.exception.PhoenixTimeoutException;

/**
 * 内存池对象，系统启动会初始化一块给定大小的内存来用。
 * 主要是为了防止在不停的数据发送情况下，不停创建和销毁ByteBuffer带来的系统开销。
 *
 * @author brucelee
 * @since jdk1.6
 */
public final class BufferPool {

    // default size of every buffer to allocate
    private final int batchSize;
    private final long totalMemory;
    private final ReentrantLock lock;
    private final Deque<ByteBuffer> freeList;
    private final Deque<Condition> waiters;
    // nonPooledAvailableMemory = totalMemory - freeList.size() * batchSize
    private long nonPooledAvailableMemory;

    /**
     * create a new buffer pool
     *
     * @param batchSize   the buffer size to cache in the free list
     * @param totalMemory the maximum amount of memory that the buffer pool can allocate
     */
    public BufferPool(int batchSize, long totalMemory) {
        this.batchSize = batchSize;
        this.totalMemory = totalMemory;
        this.lock = new ReentrantLock();
        this.freeList = new ArrayDeque<ByteBuffer>();
        this.waiters = new ArrayDeque<Condition>();
    }

    /**
     * 分配一个固定大小的ByteBuffer对象，如果内存不够，就等待maxTimeToBlockMs时间再取，
     * 如果仍然没有取到，就从jvm中分配一块
     *
     * @param size             想要分配的内存大小
     * @param maxTimeToBlockMs 如果内存池中的内存不够了，需要等待maxTimeToBlockMs时间再取
     *
     * @return The buffer
     */
    public ByteBuffer allocate(int size, long maxTimeToBlockMs) throws InterruptedException {
        if (size > totalMemory) {
            throw new IllegalArgumentException(
                    "the allocate size:" + size + " exceeds the total memory:" + totalMemory);
        }

        ByteBuffer buffer = null;
        this.lock.lock();
        try {
            // check whether has the free buffer to allocate
            // the size to allocate equals to the batchSize
            if (size == batchSize && !this.freeList.isEmpty()) {
                return this.freeList.pollFirst();
            }

            // if the size to allocate larger than the batchSize, we should free some buffer from freeList
            // or if the this.freeList is empty, we should allocate from the nonPooledAvailableMemory
            int freeListSize = this.freeList.size() * this.batchSize;
            if (this.nonPooledAvailableMemory + freeListSize >= size) {
                // we have enough free memory, the size to allocate larger than the default batchSize so we should
                // freeUp enough memory for this buffer request
                freeUp(size);
                this.nonPooledAvailableMemory = this.nonPooledAvailableMemory - size;
            } else {
                // no enough free memory, maybe we will have to block
                int accumulated = 0;
                Condition moreMemory = this.lock.newCondition();
                try {
                    long remaingTimeToBlockNs = TimeUnit.MILLISECONDS.toNanos(maxTimeToBlockMs);
                    this.waiters.addLast(moreMemory);

                    while (accumulated < size) {
                        long startWaitNs = System.nanoTime();
                        long timeNs;
                        boolean waitingTimeElapsed;
                        try {
                            waitingTimeElapsed = moreMemory.await(remaingTimeToBlockNs, TimeUnit.NANOSECONDS);
                        } finally {
                            long endWaitNs = System.nanoTime();
                            timeNs = endWaitNs - startWaitNs;
                        }

                        // waiting time elapsed
                        if (waitingTimeElapsed) {
                            throw new PhoenixTimeoutException(
                                    "failed to allocate memory within configured max blocking time" + " "
                                            + maxTimeToBlockMs);
                        }

                        // signal by other thread, then check if the request size equals to batchSize and freeList
                        // is not empty
                        if (accumulated == 0 && size == this.batchSize && !this.freeList.isEmpty()) {
                            buffer = this.freeList.pollFirst();
                            accumulated = size;
                        } else {
                            // the request size larger than the batchSize
                            freeUp(size - accumulated);
                            int got = (int) Math.min(size - accumulated, this.nonPooledAvailableMemory);
                            this.nonPooledAvailableMemory = this.nonPooledAvailableMemory - got;
                            accumulated = accumulated + got;
                        }
                    }
                    //
                    accumulated = 0;

                } finally {
                    this.nonPooledAvailableMemory = this.nonPooledAvailableMemory + accumulated;
                    this.waiters.remove(moreMemory);
                }
            }
        } finally {
            try {
                if (!(this.nonPooledAvailableMemory == 0 && this.freeList.isEmpty()) && !this.waiters.isEmpty()) {
                    this.waiters.peekFirst().signal();
                }
            } finally {
                lock.unlock();
            }
        }

        if (buffer == null) {
            return safeAllocateByteBuffer(size);
        } else {
            return buffer;
        }

    }

    private ByteBuffer safeAllocateByteBuffer(int size) {
        boolean error = true;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            error = false;
            return buffer;
        } finally {
            if (error) {
                this.lock.lock();
                try {
                    this.nonPooledAvailableMemory = this.nonPooledAvailableMemory + size;
                    if (!this.waiters.isEmpty()) {
                        this.waiters.peekFirst().signal();
                    }
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    /**
     * @param size
     */
    private void freeUp(int size) {
        while (this.nonPooledAvailableMemory < size && !this.freeList.isEmpty()) {
            this.nonPooledAvailableMemory = this.nonPooledAvailableMemory + this.freeList.pollLast()
                                                                                         .capacity();
        }
    }

    /**
     * 分配一个固定大小的ByteBuffer对象，如果内存次内存不够，就等待maxTimeToBlockMs时间再取，
     * 如果仍然没有取到，就从jvm中分配一块
     *
     * @param size 想要分配的内存大小
     *
     * @return The buffer
     */
    public ByteBuffer allocateDirect(int size) {
        return ByteBuffer.allocate(size);
    }

    /**
     * release the byte buffer to the buffer pool
     *
     * @param buffer
     * @param size the size to return to the pool,
     */
    public void release(ByteBuffer buffer, int size) {
        this.lock.lock();
        try {
            if (size == this.batchSize && size == buffer.capacity()) {
                buffer.clear();
                this.freeList.add(buffer);
            } else {
                this.nonPooledAvailableMemory += size;
            }
            Condition moreMem = this.waiters.peekFirst();
            if (moreMem != null) {
                moreMem.signal();
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void release(ByteBuffer buffer) {
        release(buffer, buffer.capacity());
    }

}
