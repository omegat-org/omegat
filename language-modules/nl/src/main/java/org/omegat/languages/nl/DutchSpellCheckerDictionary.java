package org.omegat.languages.nl;

import java.io.IOException;
import java.io.InputStream;

import morfologik.stemming.Dictionary;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

public class DutchSpellCheckerDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/nl/spelling/";

    private InputStream infoInputStream;
    private InputStream dictInputStream;

    @Override
    public Dictionary getMofologikDictionary() {
        infoInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "nl_NL.info");
        dictInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "nl_NL.dict");
        try {
            return Dictionary.read(dictInputStream, infoInputStream);
        } catch (IOException ignored) {
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
