package com.giraone.io.copier.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * See <a href="https://stackoverflow.com/questions/1429172/how-to-list-the-files-inside-a-jar-file">how-to-list-the-files-inside-a-jar-file</a>
 */
public class ResourceWalker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceWalker.class);

    private static final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * Walk through a given resource tree. The walk always starts with the resource itself!
     * So directory with 2 entries, will have 3 nodes!
     *
     * @param resource the resource to start.
     * @param maxDepth walking depth, must be greater or equal 1
     * @param consumer the consumer process to consume the tree nodes
     */
    public void walk(String resource, int maxDepth, Consumer<File> consumer) {
        try {
            innerWalk(resource, maxDepth, consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private void innerWalk(String resource, int maxDepth, Consumer<File> consumer) throws Exception {

        final URL url = ResourceUtils.getURL(resource);
        final URI uri = url.toURI();
        if (URL_PROTOCOL_JAR.equals(uri.getScheme())) {
            LOGGER.debug("JAR URL \"{}\". Using safe walk.", uri);
            // final JarURLConnection connection = (JarURLConnection) url.openConnection();
            // final URL url2 = connection.getJarFileURL();
            // final URI uri2 = url2.toURI();
            safeWalkJar(resource, uri, maxDepth, consumer);
        } else {
            LOGGER.debug("Non-JAR URL \"{}\". Using normal walk.", uri);
            File file = new File(uri);
            if (file.exists()) {
                final Path path = file.toPath();
                Files.walk(path, maxDepth)
                    .map(Path::toFile)
                    .forEach(consumer);
            } else {
                throw new FileNotFoundException("File for resource \"" + resource + "\", uri=\"" + uri + "\" does not exist!");
            }
        }
    }

    private void safeWalkJar(String resource, URI uri, int maxDepth, Consumer<File> consumer) throws Exception {

        try (FileSystem fs = getFileSystem(uri)) {
            Files.walk(fs.getPath(resource), maxDepth)
                .map(Path::toFile)
                .forEach(consumer);
        }
    }
}
