/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.skyc.phoenix.client.network.NetworkConnection;

public class ConnectionRoundRobinPickerImpl implements ConnectionPicker {

    private final AtomicInteger incr = new AtomicInteger(0);

    private List<NetworkConnection> connectionList;

    public ConnectionRoundRobinPickerImpl(List<NetworkConnection> connectionList) {
        this.connectionList = connectionList;
    }

    @Override
    public NetworkConnection pickOne() {
        int index = incr.getAndIncrement();
        if (index < Integer.MAX_VALUE) {
            index = index / connectionList.size();
        } else {
            incr.set(0);
        }
        return connectionList.get(index);
    }

}
