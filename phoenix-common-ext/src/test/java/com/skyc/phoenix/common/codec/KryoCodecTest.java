package com.skyc.phoenix.common.codec;

import com.skyc.phoenix.common.codec.kryo.KryoCodec;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KryoCodecTest {

    @Test
    public void testCodec() {
        Student student = new Student();
        student.setName("brucelee");
        student.setAge(30);
        Student.Street street = new Student.Street();
        street.setRoad("xinhuan road");
        street.setDoor("door");
        street.setNum(777);
        student.setStreet(street);

        List<String> others = new ArrayList<String>();
        others.add("other1");
        others.add("other2");
        student.setOthers(others);

        Map<String, String> othersMap = new HashMap<String, String>();
        othersMap.put("key1", "value1");
        student.setOthersMap(othersMap);

        KryoCodec<Student> kryoEncoder = new KryoCodec<Student>(Student.class);
        byte[] bytes = kryoEncoder.encode(student);
        System.out.println(bytes.length);

        KryoCodec<Student> kryoDecoder = new KryoCodec<Student>(Student.class);

        Student result = kryoDecoder.decode(bytes);
        System.out.println(result);
    }
}
