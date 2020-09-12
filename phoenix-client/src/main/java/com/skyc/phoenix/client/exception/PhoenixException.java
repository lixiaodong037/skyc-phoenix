/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.exception;

/**
 * the phoenix exception for RuntimeException
 */
public class PhoenixException extends RuntimeException {

    public PhoenixException() {
    }

    public PhoenixException(String message) {
        super(message);
    }

    public PhoenixException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoenixException(Throwable cause) {
        super(cause);
    }

    public PhoenixException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
