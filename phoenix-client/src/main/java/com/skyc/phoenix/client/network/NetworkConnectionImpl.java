/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * A connection interface that define the network action for a connection.
 *
 * @author brucelee
 */
public class NetworkConnectionImpl implements NetworkConnection {

    /**
     * the host
     */
    private String host;

    /**
     * the port
     */
    private int port;

    /**
     * the sendBufferSize for a tcp request communication
     */
    private int sendBufferSize;

    /**
     * the receiveBufferSize for a tcp response communication
     */
    private int receiveBufferSize;

    /**
     * the selector that this connection will register to
     */
    private Selector nioSelector;

    /**
     * the selection key
     */
    private SelectionKey selectionKey;

    /**
     * the socket channel
     */
    private SocketChannel socketChannel;

    /**
     * the content to send
     */
    private volatile Send toSend;

    /**
     * constructor
     */
    public NetworkConnectionImpl(String host, int port, int sendBufferSize, int receiveBufferSize,
                                 Selector nioSelector) {
        this.host = host;
        this.port = port;
        this.sendBufferSize = sendBufferSize;
        this.receiveBufferSize = receiveBufferSize;
    }

    @Override
    public void connect() throws IOException {
        this.socketChannel = SocketChannel.open();
        configSocketChannel(socketChannel, this.sendBufferSize, receiveBufferSize);
        SocketAddress socketAddress = new InetSocketAddress(host, port);

        // connect the remote
        try {
            socketChannel.connect(socketAddress);
        } catch (IOException e) {
            throw new IOException("connect exception, address: " + socketAddress, e);
        }

        // register the connect event on this selector
        this.selectionKey = socketChannel.register(this.nioSelector, SelectionKey.OP_CONNECT);
        // set the attachment for this selectionKey
        this.selectionKey.attach(this);
    }

    @Override
    public boolean finishConnect() throws IOException {
        if (this.socketChannel.finishConnect()) {
            SelectionKeyUtil.removeInterestOps(this.selectionKey, SelectionKey.OP_CONNECT);
            SelectionKeyUtil.addInterestOps(this.selectionKey, SelectionKey.OP_READ);
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnected() throws IOException {
        return socketChannel.isConnected();
    }

    private Socket configSocketChannel(SocketChannel socketChannel, int sendBufferSize, int receiveBufferSize)
            throws IOException {
        // set client socketchannel non blocking mode
        socketChannel.configureBlocking(false);
        Socket socket = socketChannel.socket();
        // long connection
        socket.setKeepAlive(true);
        if (this.sendBufferSize != -1) {
            socket.setSendBufferSize(this.sendBufferSize);
        }
        if (this.receiveBufferSize != -1) {
            socket.setReceiveBufferSize(this.receiveBufferSize);
        }
        // don't use nagle algorithm
        socket.setTcpNoDelay(true);

        return socket;
    }

    @Override
    public void close() throws IOException {
        this.socketChannel.close();
        this.selectionKey.cancel();
        this.selectionKey.attach(null);
    }

    @Override
    public void setSend(Send send) {
        if (send == null) {
            throw new IllegalArgumentException("the send content cannot be null!");
        }
        this.toSend = send;
        // add write event to the selectionKey, which will be selected from a nioselector
        SelectionKeyUtil.addInterestOps(this.selectionKey, SelectionKey.OP_WRITE);
    }

    @Override
    public Send write() throws IOException {
        this.toSend.writeTo(this.socketChannel);
        Send result = this.toSend;
        // if send complete,then remove the opwrite event
        if (this.toSend.completed()) {
            SelectionKeyUtil.removeInterestOps(this.selectionKey, SelectionKey.OP_WRITE);
            this.toSend = null;
        }
        return result;
    }

    @Override
    public Receive read() throws IOException, InterruptedException {
        Receive receive = new ByteBufferReceive();
        receive.readFrom(this.socketChannel);
        if (receive.completed()) {
            receive.payload().rewind();
        }
        return receive;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return this.selectionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkConnectionImpl that = (NetworkConnectionImpl) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
