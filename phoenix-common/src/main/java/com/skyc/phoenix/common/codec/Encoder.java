package com.skyc.phoenix.common.codec;

public interface Encoder<T> {

    byte[] encode(T t);
}
