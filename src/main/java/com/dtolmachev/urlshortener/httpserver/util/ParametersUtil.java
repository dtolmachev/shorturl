package com.dtolmachev.urlshortener.httpserver.util;

import org.joda.time.Duration;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class ParametersUtil {

    public static final String ALIAS_PARAMETER = "alias";
    public static final String LOCATION_HEADER = "Location";
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String TEXT_HTML_CONTENT_TYPE = "text/html";
    public static final int URL_MAX_LENGTH = 2048;
    public static final Duration DEFAULT_TTL = Duration.standardMinutes(5);

    public static String generateRequestId(HttpServletRequest request) {
        String reqId = request.getHeader(X_REQUEST_ID);
        if (reqId != null) {
            return reqId;
        }
        return UUID.randomUUID().toString();
    }
}
