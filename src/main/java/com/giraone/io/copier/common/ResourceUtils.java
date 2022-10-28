package com.giraone.io.copier.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Miscellaneous classpath resource utility methods inspired by org.springframework.util.ResourceUtils.
 */
public class ResourceUtils {

    /**
     * Pseudo prefix for loading from the class path: "classpath:".
     */
    public static final String CLASSPATH_URL_PREFIX = "classpath:";
    /**
     * URL protocol for an entry from a jar file: "jar".
     */
    public static final String URL_PROTOCOL_JAR = "jar";

    public static URL getURL(String resource) throws FileNotFoundException {

        if (resource.startsWith(CLASSPATH_URL_PREFIX)) {
            String resourcePath = resource.substring(CLASSPATH_URL_PREFIX.length());
            ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
            URL url = (classLoader != null ? classLoader.getResource(resourcePath) : ClassLoader.getSystemResource(resourcePath));
            if (url == null) {
                throw new FileNotFoundException("resource \""
                    + resource + "\" cannot be resolved to URL because it does not exist!");
            }
            return url;
        }
        try {
            // try URL
            return new URL(resource);
        } catch (MalformedURLException malformedURLException) {
            // no URL -> treat as file path
            try {
                final File file = new File(resource);
                if (!file.exists()) {
                    throw new FileNotFoundException("Resource \"" + resource + "\" is neither a URL nor an existing file!");
                }
                return file.toURI().toURL();
            } catch (MalformedURLException malformedURLException2) {
                throw new FileNotFoundException("Resource \"" + resource + "\" is neither a valid URL nor a file system path!");
            }
        }
    }
}