package com.skyc.phoenix.common.codec.kryo;

import com.esotericsoftware.kryo.io.Input;
import com.skyc.phoenix.common.codec.Decoder;
import com.skyc.phoenix.common.codec.PhoenixCodecException;

/**
 * kryo decoder
 *
 * @author brucelee
 * @since jdk1.6
 *
 * @param <T>
 */
public class KryoDecoder<T> implements Decoder<T> {

    private Class<T> entityClass;

    public KryoDecoder() {
    }

    public KryoDecoder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }


    @Override
    public T decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new PhoenixCodecException("the input bytes cannot be null!");
        }
        Input input = new Input(bytes);
        T t = (T) ThreadLocalKryoUtil.getKryo().readClassAndObject(input);
        return t;
    }
}
