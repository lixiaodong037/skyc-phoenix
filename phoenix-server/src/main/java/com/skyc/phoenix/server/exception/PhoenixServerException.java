/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.server.exception;

import com.skyc.phoenix.common.exception.PhoenixException;

/**
 * 服务端异常信息
 *
 * @author brucelee
 * @since jdk1.6
 */
public class PhoenixServerException extends PhoenixException {
    public PhoenixServerException() {
    }

    public PhoenixServerException(String message) {
        super(message);
    }

    public PhoenixServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoenixServerException(Throwable cause) {
        super(cause);
    }

    public PhoenixServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
