/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.Closeable;
import java.io.IOException;

/**
 * This class is responsible for listening accept/read/write event.
 * dispatch the
 */
public interface NetworkClient extends Closeable {

    /**
     * create and init a network connection.
     *
     * @param host
     * @param port
     * @param sendBufferSize
     * @param receiveBufferSize
     * @return
     */
    void initConnection(String host, int port, int sendBufferSize, int receiveBufferSize);

    /**
     * poll from selector waiting up to he given timeout
     *
     * @param timeout
     */
    void poll(long timeout) throws IOException, InterruptedException;

    /**
     * send the content to one {#NetworkConnection}
     *
     * @param send the content to send
     */
    void send(Send send);

    /**
     * wakeup the connection
     */
    public void wakeup();

    /**
     * pick one connection
     */
    public NetworkConnection pickOneConnection();
}
