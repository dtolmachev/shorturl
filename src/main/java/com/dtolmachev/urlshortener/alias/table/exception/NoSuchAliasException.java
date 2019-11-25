package com.dtolmachev.urlshortener.alias.table.exception;

import com.dtolmachev.urlshortener.alias.model.Alias;

public class NoSuchAliasException extends RuntimeException {

    private final Alias alias;

    public NoSuchAliasException(Alias alias) {
        super();
        this.alias = alias;
    }
}
