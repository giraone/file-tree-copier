package com.giraone.io.copier.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giraone.io.copier.web.index.AutoIndexItem;
import com.giraone.io.copier.web.index.AutoIndexItemType;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerServingTree extends ClientAndServer {

    public static final int PORT_FOR_MOCKSERVER = 18787;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MockServerServingTree() {
        super();
    }

    @SuppressWarnings("try")
    public void startClientAndServer() {
        startClientAndServer(PORT_FOR_MOCKSERVER);
    }

    public URL getRootUrlHostAndPort() throws MalformedURLException {
       return new URL("http://127.0.0.1:" + PORT_FOR_MOCKSERVER);
    }

    public void createMockForAutoIndex(String urlPath, int filesLevel1, int dirsLevel1, int filesLevel2) {

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

    private void createMockEndpoint(String urlPath, List<AutoIndexItem> lst) {
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(lst);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        createMockEndpoint(urlPath, jsonBody);
    }

    @SuppressWarnings("try")
    private void createMockEndpoint(String urlPath, String jsonBody) {

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
