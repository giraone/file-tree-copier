package com.giraone.io.copier;

import com.giraone.io.copier.resource.ClassPathFileTreeProvider;
import com.giraone.io.copier.resource.ClassPathResourceFile;
import com.giraone.io.copier.web.WebServerFile;
import com.giraone.io.copier.web.WebServerFileTreeProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileTreeCopierTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTreeCopierTest.class);

    @Test
    void copyUsingWebServerFileTreeProvider() throws IOException {

        // arrange
        FileTreeCopier<WebServerFile> fileTreeCopier = new FileTreeCopier<>();
        URL url = new URL("https://reporting-assets.pcfpub.dev.datev.de/common/");
        WebServerFileTreeProvider source = new WebServerFileTreeProvider(url);
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = geTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);

        // act
        long start = System.currentTimeMillis();
        int copied = fileTreeCopier.copy();
        long end = System.currentTimeMillis();
        LOGGER.info("Copying of {} file took {} msecs", copied, end-start);

        // assert
        assertThat(copied).isEqualTo(40);
    }

    @Test
    void copyUsingClassPathFileTreeProvider() throws IOException {

        // arrange
        FileTreeCopier<ClassPathResourceFile> fileTreeCopier = new FileTreeCopier<>();
        String resourcePath = "test-data/tree1";
        ClassPathFileTreeProvider source = new ClassPathFileTreeProvider(resourcePath);
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = geTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);

        // act
        long start = System.currentTimeMillis();
        int copied = fileTreeCopier.copy();
        long end = System.currentTimeMillis();
        LOGGER.info("Copying of {} file took {} msecs", copied, end-start);

        // assert
        assertThat(copied).isEqualTo(40);
    }

    private File geTmpDirectory() throws IOException {
        File tmpDir = File.createTempFile("file-tree-", "");
        assertThat(tmpDir.delete()).isTrue();
        assertThat(tmpDir.mkdir()).isTrue();
        LOGGER.info("Using TEMP directory \"{}\"", tmpDir);
        return tmpDir;
    }
}