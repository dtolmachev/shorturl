package com.dtolmachev.urlshortener.alias.generator;

import com.dtolmachev.urlshortener.alias.model.Alias;

import java.util.List;

public interface AliasGenerator {

    List<Alias> generate(int size);

    Alias generate();
}
