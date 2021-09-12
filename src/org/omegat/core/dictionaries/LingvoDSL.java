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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    protected static final String[] EMPTY_RESULT = new String[0];

    static private class RE {
        public String regex;
        public String replacement;

        public RE(String regex, String replacement) {
            this.regex = regex;
            this.replacement = replacement;
        }
    }

    protected static final List<RE> RE_LIST = new ArrayList<>();

    static {
        RE_LIST.add(new RE("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>"));
        RE_LIST.add(new RE("\\[i\\](.+?)\\[/i\\]", "<span style='font-style: italic'>$1</span>"));
        RE_LIST.add(new RE("\\[trn\\](.+?)\\[/trn\\]", "<br>&nbsp;-&nbsp;$1"));
        RE_LIST.add(new RE("\\[t\\](.+?)\\[/t\\]", "$1&nbsp;"));
        RE_LIST.add(new RE("\\[br\\]", "<br/>"));
        // Green is default color in Lingvo
        RE_LIST.add(new RE("\\[c\\](.+?)\\[/c\\]", "<span style='color:green'>$1</span>"));
        // The following line tries to replace [c value]text[/c] with text colored as per the value.
        // Since the color names are plain words like 'red', or 'blue', or 'steelgray' etc.,
        // FIXME: I use the ([a-z]+?) regular expression, but am not sure if it is correct.
        RE_LIST.add(new RE("\\[c\\s([a-z]+?)\\](.+?)\\[/c\\]", "<span style='color:$1'>$2</span>"));
        RE_LIST.add(new RE("\\[com\\]", ""));
        RE_LIST.add(new RE("\\[/com\\]", ""));
        RE_LIST.add(new RE("\\[ex\\]", ""));
        RE_LIST.add(new RE("\\[/ex\\]", ""));
        RE_LIST.add(new RE("\\[lang\\]", ""));
        RE_LIST.add(new RE("\\[/lang\\]", ""));
        RE_LIST.add(new RE("\\[m\\]", ""));
        RE_LIST.add(new RE("\\[/m\\]", ""));
        RE_LIST.add(new RE("\\[m1\\]", ""));
        RE_LIST.add(new RE("\\[/m1\\]", ""));
        RE_LIST.add(new RE("\\[m2\\]", ""));
        RE_LIST.add(new RE("\\[/m2\\]", ""));
        RE_LIST.add(new RE("\\[m3\\]", ""));
        RE_LIST.add(new RE("\\[/m3\\]", ""));
        RE_LIST.add(new RE("\\[m4\\]", ""));
        RE_LIST.add(new RE("\\[/m4\\]", ""));
        RE_LIST.add(new RE("\\[m5\\]", ""));
        RE_LIST.add(new RE("\\[/m5\\]", ""));
        RE_LIST.add(new RE("\\[m6\\]", ""));
        RE_LIST.add(new RE("\\[/m6\\]", ""));
        RE_LIST.add(new RE("\\[m7\\]", ""));
        RE_LIST.add(new RE("\\[/m7\\]", ""));
        RE_LIST.add(new RE("\\[m8\\]", ""));
        RE_LIST.add(new RE("\\[/m8\\]", ""));
        RE_LIST.add(new RE("\\[m9\\]", ""));
        RE_LIST.add(new RE("\\[/m9\\]", ""));
        RE_LIST.add(new RE("\\[p\\]", ""));
        RE_LIST.add(new RE("\\[/p\\]", ""));
        RE_LIST.add(new RE("\\[preview\\]", ""));
        RE_LIST.add(new RE("\\[/preview\\]", ""));
        RE_LIST.add(new RE("\\[ref\\]", ""));
        RE_LIST.add(new RE("\\[/ref\\]", ""));
        RE_LIST.add(new RE("\\[s\\]", ""));
        RE_LIST.add(new RE("\\[/s\\]", ""));
        RE_LIST.add(new RE("\\[sub\\](.+?)\\[/sub\\]", "<sub>$1</sub>"));
        RE_LIST.add(new RE("\\[sup\\](.+?)\\[/sup\\]", "<sup>$1</sup>"));
        RE_LIST.add(new RE("\\[trn1\\]", ""));
        RE_LIST.add(new RE("\\[/trn1\\]", ""));
        RE_LIST.add(new RE("\\[trs\\]", ""));
        RE_LIST.add(new RE("\\[/trs\\]", ""));
        // FIXME: In the following two lines, the exclamation marks are escaped. Maybe, it is superfluous.
        RE_LIST.add(new RE("\\[\\!trs\\]", ""));
        RE_LIST.add(new RE("\\[/\\!trs\\]", ""));
        RE_LIST.add(new RE("\\[u\\](.+?)\\[/u\\]",
                "<span style='text-decoration:underline'>$1</span>"));
        RE_LIST.add(new RE("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>"));
        RE_LIST.add(new RE("\\[video\\]", ""));
        RE_LIST.add(new RE("\\[/video\\]", ""));
        // The following line tries to replace a letter surrounded by ['][/'] tags (indicating stress)
        // with a red letter (the default behavior in Lingvo).
        RE_LIST.add(new RE("\\['\\].\\[/'\\]", "<span style='color:red'>$1</span>"));
        // FIXME: In the following two lines, the asterisk symbols are escaped. Maybe, it is superfluous.
        RE_LIST.add(new RE("\\[\\*\\]", ""));
        RE_LIST.add(new RE("\\[/\\*\\]", ""));
    }

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
            data = new DictionaryData<>(language);
            readDslFile(file);
        }

        private void readDslFile(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                // Un-gzip if necessary
                InputStream is = file.getName().endsWith(".dz") ? new GZIPInputStream(fis, 8192) : fis;
                try (BOMInputStream bis = new BOMInputStream(is)) {
                    // Detect charset
                    Charset charset = bis.hasBOM() ? StandardCharsets.UTF_8 : StandardCharsets.UTF_16;
                    try (InputStreamReader isr = new InputStreamReader(bis, charset);
                            BufferedReader reader = new BufferedReader(isr)) {
                        loadData(reader.lines());
                    }
                }
            }
        }

        private String replaceTag(final String line) {
            String result = line;
            for (RE re : RE_LIST) {
                result = result.replaceAll(re.regex, re.replacement);
            }
            return result.replaceAll("\\[\\[(.+?)\\]\\]", "[$1]");
        }

        private void loadData(Stream<String> stream) {
            StringBuilder word = new StringBuilder();
            StringBuilder trans = new StringBuilder();
            stream.filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> { line = replaceTag(line);
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
