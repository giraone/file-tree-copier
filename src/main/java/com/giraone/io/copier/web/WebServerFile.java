package com.giraone.io.copier.web;

import com.giraone.io.copier.AbstractSourceFile;

import java.net.URL;

public class WebServerFile extends AbstractSourceFile {

    private final URL url;
    private final boolean directory;

    public WebServerFile(URL url) {
        this(url, null, true);
    }

    public WebServerFile(URL url, String name, boolean directory) {
        super(name);
        this.url = url;
        this.directory = directory;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "WebServerFile{" +
            "url=" + url +
            ", name=" + name +
            '}';
    }
}
