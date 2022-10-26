package com.giraone.io.copier.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceWalkerTest {

    @ParameterizedTest
    @CsvSource(value = {
        "classpath:test-data/tree1,1,4",
        "classpath:test-data/tree1/dir1,1,5",
        "classpath:test-data/tree1/dir1/dir11,1,2",
        "classpath:test-data/tree1/dir1,10,7",
        "classpath:test-data/tree1/dir1/dir11,10,2",
    })
    void walk(String resource, int maxDepth, int expectedSize) throws Exception {

        List<File> result = new ArrayList<>();
        ResourceWalker resourceWalker = new ResourceWalker();
        resourceWalker.walk(
            resource,
            maxDepth,
            path -> result.add(path));

        assertThat(result).hasSize(expectedSize);
    }
}