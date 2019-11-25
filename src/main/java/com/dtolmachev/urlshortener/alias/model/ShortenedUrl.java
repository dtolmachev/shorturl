package com.dtolmachev.urlshortener.alias.model;

import lombok.Builder;
import lombok.Value;
import org.joda.time.DateTime;

import java.net.URL;

@Value
@Builder
public class ShortenedUrl {

    Alias alias;

    URL url;

    DateTime createDate;

    DateTime expireDate;
}
