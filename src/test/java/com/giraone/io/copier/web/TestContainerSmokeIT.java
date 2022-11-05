package com.giraone.io.copier.web;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TestContainerSmokeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainerSmokeIT.class);
    private final static DockerImageName CONTAINER_IMAGE = DockerImageName.parse("nginx:1.23.2");
    private static NginxContainer<?> container;

    @BeforeAll
    @SuppressWarnings("resource")
    static void setup() {

        container = new NginxContainer<>(CONTAINER_IMAGE)
            .withExposedPorts(80) // waiting for the exposed port to start listening
        ;

        if (!container.isRunning()) {
            container.start();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(container.getLogs());
                URL baseUrl;
                try {
                    baseUrl = container.getBaseUrl("http", 80);
                    LOGGER.debug("NGINX base URL = {}", baseUrl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
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

    // Only for troubleshooting: usage without @BeforeAll/@AfterAll and with GenericContainer
    /*
    @Test
    void testGenericContainer() {

        try (GenericContainer container = new GenericContainer(CONTAINER_IMAGE)) {
            container.start();
            LOGGER.info(container.getLogs());
            if (container.isRunning()) {
                container.stop();
            }
        }
    }
    */

    @Test
    void checkResponseForIndexHtml() throws IOException {

        URL baseUrl = container.getBaseUrl("http", 80);
        URL url = new URL(baseUrl + "/index.html");
        InputStream inputStream = url.openStream();
        String result = new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.joining("\n"));
        LOGGER.debug("Request to {} returned {}", url, result);
        assertThat(result).contains("Welcome to nginx!");
    }
}
