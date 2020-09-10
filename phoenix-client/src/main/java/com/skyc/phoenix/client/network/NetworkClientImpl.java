/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.client.core.ConnectionPicker;
import com.skyc.phoenix.client.exception.PhoenixException;

/**
 * This class is responsible for listening accept/read/write event.
 * dispatch the
 */
public class NetworkClientImpl implements NetworkClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(NetworkClientImpl.class);

    private static final int TRY_TIME = 3;

    /**
     * the id generator for every connection
     */
    private final AtomicInteger connIdGen = new AtomicInteger(0);

    /**
     * the connection map
     */
    private final Set<NetworkConnection> activeNetworkConnections;
    private final Set<NetworkConnection> failedAndClosedConnections;
    private final Set<NetworkConnection> connectedNetworkConnections;
    private final List<Send> completedSends;

    /**
     * the selector of all connections
     */
    private final Selector selector;

    private final ConnectionPicker connectionPicker;

    public NetworkClientImpl(ConnectionPicker connectionPicker) {
        this.connectionPicker = connectionPicker;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new PhoenixException("cannot open the selector!", e);
        }
        this.activeNetworkConnections = new HashSet<NetworkConnection>();
        this.failedAndClosedConnections = new HashSet<NetworkConnection>();
        this.connectedNetworkConnections = new HashSet<NetworkConnection>();
        this.completedSends = new ArrayList<Send>();
    }

    @Override
    public void close() throws IOException {
        // close every connection
        for (NetworkConnection connection : activeNetworkConnections) {
            connection.close();
            failedAndClosedConnections.add(connection);
        }
        // close the selector
        this.selector.close();
    }

    @Override
    public void initConnection(String host, int port, int sendBufferSize, int receiveBufferSize) {
        NetworkConnection connection =
                new NetworkConnectionImpl(host, port, sendBufferSize, receiveBufferSize, this.selector);
        try {
            connection.connect();
            activeNetworkConnections.add(connection);
        } catch (IOException e) {
            log.error("init connection exception, host:{}, port:{}", host, port);
            failedAndClosedConnections.add(connection);
        }
    }

    @Override
    public void poll(long timeout) throws IOException, InterruptedException {
        int numReadyKeys = selector.select(timeout);
        if (numReadyKeys > 0) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                NetworkConnection connection = (NetworkConnection) key.attachment();
                if (key.isConnectable()) {
                    if (connection.finishConnect()) {
                        connectedNetworkConnections.add(connection);
                    }
                }
                // read event
                if (connection.isConnected() && key.isReadable()) {
                    connection.read();
                }
                // write event
                if (connection.isConnected() && key.isWritable()) {
                    // write the current content to remote in the connection
                    connection.write();
                }
            }
        }
    }

    @Override
    public void send(Send send) {
        NetworkConnection connection = pickOneConnection();
        for (int i = 0; i < TRY_TIME; i++) {
            if (failedAndClosedConnections.contains(connection)) {
                connection = pickOneConnection();
            } else {
                connection.setSend(send);
            }
        }
    }

    @Override
    public void wakeup() {
        this.selector.wakeup();
    }

    @Override
    public NetworkConnection pickOneConnection() {
        return connectionPicker.pickOne();
    }
}
