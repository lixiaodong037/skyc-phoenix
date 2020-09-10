package com.skyc.phoenix.common.codec.kryo;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.skyc.phoenix.common.codec.Encoder;
import com.skyc.phoenix.common.codec.PhoenixCodecException;

/**
 * the kryo encoder class, the object with T type must has the default constructor.
 *
 * @author brucelee
 * @since jdk1.6
 * @param <T>
 */
public class KryoEncoder<T> implements Encoder<T> {

    private Class<T> entityClass;

    public KryoEncoder() {
    }

    public KryoEncoder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public byte[] encode(T t) {
        if (t == null) {
            throw new PhoenixCodecException("the input param t cannot be null!");
        }

        Output output = new Output(new ByteArrayOutputStream());
        ThreadLocalKryoUtil.getKryo().register(entityClass);
        ThreadLocalKryoUtil.getKryo().writeClassAndObject(output, t);

        byte[] b = output.getBuffer();
        try {
            output.flush();
            output.close();
        } catch (KryoException e) {
            throw new PhoenixCodecException("flush and close the stream exception!", e);
        }
        return b;
    }
}
