package com.skyc.phoenix.client.common;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Utils {

    /**
     * Compute the CRC32 of the byteBuffer for specified size
     *
     * @param byteBuffer The byteBuffer to compute the checksum for
     * @param startOffset
     * @param size
     * @return
     */
    public static long crc32(final ByteBuffer byteBuffer, int startOffset, int size) {
        return crc32(byteBuffer.array(), byteBuffer.arrayOffset() + startOffset, size);
    }

    /**
     * Compute the CRC32 of the byte array.
     *
     * @param byteBuffer The byteBuffer to compute the checksum for
     * @return The CRC32
     */
    public static long crc32(final ByteBuffer byteBuffer) {
        return crc32(byteBuffer.array(), 0, byteBuffer.array().length);
    }

    /**
     * Compute the CRC32 of the byte array.
     *
     * @param bytes The array to compute the checksum for
     * @return The CRC32
     */
    public static long crc32(final byte[] bytes) {
        return crc32(bytes, 0, bytes.length);
    }

    /**
     * Compute the CRC32 of the segment of the byte array given by the
     * specificed size and offset.
     *
     * @param bytes The bytes to checksum
     * @param offset the offset at which to begin checksumming
     * @param size the number of bytes to checksum
     * @return The CRC32
     */
    public static long crc32(final byte[] bytes, final int offset, final int size) {
        CRC32 crc = new CRC32();
        crc.update(bytes, offset, size);
        return crc.getValue();
    }

    /**
     * get the unsigned int for the value
     * @param value
     * @return
     */
    public static int getUnsignedInt(final long value) {
        return (int) (value & 0xffffffffL);
    }
}
