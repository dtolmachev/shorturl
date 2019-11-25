package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.service.config.UrlCleanerConfig;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class URLCleaner {

    private final UrlCleanerConfig config;
    private final URLDao urlDao;
    private final ScheduledExecutorService cleanerExecutor;

    public URLCleaner(UrlCleanerConfig config, URLDao urlDao) {
        this.config = config;
        this.urlDao = urlDao;
        this.cleanerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        cleanerExecutor.scheduleAtFixedRate(this::cleanSafely,
                config.getInitialDelay(),
                config.getPeriod(),
                TimeUnit.SECONDS);
    }

    public void stop() {
        cleanerExecutor.shutdown();
    }

    public void cleanSafely() {
        try {
            urlDao.cleanExpired();
        } catch (Exception e) {
            log.warn("error during cleaning expired urls", e);
        }
    }
}
