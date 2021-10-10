/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.node;

import java.util.List;

/**
 * Parse the bootstrapServer to {@link ServerNode}
 *
 * @author brucelee
 * @since jdk1.6
 */
public interface BootstrapServerAddressParser {

    /**
     * parse the bootstrapServer to server nodes
     * @param bootstrapServer
     * @return
     */
    List<ServerNode> parseToNodes(String bootstrapServer);
}
