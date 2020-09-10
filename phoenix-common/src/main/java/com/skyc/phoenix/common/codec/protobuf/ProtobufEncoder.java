package com.skyc.phoenix.common.codec.protobuf;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.skyc.phoenix.common.codec.PhoenixCodecException;
import com.skyc.phoenix.common.codec.Encoder;

/**
 * the ProtobufEncoder
 */
public class ProtobufEncoder implements Encoder<MessageLiteOrBuilder> {

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
