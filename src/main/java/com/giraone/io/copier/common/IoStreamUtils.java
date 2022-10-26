package com.giraone.io.copier.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
