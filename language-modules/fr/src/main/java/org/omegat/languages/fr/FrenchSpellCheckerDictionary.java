package org.omegat.languages.fr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.languagetool.JLanguageTool;

import org.omegat.core.spellchecker.ISpellCheckerDictionary;
import org.omegat.core.spellchecker.SpellCheckDictionaryType;

public class FrenchSpellCheckerDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICTIONARY_PATH = "/org/omegat/languages/fr/fr_FR.dic";
    private static final String AFFIX_PATH = "/org/omegat/languages/fr/fr_FR.aff";

    private InputStream affixInputStream;
    private InputStream dictInputStream;

    @Override
    public Dictionary getHunspellDictionary() {
        affixInputStream = JLanguageTool.getDataBroker().getAsStream(AFFIX_PATH);
        dictInputStream = JLanguageTool.getDataBroker().getAsStream(DICTIONARY_PATH);
        try {
            return new Dictionary(affixInputStream, Collections.singletonList(dictInputStream), true);
        } catch (IOException | ParseException ignored) {
        }
        return null;
    }

    @Override
    public Path installHunspellDictionary(Path dictionaryDir) {
        try {
            Path dictionaryPath = dictionaryDir.resolve("fr_FR.dic");
            try (InputStream dicStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(DICTIONARY_PATH);
                 FileOutputStream fos = new FileOutputStream(dictionaryPath.toFile())) {
                IOUtils.copy(dicStream, fos);
            }
            File affixFile = dictionaryDir.resolve("fr_FR.aff").toFile();
            try (InputStream affStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(AFFIX_PATH);
                 FileOutputStream fos = new FileOutputStream(affixFile)) {
                IOUtils.copy(affStream, fos);
            }
            return dictionaryPath;
        } catch (Exception ignored) {
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
        } catch (IOException ignored) {}
    }
}
