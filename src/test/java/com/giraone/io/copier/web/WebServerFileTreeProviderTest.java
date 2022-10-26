package com.giraone.io.copier.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giraone.io.copier.AbstractSourceFile;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.model.FileTree;
import com.giraone.io.copier.web.index.AutoIndexItem;
import com.giraone.io.copier.web.index.AutoIndexItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class WebServerFileTreeProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerFileTreeProviderTest.class);
    private static final int PORT_FOR_MOCKSERVER = 18787;
    private static final String URL_root = "/";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static ClientAndServer mockServer;

    @BeforeAll
    static void setup() {
        mockServer = startClientAndServer(PORT_FOR_MOCKSERVER);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @BeforeEach
    void beforeEach() {
        mockServer.reset();
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
        "2,0,0,2",
        "0,2,0,2",
        "2,2,0,4",
    })
    void provideTree(int filesLevel1, int dirsLevel1, int filesLevel2, int expectedChildrenLevel1) throws MalformedURLException {

        // arrange
        createMockForAutoIndex(URL_root, filesLevel1, dirsLevel1, filesLevel2);
        URL rootUrl = new URL("http://127.0.0.1:" + PORT_FOR_MOCKSERVER);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        // act
        FileTree<WebServerFile> relativeTargetFilePath = fileTreeProvider.provideTree();
        // assert
        assertThat(relativeTargetFilePath).isNotNull();
        List<FileTree.FileTreeNode<WebServerFile>> list = relativeTargetFilePath.traverse().collect(Collectors.toList());
        assertThat(list).hasSize(expectedChildrenLevel1);
    }

    @Test
    void provideTreeFromAutoIndexWithoutFilter() throws MalformedURLException {

        // arrange
        createMockForAutoIndex(URL_root, 0, 1, 1);
        URL rootUrl = new URL("http://127.0.0.1:" + PORT_FOR_MOCKSERVER);
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
        List<FileTree.FileTreeNode<WebServerFile>> rootList = fileTreeNode.traverse().collect(Collectors.toList());
        assertThat(rootList).hasSize(1);
        FileTree.FileTreeNode<WebServerFile> dir1 = rootList.get(0);
        assertThat(dir1.getParent()).isNotNull();
        assertThat(dir1.getData()).isNotNull();
        assertThat(dir1.getData().getUrl()).isNotNull();
        assertThat(dir1.getData().getUrl().toExternalForm()).isEqualTo(rootUrl + "/folder1/");
        assertThat(dir1.getData().getName()).isEqualTo("folder1");
    }

    @ParameterizedTest
    @CsvSource({
        "4,file1,1",
        "10,file1,2",
    })
    void provideTreeFromAutoIndexWithFilter(int filesLevel1, String fileContains, int expectedCount) throws MalformedURLException {

        // arrange
        createMockForAutoIndex(URL_root, filesLevel1, 0, 0);
        URL rootUrl = new URL("http://127.0.0.1:" + PORT_FOR_MOCKSERVER);
        WebServerFileTreeProvider fileTreeProvider = new WebServerFileTreeProvider(rootUrl);
        Function<SourceFile, Boolean> fct = sourceFile -> {
            boolean ret = sourceFile.isDirectory() || sourceFile.getName().contains(fileContains);
            LOGGER.debug("Applied sourceFileFileFunction for {} returns {}", sourceFile, ret);
            return ret;
        };
        fileTreeProvider.withFilter(fct);
        WebServerFile file = new WebServerFile(rootUrl);
        final FileTree.FileTreeNode<WebServerFile> fileTreeNode = new FileTree.FileTreeNode<>(file, null);
        // act
        fileTreeProvider.provideTreeFromAutoIndex(rootUrl, fileTreeNode);
        // assert
        List<FileTree.FileTreeNode<WebServerFile>> rootList = fileTreeNode.traverse().collect(Collectors.toList());
        assertThat(rootList).hasSize(expectedCount);

    }

    //------------------------------------------------------------------------------------------------------------------

    private static void createMockForAutoIndex(String urlPath, int filesLevel1, int dirsLevel1, int filesLevel2) {

        List<AutoIndexItem> listLevel1 = new ArrayList<>();
        List<String> dirs = new ArrayList<>();
        for (int i = 1; i <= filesLevel1; i++) {
            listLevel1.add(new AutoIndexItem("file" + i + ".txt", AutoIndexItemType.file));
        }
        for (int i = 1; i <= dirsLevel1; i++) {
            AutoIndexItem a = new AutoIndexItem("folder" + i, AutoIndexItemType.directory);
            listLevel1.add(a);
            dirs.add(a.getName());
        }
        createMockEndpoint(urlPath, listLevel1);

        for (String d : dirs) {
            List<AutoIndexItem> listLevel2 = new ArrayList<>();
            for (int i = 1; i <= filesLevel2; i++) {
                listLevel2.add(new AutoIndexItem(d + "-file" + i + ".txt", AutoIndexItemType.file));
            }
            createMockEndpoint(urlPath + d + "/", listLevel2);
        }
    }

    private static String createMockEndpoint(String urlPath, List<AutoIndexItem> lst) {
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(lst);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        createMockEndpoint(urlPath, jsonBody);
        return jsonBody;
    }

    private static void createMockEndpoint(String urlPath, String jsonBody) {

        new MockServerClient("127.0.0.1", PORT_FOR_MOCKSERVER)
            .when(
                request()
                    .withMethod("GET")
                    .withPath(urlPath)
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                    )
                    .withBody(jsonBody)
                    .withDelay(TimeUnit.MILLISECONDS, 50)
            );
    }
}