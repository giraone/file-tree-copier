package com.giraone.io.copier.web;

import com.giraone.io.copier.AbstractSourceFile;
import com.giraone.io.copier.ReadFromUrlStreamProvider;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.model.FileTree;
import com.giraone.io.copier.resource.DirectReadFromUrlStreamProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class WebServerFileTreeProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerFileTreeProviderTest.class);

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

    @ParameterizedTest
    @CsvSource(value = {
        "http://localhost:8080/subdir1,http://localhost:8080/subdir1/subdir11/subdir111,subdir11/subdir111",
        "http://localhost:8080/subdir1,http://localhost:8080/subdir1/subdir11,subdir11",
        "http://localhost:8080/,http://localhost:8080/subdir1,subdir1",
        "http://localhost:8080,http://localhost:8080/subdir1,subdir1",
        "http://localhost:8080/subdir1,http://localhost:8080/xxx,"
    })
    void calculateRelativeTargetFilePath(String rootUrlString, String fileUrlString, String expected) throws MalformedURLException {

        // arrange
        URL rootUrl = new URL(rootUrlString);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        URL fileUrl = new URL(fileUrlString);
        String name = AbstractSourceFile.lastPart(fileUrlString);
        WebServerFile fileTreeNode = new WebServerFile(fileUrl, name, false);
        // act
        String relativeTargetFilePath = fileTreeProvider.calculateRelativeTargetFilePath(fileTreeNode);
        // assert
        assertThat(relativeTargetFilePath).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "2,0,0,2,0",
        "0,2,0,2,0",
        "2,2,0,4,0",
        "2,2,2,4,4",
        "2,4,3,6,12",
    })
    void provideTree(int filesLevel1, int dirsLevel1, int filesLevel2,
                     int expectedChildrenLevel1, int expectedChildrenLevel2) throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTree/" + filesLevel1 + "-" + dirsLevel1 + "-" + filesLevel2 + "/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, filesLevel1, dirsLevel1, filesLevel2);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        // act
        FileTree<WebServerFile> tree = fileTreeProvider.provideTree();
        // assert
        assertThat(tree).isNotNull();
        List<FileTree.FileTreeNode<WebServerFile>> list1 = tree.getChildren().collect(Collectors.toList());
        assertThat(list1).hasSize(expectedChildrenLevel1);
        int count = 0;
        for (FileTree.FileTreeNode<WebServerFile> item2 : list1) {
            if (item2.hasChildren()) {
                List<FileTree.FileTreeNode<WebServerFile>> list2 = item2.getChildren().collect(Collectors.toList());
                count += list2.size();
            }
        }
        assertThat(count).isEqualTo(expectedChildrenLevel2);
    }

    @Test
    void provideTreeFromAutoIndexWithoutFilter() throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTreeFromAutoIndexWithoutFilter/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, 0, 1, 1);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        WebServerFile file = new WebServerFile(rootUrl);
        final FileTree.FileTreeNode<WebServerFile> fileTreeNode = new FileTree.FileTreeNode<>(file, null);
        // act
        fileTreeProvider.provideTreeFromAutoIndex(rootUrl, fileTreeNode);
        // assert
        assertThat(fileTreeNode.getParent()).isNull();
        assertThat(fileTreeNode.getData()).isNotNull();
        assertThat(fileTreeNode.getData().getUrl()).isEqualTo(rootUrl);
        assertThat(fileTreeNode.getData().getName()).isNull();
        List<FileTree.FileTreeNode<WebServerFile>> rootList = fileTreeNode.getChildren().collect(Collectors.toList());
        assertThat(rootList).hasSize(1);
        FileTree.FileTreeNode<WebServerFile> dir1 = rootList.get(0);
        assertThat(dir1.getParent()).isNotNull();
        assertThat(dir1.getData()).isNotNull();
        assertThat(dir1.getData().getUrl()).isNotNull();
        assertThat(dir1.getData().getUrl().toExternalForm()).isEqualTo(rootUrl + "folder1/");
        assertThat(dir1.getData().getName()).isEqualTo("folder1");
    }

    @ParameterizedTest
    @CsvSource({
        "folder1,2",
        "folder,4",
    })
    void provideTreeFromAutoIndexWithTraverseFilter(String directoryContains, int expectedFilesLevel2)
        throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTreeFromAutoIndexWithFilter/" + directoryContains + "/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, 0, 2, 2);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        Function<SourceFile, Boolean> traverseFilterFct = sourceFile -> {
            boolean ret = sourceFile.getName().contains(directoryContains);
            LOGGER.debug("Applied traverseFilter for {} returns {}", sourceFile, ret);
            return ret;
        };
        fileTreeProvider.withTraverseFilter(traverseFilterFct);
        // act
        FileTree<WebServerFile> tree = fileTreeProvider.provideTree();
        // assert
        List<FileTree.FileTreeNode<WebServerFile>> traverseList = tree.getRecursiveFileList();
        assertThat(traverseList).hasSize(expectedFilesLevel2);
    }

    @Test
    void provideTreeFromAutoIndexWithFilterReturningNoData()
        throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTreeFromAutoIndexWithFilterReturningNoData/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, 2, 2, 2);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        fileTreeProvider.withFileFilter(sourceFile -> false);
        // act
        FileTree<WebServerFile> tree = fileTreeProvider.provideTree();
        // assert
        List<FileTree.FileTreeNode<WebServerFile>> traverseList = tree.getRecursiveFileList();
        assertThat(traverseList).hasSize(0);
    }

    @ParameterizedTest
    @CsvSource({
        "4,file1,1",
        "10,file1,2",
    })
    void provideTreeFromAutoIndexWithFileFilter(int filesLevel1, String fileContains, int expectedFilesLevel1)
        throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTreeFromAutoIndexWithFilter/" + filesLevel1 + "/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, filesLevel1, 0, 0);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        Function<SourceFile, Boolean> fileFilterFct = sourceFile -> {
            boolean ret = sourceFile.getName().contains(fileContains);
            LOGGER.debug("Applied fileFilter for {} returns {}", sourceFile, ret);
            return ret;
        };
        fileTreeProvider.withFileFilter(fileFilterFct);
        // act
        FileTree<WebServerFile> tree = fileTreeProvider.provideTree();
        // assert
        List<FileTree.FileTreeNode<WebServerFile>> traverseList = tree.getRecursiveFileList();
        assertThat(traverseList).hasSize(expectedFilesLevel1);
    }

    @Test
    void overwrittenIsCalled() throws MalformedURLException {

        // arrange
        URL rootUrlHostAndPort = mockServerServingTree.getRootUrlHostAndPort();
        String rootPath = "/provideTreeFromAutoIndexWithoutFilter/";
        URL rootUrl = new URL(rootUrlHostAndPort + rootPath);
        mockServerServingTree.createMockForAutoIndex(rootPath, 1, 1, 1);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);

        AtomicInteger calls = new AtomicInteger(0);
        ReadFromUrlStreamProvider overwrittenProvider = url -> {
            calls.getAndIncrement();
            DirectReadFromUrlStreamProvider p = new DirectReadFromUrlStreamProvider();
            return p.openInputStream(url);
        };
        fileTreeProvider.withHttpClient(overwrittenProvider);

        WebServerFile file = new WebServerFile(rootUrl);
        final FileTree.FileTreeNode<WebServerFile> fileTreeNode = new FileTree.FileTreeNode<>(file, null);
        // act
        fileTreeProvider.provideTreeFromAutoIndex(rootUrl, fileTreeNode);
        // assert
        assertThat(fileTreeNode.getData()).isNotNull();
        // Must be called twice: once for the root (index), once for the only file
        assertThat(calls.get()).isEqualTo(2);
    }
}