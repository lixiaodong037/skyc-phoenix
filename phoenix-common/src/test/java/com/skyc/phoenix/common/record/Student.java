/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.record;

import com.skyc.phoenix.common.codec.Codec;

public class Student implements RecordBody {
    private String id;
    private String name;
    private int age;
    private String street;

    private Codec<Student> codec;

    public Student() {
    }

    public Student(Codec codec) {
        this.codec = codec;
    }

    public Student(String id, String name, int age, String street, Codec codec) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.street = street;

        this.codec = codec;
    }

    @Override
    public byte[] getBodyAsBytes() {
        //            return (id + "," + name + "," + age + ", " + street).getBytes();
        return codec.encode(this);
    }

    @Override
    public RecordBody parseToBody(byte[] body) {
        Student stu = codec.decode(body);
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
