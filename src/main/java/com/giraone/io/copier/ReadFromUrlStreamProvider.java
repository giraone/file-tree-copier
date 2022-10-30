package com.giraone.io.copier;

import java.io.InputStream;
import java.net.URL;

public interface ReadFromUrlStreamProvider {

    /**
     * Open an input stream to read the content of a URL. For HTTP URLs, this is typically an HTTP GET request.
     * @param url the URL to be read from
     * @return the InputStream
     */
     InputStream openInputStream(URL url);
}
