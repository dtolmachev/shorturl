package com.dtolmachev.urlshortener.alias.table.inmemory;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasTableException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemoryAliasDao implements AliasDao {

    private final ConcurrentLinkedDeque<Alias> aliasSet;

    public InMemoryAliasDao() {
        this.aliasSet = new ConcurrentLinkedDeque<>();
    }

    @Override
    public long size() {
        return aliasSet.size();
    }

    @Override
    public Alias get() {
        if (aliasSet.isEmpty()) {
            throw new EmptyAliasTableException();
        }
        return aliasSet.poll();
    }

    @Override
    public List<Alias> get(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> aliasSet.poll())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void save(List<Alias> aliases) {
        aliases.forEach(aliasSet::offerLast);
    }

    @Override
    public boolean remove(List<Alias> aliases) {
        return aliasSet.removeAll(aliases);
    }

    @Override
    public void clear() {
        aliasSet.clear();
    }
}
