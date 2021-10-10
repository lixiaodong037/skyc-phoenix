/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.Closeable;
import java.io.IOException;

import com.skyc.phoenix.common.node.ServerNode;

/**
 * This class is responsible for listening accept/read/write event.
 * dispatch the
 */
public interface NetworkClient extends Closeable {

    /**
     * Create and init a network connection.
     *
     * @param node
     * @param sendBufferSize
     * @param receiveBufferSize
     * @return
     */
    void initConnection(ServerNode node, int sendBufferSize, int receiveBufferSize);

    /**
     * poll from selector waiting up to he given timeout
     *
     * @param timeout
     */
    void poll(long timeout) throws IOException, InterruptedException;

    /**
     * send the content to one {#NetworkConnection}
     *
     * @param clientRequest the content to send
     */
    void send(ClientRequest clientRequest);

    /**
     * wakeup the connection
     */
    public void wakeup();

    /**
     * pick one connection
     */
    public NetworkConnection pickOneConnection();
}
