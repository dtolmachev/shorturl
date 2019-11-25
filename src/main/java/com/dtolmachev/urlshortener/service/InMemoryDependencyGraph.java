package com.dtolmachev.urlshortener.service;

import com.dtolmachev.urlshortener.alias.AliasService;
import com.dtolmachev.urlshortener.alias.SimpleAliasService;
import com.dtolmachev.urlshortener.alias.URLCleaner;
import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.table.inmemory.InMemoryAliasDao;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.inmemory.InMemoryURLDao;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.service.config.Configuration;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class InMemoryDependencyGraph implements DependencyGraph {

    private final AliasDao aliasDao;
    private final URLDao urlDao;
    private final AliasGenerator aliasGenerator;
    private final AliasService aliasService;
    private final URLCleaner urlCleaner;

    public static DependencyGraph create() {
        InMemoryAliasDao aliasDao = new InMemoryAliasDao();
        AliasGenerator aliasGenerator = new AliasGeneratorImpl();
        URLDao urlDao = new InMemoryURLDao();
        URLCleaner urlCleaner = new URLCleaner(Configuration.urlCleanerConfig, urlDao);
        SimpleAliasService aliasService = new SimpleAliasService(Configuration.aliasServiceConfig, aliasDao, urlDao, aliasGenerator);
        return new InMemoryDependencyGraph(aliasDao, urlDao, aliasGenerator, aliasService, urlCleaner);
    }

    public void start() {
        aliasService.start();
        urlCleaner.start();
    }

    public void stop() {
        aliasService.stop();
        urlCleaner.stop();
    }
}
