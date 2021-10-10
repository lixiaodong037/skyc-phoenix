/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.server;

import com.skyc.phoenix.client.exception.PhoenixClientException;
import com.skyc.phoenix.common.node.ServerAddressParser;
import com.skyc.phoenix.common.node.ServerNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认服务端地址parser  192.168.1.1:9400,192.168.1.2:9400,192.168.1.3:9400
 *
 * @author brucelee
 * @since jdk1.6
 */
public class DefaultServerAddressParser implements ServerAddressParser {

    private static final String ADDR_SEP = ",";
    private static final String IP_PORT_SEP = ":";

    @Override
    public List<ServerNode> parseToServerNodes(String bootstrapServer) {
        List<ServerNode> serverNodes = Collections.emptyList();
        if (null != bootstrapServer && !"".equals(bootstrapServer)) {
            String[] ipPorts = bootstrapServer.split(ADDR_SEP);
            serverNodes = new ArrayList<ServerNode>(ipPorts.length);
            for(String ipPort : ipPorts) {
                String[] ipPortArray = ipPort.split(IP_PORT_SEP);
                if (ipPortArray.length != 2) {
                    throw new PhoenixClientException("the input bootstrapServer:" + bootstrapServer + " is invalid!");
                }
                serverNodes.add(new ServerNode(ipPortArray[0], Integer.parseInt(ipPortArray[1])));
            }
        }
        return serverNodes;
    }
}
