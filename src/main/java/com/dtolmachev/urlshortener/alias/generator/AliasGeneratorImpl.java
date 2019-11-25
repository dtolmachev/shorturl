package com.dtolmachev.urlshortener.alias.generator;

import com.dtolmachev.urlshortener.alias.model.Alias;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AliasGeneratorImpl implements AliasGenerator {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final int ALIAS_LENGTH = 8;

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();

    @Override
    public List<Alias> generate(int size) {
        return IntStream.range(0, size)
                .mapToObj(idx -> generate())
                .collect(Collectors.toList());
    }

    public Alias generate() {
        StringBuilder sb = new StringBuilder(ALIAS_LENGTH);
        IntStream.range(0, ALIAS_LENGTH)
                .forEach(r -> {
                    int rndCharIndx = random.nextInt(DATA_FOR_RANDOM_STRING.length());
                    char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharIndx);
                    sb.append(rndChar);
                });
        return Alias.create(sb.toString());
    }

}
