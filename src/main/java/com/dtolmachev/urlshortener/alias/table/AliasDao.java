package com.dtolmachev.urlshortener.alias.table;

import com.dtolmachev.urlshortener.alias.model.Alias;

import java.util.List;

public interface AliasDao {

    long size();

    Alias get();

    List<Alias> get(int size);

    void save(List<Alias> aliases);

    boolean remove(List<Alias> aliases);

    void clear();
}
