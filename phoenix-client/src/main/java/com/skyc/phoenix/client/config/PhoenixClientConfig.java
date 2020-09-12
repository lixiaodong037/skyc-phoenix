/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.config;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.skyc.phoenix.client.exception.PhoenixConfigException;

/**
 * A Configuration for the Phoenix Client.
 *
 * @author brucelee
 */
public class PhoenixClientConfig {

    private Properties values;

    private final Set<String> used;

    public static final String BUFFER_MEMORY_CONFIG = "buffer.memory";

    public static final String BATCH_SIZE_CONFIG = "batch.size";

    public static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers";

    public static final String SEND_BUFFER_CONFIG = "send.buffer.bytes";

    public static final String RECEIVE_BUFFER_CONFIG = "receive.buffer.bytes";

    public static final String RETRIES_CONFIG = "retries";

    public PhoenixClientConfig(Properties properties) {
        this.values = properties;
        this.used = new HashSet<String>();
    }

    protected Object get(String key) {
        if (!values.containsKey(key)) {
            throw new PhoenixConfigException(String.format("Unknown configuration '%s'", key));
        }
        used.add(key);
        return values.get(key);
    }

    public Short getShort(String key) {
        return (Short) get(key);
    }

    public Integer getInt(String key) {
        return (Integer) get(key);
    }

    public Long getLong(String key) {
        return (Long) get(key);
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public List<String> getList(String key) {
        return (List<String>) get(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }


}
