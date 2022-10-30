package com.giraone.io.copier.copy;

import com.giraone.io.copier.common.IoStreamUtils;
import com.giraone.io.copier.common.PathUtils;
import com.giraone.io.copier.common.ResourceUtils;
import com.giraone.io.copier.resource.DirectReadFromUrlStreamProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class FileCopyTest {

    @ParameterizedTest
    @CsvSource(value = {
        "classpath:test-data/tree2/medium.jpg,780831",
        "classpath:test-data/tree2/large.png,26029731",
    })
    void copyUrlContentToFile(String resource, long expected) throws IOException, NoSuchAlgorithmException {

        // arrange
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        URL url = ResourceUtils.getURL(resource);
        String checksumOfSource = IoStreamUtils.calculateChecksumString(digest, url.openStream());
        String fileName = PathUtils.getFileName(resource);
        String fileExtension = PathUtils.getFileExtension(resource);
        File targetFile = File.createTempFile(fileName, fileExtension);
        targetFile.deleteOnExit();

        // act
        long bytesCopied = FileCopy.copyUrlContentToFile(url, new DirectReadFromUrlStreamProvider(), targetFile);

        // assert
        assertThat(bytesCopied).isEqualTo(expected);
        String checksumOfTarget = IoStreamUtils.calculateChecksumString(digest, url.openStream());
        assertThat(checksumOfTarget).isEqualTo(checksumOfSource);
    }
}