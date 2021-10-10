/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.sample;

import com.skyc.phoenix.client.PhoenixClient;
import com.skyc.phoenix.client.config.PhoenixClientConfig;
import com.skyc.phoenix.client.record.RecordCallback;
import com.skyc.phoenix.client.record.RecordMetaData;
import com.skyc.phoenix.common.codec.Codec;
import com.skyc.phoenix.common.codec.kryo.KryoCodec;
import com.skyc.phoenix.common.record.PhoenixRecord;

public class PhoenixClientStart {

    public static void main(String[] args) {
        PhoenixClientConfig phoenixClientConfig = PhoenixClientConfig.defaultClientConfig();
        phoenixClientConfig.put(PhoenixClientConfig.BOOTSTRAP_SERVERS, "127.0.0.1:9400");
        PhoenixClient phoenixClient = new PhoenixClient(phoenixClientConfig);

        Codec<StudentHeader> codecHeader = new KryoCodec<StudentHeader>(StudentHeader.class);
        Codec<Student> codecBody = new KryoCodec<Student>(Student.class);

        StudentHeader studentHeader = new StudentHeader("111", "lixiaodong", codecHeader);
        Student student = new Student("111", "lixiaodong", 31, "pudong street new all the same", codecBody);
        PhoenixRecord record = new PhoenixRecord(studentHeader, student);
//        for (int i = 0; i < 5; i++) {
            phoenixClient.send(record, new RecordCallback() {
                @Override
                public void onCompletion(RecordMetaData metadata, Exception exception) {
                    System.out.println("record callback.....");
                }
            });
//        }
        System.out.println("aaaaa");
    }
}
