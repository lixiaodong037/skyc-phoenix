/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.config;

import java.util.Objects;

/**
 * The server instance info
 */
public class ServerInstance {

    /**
     * the ip or host of server
     */
    private String host;

    /**
     * the port
     */
    private int port;

    /**
     * the idc
     */
    private String idc;

    /**
     * the weight of every instance, client support send msg at weight of every instance
     */
    private int weight;

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

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInstance that = (ServerInstance) o;
        return port == that.port && weight == that.weight && Objects.equals(host, that.host) && Objects.equals(idc,
                that.idc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, idc, weight);
    }
}
