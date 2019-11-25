package com.dtolmachev.urlshortener.alias.table.exception;

public class TooLongUrlException extends RuntimeException {

    private final String url;

    public TooLongUrlException(String url) {
        super();
        this.url = url;
    }
}
