package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.model.Alias;

public interface AliasService {

    void start();

    void stop();

    Alias getRandomAlias();
}
