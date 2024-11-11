package org.omegat.languages.el;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collections;

import org.apache.lucene.analysis.hunspell.Dictionary;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

public class GreekSpellCheckerDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_PATH = "/org/languagetool/resource/el/hunspell/";

    private InputStream affixInputStream;
    private InputStream dictInputStream;

    @Override
    public Dictionary getHunspellDictionary() {
        affixInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "el_GR.aff");
        dictInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH + "el_GR.dic");
        try {
            return new Dictionary(affixInputStream, Collections.singletonList(dictInputStream), true);
        } catch (IOException | ParseException ignored) {
        }
        return null;
    }

    @Override
    public SpellCheckDictionaryType getDictionaryType() {
        return SpellCheckDictionaryType.HUNSPELL;
    }

    @Override
    public void close() {
        try {
            affixInputStream.close();
            dictInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
