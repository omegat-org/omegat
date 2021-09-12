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

    static private class RegMap {
        public String regex;
        public String replacement;

        public RegMap(String regex, String replacement) {
            this.regex = regex;
            this.replacement = replacement;
        }
    }

    protected static final List<RegMap> RE_MAP = new ArrayList<>();

    static {
        RE_MAP.add(new RegMap("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>"));
        RE_MAP.add(new RegMap("\\[i\\](.+?)\\[/i\\]", "<span style='font-style: italic'>$1</span>"));
        RE_MAP.add(new RegMap("\\[trn\\](.+?)\\[/trn\\]", "<br>&nbsp;-&nbsp;$1"));
        RE_MAP.add(new RegMap("\\[t\\](.+?)\\[/t\\]", "$1&nbsp;"));
        RE_MAP.add(new RegMap("\\[br\\]", "<br/>"));
        //The following line tries to replace [c]text[/c] with green text (default color in Lingvo)
        RE_MAP.add(new RegMap("\\[c\\](.+?)\\[/c\\]", "<span style='color:green'>$1</span>"));
        //The following line tries to replace [c value]text[/c] with text colored as per the value. Since the color names are plain words like 'red', or 'blue', or 'steelgray' etc., I use the ([a-z]+?) regular expression, but am not sure if it is correct.
        RE_MAP.add(new RegMap("\\[c\\s([a-z]+?)\\](.+?)\\[/c\\]", "<span style='color:$1'>$2</span>"));
        RE_MAP.add(new RegMap("\\[com\\]", ""));
        RE_MAP.add(new RegMap("\\[/com\\]", ""));
        RE_MAP.add(new RegMap("\\[ex\\]", ""));
        RE_MAP.add(new RegMap("\\[/ex\\]", ""));
        RE_MAP.add(new RegMap("\\[lang\\]", ""));
        RE_MAP.add(new RegMap("\\[/lang\\]", ""));
        RE_MAP.add(new RegMap("\\[m\\]", ""));
        RE_MAP.add(new RegMap("\\[/m\\]", ""));
        RE_MAP.add(new RegMap("\\[m1\\]", ""));
        RE_MAP.add(new RegMap("\\[/m1\\]", ""));
        RE_MAP.add(new RegMap("\\[m2\\]", ""));
        RE_MAP.add(new RegMap("\\[/m2\\]", ""));
        RE_MAP.add(new RegMap("\\[m3\\]", ""));
        RE_MAP.add(new RegMap("\\[/m3\\]", ""));
        RE_MAP.add(new RegMap("\\[m4\\]", ""));
        RE_MAP.add(new RegMap("\\[/m4\\]", ""));
        RE_MAP.add(new RegMap("\\[m5\\]", ""));
        RE_MAP.add(new RegMap("\\[/m5\\]", ""));
        RE_MAP.add(new RegMap("\\[m6\\]", ""));
        RE_MAP.add(new RegMap("\\[/m6\\]", ""));
        RE_MAP.add(new RegMap("\\[m7\\]", ""));
        RE_MAP.add(new RegMap("\\[/m7\\]", ""));
        RE_MAP.add(new RegMap("\\[m8\\]", ""));
        RE_MAP.add(new RegMap("\\[/m8\\]", ""));
        RE_MAP.add(new RegMap("\\[m9\\]", ""));
        RE_MAP.add(new RegMap("\\[/m9\\]", ""));
        RE_MAP.add(new RegMap("\\[p\\]", ""));
        RE_MAP.add(new RegMap("\\[/p\\]", ""));
        RE_MAP.add(new RegMap("\\[preview\\]", ""));
        RE_MAP.add(new RegMap("\\[/preview\\]", ""));
        RE_MAP.add(new RegMap("\\[ref\\]", ""));
        RE_MAP.add(new RegMap("\\[/ref\\]", ""));
        RE_MAP.add(new RegMap("\\[s\\]", ""));
        RE_MAP.add(new RegMap("\\[/s\\]", ""));
        //The following line tries to replace [sub]text[/sub] with subscript text
        RE_MAP.add(new RegMap("\\[sub\\](.+?)\\[/sub\\]", "<sub>$1</sub>"));
        //The following line tries to replace [sup]text[/sup] with superscript text
        RE_MAP.add(new RegMap("\\[sup\\](.+?)\\[/sup\\]", "<sup>$1</sup>"));
        RE_MAP.add(new RegMap("\\[trn1\\]", ""));
        RE_MAP.add(new RegMap("\\[/trn1\\]", ""));
        RE_MAP.add(new RegMap("\\[trs\\]", ""));
        RE_MAP.add(new RegMap("\\[/trs\\]", ""));
        // In the following two lines, the exclamation marks are escaped. Maybe, it is superfluous.
        RE_MAP.add(new RegMap("\\[\\!trs\\]", ""));
        RE_MAP.add(new RegMap("\\[/\\!trs\\]", ""));
        //The following line tries to replace [u]text[/u] with underlined text
        RE_MAP.add(new RegMap("\\[u\\](.+?)\\[/u\\]", "<span style='text-decoration:underline'>$1</span>"));
        //The following line tries to replace [url]text[/url] with a hyperlink
        RE_MAP.add(new RegMap("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>"));
        RE_MAP.add(new RegMap("\\[video\\]", ""));
        RE_MAP.add(new RegMap("\\[/video\\]", ""));
        //The following line tries to replace a letter surrounded by ['][/'] tags (indicating stress) with a red letter (the default behavior in Lingvo). Check the syntax: does ' need to be escaped?
        RE_MAP.add(new RegMap("\\['\\].\\[/'\\]", "<span style='color:red'>$1</span>"));
        // In the following two lines, the asterisk symbols are escaped. Maybe, it is superfluous.
        RE_MAP.add(new RegMap("\\[\\*\\]", ""));
        RE_MAP.add(new RegMap("\\[/\\*\\]", ""));
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
            for (RegMap regMap : RE_MAP) {
                result = result.replaceAll(regMap.regex, regMap.replacement);
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
