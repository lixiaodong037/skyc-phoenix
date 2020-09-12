/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.core;

import com.skyc.phoenix.client.network.NetworkConnection;

/**
 * A connection picker interface
 *
 * @author brucelee
 * @since  JDK1.6
 */
public interface ConnectionPicker {
    NetworkConnection pickOne();
}
