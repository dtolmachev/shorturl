package com.dtolmachev.urlshortener.httpserver;

import com.dtolmachev.urlshortener.httpserver.util.routing.RequestSpec;
import com.dtolmachev.urlshortener.httpserver.util.routing.Rewriter;
import org.junit.Assert;
import org.junit.Test;

import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.createRoute;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.getRoute;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.healthRoute;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.pingRoute;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.unknownRoute;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.wrongMethodRoute;
import static java.util.Arrays.asList;

public class RewriteFilterTest {

    Rewriter rewriter = new Rewriter(
            asList(createRoute,
                    getRoute,
                    healthRoute,
                    pingRoute,
                    unknownRoute,
                    wrongMethodRoute),
            unknownRoute,
            wrongMethodRoute
    );

    @Test
    public void testPingOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/ping"));
        Assert.assertEquals(result, "/ping");
    }

    @Test
    public void testPingWithSlashOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/ping/"));
        Assert.assertEquals(result, "/ping");
    }

    @Test
    public void testPingFail() {
        String result = rewriter.apply(new RequestSpec("GET", "/ping_"));
        Assert.assertEquals(result, "/unknown?uri=/ping_");
    }

    @Test
    public void testPingWrongMethod() {
        String result = rewriter.apply(new RequestSpec("POST", "/ping"));
        Assert.assertEquals(result, "/wrongmethod?uri=/ping");
    }

    @Test
    public void testHealthOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/health"));
        Assert.assertEquals(result, "/health");
    }

    @Test
    public void testHealthWithSlashOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/health/"));
        Assert.assertEquals(result, "/health");
    }

    @Test
    public void testHealthFail() {
        String result = rewriter.apply(new RequestSpec("GET", "/health_"));
        Assert.assertEquals(result, "/unknown?uri=/health_");
    }

    @Test
    public void testHealthWrongMethod() {
        String result = rewriter.apply(new RequestSpec("POST", "/health"));
        Assert.assertEquals(result, "/wrongmethod?uri=/health");
    }

    @Test
    public void testGetOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/lciBhbml"));
        Assert.assertEquals(result, "/get?alias=lciBhbml");
    }

    @Test
    public void testGetWithSlashOk() {
        String result = rewriter.apply(new RequestSpec("GET", "/tYWxzLCB/"));
        Assert.assertEquals(result, "/get?alias=tYWxzLCB");
    }

    @Test
    public void testGetFail() {
        String result = rewriter.apply(new RequestSpec("GET", "/tYWxzLCB1"));
        Assert.assertEquals(result, "/unknown?uri=/tYWxzLCB1");
    }

    @Test
    public void testGetFail2() {
        String result = rewriter.apply(new RequestSpec("GET", "/tYWxzLCB$"));
        Assert.assertEquals(result, "/unknown?uri=/tYWxzLCB$");
    }

    @Test
    public void testGetWrongMethod() {
        String result = rewriter.apply(new RequestSpec("POST", "/tYWxzLCB"));
        Assert.assertEquals(result, "/wrongmethod?uri=/tYWxzLCB");
    }

    @Test
    public void testCreateOk() {
        String result = rewriter.apply(new RequestSpec("PUT", "/create"));
        Assert.assertEquals(result, "/create");
    }

    @Test
    public void testCreateWithSlashOk() {
        String result = rewriter.apply(new RequestSpec("PUT", "/create/"));
        Assert.assertEquals(result, "/create");
    }

    @Test
    public void testCreateWrongMethod() {
        String result = rewriter.apply(new RequestSpec("POST", "/create"));
        Assert.assertEquals(result, "/wrongmethod?uri=/create");
    }
}
