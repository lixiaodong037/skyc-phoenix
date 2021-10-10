/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * The set of requests which have been sent or are being sent but haven't yet received a response.
 *
 * @author brucelee
 */
public class InflightRequests {

    private final int maxInflightRequestsPerConnection;
    private final Map<String, Deque<ClientRequest>> requests = new HashMap<String, Deque<ClientRequest>>();

    public InflightRequests(int maxInflightRequestsPerConnection) {
        this.maxInflightRequestsPerConnection = maxInflightRequestsPerConnection;
    }

    public void add(ClientRequest clientRequest) {
        String destination = clientRequest.getDestination();
        Deque<ClientRequest> reqs = this.requests.get(destination);
        if (reqs == null) {
            reqs = new ArrayDeque<ClientRequest>();
            requests.put(destination, reqs);
        }
        reqs.addFirst(clientRequest);
    }

    private Deque<ClientRequest> requestQueue(String node) {
        Deque<ClientRequest> reqs = requests.get(node);
        if (reqs == null || reqs.isEmpty()) {
            throw new IllegalStateException("Response from server for which there are no in-flight requests");
        }
        return reqs;
    }

    /**
     * Get the oldest request for the given node
     *
     * @param node
     *
     * @return
     */
    public ClientRequest completeNext(String node) {
        return requestQueue(node).pollLast();
    }

    /**
     * Get the last request we send to the given node
     *
     * @param node
     *
     * @return
     */
    public ClientRequest lastSent(String node) {
        return requestQueue(node).peekFirst();
    }

    /**
     * Complete the last request that was send to a given node.
     * Get the last request from the queue, and delete it.
     *
     * @param node
     *
     * @return
     */
    public ClientRequest completeLastSend(String node) {
        return requestQueue(node).pollFirst();
    }

    /**
     * Can we send more requests to this node?
     *
      * @param node
     * @return true if we have no requests still being sent to the given node
     */
    public boolean canSendMore(String node) {
        Deque<ClientRequest> queue = requests.get(node);
        if (queue == null || queue.isEmpty() || (
                queue.peekFirst()
                     .getSend()
                     .completed() && queue.size() < this.maxInflightRequestsPerConnection)) {
            return true;
        }
        return false;
    }

    public int inFlightRequestCount(String node) {
        Deque<ClientRequest> queue = requests.get(node);
        return queue == null ? 0 : queue.size();
    }

    public Deque<ClientRequest> clear(String node) {
        return requests.remove(node);
    }
}
