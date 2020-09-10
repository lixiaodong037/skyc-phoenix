/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import java.util.List;
import java.util.Random;

import com.skyc.phoenix.client.network.NetworkConnection;

/**
 * A random connection picker implement.
 *
 * @author brucelee
 * @since  JDK1.6
 */
public class ConnectionPickerImpl implements ConnectionPicker {

    private List<NetworkConnection> connectionList;

    public ConnectionPickerImpl(List<NetworkConnection> connectionList) {
        this.connectionList = connectionList;
    }

    @Override
    public NetworkConnection pickOne() {
        int size = connectionList.size();
        int index = new Random().nextInt(size);
        return connectionList.get(index);
    }
}
