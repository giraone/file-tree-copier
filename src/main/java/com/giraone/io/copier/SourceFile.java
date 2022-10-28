package com.giraone.io.copier;

import java.net.URL;

public interface SourceFile {

    /**
     * Return the file or directory name.
     *
     * @return the name - maybe null only for the root URL or root folder.
     */
    String getName();

    /**
     * Differentiate between directory and file.
     *
     * @return true, if it is a directory (may have children).
     */
    boolean isDirectory();

    /**
     * Get URL for source file.
     *
     * @return URL or null (e.g. when source file is not accessible for the caller).
     */
    URL getUrl();
}
