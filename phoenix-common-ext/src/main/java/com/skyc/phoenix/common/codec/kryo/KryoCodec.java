/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.codec.kryo;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.skyc.phoenix.common.codec.Codec;
import com.skyc.phoenix.common.codec.PhoenixCodecException;

import java.io.ByteArrayOutputStream;

public class KryoCodec<T> implements Codec<T> {

    private Class<T> entityClass;

    public KryoCodec() {
    }

    public KryoCodec(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new PhoenixCodecException("the input bytes cannot be null!");
        }
        Input input = new Input(bytes);
        T t = (T) ThreadLocalKryoUtil.getKryo().readObject(input, entityClass);
        return t;
    }

    @Override
    public byte[] encode(T t) {
        if (t == null) {
            throw new PhoenixCodecException("the input param t cannot be null!");
        }

        Output output = new Output(new ByteArrayOutputStream());
        ThreadLocalKryoUtil.getKryo().register(entityClass);
        ThreadLocalKryoUtil.getKryo().writeObject(output, t);

        byte[] b = output.toBytes();
        try {
            output.flush();
            output.close();
        } catch (KryoException e) {
            throw new PhoenixCodecException("flush and close the stream exception!", e);
        }
        return b;
    }

}
