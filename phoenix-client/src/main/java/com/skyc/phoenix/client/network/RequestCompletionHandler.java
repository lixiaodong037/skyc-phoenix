/*
 * Copyright (C) 2020 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.network;

public interface RequestCompletionHandler {
    void onComplete(ClientResponse clientResponse);
}
