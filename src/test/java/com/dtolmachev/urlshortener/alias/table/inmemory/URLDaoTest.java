package com.dtolmachev.urlshortener.alias.table.inmemory;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.httpserver.util.ParametersUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class URLDaoTest {

    private final AliasGenerator generator = new AliasGeneratorImpl();
    private final URLDao urlDao = new InMemoryURLDao();

    @Before
    public void clearTable() {
        urlDao.clear();
    }

    @Test
    public void testEmptyTable() {
        Assert.assertEquals(urlDao.size(), 0);
    }

    @Test
    public void testAddAlias() throws MalformedURLException {
        URL toSave = new URL("https://mvnrepository.com/artifact/postgresql/postgresql");
        Alias alias = generator.generate();
        DateTime createTime = DateTime.now();
        DateTime expireTime = DateTime.now().plus(ParametersUtil.DEFAULT_TTL);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .alias(alias)
                .url(toSave)
                .createDate(createTime)
                .expireDate(expireTime)
                .build();
        urlDao.save(shortenedUrl);
        Assert.assertEquals(urlDao.size(), 1);
        ShortenedUrl saved = urlDao.get(alias);

        Assert.assertEquals(toSave, saved.getUrl());
        Assert.assertEquals(alias, saved.getAlias());
        Assert.assertEquals(createTime, saved.getCreateDate());
        Assert.assertEquals(expireTime, saved.getExpireDate());
    }

    @Test
    public void testSingleAlreadyUsed() throws MalformedURLException {
        URL toSave = new URL("https://mvnrepository.com/artifact/postgresql/postgresql");
        Alias alias = generator.generate();
        DateTime createTime = DateTime.now();
        DateTime expireTime = DateTime.now().plus(ParametersUtil.DEFAULT_TTL);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .alias(alias)
                .url(toSave)
                .createDate(createTime)
                .expireDate(expireTime)
                .build();
        urlDao.save(shortenedUrl);

        List<Alias> alreadyUsedList = urlDao.getAlreadyUsed(List.of(alias));
        Assert.assertEquals(alreadyUsedList.size(), 1);
        Alias alreadyUsed = alreadyUsedList.get(0);
        Assert.assertEquals(alias, alreadyUsed);
    }

    @Test
    public void testCleanExpired() throws MalformedURLException {
        URL toSave = new URL("https://mvnrepository.com/artifact/postgresql/postgresql");
        Alias alias = generator.generate();
        DateTime createTime = DateTime.now();
        DateTime expireTime = DateTime.now();

        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .alias(alias)
                .url(toSave)
                .createDate(createTime)
                .expireDate(expireTime)
                .build();

        urlDao.save(shortenedUrl);
        long size1 = urlDao.size();
        Assert.assertEquals(1, size1);
        urlDao.cleanExpired();

        long size2 = urlDao.size();
        Assert.assertEquals(0, size2);
    }
}
