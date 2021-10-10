/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.codec.protobuf;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.skyc.phoenix.common.codec.Codec;
import com.skyc.phoenix.common.codec.PhoenixCodecException;

public class ProtobufCodec implements Codec<MessageLiteOrBuilder> {
    static {
        try {
            // MessageLite.getParsetForType() is not available until protobuf
            // 2.5.0.
            MessageLite.class.getDeclaredMethod("getParserForType");
        } catch (Throwable t) {
            throw new PhoenixCodecException(">>>> protobuf version error, please use "
                    + "the protobuf version 2.5.0+(including 2.5.0)");
        }
    }

    private MessageLite prototype;
    private ExtensionRegistry extensionRegistry;

    public ProtobufCodec(MessageLite prototype) {
        this.prototype = prototype;
    }

    public ProtobufCodec(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        this.prototype = prototype;
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public MessageLiteOrBuilder decode(byte[] bytes) {
        if (bytes == null || bytes.length < 0) {
            throw new PhoenixCodecException("the input bytes cannot be empty!");
        }

        MessageLiteOrBuilder liteOrBuilder = null;
        try {
            if (extensionRegistry == null) {
                liteOrBuilder = prototype.getParserForType().parseFrom(bytes);
            } else {
                liteOrBuilder = prototype.getParserForType().parseFrom(bytes, extensionRegistry);
            }
        } catch (Exception e) {
            throw new PhoenixCodecException("decode the protobuf bytes content exception!", e);
        }

        return liteOrBuilder;
    }

    @Override
    public byte[] encode(MessageLiteOrBuilder t) {
        try {
            if (t instanceof MessageLite) {
                return ((MessageLite) t).toByteArray();
            }

            if (t instanceof MessageLite.Builder) {
                return ((MessageLite.Builder) t).build().toByteArray();
            }

        } catch (Exception e) {
            throw new PhoenixCodecException("encode to bytes exception!", e);
        }
        return new byte[0];
    }
}
