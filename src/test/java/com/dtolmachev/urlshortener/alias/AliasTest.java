package com.dtolmachev.urlshortener.alias;

import com.dtolmachev.urlshortener.alias.model.Alias;
import org.junit.Test;

public class AliasTest {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        Alias.create("");
    }

    @Test
    public void testSingleLetter() {
        for (int i = 0; i < DATA_FOR_RANDOM_STRING.length(); i++) {
            try {
                Alias.create(new String(new char[]{DATA_FOR_RANDOM_STRING.charAt(i)}));
            } catch (IllegalArgumentException e) {
                continue;
            }

            throw new RuntimeException("test failed");
        }
    }

    @Test
    public void testValidAliases() {
        Alias.create("TWFuIGlz");
        Alias.create("IGRpc3R1");
        Alias.create("pbmd1aXN");
        Alias.create("oZWQsIG5");
        Alias.create("vdCBvbmx");
        Alias.create("vdCBvbmx");
        Alias.create("5IGJ5IGh");
        Alias.create("pcyByZWF");
        Alias.create("zb24sIGJ");
        Alias.create("1dCBieSB");
        Alias.create("0aGlzIHN");
        Alias.create("pbmd1bGF");
        Alias.create("yIHBhc3N");
        Alias.create("pb24gZnJ");
        Alias.create("vbSBvdGh");
        Alias.create("lciBhbml");
        Alias.create("tYWxzLCB");
        Alias.create("3aGljaCB");
        Alias.create("pcyBhIGx");
        Alias.create("1c3Qgb2Y");
        Alias.create("gdGhlIG1");
        Alias.create("pbmQsIHR");
        Alias.create("oYXQgYnk");
        Alias.create("gYSBwZXJ");
        Alias.create("zZXZlcmF");
        Alias.create("uY2Ugb2Y");
        Alias.create("gZGVsaWd");
        Alias.create("odCBpbiB");
        Alias.create("0aGUgY29");
        Alias.create("udGludWV");
        Alias.create("kIGFuZCB");
        Alias.create("pbmRlZmF");
        Alias.create("0aWdhYmx");
        Alias.create("lIGdlbmV");
        Alias.create("yYXRpb24");
        Alias.create("gb2Yga25");
        Alias.create("vd2xlZGd");
        Alias.create("lLCBleGN");
        Alias.create("lZWRzIHR");
        Alias.create("oZSBzaG9");
        Alias.create("ydCB2ZWh");
        Alias.create("lbWVuY2U");
        Alias.create("gb2YgYW5");
        Alias.create("5IGNhcm5");
    }
}
