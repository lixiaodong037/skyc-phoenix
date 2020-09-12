package com.skyc.phoenix.client.exception;

public class PhoenixConfigException extends PhoenixException {

    public PhoenixConfigException() {
    }

    public PhoenixConfigException(String message) {
        super(message);
    }

    public PhoenixConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoenixConfigException(Throwable cause) {
        super(cause);
    }

    public PhoenixConfigException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
