package com.giraone.io.copier.resource;

import com.giraone.io.copier.ReadFromUrlStreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The default implementation for reading content from a URL.
 * This is simply {@link URL#openStream()}.
 */
public class DirectReadFromUrlStreamProvider implements ReadFromUrlStreamProvider {

    @Override
    public InputStream openInputStream(URL url) {

        try {
            return url.openStream();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open URL \"" + url + "\"", e);
        }
    }
}
