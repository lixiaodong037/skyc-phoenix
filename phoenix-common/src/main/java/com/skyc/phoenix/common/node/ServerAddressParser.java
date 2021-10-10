/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.node;

import java.util.List;

/**
 * Parse the serverAddrStr to {@link ServerNode}
 *
 * @author brucelee
 * @since jdk1.6
 */
public interface ServerAddressParser {

    /**
     * parse the bootstrapServer to server nodes
     *
     * @param serverAddrStr
     * @return
     */
    List<ServerNode> parseToServerNodes(String serverAddrStr);
}
