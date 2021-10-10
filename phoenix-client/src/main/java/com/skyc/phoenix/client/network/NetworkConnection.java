/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 一个网络连接器对象
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
     * 判断是否连接已经成功建立完毕，如果建立完成就移出opconnect信号，加入读信号
     *
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
     * whether the current connection is ready to send
     *
     * @return
     * @throws IOException
     */
    boolean isReady();

    /**
     * get the selection key of this connection
     *
     * @return
     */
    SelectionKey getSelectionKey();

    /**
     * connection destination
     *
     * @return
     */
    String destination();

}
