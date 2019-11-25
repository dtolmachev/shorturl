package com.dtolmachev.urlshortener.alias.table.postgresql;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.httpserver.util.ParametersUtil;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class URLPostgresDaoTest extends PostgresAbstractTest {

    private URLDao urlDao;
    private AliasGenerator generator;

    public URLPostgresDaoTest() {
        urlDao = new URLDaoImpl(jdbcTemplate);
        generator = new AliasGeneratorImpl();
    }

    @After
    public void close() {
        dataSource.close();
    }

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
        Alias alias = generator.generate();
        ShortenedUrl shortenedUrl = makeShortenedUrl(alias);
        urlDao.save(shortenedUrl);

        List<Alias> alreadyUsedList = urlDao.getAlreadyUsed(List.of(alias));
        Assert.assertEquals(alreadyUsedList.size(), 1);
        Alias alreadyUsed = alreadyUsedList.get(0);
        Assert.assertEquals(alias, alreadyUsed);
    }

    @Test
    public void testMultipleAlreadyUsed() {
        Alias alias1 = generator.generate();
        ShortenedUrl shortenedUrl = makeShortenedUrl(alias1);
        urlDao.save(shortenedUrl);
        Alias alias2 = generator.generate();
        ShortenedUrl shortenedUrl2 = makeShortenedUrl(alias2);
        urlDao.save(shortenedUrl2);

        List<Alias> alreadyUsedList = urlDao.getAlreadyUsed(List.of(alias1, alias2));
        Assert.assertEquals(alreadyUsedList.size(), 2);
        Assert.assertTrue(alreadyUsedList.contains(alias1));
        Assert.assertTrue(alreadyUsedList.contains(alias2));
    }

    @Test
    public void testAlreadySaved() {
        Alias alias1 = generator.generate();
        ShortenedUrl shortenedUrl = makeShortenedUrl(alias1);
        urlDao.save(shortenedUrl);

        ShortenedUrl found = urlDao.get(shortenedUrl.getUrl());
        Assert.assertEquals(shortenedUrl, found);
    }

    @SneakyThrows
    private ShortenedUrl makeShortenedUrl(Alias alias) {
        URL toSave = new URL("https://mvnrepository.com/artifact/postgresql/postgresql");
        DateTime createTime = DateTime.now();
        DateTime expireTime = DateTime.now().plus(ParametersUtil.DEFAULT_TTL);
        return ShortenedUrl.builder()
                .alias(alias)
                .url(toSave)
                .createDate(createTime)
                .expireDate(expireTime)
                .build();
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
