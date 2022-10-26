package com.giraone.io.copier.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static com.giraone.io.copier.common.ResourceUtils.URL_PROTOCOL_JAR;
import static java.nio.file.FileSystems.getFileSystem;

/**
 * Helper to walk a resource (classpath) file system tree.
 */
public class ResourceWalker {

    private static final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public void walk(String resource, int maxDepth, Consumer<File> consumer) throws Exception {

        final URL url = ResourceUtils.getURL(resource);
        if (url == null) {
            throw new FileNotFoundException("resource \""
                + resource + "\" cannot be resolved to URL because it does not exist!");
        }
        final URI uri = url.toURI();
        if (URL_PROTOCOL_JAR.equals(uri.getScheme())) {
            safeWalkJar(resource, uri, maxDepth, consumer);
        } else {
            File file = new File(uri);
            if (file.exists()) {
                final Path path = file.toPath();
                Files.walk(path, maxDepth)
                    .map(p -> p.toFile())
                    .forEach(consumer);
            } else {
                throw new FileNotFoundException("File for resource \"" + resource + "\", uri=\""+ uri + "\" does not exist!");
            }
        }
    }

    private void safeWalkJar(String resource, URI uri, int maxDepth, Consumer<File> consumer) throws Exception {

        synchronized (getLock(uri)) {
            try (FileSystem fs = getFileSystem(uri)) {
                Files.walk(fs.getPath(resource), maxDepth)
                    .map(p -> p.toFile())
                    .forEach(consumer);
            }
        }
    }

    private Object getLock(URI uri) {

        String fileName = parseFileName(uri);
        locks.computeIfAbsent(fileName, s -> new Object());
        return locks.get(fileName);
    }

    private String parseFileName(URI uri) {

        String schemeSpecificPart = uri.getSchemeSpecificPart();
        return schemeSpecificPart.substring(0, schemeSpecificPart.indexOf("!"));
    }
}
