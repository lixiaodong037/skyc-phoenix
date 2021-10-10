/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.record;

import com.skyc.phoenix.common.codec.Codec;

public class StudentHeader implements RecordHeader {

    private String id;
    private String name;

    private Codec<StudentHeader> codec;

    public StudentHeader() {
    }

    public StudentHeader(Codec codec) {
        this.codec = codec;
    }

    public StudentHeader(String id, String name, Codec codec) {
        this.id = id;
        this.name = name;
        this.codec = codec;
    }

    @Override
    public byte[] getHeaderAsBytes() {
        //            return (id + "," + name).getBytes();
        return codec.encode(this);
    }

    @Override
    public RecordHeader parseToHeader(byte[] header) {
        StudentHeader stu = codec.decode(header);

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
