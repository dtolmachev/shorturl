package com.dtolmachev.urlshortener.httpserver.filters;

import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasCacheException;
import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasTableException;
import com.dtolmachev.urlshortener.alias.table.exception.NoSuchAliasException;
import com.dtolmachev.urlshortener.alias.table.exception.TooLongUrlException;
import com.dtolmachev.urlshortener.alias.table.exception.WrongUrlFormatException;
import com.dtolmachev.urlshortener.httpserver.util.ParametersUtil;
import com.dtolmachev.urlshortener.httpserver.util.ResponseWriter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.TEXT_HTML_CONTENT_TYPE;
import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.X_REQUEST_ID;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;

@Log4j2
public class ExceptionFilter implements Filter {

    private static final Error DEFAULT_ERROR = new Error(SC_INTERNAL_SERVER_ERROR, "Internal problem");

    private static final Map<Class<? extends Exception>, Error> exceptionToError =
            ImmutableMap.<Class<? extends Exception>, Error>builder()
                    .put(EmptyAliasTableException.class,
                            new Error(SC_INTERNAL_SERVER_ERROR, "Internal problem, cannot make alias for url"))
                    .put(EmptyAliasCacheException.class,
                            new Error(SC_INTERNAL_SERVER_ERROR, "Internal problem, cannot make alias for url"))
                    .put(NoSuchAliasException.class,
                            new Error(SC_NOT_FOUND, "Alias not found"))
                    .put(TooLongUrlException.class,
                            new Error(SC_REQUEST_ENTITY_TOO_LARGE, "URL is too long"))
                    .put(WrongUrlFormatException.class,
                            new Error(SC_BAD_REQUEST, "URL has wrong format"))
                    .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        Stopwatch stopwatch = Stopwatch.createStarted();
        String reqId = ParametersUtil.generateRequestId(req);
        try {
            resp.setHeader(X_REQUEST_ID, reqId);
            chain.doFilter(request, response);
        } catch (Exception e) {
            Error error = handleException(e);
            responseError(req, resp, error);
        } finally {
            logRequest(reqId, req, resp, stopwatch.stop());
        }
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() { }

    private Error handleException(Exception e) {
        Error error = exceptionToError.get(e.getClass());
        if (error == null) {
            log.error(String.format("Internal error: %s, %s", e.getMessage(), e));
            return DEFAULT_ERROR;
        }
        return error;
    }

    @SneakyThrows
    private void responseError(HttpServletRequest req, HttpServletResponse resp, Error error) {
        resp.setHeader(CONTENT_TYPE, TEXT_HTML_CONTENT_TYPE);
        resp.setStatus(error.httpCode);
        ResponseWriter.write(resp, error.msg);
    }

    private void logRequest(String reqId,
                            HttpServletRequest req,
                            HttpServletResponse resp,
                            Stopwatch stopwatch) {
        String logStr = String
                .format("request-id [%s], request: [%s], response [%s], duration [%s]", reqId, requestToString(req), resp, stopwatch);
        log.info(logStr);
    }

    private String requestToString(HttpServletRequest req) {
        return String.format("%s, %s, %s", req.getMethod(), req.getRequestURI(), req.getPathInfo());
    }

    @Value
    private static class Error {
        int httpCode;
        String msg;
    }
}
