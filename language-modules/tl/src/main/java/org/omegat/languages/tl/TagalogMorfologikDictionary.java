package org.omegat.languages.tl;

import java.io.IOException;
import java.io.InputStream;

import morfologik.stemming.Dictionary;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

public class TagalogMorfologikDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_BASE = "/org/languagetool/resource/tl/hunspell/";
    private static final String DICT_EXT = ".dict";
    private static final String META_EXT = ".info";
    private static final String DICT = "tagalog";

    private InputStream infoInputStream;
    private InputStream dictInputStream;

    @Override
    public Dictionary getMorfologikDictionary(String language) {
            if ("ta".equals(language)) {
            infoInputStream = JLanguageTool.getDataBroker()
                    .getAsStream(DICTIONARY_BASE + DICT + META_EXT);
            dictInputStream = JLanguageTool.getDataBroker()
                    .getAsStream(DICTIONARY_BASE + DICT + DICT_EXT);
            try {
                return Dictionary.read(dictInputStream, infoInputStream);
            } catch (IOException ignored) {
            }

        }
        return null;
    }

    @Override
    public SpellCheckDictionaryType getDictionaryType() {
        return SpellCheckDictionaryType.MORFOLOGIK;
    }

    @Override
    public void close() {
        try {
            infoInputStream.close();
            dictInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
