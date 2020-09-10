package com.skyc.phoenix.client.record;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.skyc.phoenix.common.codec.Decoder;
import com.skyc.phoenix.common.codec.Encoder;
import com.skyc.phoenix.common.codec.kryo.KryoDecoder;
import com.skyc.phoenix.common.codec.kryo.KryoEncoder;

public class PhoenixRecordTest {

    private static Encoder<StudentHeader> encoderHeader = new KryoEncoder<StudentHeader>(StudentHeader.class);
    private static Decoder<StudentHeader> decoderHeader = new KryoDecoder<StudentHeader>(StudentHeader.class);
    private static Encoder<Student> encoder = new KryoEncoder<Student>(Student.class);
    private static Decoder<Student> decoder = new KryoDecoder<Student>(Student.class);

    @Test
    public void testToByteBuffer() {
        StudentHeader studentHeader = new StudentHeader("111", "lixiaodong");
        Student student = new Student("111", "lixiaodong", 31, "pudong street new all the same");

        PhoenixRecord record = new PhoenixRecord(studentHeader, student);

        ByteBuffer buffer = ByteBuffer.allocate(record.size());
        record.appendTo(buffer);

        PhoenixRecord record1 = record.parseFrom(buffer.array());

        Assert.assertTrue(((StudentHeader) record1.getHeader()).id.equals("111"));
        Assert.assertTrue(((StudentHeader) record1.getHeader()).name.equals("lixiaodong"));

        Assert.assertTrue(((Student) record1.getBody()).name.equals("lixiaodong"));
        Assert.assertTrue(((Student) record1.getBody()).age == 31);
        System.out.println(record1.getHeader());


    }

    @Test
    public void testSize() {

    }

    @Test
    public void testParse() {
    }

    public static class StudentHeader implements RecordHeader {

        private String id;
        private String name;

        public StudentHeader() {
        }

        public StudentHeader(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public byte[] getHeaderAsBytes() {
            //            return (id + "," + name).getBytes();
            return encoderHeader.encode(this);
        }

        @Override
        public RecordHeader parseToHeader(byte[] header) {
            StudentHeader stu = decoderHeader.decode(header);

            //            StudentHeader stu = = new StudentHeader();
            //            String[] array = new String(header).split(",");
            //            stu.id = array[0];
            //            stu.name = array[1];
            return stu;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "StudentHeader{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
        }
    }

    public static class Student implements RecordBody {
        private String id;
        private String name;
        private int age;
        private String street;

        public Student() {
        }

        public Student(String id, String name, int age, String street) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.street = street;
        }

        @Override
        public byte[] getBodyAsBytes() {
            //            return (id + "," + name + "," + age + ", " + street).getBytes();
            return encoder.encode(this);
        }

        @Override
        public RecordBody parseToBody(byte[] body) {
            Student stu = decoder.decode(body);
            //            String[] array = new String(body).split(",");
            //            stu.id = array[0];
            //            stu.name = array[1];
            //            stu.age = Integer.parseInt(array[2]);
            //            stu.street = array[3];
            return this;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        @Override
        public String toString() {
            return "Student{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", age=" + age + ", street='" + street
                           + '\'' + '}';
        }

    }
}
