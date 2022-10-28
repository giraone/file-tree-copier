package com.giraone.io.copier.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PathUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "dir/file.jpg,file.jpg",
        "/file.jpg,file.jpg",
        "file.jpg,file.jpg",
        "dir/dir2,dir2",
        "dir/dir2/,dir2",
        ","
    })
    void getFileName(String path, String expected) {

        String actual = PathUtils.getFileName(path);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "dir/file.jpg,.jpg",
        "/file.jpg,.jpg",
        "file.jpg,.jpg",
        "dir/.bashrc,",
        ","
    })
    void getFileExtension(String path, String expected) {

        String actual = PathUtils.getFileExtension(path);
        assertThat(actual).isEqualTo(expected);
    }
}