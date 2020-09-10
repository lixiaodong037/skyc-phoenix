/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * A connection to server, one connection corresponding to one remote server address.
 *
 * @author brucelee
 */
public interface NetworkConnection {

    /**
     * the connect action
     *
     * @return the id of this action
     * @throws IOException
     */
    void connect() throws IOException;

    /**
     * judge whether the connection has
     * @return
     * @throws IOException
     */
    boolean finishConnect() throws IOException;

    /**
     * return the connected flag
     *
     * @return the connected flag
     * @throws IOException
     */
    boolean isConnected() throws IOException;

    /**
     * close the connection
     *
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Set send object to the connection which will notify the channel interested ops.
     *
     * @param send the params for send method
     */
    void setSend(Send send);

    /**
     * write the content to remote
     *
     * @return the send object for every write operation
     * @throws IOException
     */
    Send write() throws IOException;

    /**
     * receive content from remote.
     *
     * @return the response from remote
     *
     * @throws IOException
     */
    Receive read() throws IOException, InterruptedException;

    /**
     * get the selection key of this connection
     *
     * @return
     */
    SelectionKey getSelectionKey();

}
