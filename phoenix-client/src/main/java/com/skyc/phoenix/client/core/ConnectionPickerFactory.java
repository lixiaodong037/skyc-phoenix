/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.skyc.phoenix.client.network.NetworkConnection;

/**
 * A connection picker factory.
 *
 * @author brucelee
 * @since JDK1.6
 */
public class ConnectionPickerFactory {

    private static final String RANDOM = "random";
    private static final String ROUND_ROBIN = "roundRobin";

    public static ConnectionPicker createByType(String pickerType, List<NetworkConnection> connectionList) {
        if (RANDOM.equalsIgnoreCase(pickerType)) {
            return new RandomConnectionPickerImpl(connectionList);
        } else if (ROUND_ROBIN.equalsIgnoreCase(pickerType)) {
            return new RoundRobinConnectionPickerImpl(connectionList);
        }
        return new RandomConnectionPickerImpl(connectionList);
    }

    private static class RandomConnectionPickerImpl implements ConnectionPicker {

        private List<NetworkConnection> connectionList;

        public RandomConnectionPickerImpl(List<NetworkConnection> connectionList) {
            this.connectionList = connectionList;
        }

        @Override
        public NetworkConnection pickOne() {
            int size = connectionList.size();
            int index = new Random().nextInt(size);
            return connectionList.get(index);
        }
    }

    private static class RoundRobinConnectionPickerImpl implements ConnectionPicker {

        private final AtomicInteger incr = new AtomicInteger(0);

        private List<NetworkConnection> connectionList;

        public RoundRobinConnectionPickerImpl(List<NetworkConnection> connectionList) {
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

}
