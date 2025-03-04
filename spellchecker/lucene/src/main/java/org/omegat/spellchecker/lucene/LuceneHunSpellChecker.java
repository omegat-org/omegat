/*******************************************************************************
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2024 Hiroshi Miura
 *          Home page: https://www.omegat.org/
 *          Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.omegat.spellchecker.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.Hunspell;
import org.apache.lucene.store.NIOFSDirectory;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.AbstractSpellChecker;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.ISpellCheckerProvider;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.util.Preferences;

public class LuceneHunSpellChecker extends AbstractSpellChecker implements ISpellChecker {

    /**
     * affix file extension
     */
    public static final String SC_AFFIX_FILENAME = "index.aff";

    /**
     * dictionary file extension
     */
    public static final String SC_DICTIONARY_FILENAME = "index.dic";

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerSpellCheckClass(LuceneHunSpellChecker.class);
    }

    public static void unloadPlugins() {
    }

    public LuceneHunSpellChecker() {
        super();
    }

    @Override
    protected Optional<ISpellCheckerProvider> initializeWithLanguage(final String language) {
        // check that the dict exists
        String dictionaryDir = Preferences.getPreferenceDefault(Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY,
                SpellCheckerManager.DEFAULT_DICTIONARY_DIR.getPath());

        File affixName = Path.of(dictionaryDir).resolve(Path.of(language)).resolve(SC_AFFIX_FILENAME).toFile();
        File dictionaryName = Path.of(dictionaryDir).resolve(language).resolve(SC_DICTIONARY_FILENAME).toFile();

        if (isInvalidFile(affixName) || isInvalidFile(dictionaryName)) {
            return Optional.empty();
        }

        try {
            ISpellCheckerProvider result = new LuceneProvider(dictionaryName, affixName);
            return Optional.of(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    private static final class LuceneProvider implements ISpellCheckerProvider {
        private final InputStream dictInputStream;
        private final InputStream affixInputStream;
        private final Hunspell hunspell;

        private LuceneProvider(File dictName, File affixName) throws IOException, ParseException {
            Path tempDir = Path.of(FileUtils.getTempDirectoryPath());
            dictInputStream = new FileInputStream(dictName);
            affixInputStream = new FileInputStream(affixName);
            Dictionary dict = new Dictionary(new NIOFSDirectory(tempDir), "omegat",
                    affixInputStream, dictInputStream);
            hunspell = new Hunspell(dict);
        }

        @Override
        public boolean isCorrect(final String word) {
            return hunspell.spell(word);
        }

        @Override
        public List<String> suggest(final String word) {
            return hunspell.suggest(word);
        }

        @Override
        public void learnWord(final String word) {
        }

        @Override
        public void destroy() {
            try {
                dictInputStream.close();
                affixInputStream.close();
            } catch (Exception ignored) {
            }
        }
    }
}
