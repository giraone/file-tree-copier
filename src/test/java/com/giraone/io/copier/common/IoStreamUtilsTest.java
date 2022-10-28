package com.giraone.io.copier.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class IoStreamUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "classpath:test-data/tree2/medium.jpg,e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        "classpath:test-data/tree2/large.png,e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    })
    void calculateChecksumString(String resource, String expected) throws NoSuchAlgorithmException, IOException {

        // arrange
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        URL url = ResourceUtils.getURL(resource);
        InputStream in = url.openStream();

        // act
        String checksumString = IoStreamUtils.calculateChecksumString(digest, in);

        // assert
        assertThat(checksumString).isEqualTo(expected);
    }
}