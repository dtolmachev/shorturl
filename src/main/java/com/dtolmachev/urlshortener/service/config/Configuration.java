package com.dtolmachev.urlshortener.service.config;

import org.springframework.transaction.TransactionDefinition;

import java.sql.Connection;
import java.time.Duration;
import java.util.regex.Pattern;

public class Configuration {

    public static final String serviceName = "url-shortener";

    public static final String ALIAS_REGEX = "[A-Za-z0-9+/]{8}";
    public static final Pattern ALIAS_PATTERN = Pattern.compile(ALIAS_REGEX);

    public static final HttpServerConfig httpServerConfig = HttpServerConfig
            .builder()
            .host("127.0.0.1")
            .port(8080)
            .idleTimeout(Duration.ofMillis(5000))
            .minThreads(1)
            .maxThreads(10)
            .queueCapacity(10)
            .build();

    public static final AliasServiceConfig aliasServiceConfig = AliasServiceConfig
            .builder()
            .period(10)
            .initialDelay(0)
            .bufferSize(1000)
            .chunkSize(100)
            .cacheSize(100)
            .cacheChunckSize(100)
            .build();

    public static final DataSourceConfig dataSourceConfig = DataSourceConfig.builder()
            .name("prod_dataasource")
            .host("127.0.0.1")
            .port(5432)
            .dbName("shorturl")
            .username("demo")
            .password("demo")
            .maxConnections(10)
            .build();

    public static final UrlCleanerConfig urlCleanerConfig = UrlCleanerConfig.builder()
            .initialDelay(5)
            .period(5)
            .build();

    public static final TransactionTemplateConfig transactionTemplateConfig = TransactionTemplateConfig.builder()
            .isolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE)
            .timeout(10000)
            .build();
}
