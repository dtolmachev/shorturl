package com.dtolmachev.urlshortener.integration;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.httpserver.HttpServer;
import com.dtolmachev.urlshortener.service.DependencyGraph;
import com.dtolmachev.urlshortener.service.DependencyGraphImpl;
import com.dtolmachev.urlshortener.service.config.HttpServerConfig;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;

public class IntegrationTest {

    private static HttpServerConfig config;

    private static DependencyGraph dg;
    private static HttpServer server;

    @BeforeClass
    @SneakyThrows
    public static void init() {
        config = createConfig();
        dg = DependencyGraphImpl.create();
        server = new HttpServer(config, dg);

        dg.start();
        server.start();
    }

    @AfterClass
    public static void stop() {
        dg.stop();
        server.stop();
    }

    @Test
    public void simplePutAndGet() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URL url = new URL("https://openjdk.java.net/groups/net/httpclient/recipes.html");
        HttpRequest putUrlRequest = createPutRequest(url);
        HttpResponse<String> response = client.send(putUrlRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_OK, response.statusCode());
        Alias alias = getAlias(response);

        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testGetURI(config, alias))
                .GET()
                .build();
        HttpResponse<String> response2 = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_MOVED_PERMANENTLY, response2.statusCode());
        Assert.assertEquals(url.toString(), response2.headers().firstValue(LOCATION_HEADER).orElse(""));
    }

    @Test
    public void testPutTheSameUrl() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URL toPut = new URL("https://openjdk.java.net/groups/net/httpclient/recipes.html");
        HttpRequest putUrlRequest = createPutRequest(toPut);
        HttpResponse<String> response = client.send(putUrlRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_OK, response.statusCode());
        Alias alias = getAlias(response);

        HttpResponse<String> response2 = client.send(putUrlRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_OK, response2.statusCode());
        Alias alias2 = getAlias(response);
        Assert.assertEquals(alias, alias2);
    }

    @Test
    public void testNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testGetURI(config, dg.getAliasGenerator().generate()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_NOT_FOUND, response.statusCode());
    }

    @Test
    public void tryPutInvalidUrl() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testPutURI(config, "sldkflsdf"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    public void tryPutEmptyUrl() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testPutURI(config, ""))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    public void tryPutWrongMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testPutURI(config, "https://github.com/kagkarlsson/db-scheduler#why-db-scheduler-when-there-is-quartz"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_METHOD_NOT_ALLOWED, response.statusCode());
    }

    @Test
    public void testPutTooLongUrl() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String urlStr = "https://www.google.com/search?as_q=you+have+to+write+a+really+really+long+search+to+get+to+2000+characters.+like+seriously%2C+you+have+no+idea+how+long+it+has+to+be&as_epq=2000+characters+is+absolutely+freaking+enormous.+You+can+fit+sooooooooooooooooooooooooooooooooo+much+data+into+2000+characters.+My+hands+are+getting+tired+typing+this+many+characters.+I+didn%27t+even+realise+how+long+it+was+going+to+take+to+type+them+all.&as_oq=Argh!+So+many+characters.+I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.+I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.I%27m+bored+now%2C+so+I%27ll+just+copy+and+paste.&as_eq=It+has+to+be+freaking+enormously+freaking+enormous&as_nlo=123&as_nhi=456&lr=lang_hu&cr=countryAD&as_qdr=m&as_sitesearch=stackoverflow.com&as_occt=title&safe=active&tbs=rl%3A1%2Crls%3A0&as_filetype=xls&as_rights=(cc_publicdomain%7Ccc_attribute%7Ccc_sharealike%7Ccc_nonderived).-(cc_noncommercial)&gws_rd=sslaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        HttpRequest getUrl = HttpRequest.newBuilder()
                .uri(testPutURI(config, urlStr))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(getUrl, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(SC_REQUEST_ENTITY_TOO_LARGE, response.statusCode());
    }

    private HttpRequest createPutRequest(URL url) {
        return HttpRequest.newBuilder()
                .uri(testPutURI(config, url.toString()))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();
    }

    private Alias getAlias(HttpResponse<String> response) {
        return Alias.create(response.body().replace("\"", ""));
    }

    @SneakyThrows
    private URI testPutURI(HttpServerConfig config, String url) {
        String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);
        return URI.create("http://" + config.getHost() + ":" + config.getPort() + "/create?url=" + encodedUrl);
    }

    private URI testGetURI(HttpServerConfig config, Alias alias) {
        return URI.create("http://" + config.getHost() + ":" + config.getPort() + "/" + alias.getValue());
    }

    private static HttpServerConfig createConfig() {
        return HttpServerConfig.builder()
                .host("127.0.0.1")
                .port(8080)
                .idleTimeout(Duration.parse("PT1S"))
                .minThreads(1)
                .maxThreads(5)
                .queueCapacity(10)
                .build();
    }
}
