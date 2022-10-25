package com.giraone.io.copier;

import java.io.Serializable;

public abstract class AbstractSourceFile implements SourceFile, Serializable {

    protected final String name;

    public AbstractSourceFile(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Extract the last part of a path (file or URL). If the last character is a slash (/), it is removed,
     * before the last part is determined.
     *
     * @param path Input path
     * @return the last part or null, if the input was null
     */
    public static String lastPart(String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith("/")) {
            return lastPart(path.substring(0, path.length() - 1));
        }
        int i = path.lastIndexOf('/');
        if (i < 0) {
            return path;
        } else {
            return path.substring(i + 1);
        }
    }
}
