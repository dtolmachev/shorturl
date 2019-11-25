package com.dtolmachev.urlshortener.service;

import com.dtolmachev.urlshortener.alias.AliasService;
import com.dtolmachev.urlshortener.alias.AliasServiceImpl;
import com.dtolmachev.urlshortener.alias.URLCleaner;
import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.URLDao;

public interface DependencyGraph {

    AliasDao getAliasDao();

    URLDao getUrlDao();

    AliasGenerator getAliasGenerator();

    AliasService getAliasService();

    URLCleaner getUrlCleaner();

    void start();

    void stop();
}
