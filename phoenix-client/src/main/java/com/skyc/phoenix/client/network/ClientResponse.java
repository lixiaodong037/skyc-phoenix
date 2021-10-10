/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.util.Collections;
import java.util.List;

/**
 * A response from the server.a
 */
public class ClientResponse {

    private final List<RequestCompletionHandler> callbacks;
    private final String destinationServerNode;
    private final long timestamp;
    private final boolean disconnected;

    public ClientResponse(List<RequestCompletionHandler> callbacks, String destinationServerNode, long timestamp,
                          boolean disconnected) {
        this.callbacks = callbacks;
        this.destinationServerNode = destinationServerNode;
        this.timestamp = timestamp;
        this.disconnected = disconnected;
    }

    public List<RequestCompletionHandler> callbacks() {
        return callbacks;
    }

    public String getDestinationServerNode() {
        return destinationServerNode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void onComplete() {
        if (callbacks != null && callbacks.size() > 0) {
            for (RequestCompletionHandler callback : callbacks) {
                callback.onComplete(this);
            }
        }
    }

    public static ClientResponse parseResponse(byte[] payload) {
        return null;
    }
}
