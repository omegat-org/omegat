/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core.spellchecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.omegat.util.Log;

public abstract class AbstractHunspellDictionary implements ISpellCheckerDictionary, AutoCloseable {

    private static final String DICT_EXT = ".dic";
    private static final String AFFIX_EXT = ".aff";
    private InputStream affixInputStream;
    private InputStream dicInputStream;

    protected abstract String[] getDictionaries();

    protected String getDictionary(String language) {
        return Arrays.stream(getDictionaries()).filter(lang -> lang.startsWith(language)).findFirst()
                .orElse(null);
    }

    protected abstract InputStream getResourceAsStream(String resource);

    @Override
    public Dictionary getHunspellDictionary(String language) {
        String target = getDictionary(language);
        if (target != null) {
            affixInputStream = getResourceAsStream(target + AFFIX_EXT);
            dicInputStream = getResourceAsStream(target + DICT_EXT);
            try {
                String prefix = "hunspell-dictionary";
                Directory tmpDir = new NIOFSDirectory(Files.createTempDirectory(prefix));
                return new Dictionary(tmpDir, prefix, affixInputStream,
                        Collections.singletonList(dicInputStream), true);
            } catch (IOException | ParseException ex) {
                Log.log(ex);
            }
        }
        return null;
    }

    @Override
    public Path installHunspellDictionary(Path dictionaryDir, String language) {
        String target = getDictionary(language);
        if (target != null) {
            try {
                Path dictionaryPath = dictionaryDir.resolve(target + DICT_EXT);
                try (InputStream dicStream = getResourceAsStream(target + DICT_EXT);
                        FileOutputStream fos = new FileOutputStream(dictionaryPath.toFile())) {
                    IOUtils.copy(dicStream, fos);
                }
                File affixFile = dictionaryDir.resolve(target + AFFIX_EXT).toFile();
                try (InputStream affStream = getResourceAsStream(target + AFFIX_EXT);
                        FileOutputStream fos = new FileOutputStream(affixFile)) {
                    IOUtils.copy(affStream, fos);
                }
                return dictionaryPath;
            } catch (IOException ignored) {
            }
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
            dicInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
