package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasCacheException;
import com.dtolmachev.urlshortener.service.config.AliasServiceConfig;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class AliasServiceImpl implements AliasService {

    private final ScheduledExecutorService statisticsExecutor = Executors.newScheduledThreadPool(2);

    private final AliasServiceConfig config;
    private final AliasDao aliasDao;
    private final URLDao urlDao;
    private final AliasGenerator aliasGenerator;

    private final ConcurrentLinkedDeque<Alias> cache;

    public AliasServiceImpl(AliasServiceConfig config,
                            AliasDao aliasDao,
                            URLDao urlDao,
                            AliasGenerator aliasGenerator) {
        validate(config);
        this.config = config;
        this.aliasDao = aliasDao;
        this.urlDao = urlDao;
        this.aliasGenerator = aliasGenerator;
        this.cache = new ConcurrentLinkedDeque<>();
    }

    public void start() {
        statisticsExecutor.scheduleAtFixedRate(this::generateAliasesSafely,
                config.getInitialDelay(),
                config.getPeriod(),
                TimeUnit.SECONDS);
        statisticsExecutor.scheduleAtFixedRate(this::flushToCache,
                5,
                1,
                TimeUnit.SECONDS);
    }

    public Alias getRandomAlias() {
        Alias alias = cache.poll();
        if (alias == null) {
            throw new EmptyAliasCacheException();
        }
        return alias;
    }

    public void stop() {
        statisticsExecutor.shutdown();
    }

    private void generateAliasesSafely() {
        try {
            while (lackOfAliasesInDb()) {
                log.info("alias table size {}, generating aliases to have {} aliases in buffer", aliasDao.size(), config.getBufferSize());
                List<Alias> aliases = aliasGenerator.generate(config.getChunkSize());
                List<Alias> alreadyUsed = urlDao.getAlreadyUsed(aliases);
                aliases.removeAll(alreadyUsed);
                aliasDao.save(aliases);
            }
        } catch (Exception e) {
            log.warn("error during alias generation", e);
        }
    }

    private void flushToCache() {
        try {
            while (lackOfAliasesInCache()) {
                log.info("cache size {}, flush cache size to have {} aliases in cache", cache.size(), config.getCacheSize());
                List<Alias> alias = aliasDao.get(config.getCacheChunckSize());
                cache.addAll(alias);
                if (!alias.isEmpty()) {
                    aliasDao.remove(alias);
                }
            }
        } catch (Exception e) {
            log.warn("error during flush to cache", e);
        }
    }

    private boolean lackOfAliasesInDb() {
        return aliasDao.size() < config.getBufferSize();
    }

    private boolean lackOfAliasesInCache() {
        return cache.size() < config.getCacheSize();
    }

    private void validate(AliasServiceConfig config) {
        if (config.getInitialDelay() < 0) {
            throw new IllegalArgumentException("initial delay should be zero or a positive number");
        }
        if (config.getPeriod() <= 0) {
            throw new IllegalArgumentException("period should be a positive number");
        }
        if (config.getBufferSize() <= 0) {
            throw new IllegalArgumentException("alias buffer size should be a positive number");
        }
        if (config.getChunkSize() <= 0) {
            throw new IllegalArgumentException("chunk size should be a positive number");
        }
        if (config.getCacheSize() <= 0) {
            throw new IllegalArgumentException("cache size should be a positive number");
        }
        if (config.getCacheChunckSize() <= 0) {
            throw new IllegalArgumentException("cache chunk size should be a positive number");
        }
    }
}
