/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skyc.phoenix.client.core.ConnectionPicker;
import com.skyc.phoenix.client.core.ConnectionPickerFactory;
import com.skyc.phoenix.client.exception.PhoenixClientException;
import com.skyc.phoenix.common.node.ServerNode;

/**
 * This class is responsible for listening accept/read/write event.
 *
 * @author brucelee
 * @since jdk1.6
 */
public class NetworkClientImpl implements NetworkClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(NetworkClientImpl.class);

    /**
     * the id generator for every connection
     */
    private final AtomicInteger connIdGen = new AtomicInteger(0);

    /**
     * the connection map
     */
    private final List<ServerNode> serverNodeList;
    private final Set<NetworkConnection> networkConnectionList;
    private final Set<NetworkConnection> failedAndClosedConnections;
    private final List<Send> completedSends = new ArrayList<Send>();

    /**
     * the selector of all connections
     */
    private final Selector selector;
    private final AtomicLong requestIncrNum = new AtomicLong(1);
    private List<ClientResponse> abortedSends = new ArrayList<ClientResponse>();
    private int retryTime = 3;
    private ConnectionPicker connectionPicker;
    private Map<String, ClientRequest> clientRequestMap = new HashMap<String, ClientRequest>();
    private InflightRequests inflightRequests;
    private Map<NetworkConnection, Deque<Receive>> stagedReceives = new HashMap<NetworkConnection, Deque<Receive>>();
    private List<Receive> completedReceives = new ArrayList<Receive>();

    public NetworkClientImpl(List<ServerNode> serverNodeList, boolean initConnectionForce, int sendBufferSize,
                             int receiveBufferSize, String connectionPickerType, int retryTime,
                              int maxInflightRequestPerConnection) {
        if (serverNodeList == null || serverNodeList.size() <= 0) {
            throw new PhoenixClientException("the input nodeList cannot be empty!");
        }
        this.serverNodeList = serverNodeList;
        this.retryTime = retryTime;

        this.networkConnectionList = new HashSet<NetworkConnection>();
        this.failedAndClosedConnections = new HashSet<NetworkConnection>();

        // open selector
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new PhoenixClientException("cannot open the selector!", e);
        }

        // init or create the connection list
        for (ServerNode serverNode : serverNodeList) {
            if (initConnectionForce) {
                this.initConnection(serverNode, sendBufferSize, receiveBufferSize);
            } else {
                NetworkConnection connection =
                        new NetworkConnectionImpl(serverNode, sendBufferSize, receiveBufferSize, this.selector);
                this.networkConnectionList.add(connection);
            }
        }

        // set the connection picker
        this.connectionPicker = ConnectionPickerFactory.createByType(connectionPickerType,
                new ArrayList<NetworkConnection>(this.networkConnectionList));
        this.inflightRequests = new InflightRequests(maxInflightRequestPerConnection);
    }

    @Override
    public void close() throws IOException {
        // close every connection
        for (NetworkConnection connection : networkConnectionList) {
            close(connection);
        }
        // close the selector
        this.selector.close();
    }

    private void close(NetworkConnection networkConnection) throws IOException {
        networkConnection.close();
        this.networkConnectionList.remove(networkConnection);
        this.failedAndClosedConnections.add(networkConnection);
    }

    @Override
    public void initConnection(ServerNode node, int sendBufferSize, int receiveBufferSize) {
        NetworkConnection connection =
                new NetworkConnectionImpl(node, sendBufferSize, receiveBufferSize, this.selector);
        doInitConnection(connection);
    }

    private boolean doInitConnection(NetworkConnection connection) {
        boolean initSuccess = false;
        for (int i = 0; i < retryTime; i++) {
            try {
                connection.connect();
                initSuccess = true;
                break;
            } catch (IOException e) {
                log.error("init connection exception, retryTime:{}, node:{}", i, connection.destination());
                initSuccess = false;
            }
        }

        if (initSuccess) {
            if (!networkConnectionList.contains(connection)) {
                networkConnectionList.add(connection);
            }
        } else {
            networkConnectionList.remove(connection);
            failedAndClosedConnections.add(connection);
        }

        return initSuccess;
    }

    @Override
    public void poll(long timeout) {
        // process abortedSends
        if (!abortedSends.isEmpty()) {
            List<ClientResponse> responses = new ArrayList<ClientResponse>();
            handleAbortedSends(responses);
            completeResponses(responses);
        }

        // do select and io
        try {
            doSelectAndIO(timeout);
        } catch (IOException e) {
            log.error("IOException when doing selector and connection io!");
        }

        // process completed sends
        List<ClientResponse> responses = new ArrayList<ClientResponse>();
        handleCompletedSends(responses, System.currentTimeMillis());
        handleCompletedReceives(responses);
        handleDisconnections(responses);
        completeResponses(responses);
    }

    private void handleAbortedSends(List<ClientResponse> responses) {
        responses.addAll(abortedSends);
        abortedSends.clear();
    }

    private void handleDisconnections(List<ClientResponse> responses) {
        for (NetworkConnection networkConnection : this.failedAndClosedConnections) {
            for (ClientRequest request : this.inflightRequests.clear(networkConnection.destination())) {
                responses.add(request.disconnected(System.currentTimeMillis()));
            }
        }
    }

    private void handleCompletedSends(List<ClientResponse> responses, long now) {
        // if no response is expected then when the send is completed, return it
        for (Send send : this.completedSends) {
            ClientRequest clientRequest = inflightRequests.lastSent(send.destination());
            // we don't need expected response
            if (!clientRequest.isExpectedResponse()) {
                responses.add(clientRequest.completed(null, System.currentTimeMillis()));
            }
        }
    }

    private void handleCompletedReceives(List<ClientResponse> responses) {
        for (Receive receive : this.completedReceives) {
            String node = receive.sendDest();
            ClientRequest clientRequest = this.inflightRequests.completeNext(node);
            ClientResponse clientResponse = ClientResponse.parseResponse(receive.payload().array());
            responses.add(clientRequest.completed(clientResponse, System.currentTimeMillis()));
        }
    }

    private void completeResponses(List<ClientResponse> responses) {
        for (ClientResponse response : responses) {
            try {
                response.onComplete();
            } catch (Exception e) {
                log.error("Exception when complete response!", e);
            }
            requestIncrNum.decrementAndGet();
        }
    }

    /**
     * Select ready keys
     * @param timeout
     * @throws IOException
     */
    private void doSelectAndIO(long timeout) throws IOException {
        int numReadyKeys = 0;
        if (timeout == 0) {
            numReadyKeys = selector.selectNow();
        } else {
            numReadyKeys = selector.select(timeout);
        }
        if (numReadyKeys > 0) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                processSelectionKeyIO(key);
            }
        }
        addToCompletedReceives();
    }

    /**
     * 客户端通过SelectionKey来判断读写事件
     * @param key
     */
    private void processSelectionKeyIO(SelectionKey key) {
        boolean sendFailed = false;
        NetworkConnection connection = (NetworkConnection) key.attachment();
        try {
            if (key.isConnectable()) {
                if (connection.finishConnect()) {
                    networkConnectionList.add(connection);
                } else {
                    return;
                }
            }
            // read event
            if (connection.isConnected() && key.isReadable()) {
                Receive receive;
                while ((receive = connection.read()) != null) {
                    addToStagedReceives(connection, receive);
                }
            }
            // write event
            if (connection.isConnected() && key.isWritable()) {
                // write the current content to remote in the connection
                try {
                    Send send = connection.write();
                    if (send != null) {
                        completedSends.add(send);
                    }
                } catch (Exception e) {
                    sendFailed = true;
                    throw e;
                }
            }

            if (!key.isValid()) {
                close(connection);
            }
        } catch (Exception e) {
            log.error("processSelectionKeyIO exception.", e);
            try {
                close(connection);
            } catch (IOException ioException) {
                log.error("io exception when close the connection: " + connection.destination(), e);
            }
        }
    }

    private void addToCompletedReceives() {
        if (!this.stagedReceives.isEmpty()) {
            Iterator<NetworkConnection> it = this.stagedReceives.keySet().iterator();
            NetworkConnection connection;
            while(it.hasNext()) {
                connection = it.next();
                Deque<Receive> queue = this.stagedReceives.get(connection);
                if (!queue.isEmpty()) {
                    this.completedReceives.add(queue.poll());
                } else {
                    it.remove();
                }
            }
        }
    }

    private void addToStagedReceives(NetworkConnection connection, Receive receive) {
        if (!stagedReceives.containsKey(connection)) {
            stagedReceives.put(connection, new ArrayDeque<Receive>());
        }
        Deque<Receive> deque = stagedReceives.get(connection);
        deque.add(receive);
    }

    @Override
    public void send(ClientRequest clientRequest) {
        // pick connection from connectionList, if one connection is unReady pic another one.
        boolean succeed = false;
        NetworkConnection connection = pickOneConnection();
        // increment the request num
        Long reqNum = requestIncrNum.getAndIncrement();
        for (int i = 0; i < retryTime; i++) {
            if (!connection.isReady() && !doInitConnection(connection)) {
                connection = pickOneConnection();
                continue;
            } else {
                setDestinationForRequest(clientRequest, connection.destination());
                connection.setSend(clientRequest.getSend());
                succeed = true;
                break;
            }
        }

        inflightRequests.add(clientRequest);

        // current send will be aborted
        if (!succeed) {
            abortedSends.add(new ClientResponse(clientRequest.getCallbacks(), connection.destination(),
                    System.currentTimeMillis(), true));
        }
    }

    private void setDestinationForRequest(ClientRequest clientRequest, String connDest) {
        clientRequest.setDestination(connDest);
        clientRequest.getSend().setDestination(connDest);
    }

    private String getDest(String connStr, long reqNum) {
        return connStr + "_" + reqNum;
    }

    @Override
    public void wakeup() {
        this.selector.wakeup();
    }

    @Override
    public NetworkConnection pickOneConnection() {
        return connectionPicker.pickOne();
    }

    public void setConnectionPicker(ConnectionPicker connectionPicker) {
        this.connectionPicker = connectionPicker;
    }
}