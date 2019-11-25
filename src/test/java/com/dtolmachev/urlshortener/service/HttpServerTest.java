package com.dtolmachev.urlshortener.service;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.service.config.HttpServerConfig;
import com.dtolmachev.urlshortener.httpserver.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.LOCATION_HEADER;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;

public class HttpServerTest {


    @Test
    public void connect() throws Exception {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        server.start();
        HttpURLConnection con = connect(config);
        int result = con.getResponseCode();
        assertEquals(result, 404);
        dg.stop();
        server.stop();
    }

    @Test
    public void noAliasesInBuffer() throws IOException, InterruptedException {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            URL toPut = new URL("https://openjdk.java.net/groups/net/httpclient/recipes.html");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(testPutURI(config, toPut.toString()))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            Assert.assertEquals(SC_INTERNAL_SERVER_ERROR, response.statusCode());
        } finally {
            server.stop();
        }
    }

    @Test
    public void correctSaveAndGet() throws IOException, InterruptedException {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        dg.start();
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            URL toPut = new URL("https://openjdk.java.net/groups/net/httpclient/recipes.html");
            HttpRequest saveUrl = HttpRequest.newBuilder()
                    .uri(testPutURI(config, toPut.toString()))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response = client.send(saveUrl, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(SC_OK, response.statusCode());
            Alias alias = Alias.create(response.body().replace("\"", ""));

            HttpRequest getUrl = HttpRequest.newBuilder()
                    .uri(testGetURI(config, alias))
                    .GET()
                    .build();
            HttpResponse<String> response2 = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(SC_MOVED_PERMANENTLY, response2.statusCode());
            Assert.assertEquals(toPut.toString(), response2.headers().firstValue(LOCATION_HEADER).orElse(""));
        } finally {
            dg.stop();
            server.stop();
        }
    }

    @Test
    public void emptyUrlSave() throws IOException, InterruptedException {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        dg.start();
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest saveUrl = HttpRequest.newBuilder()
                    .uri(testPutURI(config, ""))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response = client.send(saveUrl, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(SC_BAD_REQUEST, response.statusCode());
        } finally {
            dg.stop();
            server.stop();
        }
    }

    @Test
    public void wrongUrlSave() throws IOException, InterruptedException {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        dg.start();
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest saveUrl = HttpRequest.newBuilder()
                    .uri(testPutURI(config, "sfsadscsaef"))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response = client.send(saveUrl, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(SC_BAD_REQUEST, response.statusCode());
        } finally {
            dg.stop();
            server.stop();
        }
    }

    @Test
    public void unknownAlias() throws IOException, InterruptedException  {
        HttpServerConfig config = createConfig();
        DependencyGraph dg = InMemoryDependencyGraph.create();
        HttpServer server = new HttpServer(config, dg);
        dg.start();
        server.start();

        Thread.sleep(500);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest getUrl = HttpRequest.newBuilder()
                    .uri(testGetURI(config, dg.getAliasService().getRandomAlias()))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(SC_NOT_FOUND, response.statusCode());
        } finally {
            dg.stop();
            server.stop();
        }
    }

    @Test(expected = ConnectException.class)
    public void create() throws Exception {
        HttpServerConfig config = createConfig();
        new HttpServer(config, InMemoryDependencyGraph.create());
        connect(config);
    }

    @Test(expected = ConnectException.class)
    public void stopped() throws Exception {
        HttpServerConfig config = createConfig();
        HttpServer server = new HttpServer(config, InMemoryDependencyGraph.create());
        server.start();
        server.stop();
        connect(config);
    }

    private HttpServerConfig createConfig() {
        return HttpServerConfig.builder()
                .host("127.0.0.1")
                .port(8888)
                .idleTimeout(Duration.parse("PT1S"))
                .minThreads(1)
                .maxThreads(5)
                .queueCapacity(10)
                .build();
    }

    private URI testPutURI(HttpServerConfig config, String url) {
        return URI.create("http://" + config.getHost() + ":" + config.getPort() + "/create?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
    }

    private URI testGetURI(HttpServerConfig config, Alias alias) {
        return URI.create("http://" + config.getHost() + ":" + config.getPort() + "/" + alias.getValue());
    }

    private HttpURLConnection connect(HttpServerConfig config) throws IOException {
        URL obj = new URL("http://" + config.getHost() + ":" + config.getPort());
        HttpURLConnection result = (HttpURLConnection) obj.openConnection();
        result.setConnectTimeout(1000);
        result.setReadTimeout(1000);
        result.setRequestMethod("GET");
        result.connect();
        return result;
    }
}
