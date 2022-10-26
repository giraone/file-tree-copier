package com.giraone.io.copier.copy;

import com.giraone.io.copier.common.IoStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileCopy {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCopy.class);

    // Class has only static methods
    private FileCopy() {
    }

    public static void copyUrlContentToFile(URL url, File file) throws IOException {

        LOGGER.info("OPEN URL \"{}\"", url);
        try (OutputStream out = new FileOutputStream(file)) {
            try (InputStream in = url.openStream()) {
                long copied = IoStreamUtils.pipeBlobStream(in, out);
                LOGGER.debug("Copied URL \"{}\" with {} bytes to file \"{}\"", url, copied, file);
            }
        }
    }

    public static void setLastModified(File file, long lastModified) {

        boolean setLastModifiedOk = file.setLastModified(lastModified);
        if (!setLastModifiedOk) {
            LOGGER.warn("Cannot set modification date of file \"{}\"!", file);
        }
    }
}
