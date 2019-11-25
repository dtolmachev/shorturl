package com.dtolmachev.urlshortener.alias.table.inmemory;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class AliasDaoTest {

    private final AliasGenerator generator = new AliasGeneratorImpl();
    private final AliasDao aliasDao = new InMemoryAliasDao();

    @Before
    public void clearTable() {
        aliasDao.clear();
    }

    @Test
    public void testEmptyTable() {
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
        aliasDao.save(asList(toSave1, toSave2));
        Assert.assertEquals(2, aliasDao.size());

        Alias alias1 = aliasDao.get();
        Assert.assertEquals(toSave1, alias1);

        Alias alias2 = aliasDao.get();
        Assert.assertEquals(toSave2, alias2);
    }

    @Test
    public void testMultipleAliases() {
        List<Alias> aliases = generator.generate(100);
        aliasDao.save(aliases);

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(100 - i, aliasDao.size());
            Alias alias = aliasDao.get();
            Assert.assertEquals(alias, aliases.get(i));
        }
    }

    @Test
    public void testGetLimit() {
        List<Alias> aliases = generator.generate(10);
        aliasDao.save(aliases);

        List<Alias> alias = aliasDao.get(5);
        Assert.assertEquals(5, alias.size());

        aliasDao.remove(alias);
        Assert.assertEquals(5, aliasDao.size());
    }
}
