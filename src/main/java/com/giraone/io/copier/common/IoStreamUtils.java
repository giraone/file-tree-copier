package com.giraone.io.copier.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * A class to stream byte content from InputStream/OutputStreams.
 */
public final class IoStreamUtils {

    private static final int BUFFER_SIZE = 4096;

    // Class has only static methods
    private IoStreamUtils() {
    }

    public static long pipeBlobStream(InputStream in, OutputStream out) throws IOException {
        return pipeBlobStream(in, out, BUFFER_SIZE);
    }

    public static long pipeBlobStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        long size = 0L;
        final byte[] buf = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buf)) >= 0) {
            out.write(buf, 0, bytesRead);
            size += bytesRead;
        }
        return size;
    }

    /**
     * Calculate checksum of an InputStream, e.g. FileInputStream
     *
     * @param digest the checksum algorithm
     * @param in the input stream to read from - will be closed
     * @return the checksum as a String
     * @throws IOException when input cannot be read
     */
    public static byte[] calculateChecksum(MessageDigest digest, InputStream in) throws IOException {
        byte[] byteArray = new byte[BUFFER_SIZE];
        int readBytes;
        try (in) {
            while ((readBytes = in.read(byteArray)) != -1) {
                digest.update(byteArray, 0, readBytes);
            }
        }
        return digest.digest();
    }

    /**
     * Calculate checksum of an InputStream, e.g. FileInputStream
     *
     * @param digest the checksum algorithm
     * @param in the input stream t oread from - will be closed
     * @return the checksum as a String
     * @throws IOException when input cannot be read
     */
    public static String calculateChecksumString(MessageDigest digest, InputStream in) throws IOException {

        byte[] bytes = calculateChecksum(digest, in);
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
