package com.skyc.phoenix.common.codec;

/**
 * the codec exception.
 */
public class PhoenixCodecException extends RuntimeException {

    public PhoenixCodecException() {
    }

    public PhoenixCodecException(String message) {
        super(message);
    }

    public PhoenixCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoenixCodecException(Throwable cause) {
        super(cause);
    }

    public PhoenixCodecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
