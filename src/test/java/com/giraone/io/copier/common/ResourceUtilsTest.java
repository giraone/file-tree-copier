package com.giraone.io.copier.common;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceUtilsTest {

    @Test
    void getURLForNotExistingResource() {

        assertThatThrownBy(() -> ResourceUtils.getURL("/unknown"))
            .isInstanceOf(FileNotFoundException.class)
            .hasMessage("Resource \"/unknown\" is neither a URL nor an existing file!");
    }

    @Test
    void getURLForExistingResourceFile() throws FileNotFoundException {

        URL url = ResourceUtils.getURL("classpath:test-data/tree1/file1.txt");
        assertThat(url).isNotNull();
        assertThat(url.toExternalForm()).endsWith("/file1.txt");
    }

    @Test
    void getURLForExistingResourceDirectory() throws FileNotFoundException {

        URL url = ResourceUtils.getURL("classpath:test-data/tree1");
        assertThat(url).isNotNull();
        assertThat(url.toExternalForm()).endsWith("/tree1");
    }
}