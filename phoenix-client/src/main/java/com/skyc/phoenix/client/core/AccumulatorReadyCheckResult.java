/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

/**
 * AccumulatorReadyCheckResult
 */
public class AccumulatorReadyCheckResult {
    private boolean isReady;
    private long nextReadyCheckDelayMs;

    public AccumulatorReadyCheckResult() {
        this.isReady = isReady;
        this.nextReadyCheckDelayMs = nextReadyCheckDelayMs;
    }

    public AccumulatorReadyCheckResult(boolean isReady, long nextReadyCheckDelayMs) {
        this.isReady = isReady;
        this.nextReadyCheckDelayMs = nextReadyCheckDelayMs;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setNextReadyCheckDelayMs(long nextReadyCheckDelayMs) {
        this.nextReadyCheckDelayMs = nextReadyCheckDelayMs;
    }

    public boolean isReady() {
        return isReady;
    }

    public long getNextReadyCheckDelayMs() {
        return nextReadyCheckDelayMs;
    }
}