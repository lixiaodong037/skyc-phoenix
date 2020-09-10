package com.skyc.phoenix.common.codec;

public interface Decoder<T> {

    T decode(byte[] bytes);
}
