package com.dtolmachev.urlshortener.service.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AliasServiceConfig {

    int initialDelay;

    int period;

    int bufferSize;

    int chunkSize;

    int cacheSize;

    int cacheChunckSize;
}
