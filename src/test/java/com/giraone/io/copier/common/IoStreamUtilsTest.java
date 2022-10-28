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
        "classpath:test-data/tree2/medium.jpg,84a4da0e4c52c469ace6e0c674a9144cd43eb2628c401c8b56b41242e2be4af1",
        "classpath:test-data/tree2/large.png,13d64eddbc87182428e2ecae7a1f40685fe1232ffc96748ee394e8ac2df3f4e1",
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