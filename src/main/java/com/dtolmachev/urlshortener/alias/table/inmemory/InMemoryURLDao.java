package com.dtolmachev.urlshortener.alias.table.inmemory;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.exception.NoSuchAliasException;
import org.joda.time.DateTime;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryURLDao implements URLDao {

    private final ConcurrentHashMap<Alias, ShortenedUrl> aliasToURL;

    public InMemoryURLDao() {
        this.aliasToURL = new ConcurrentHashMap<>();
    }

    @Override
    public boolean save(ShortenedUrl shortenedUrl) {
        aliasToURL.put(shortenedUrl.getAlias(), shortenedUrl);
        return true;
    }

    @Override
    public ShortenedUrl get(Alias alias) {
        ShortenedUrl url = aliasToURL.get(alias);
        if (url != null) {
            return url;
        }
        throw new NoSuchAliasException(alias);
    }

    @Override
    public ShortenedUrl get(URL url) {
        return aliasToURL.values()
                .stream()
                .filter(su -> su.getUrl().toString().equals(url.toString()))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<Alias> getAlreadyUsed(List<Alias> aliases) {
        return aliasToURL.keySet()
                .stream()
                .filter(aliases::contains)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanExpired() {
        List<Alias> toDelete = aliasToURL.values()
                .stream()
                .filter(su -> su.getExpireDate().isBefore(DateTime.now()))
                .map(ShortenedUrl::getAlias)
                .collect(Collectors.toList());
        toDelete.forEach(aliasToURL::remove);
    }

    @Override
    public void clear() {
        aliasToURL.clear();
    }

    @Override
    public long size() {
        return aliasToURL.size();
    }
}
