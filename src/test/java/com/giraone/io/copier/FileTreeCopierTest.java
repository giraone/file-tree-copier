package com.giraone.io.copier;

import com.giraone.io.copier.resource.ClassPathFileTreeProvider;
import com.giraone.io.copier.resource.ClassPathResourceFile;
import com.giraone.io.copier.web.MockServerServingTree;
import com.giraone.io.copier.web.WebServerFile;
import com.giraone.io.copier.web.WebServerFileTreeProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileTreeCopierTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTreeCopierTest.class);

    private static final MockServerServingTree mockServerServingTree = new MockServerServingTree();

    @BeforeAll
    static void setup() {
        MockServerServingTree.startClientAndServer();
    }

    @AfterAll
    static void stopServer() {
        mockServerServingTree.stop();
    }

    @BeforeEach
    void beforeEach() {
        mockServerServingTree.reset();
    }

    @Test
    void withTargetDirectoryNull() {
        assertThatThrownBy(() -> new FileTreeCopier<>().withTargetDirectory(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    void withTargetDirectoryIsFile() throws IOException {

        File targetDirectory = File.createTempFile("withTargetDirectoryFile", "");
        targetDirectory.deleteOnExit();
        assertThatThrownBy(() -> new FileTreeCopier<>().withTargetDirectory(targetDirectory))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be a directory");
    }

    @Test
    void withTargetDirectoryNotExists() throws IOException {

        File targetDirectory = File.createTempFile("withTargetDirectoryNotExists", "");
        assertThat(targetDirectory.delete()).isTrue();
        assertThatThrownBy(() -> new FileTreeCopier<>().withTargetDirectory(targetDirectory))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must exist");
    }

    @Test
    void copyUsingWebServerFileTreeProvider() throws IOException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/copyUsingWebServerFileTreeProvider/2-2-2/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, 2, 2, 2);
        WebServerFileTreeProvider source = new WebServerFileTreeProvider(rootUrl);

        FileTreeCopier<WebServerFile> fileTreeCopier = new FileTreeCopier<>();
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = getTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);

        // act
        long start = System.currentTimeMillis();
        CopierResult copierResult = fileTreeCopier.copy();
        long end = System.currentTimeMillis();
        LOGGER.info("Copying of {} directories, {} files, {} bytes took {} msecs",
            copierResult.getDirectoriesCreated(), copierResult.getFilesCopied(),
            copierResult.getBytesCopied(), end - start);

        /*
        │   file1.txt
        │   file2.txt
        │
        ├───folder1
        │       folder1-file1.txt
        │       folder1-file2.txt
        │
        └───folder2
                folder2-file1.txt
                folder2-file2.txt
        */

        // assert
        assertThat(copierResult.getDirectoriesCreated()).isEqualTo(2);
        assertThat(copierResult.getFilesCopied()).isEqualTo(6);
        assertThat(copierResult.getBytesCopied()).isEqualTo(86L);
        assertThat(copierResult.getTotalTimeMillis()).isGreaterThan(0L);

        // clean up
        deleteDirectory(tmpDir);
    }

    @Test
    void copyUsingClassPathFileTreeProvider() throws IOException {

        // arrange
        FileTreeCopier<ClassPathResourceFile> fileTreeCopier = new FileTreeCopier<>();
        String resourcePath = "classpath:test-data/tree1";
        ClassPathFileTreeProvider source = new ClassPathFileTreeProvider(resourcePath);
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = getTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);

        // act
        long start = System.currentTimeMillis();
        CopierResult copierResult = fileTreeCopier.copy();
        long end = System.currentTimeMillis();
        LOGGER.info("Copying of {} directories, {} files, {} bytes took {} msecs",
            copierResult.getDirectoriesCreated(), copierResult.getFilesCopied(),
            copierResult.getBytesCopied(), end - start);

        /*
        │   file1.txt
        │   file2.txt
        │
        └───dir1
            │   file11.txt
            │   file12.txt
            │
            ├───dir11
            │       file111.txt
            │
            └───dir12
                    file121.txt
        */

        // assert
        // 2: dir1, dir1/dir11, dir1/dir12
        assertThat(copierResult.getDirectoriesCreated()).isEqualTo(3);
        assertThat(copierResult.getFilesCopied()).isEqualTo(6);
        assertThat(copierResult.getBytesCopied()).isEqualTo(42L);
        assertThat(copierResult.getTotalTimeMillis()).isGreaterThan(0L);

        // clean up
        deleteDirectory(tmpDir);
    }

    @Test
    void copyUsingClassPathFileTreeProviderFlat() throws IOException {
        // arrange
        FileTreeCopier<ClassPathResourceFile> fileTreeCopier = new FileTreeCopier<>();
        String resourcePath = "classpath:test-data/tree1";
        ClassPathFileTreeProvider source = new ClassPathFileTreeProvider(resourcePath);
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = getTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);
        fileTreeCopier.withFlatCopy();

        // act
        long start = System.currentTimeMillis();
        CopierResult copierResult = fileTreeCopier.copy();
        long end = System.currentTimeMillis();
        LOGGER.info("Copying of {} directories, {} files, {} bytes took {} msecs",
            copierResult.getDirectoriesCreated(), copierResult.getFilesCopied(),
            copierResult.getBytesCopied(), end - start);

        /*
        - file1.txt
        - file2.txt
        - file11.txt
        - file12.txt
        - file111.txt
        - file121.txt
        */

        // assert
        assertThat(new File(tmpDir, "file11.txt").exists()).isTrue();
        assertThat(copierResult.getDirectoriesCreated()).isEqualTo(0);
        assertThat(copierResult.getFilesCopied()).isEqualTo(6);
        assertThat(copierResult.getBytesCopied()).isEqualTo(42L);
        assertThat(copierResult.getTotalTimeMillis()).isGreaterThan(0L);

        // clean up
        deleteDirectory(tmpDir);
    }

    @ParameterizedTest
    @CsvSource({
        "false,1,1", // dir1 is created, because it has subdirectories, even if they have no files.
        "true,0,0", // in flat mode, no directories and files should be created
    })
    void copyUsingClassPathFileTreeProviderAndNoData(boolean flatCopy, int expectedTopLevelNodes, int expectedDirectoriesCreated)
        throws IOException {

        // arrange
        FileTreeCopier<ClassPathResourceFile> fileTreeCopier = new FileTreeCopier<>();
        String resourcePath = "classpath:test-data/tree1";
        ClassPathFileTreeProvider source = new ClassPathFileTreeProvider(resourcePath);
        source.withFileFilter(sourceFile -> false);
        fileTreeCopier.withFileTreeProvider(source);
        File tmpDir = getTmpDirectory();
        fileTreeCopier.withTargetDirectory(tmpDir);
        if (flatCopy) {
            fileTreeCopier.withFlatCopy();
        }
        // act
        CopierResult copierResult = fileTreeCopier.copy();
        // assert
        assertThat(tmpDir.list()).hasSize(expectedTopLevelNodes);
        assertThat(copierResult.getDirectoriesCreated()).isEqualTo(expectedDirectoriesCreated);
        assertThat(copierResult.getFilesCopied()).isEqualTo(0);
        assertThat(copierResult.getBytesCopied()).isEqualTo(0L);

        // clean up
        //deleteDirectory(tmpDir);
    }

    //------------------------------------------------------------------------------------------------------------------

    private File getTmpDirectory() throws IOException {
        File tmpDir = File.createTempFile("file-tree-", "");
        assertThat(tmpDir.delete()).isTrue();
        assertThat(tmpDir.mkdir()).isTrue();
        LOGGER.info("Using TEMP directory \"{}\"", tmpDir);
        return tmpDir;
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}