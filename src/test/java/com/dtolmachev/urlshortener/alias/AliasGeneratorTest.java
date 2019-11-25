package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;

import static com.dtolmachev.urlshortener.service.config.Configuration.ALIAS_PATTERN;

public class AliasGeneratorTest {

    private final AliasGenerator aliasGenerator = new AliasGeneratorImpl();

    @Test
    public void testGenSingleAlias() {
        for (int i = 0; i < 100; i++) {
            Alias alias = aliasGenerator.generate();
            checkAlias(alias);
        }
    }

    @Test
    public void testMultipleAlias() {
       List<Alias> aliases = aliasGenerator.generate(100);
       aliases.forEach(this::checkAlias);
    }

    private void checkAlias(Alias alias) {
        Matcher matcher = ALIAS_PATTERN.matcher(alias.getValue());
        Assert.assertTrue(matcher.matches());
    }
}
