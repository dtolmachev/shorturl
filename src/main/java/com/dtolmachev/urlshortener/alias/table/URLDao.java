package com.dtolmachev.urlshortener.alias.table;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;

import java.net.URL;
import java.util.List;

public interface URLDao {

    boolean save(ShortenedUrl shortenedUrl);

    ShortenedUrl get(Alias alias);

    ShortenedUrl get(URL url);

    List<Alias> getAlreadyUsed(List<Alias> aliases);

    void cleanExpired();

    void clear();

    long size();
}
