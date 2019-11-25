package com.dtolmachev.urlshortener.service.config;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;

@Value
@Builder
public class HttpServerConfig {

    String host;

    int port;

    @NonNull
    Duration idleTimeout;

    int minThreads;

    int maxThreads;

    int queueCapacity;
}
