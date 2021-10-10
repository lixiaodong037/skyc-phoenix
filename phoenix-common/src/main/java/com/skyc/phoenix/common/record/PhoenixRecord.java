/*
 * Copyright (C) 2021 Skyc, Inc. All Rights Reserved.
 */
package com.skyc.phoenix.common.record;

import com.skyc.phoenix.common.exception.PhoenixException;
import com.skyc.phoenix.common.utils.CrcUtils;

import java.nio.ByteBuffer;

/**
 * 一个 {@link PhoenixRecord} 包含如下几个部分
 *
 * <p>
 * crc + compress type + record length + magic + header length + header value + body length + body value
 * 2bytes + 4bytes + headervalue + 4bytes + bodyvalue
 *
 * 其中 record length 就是从magic到最后bodyvalue的所有字节流的长度
 *
 * @author brucelee
 * @since jdk1.6
 */
public class PhoenixRecord {

    // crc
    public static final int CRC_OFFSET = 0;
    public static final int CRC_LENGTH = 4;
    // 压缩类型，0代表没有压缩
    public static final byte COMPRESS_TYPE = 0;
    public static final int COMPRESS_TYPE_OFFSET = CRC_OFFSET + CRC_LENGTH;
    public static final int COMPRESS_TYPE_LENGTH = 1;
    // 记录长度偏移
    public static final int RECORD_LENGTH_OFFSET = COMPRESS_TYPE_OFFSET + COMPRESS_TYPE_LENGTH;
    public static final int RECORD_LENGTH = 4;
    // 整个记录的偏移量
    public static final int RECORD_OFFSET = RECORD_LENGTH_OFFSET + RECORD_LENGTH;
    // magic偏移量就等于记录偏移量
    public static final int MAGIC_OFFSET = RECORD_OFFSET;
    public static final int MAGIC_LENGTH = 1;
    public static final byte MAGIC = 0;
    // header的长度
    public static final int HEADER_LENGTH = 4;
    // body的长度
    public static final int BODY_LENGTH = 4;

    private static final byte[] EMPTY_BYTES = new byte[0];
    // default value of checksum
    private int checksum = 0;
    private RecordHeader header;
    private int headerLength = -1;
    private byte[] headerValue;
    private RecordBody body;
    private int bodyLength = -1;
    private byte[] bodyValue;

    public PhoenixRecord() {
    }

    public PhoenixRecord(RecordHeader header, RecordBody body) {
        this.header = header;
        this.body = body;
    }

    public PhoenixRecord(RecordBody body) {
        this.header = null;
        this.body = body;
    }

    public void appendTo(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("the input buffer cannot be null!");
        }
        // put the default checksum and record size
        // 写入checksum和内容长度先占个坑
        buffer.putInt(0); // checksum
        buffer.put((byte) 0); // 压缩类型，0默认不压缩
        buffer.putInt(0); // recordsize

        int recordLength = 0;
        // magic
        buffer.put(MAGIC);
        recordLength = recordLength + MAGIC_LENGTH;

        // header
        recordLength = recordLength + HEADER_LENGTH;
        if (header != null) {
            buffer.putInt(this.getHeaderLength());
            buffer.put(this.getHeaderValue());
            recordLength = recordLength + this.getHeaderLength();
        } else {
            // header value length is 0
            buffer.putInt(0);
            recordLength = recordLength + 4;
        }

        // body
        recordLength = recordLength + BODY_LENGTH;
        if (body != null) {
            buffer.putInt(this.getBodyLength());
            buffer.put(this.getBodyValue());
            recordLength = recordLength + this.getBodyLength();
        } else {
            buffer.putInt(0);
            recordLength = recordLength + 4;
        }

        // get the checksum
        this.checksum = CrcUtils.getUnsignedInt(CrcUtils.crc32(buffer, RECORD_OFFSET, recordLength));

        // update the checksum and record length
        buffer.putInt(0, this.checksum);
        buffer.putInt(RECORD_LENGTH_OFFSET, recordLength);
    }

    /**
     * parse the recordBytes to a record
     *
     * @param recordBytes
     */
    public PhoenixRecord parse(byte[] recordBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(recordBytes);
        int checksum = buffer.getInt();
        byte compressType = buffer.get();
        int recordSize = buffer.getInt();
        int realChecksum = CrcUtils.getUnsignedInt(CrcUtils.crc32(buffer, RECORD_OFFSET, recordSize));
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
        return CRC_LENGTH + COMPRESS_TYPE_LENGTH + RECORD_LENGTH + MAGIC_LENGTH + HEADER_LENGTH + this.getHeaderLength()
                       + BODY_LENGTH + this.getBodyLength();
    }

    private int getHeaderLength() {
        if (headerLength == -1) {
            this.headerValue = this.getHeaderValue();
            this.headerLength = headerValue == null ? 0 : headerValue.length;
        }
        return this.headerLength;
    }

    private byte[] getHeaderValue() {
        if (headerValue == null) {
            this.headerValue = this.header.getHeaderAsBytes();
        }
        return this.headerValue;
    }

    private int getBodyLength() {
        if (bodyLength == -1) {
            this.bodyValue = this.getBodyValue();
            this.bodyLength = bodyValue == null ? 0 : bodyValue.length;
        }
        return this.bodyLength;
    }

    private byte[] getBodyValue() {
        if (this.bodyValue == null) {
            this.bodyValue = this.body.getBodyAsBytes();
        }
        return this.bodyValue;
    }

    public RecordHeader getHeader() {
        return header;
    }

    public RecordBody getBody() {
        return body;
    }

}
