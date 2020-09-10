/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

import java.nio.channels.SelectionKey;

public class SelectionKeyUtil {

    public static void addInterestOps(SelectionKey targetKey, int opsToAdd) {
        targetKey.interestOps(targetKey.interestOps() | opsToAdd);
    }

    public static void removeInterestOps(SelectionKey targetKey, int opsToRemove) {
        targetKey.interestOps(targetKey.interestOps() & ~ opsToRemove);
    }
}
