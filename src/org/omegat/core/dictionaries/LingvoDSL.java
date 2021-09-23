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
import java.util.Collections;
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

        private void loadData(Stream<String> stream) {
            StringBuilder word = new StringBuilder();
            StringBuilder trans = new StringBuilder();
            stream.filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#"))
                    .map(LingvoDSLTag::replaceTag)
                    .forEach(line -> {
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

    @SuppressWarnings("visibilitymodifier")
    static class RE {
        public Pattern pattern;
        public String replacement;

        public RE(final String regex, final String replacement) {
            pattern = Pattern.compile(regex);
            this.replacement = replacement;
        }
    }

    static class LingvoDSLTag {
        private static final List<RE> RE_LIST;

        static String replaceTag(final String line) {
            String result = line;
            for (RE re : RE_LIST) {
                result = re.pattern.matcher(result).replaceAll(re.replacement);
            }
            return result;
        }

        static {
            List<RE> reList = new ArrayList<>();
            reList.add(new RE("\\[\\[(.+?)\\]\\]", "&lbrack;$1&rbrack;"));
            reList.add(new RE("\\\\\\[", "&lbrack;"));
            reList.add(new RE("\\\\\\]", "&rbrack;"));
            reList.add(new RE("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>"));
            reList.add(new RE("\\[i\\](.+?)\\[/i\\]", "<span style='font-style: italic'>$1</span>"));
            reList.add(new RE("\\[trn\\](.+?)\\[/trn\\]", "$1"));
            reList.add(new RE("\\[t\\](.+?)\\[/t\\]", "$1&nbsp;"));
            reList.add(new RE("\\[br\\]", "<br/>"));
            // Green is default color in Lingvo
            reList.add(new RE("\\[c\\](.+?)\\[/c\\]", "<span style='color:green'>$1</span>"));
            // The following line tries to replace [c value]text[/c] with text colored as per the value.
            // Since the color names are plain words like 'red', or 'blue', or 'steelgray' etc.,
            // FIXME: I use the ([a-z]+?) regular expression, but am not sure if it is correct.
            reList.add(new RE("\\[c\\s([a-z]+?)\\](.+?)\\[/c\\]", "<span style='color:$1'>$2</span>"));
            reList.add(new RE("\\[com\\]", ""));
            reList.add(new RE("\\[/com\\]", ""));
            reList.add(new RE("\\[ex\\]", ""));
            reList.add(new RE("\\[/ex\\]", ""));
            reList.add(new RE("\\[lang\\]", ""));
            reList.add(new RE("\\[/lang\\]", ""));
            reList.add(new RE("\\[m\\](.+?)\\[/m\\]", "$1"));
            reList.add(new RE("\\[m1\\](.+?)\\[/m\\]", "<p style=\"text-indent: 30px\">$1</p>"));
            reList.add(new RE("\\[m2\\](.+?)\\[/m\\]", "<p style=\"text-indent: 60px\">$1</p>"));
            reList.add(new RE("\\[m3\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m4\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m5\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m6\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m7\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m8\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[m9\\](.+?)\\[/m\\]", "<p style=\"text-indent: 90px\">$1</p>"));
            reList.add(new RE("\\[p\\]", ""));
            reList.add(new RE("\\[/p\\]", ""));
            reList.add(new RE("\\[preview\\]", ""));
            reList.add(new RE("\\[/preview\\]", ""));
            reList.add(new RE("\\[ref\\]", ""));
            reList.add(new RE("\\[/ref\\]", ""));
            reList.add(new RE("\\[s\\]", ""));
            reList.add(new RE("\\[/s\\]", ""));
            reList.add(new RE("\\[sub\\](.+?)\\[/sub\\]", "<sub>$1</sub>"));
            reList.add(new RE("\\[sup\\](.+?)\\[/sup\\]", "<sup>$1</sup>"));
            reList.add(new RE("\\[trn1\\]", ""));
            reList.add(new RE("\\[/trn1\\]", ""));
            reList.add(new RE("\\[trs\\]", ""));
            reList.add(new RE("\\[/trs\\]", ""));
            // FIXME: In the following two lines, the exclamation marks are escaped. Maybe, it is superfluous.
            reList.add(new RE("\\[\\!trs\\]", ""));
            reList.add(new RE("\\[/\\!trs\\]", ""));
            reList.add(new RE("\\[u\\](.+?)\\[/u\\]",
                    "<span style='text-decoration:underline'>$1</span>"));
            reList.add(new RE("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>"));
            reList.add(new RE("\\[video\\]", ""));
            reList.add(new RE("\\[/video\\]", ""));
            // The following line tries to replace a letter surrounded by ['][/'] tags (indicating stress)
            // with a red letter (the default behavior in Lingvo).
            reList.add(new RE("\\['\\].\\[/'\\]", "<span style='color:red'>$1</span>"));
            // FIXME: In the following two lines, the asterisk symbols are escaped. Maybe, it is superfluous.
            reList.add(new RE("\\[\\*\\]", ""));
            reList.add(new RE("\\[/\\*\\]", ""));
            RE_LIST = Collections.unmodifiableList(reList);
        }
    }
}
