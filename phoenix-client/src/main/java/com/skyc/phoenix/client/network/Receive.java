package com.skyc.phoenix.client.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;

/**
 * receive response from channel
 *
 * @author brucelee
 */
public interface Receive {

    /**
     * read from channel
     *
     * @param channel
     * @return
     * @throws IOException
     */
    int readFrom(ScatteringByteChannel channel) throws IOException, InterruptedException;

    /**
     * is this send complete?
     */
    boolean completed();

    /**
     * get the payload from receive
     *
     * @return
     */
    ByteBuffer payload();

    /**
     * close the channel
     */
    void close();

    /**
     * the
     * @return
     */
    String sendDest();
}
