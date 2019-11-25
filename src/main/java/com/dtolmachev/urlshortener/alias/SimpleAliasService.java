package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.service.config.AliasServiceConfig;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SimpleAliasService implements AliasService {

    private final ScheduledExecutorService statisticsExecutor = Executors.newScheduledThreadPool(1);

    private final AliasServiceConfig config;
    private final AliasDao aliasDao;
    private final URLDao urlDao;
    private final AliasGenerator aliasGenerator;

    public SimpleAliasService(AliasServiceConfig config,
                              AliasDao aliasDao,
                              URLDao urlDao,
                              AliasGenerator aliasGenerator) {
        validate(config);
        this.config = config;
        this.aliasDao = aliasDao;
        this.urlDao = urlDao;
        this.aliasGenerator = aliasGenerator;
    }

    public void start() {
        statisticsExecutor.scheduleAtFixedRate(this::generateAliasesSafely,
                config.getInitialDelay(),
                config.getPeriod(),
                TimeUnit.SECONDS);
    }

    public Alias getRandomAlias() {
        return aliasDao.get();
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

    private boolean lackOfAliasesInDb() {
        return aliasDao.size() < config.getBufferSize();
    }

    private void validate(AliasServiceConfig config) {
        if (config.getInitialDelay() < 0) {
            throw new IllegalArgumentException("initial delay should be zero or a positive number");
        }

        if (config.getPeriod() <= 0) {
            throw new IllegalArgumentException("period should be a positive number");
        }

        if (config.getBufferSize() <= 0) {
            throw new IllegalArgumentException("alias buffer size should be positive number");
        }

        if (config.getChunkSize() <= 0) {
            throw new IllegalArgumentException("chunk size should be positive number");
        }
    }
}
