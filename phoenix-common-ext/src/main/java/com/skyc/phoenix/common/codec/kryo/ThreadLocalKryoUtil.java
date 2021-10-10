package com.skyc.phoenix.common.codec.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * the ThreadLocalKryoUtil for getting
 *
 * @author brucelee
 * @since jdk1.6
 */
public class ThreadLocalKryoUtil {

    private static final ThreadLocal<Kryo> kryoLocal;

    static {
        kryoLocal = new ThreadLocal<Kryo>() {
            protected Kryo initialValue() {
                Kryo kryo = new Kryo();
                kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy());
                return kryo;
            }
        };
    }

    public static Kryo getKryo() {
        return kryoLocal.get();
    }
}
