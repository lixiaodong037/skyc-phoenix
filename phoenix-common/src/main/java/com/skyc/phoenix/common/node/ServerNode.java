/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.node;

import java.util.Objects;

/**
 * A node for remote server, this is the default Node class, we can extend from this class and add new attributes.
 *
 * @author burcelee
 * @since jdk1.6
 */
public class ServerNode {

    private String host;

    private int port;

    public static final ServerNode UNKNOWN_NODE = new ServerNode("UNKNOWN", -1);

    public ServerNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerNode that = (ServerNode) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
