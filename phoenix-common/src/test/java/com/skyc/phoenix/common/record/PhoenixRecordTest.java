package com.skyc.phoenix.common.record;

import com.skyc.phoenix.common.codec.Codec;
import com.skyc.phoenix.common.codec.kryo.KryoCodec;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class PhoenixRecordTest {

    private static Codec<StudentHeader> codecHeader = new KryoCodec<StudentHeader>(StudentHeader.class);
    private static Codec<Student> codec = new KryoCodec<Student>(Student.class);

    @Test
    public void testToByteBuffer() {
        StudentHeader studentHeader = new StudentHeader("111", "lixiaodong", codecHeader);
        Student student = new Student("111", "lixiaodong", 31, "pudong street new all the same", codec);

        PhoenixRecord record = new PhoenixRecord(studentHeader, student);

        ByteBuffer buffer = ByteBuffer.allocate(record.size());
        record.appendTo(buffer);

        PhoenixRecord record1 = record.parse(buffer.array());

        Assert.assertTrue(((StudentHeader) record1.getHeader()).getId().equals("111"));
        Assert.assertTrue(((StudentHeader) record1.getHeader()).getName().equals("lixiaodong"));

        Assert.assertTrue(((Student) record1.getBody()).getName().equals("lixiaodong"));
        Assert.assertTrue(((Student) record1.getBody()).getAge() == 31);
        System.out.println(record1.getHeader());


    }

    @Test
    public void testSize() {

    }

    @Test
    public void testParse() {
    }

}
