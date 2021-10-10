/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.util.List;

/**
 * A request for {@link NetworkClient}
 */
public class ClientRequest {

    private final Send send;
    private final List<RequestCompletionHandler> callbacks;
    private final boolean expectedResponse;
    private String destination;

    public ClientRequest(Send send, List<RequestCompletionHandler> callbacks, boolean expectedResponse) {
        this.send = send;
        this.callbacks = callbacks;
        this.expectedResponse = expectedResponse;
    }

    public Send getSend() {
        return send;
    }

    public List<RequestCompletionHandler> getCallbacks() {
        return callbacks;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isExpectedResponse() {
        return expectedResponse;
    }

    public ClientResponse completed(ClientResponse response, long timeMs) {
        return new ClientResponse(getCallbacks(), send.destination(), timeMs, false);
    }

    public ClientResponse disconnected(long timeMs) {
        return new ClientResponse(getCallbacks(), send.destination(), timeMs, true);
    }

}
