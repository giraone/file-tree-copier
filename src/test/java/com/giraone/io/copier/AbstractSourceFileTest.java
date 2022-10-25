package com.giraone.io.copier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractSourceFileTest {

    @ParameterizedTest
    @CsvSource({
        ",",
        "'',''",
        "path,path",
        "/path,path",
        "/path1/path2,path2",
        "/path1/path2/,path2",
        "/path1/path2/path3.txt,path3.txt"
    })
    void lastPart(String input, String expected) {

        String actual = AbstractSourceFile.lastPart(input);
        assertThat(actual).isEqualTo(expected);
    }
}