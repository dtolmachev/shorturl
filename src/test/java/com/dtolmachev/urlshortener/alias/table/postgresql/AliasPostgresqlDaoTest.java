package com.dtolmachev.urlshortener.alias.table.postgresql;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

public class AliasPostgresqlDaoTest extends PostgresAbstractTest {

    private AliasDaoImpl aliasDao;
    private AliasGenerator generator;

    public AliasPostgresqlDaoTest() {
        aliasDao = new AliasDaoImpl(jdbcTemplate);
        generator = new AliasGeneratorImpl();
    }

    @Before
    public void clearTable() {
        aliasDao.clear();
    }

    @After
    public void close() {
        dataSource.close();
    }

    @Test
    public void testEmptyTable() throws Exception{
        Assert.assertEquals(aliasDao.size(), 0);
    }

    @Test
    public void testAddAlias() {
        Alias toSave = generator.generate();
        aliasDao.save(Collections.singletonList(toSave));
        Assert.assertEquals(aliasDao.size(), 1);
        Alias alias = aliasDao.get();
        Assert.assertEquals(toSave, alias);
    }

    @Test
    public void testTwoAliases() {
        Alias toSave1 = generator.generate();
        Alias toSave2 = generator.generate();

        HashSet<Alias> set = new HashSet<>();
        set.add(toSave1);
        set.add(toSave2);

        aliasDao.save(asList(toSave1, toSave2));
        Assert.assertEquals(aliasDao.size(), 2);

        Alias alias1 = aliasDao.get();
        Assert.assertTrue(set.contains(alias1));

        Alias alias2 = aliasDao.get();
        Assert.assertTrue(set.contains(alias2));
    }

    @Test
    public void testMultipleAliases() {
        List<Alias> aliases = generator.generate(100);
        HashSet<Alias> set = new HashSet<>();
        set.addAll(aliases);

        aliasDao.save(aliases);

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(aliasDao.size(), 100 - i);
            Alias alias = aliasDao.get();
            aliasDao.deleteById(alias);
            Assert.assertTrue(set.contains(alias));
        }
    }

    @Test
    public void testGetLimit() {
        List<Alias> aliases = generator.generate(10);
        aliasDao.save(aliases);

        List<Alias> chunk = aliasDao.get(5);
        Assert.assertEquals(5, chunk.size());

        aliasDao.remove(chunk);
        Assert.assertEquals(5, aliasDao.size());
    }
}
