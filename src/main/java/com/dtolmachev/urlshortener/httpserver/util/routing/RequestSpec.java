package com.dtolmachev.urlshortener.httpserver.util.routing;

import lombok.NonNull;
import lombok.Value;

@Value
public class RequestSpec {

    @NonNull
    String method;

    @NonNull
    String uri;
}
