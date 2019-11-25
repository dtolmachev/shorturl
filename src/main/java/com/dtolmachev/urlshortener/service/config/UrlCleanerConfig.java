package com.dtolmachev.urlshortener.service.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UrlCleanerConfig {

    int initialDelay;

    int period;
}
