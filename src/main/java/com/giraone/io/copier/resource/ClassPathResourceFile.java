package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractSourceFile;
import com.giraone.io.copier.common.ResourceUtils;

import java.io.FileNotFoundException;
import java.net.URL;

public class ClassPathResourceFile extends AbstractSourceFile {

    private final String resourcePath;
    private final boolean directory;

    public ClassPathResourceFile(String resourcePath) {
        this(resourcePath, null, true);
    }

    public ClassPathResourceFile(String resourcePath, String name, boolean directory) {
        super(name);
        this.resourcePath = resourcePath;
        this.directory = directory;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public URL getUrl() {
        try {
            return ResourceUtils.getURL(resourcePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ClassPathResourceFile{" +
            "resourcePath='" + resourcePath + '\'' +
            '}';
    }
}
