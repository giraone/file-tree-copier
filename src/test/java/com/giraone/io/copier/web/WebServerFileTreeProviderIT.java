package com.giraone.io.copier.web;

import com.giraone.io.copier.common.IoStreamUtils;
import com.giraone.io.copier.model.FileTree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing with a real NGINX via org.testcontainers.
 */
public class WebServerFileTreeProviderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerFileTreeProviderIT.class);
    private final static DockerImageName CONTAINER_IMAGE = DockerImageName.parse("nginx:1.23.2");
    private static NginxContainer<?> container;

    @BeforeAll
    @SuppressWarnings("resource")
    static void setup() {

        container = new NginxContainer<>(CONTAINER_IMAGE)

            .withClasspathResourceMapping("nginx.conf", "/etc/nginx/nginx.conf", BindMode.READ_ONLY)
            .withFileSystemBind("src/test/resources/test-data", "/usr/share/nginx/html", BindMode.READ_ONLY)
            // NOT with rootless podman! That is why, we use withFileSystemBind above
            //.withClasspathResourceMapping("test-data", "/usr/share/nginx/html", BindMode.READ_ONLY)

            // .withReuse(true)

            // See https://www.testcontainers.org/features/startup_and_waits/
            // .withExposedPorts(80) // waiting for the exposed port to start listening
            // .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS)) // 60 is the default
            // .waitingFor(Wait.forHttp("/"))
        ;

        if (!container.isRunning()) {
            container.start();
            if (LOGGER.isDebugEnabled()) {
                URL baseUrl;
                try {
                    baseUrl = container.getBaseUrl("http", 80);
                    LOGGER.debug("NGINX base URL = {}", baseUrl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                LOGGER.debug(container.getLogs());
            }
        }
    }

    @AfterAll
    static void tearDown() {

        if (container != null) {
            if (container.isRunning()) {
                container.stop();
            }
        }
    }

    @Test
    void health() throws IOException {
        checkThatFileStartsWith("/health", "up");
    }

    @Test
    void getRecursiveFileListWorksWithARealNginx() throws IOException, NoSuchAlgorithmException {

        // pre-assertions
        checkThatFileStartsWith("/tree1/file1.txt", "one");
        checkThatAutoIndexIsWorking();

        // arrange
        URL baseUrl = container.getBaseUrl("http", 80);
        URL rootUrl = new URL(baseUrl + "/tree2/");
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        // act
        FileTree<WebServerFile> tree = fileTreeProvider.provideTree();
        // assert
        List<FileTree.FileTreeNode<WebServerFile>> traverseList = tree.getRecursiveFileList();
        assertThat(traverseList).hasSize(2);
        List<String> names = traverseList.stream().map(node -> node.getData().getName()).collect(Collectors.toList());
        assertThat(names).containsExactly("large.png", "medium.jpg");

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        URL url0 = traverseList.get(0).getData().getUrl();
        URL url1 = traverseList.get(1).getData().getUrl();
        String checksum0 = IoStreamUtils.calculateChecksumString(digest, url0.openStream());
        LOGGER.debug("Request to {} returned checksum {}", url0, checksum0);
        String checksum1 = IoStreamUtils.calculateChecksumString(digest, url1.openStream());
        LOGGER.debug("Request to {} returned checksum {}", url1, checksum1);
        assertThat(checksum0).isEqualTo("13d64eddbc87182428e2ecae7a1f40685fe1232ffc96748ee394e8ac2df3f4e1");
        assertThat(checksum1).isEqualTo("84a4da0e4c52c469ace6e0c674a9144cd43eb2628c401c8b56b41242e2be4af1");
    }

    private void checkThatAutoIndexIsWorking() throws IOException {

        checkThatFileStartsWith("/tree1/", "[");
    }

    private void checkThatFileStartsWith(String path, String expectedStartsWith) throws IOException {

        URL baseUrl = container.getBaseUrl("http", 80);
        URL url = new URL(baseUrl + path);
        InputStream in = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IoStreamUtils.pipeBlobStream(in, out);
        String result = out.toString();
        LOGGER.debug("Request to {} returned {}", url, result);
        assertThat(result).startsWith(expectedStartsWith);
    }
}
