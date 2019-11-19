/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.dictionaries;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.omegat.util.Language;

/**
 * Dictionary implementation for Lingvo DSL format.
 *
 * Lingvo DSL format described in Lingvo help. See also
 * http://www.dsleditor.narod.ru/art_03.htm(russian).
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class LingvoDSL implements IDictionaryFactory {
    protected static final Pattern RE_SKIP = Pattern.compile("\\[.+?\\]");
    protected static final String[] EMPTY_RESULT = new String[0];

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".dsl");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return loadDict(file, new Language(Locale.getDefault()));
    }

    @Override
    public IDictionary loadDict(File file, Language language) throws Exception {
        return new LingvoDSLDict(loadData(file, language));
    }

    private static DictionaryData<String> loadData(File file, Language language) throws Exception {
        DictionaryData<String> data = new DictionaryData<>(language);
        StringBuilder word = new StringBuilder();
        StringBuilder trans = new StringBuilder();
        Files.lines(file.toPath(), StandardCharsets.UTF_16).filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(line -> RE_SKIP.matcher(line).replaceAll("")).forEach(line -> {
                    if (Character.isWhitespace(line.codePointAt(0))) {
                        trans.append(line.trim()).append('\n');
                    } else {
                        if (word.length() > 0) {
                            data.add(word.toString(), trans.toString());
                            word.setLength(0);
                            trans.setLength(0);
                        }
                        word.append(line);
                    }
                });
        if (word.length() > 0) {
            data.add(word.toString(), trans.toString());
        }
        data.done();
        return data;
    }

    static class LingvoDSLDict implements IDictionary {
        protected final DictionaryData<String> data;

        LingvoDSLDict(DictionaryData<String> data) throws Exception {
            this.data = data;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            return data.lookUp(word).stream().map(e -> new DictionaryEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) throws Exception {
            return data.lookUpPredictive(word).stream().map(e -> new DictionaryEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
