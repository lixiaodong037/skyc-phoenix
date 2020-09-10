/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.client.record;

import java.nio.ByteBuffer;

import com.skyc.phoenix.client.common.Utils;
import com.skyc.phoenix.client.exception.PhoenixException;

/**
 * PhoenixRecord contains two parts, the record construct is
 * <p>
 * magic + header length + header value + body length + body value
 * 2bytes + 4bytes + headervalue.length + 4bytes + bodyvalue.length
 *
 * @author brucelee
 */
public class PhoenixRecord {

    private static final int RECORD_LENGTH_SIZE = 4;
    private static final int HEADER_LENGTH_SIZE = 4;
    private static final int BODY_LENGTH_SIZE = 4;
    private static final int CRC_OFFSET = 0;
    private static final int CRC_LENGTH = 4;
    private static final int MAGIC_OFFSET = CRC_OFFSET + CRC_LENGTH;
    private static final int MAGIC_LENGTH = 1;
    // the version of record
    private static final byte MAGIC = 0;
    private static final byte[] EMPTY_BYTES = new byte[0];
    // default value of checksum
    private int checksum = 0;
    private RecordHeader header;
    private byte[] headerBytes;
    private RecordBody body;
    private byte[] bodyBytes;

    public PhoenixRecord(RecordHeader header, RecordBody body) {
        this.header = header;
        this.headerBytes = this.header.getHeaderAsBytes();
        this.body = body;
        this.bodyBytes = this.body.getBodyAsBytes();
    }

    public PhoenixRecord(RecordBody body) {
        this.header = null;
        this.body = body;
    }

    public void appendTo(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("the input buffer cannot be null!");
        }
        buffer.rewind();
        // put the default checksum
        buffer.putInt(0);
        // put the default record size
        buffer.putInt(0);

        int recordLength = 0;
        // magic
        buffer.put(MAGIC);
        recordLength = recordLength + MAGIC_LENGTH;

        // header
        recordLength = recordLength + HEADER_LENGTH_SIZE;
        if (header != null) {
            byte[] headerValue = header.getHeaderAsBytes();
            if (headerValue == null) {
                headerValue = EMPTY_BYTES;
            }
            buffer.putInt(headerValue.length);
            buffer.put(headerValue);
            recordLength = recordLength + headerValue.length;
        } else {
            // header value length is 0
            buffer.putInt(0);
            recordLength = recordLength + 4;
        }

        // body
        recordLength = recordLength + BODY_LENGTH_SIZE;
        if (body != null) {
            byte[] bodyValue = body.getBodyAsBytes();
            if (bodyValue == null) {
                bodyValue = EMPTY_BYTES;
            }
            buffer.putInt(bodyValue.length);
            buffer.put(bodyValue);
            recordLength = recordLength + bodyValue.length;
        } else {
            buffer.putInt(0);
            recordLength = recordLength + 4;
        }

        // get the checksum
        this.checksum = Utils.getUnsignedInt(Utils.crc32(buffer, CRC_LENGTH + RECORD_LENGTH_SIZE, recordLength));

        // update the checksum and record length
        buffer.putInt(0, this.checksum);
        buffer.putInt(CRC_LENGTH, recordLength);
    }

    /**
     * parse the recordBytes to a record
     *
     * @param recordBytes
     */
    public PhoenixRecord parseFrom(byte[] recordBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(recordBytes);
        int checksum = buffer.getInt();
        int recordSize = buffer.getInt();
        int realChecksum = Utils.getUnsignedInt(Utils.crc32(buffer, CRC_LENGTH + RECORD_LENGTH_SIZE, recordSize));
        if (checksum != realChecksum) {
            throw new PhoenixException("the value this record has bean changed!");
        }
        byte magic = buffer.get();
        if (magic != MAGIC) {
            throw new PhoenixException("the MAGIC this record is invalid");
        }

        int headerLength = buffer.getInt();
        byte[] header = new byte[headerLength];
        buffer.get(header);
        this.header = this.header.parseToHeader(header);

        int bodyLength = buffer.getInt();
        byte[] body = new byte[bodyLength];
        buffer.get(body);
        this.body = this.body.parseToBody(body);

        return this;
    }

    public int size() {
        return CRC_LENGTH + RECORD_LENGTH_SIZE + MAGIC_LENGTH + HEADER_LENGTH_SIZE + headerBytes.length
                       + BODY_LENGTH_SIZE + bodyBytes.length;
    }

    public RecordHeader getHeader() {
        return header;
    }

    public RecordBody getBody() {
        return body;
    }

}
