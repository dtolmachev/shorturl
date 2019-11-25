package com.dtolmachev.urlshortener.alias.model;

import lombok.Value;

import java.util.regex.Matcher;

import static com.dtolmachev.urlshortener.service.config.Configuration.ALIAS_PATTERN;

@Value
public class Alias {

    public static Alias create(String value) {
        return new Alias(value);
    }

    private Alias(String value) {
        Matcher matcher = ALIAS_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("alias %s doesn't match the pattern ", value));
        }
        this.value = value;
    }

    String value;
}
