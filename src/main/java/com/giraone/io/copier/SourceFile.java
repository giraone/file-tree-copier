package com.giraone.io.copier;

import java.net.URL;

public interface SourceFile {

    String getName();

    boolean isDirectory();

    URL getUrl();
}
