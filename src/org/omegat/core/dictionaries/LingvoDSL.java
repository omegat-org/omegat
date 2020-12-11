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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.BOMInputStream;
import org.omegat.util.Language;

/**
 * Dictionary implementation for Lingvo DSL format.
 *
 * Lingvo DSL format described in Lingvo help. See also
 * http://www.dsleditor.narod.ru/art_03.htm(russian).
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class LingvoDSL implements IDictionaryFactory {
    protected static final Pattern RE_SKIP = Pattern.compile("\\[.+?\\]");
    protected static final String[] EMPTY_RESULT = new String[0];

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".dsl") || file.getPath().endsWith(".dsl.dz");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return loadDict(file, new Language(Locale.getDefault()));
    }

    @Override
    public IDictionary loadDict(File file, Language language) throws Exception {
        return new LingvoDSLDict(file, language);
    }

    static class LingvoDSLDict implements IDictionary {
        protected final DictionaryData<String> data;

        LingvoDSLDict(File file, Language language) throws Exception {
            String dataFile = file.getPath();
            data = new DictionaryData<>(language);
            if (dataFile.endsWith(".dz")) {
                try (FileInputStream fis = new FileInputStream(file);
                     GZIPInputStream gis = new GZIPInputStream(fis, 8192);
                     BOMInputStream bis = new BOMInputStream(gis)) {
                    readDslFile(bis);
                }
            } else {
                try (FileInputStream fis = new FileInputStream(file);
                     BOMInputStream bis = new BOMInputStream(fis)) {
                    readDslFile(bis);
                }
            }
        }

        private void readDslFile(BOMInputStream bis) throws IOException {
            Charset charset = StandardCharsets.UTF_16;
            if (bis.hasBOM()) {
                charset = StandardCharsets.UTF_8;
            }
            try (InputStreamReader isr = new InputStreamReader(bis, charset);
                 BufferedReader reader = new BufferedReader(isr)) {
                loadData(reader.lines());
            }
        }

        private void loadData(Stream<String> stream) {
            StringBuilder word = new StringBuilder();
            StringBuilder trans = new StringBuilder();
            stream.filter(line -> !line.isEmpty() && !line.startsWith("#"))
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
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) {
            return data.lookUp(word).stream().map(e -> new DictionaryEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            return data.lookUpPredictive(word).stream().map(e -> new DictionaryEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
