/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.codec;

public interface Codec<T> {

    T decode(byte[] bytes);

    byte[] encode(T t);
}
