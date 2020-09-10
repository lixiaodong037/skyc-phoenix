package com.skyc.phoenix.client.exception;

public class PhoenixTimeoutException extends PhoenixException {

    public PhoenixTimeoutException() {
    }

    public PhoenixTimeoutException(String message) {
        super(message);
    }

    public PhoenixTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoenixTimeoutException(Throwable cause) {
        super(cause);
    }

    public PhoenixTimeoutException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
