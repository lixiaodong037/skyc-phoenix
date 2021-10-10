/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.config;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.skyc.phoenix.common.exception.PhoenixConfigException;

/**
 * Base config of phoenix which provide the
 */
public class BaseConfig {

    private Properties values;

    private final Set<String> used = new HashSet<String>();

    public BaseConfig(Properties values) {
        this.values = values;
    }

    protected Object get(String key) {
        if (!values.containsKey(key)) {
            return null;
        }
        used.add(key);
        return values.get(key);
    }

    public void put(String key, Object value) {
        values.put(key, value);
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
