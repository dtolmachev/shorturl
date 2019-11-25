package com.dtolmachev.urlshortener.service.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DataSourceConfig {

    String name;

    String host;

    int port;

    String dbName;

    String username;

    String password;

    int maxConnections;
}
