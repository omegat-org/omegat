/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               2021 Aaron Madlon-Kay, Gabix, Hiroshi Miura
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.BOMInputStream;

import org.omegat.util.Language;

/**
 * Dictionary implementation for Lingvo DSL format.
 * <p>
 * Lingvo DSL format described in Lingvo help. See also
 * http://www.dsleditor.narod.ru/art_03.htm(russian).
 * https://documentation.help/ABBYY-Lingvo8/G_NewDicsDSL.htm (english).
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class LingvoDSL implements IDictionaryFactory {

    // An ordered list of Pair of Regex pattern and replacement string
    private static final TreeMap<Pattern, String> TAG_REPLACEMENTS = new TreeMap<>(Comparator.comparing(Pattern::pattern));

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
            stream.filter(line -> !line.isEmpty())
                  .filter(line -> !line.startsWith("#"))
                  .map(LingvoDSL::replaceTag)
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

    private static String replaceTag(final String line) {
        String result = line;
        for (Map.Entry<Pattern, String> entry : TAG_REPLACEMENTS.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(entry.getValue());
        }
        return result;
    }

    static {
        // TODO: real parser.
        // DSL language is not context-free:
        // it allows arbitrary nesting (and also the meaning of e.g. [/m] is context-dependent)
        // so this approach of replacing regex patterns is doomed to fail at at least some cases.
        // and may leave some [tag].
        // The only way to handle this language robustly is to make a real parser.
        // ---
        // following 3 lines replaces "[[...]]" and "\[..\]" into "[...]"
        // but using HTML body reference to avoid further match.
        TAG_REPLACEMENTS.put(Pattern.compile("\\[\\[(?<content>.+?)]]"), "&#91;${content}&#93;");
        TAG_REPLACEMENTS.put(Pattern.compile(Pattern.quote("\\[")), "&#91;");
        TAG_REPLACEMENTS.put(Pattern.compile(Pattern.quote("\\]")), "&#93;");
        // styling tags
        TAG_REPLACEMENTS.put(Pattern.compile("\\[b](?<content>.+?)\\[/b]"), "<strong>${content}</strong>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[i](?<content>.+?)\\[/i]"), "<span style='font-style: italic'>${content}</span>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[t](?<content>.+?)\\[/t]"), "${content}&nbsp;");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[c](?<content>.+?)\\[/c]"), "<span style='color:green'>${content}</span>");
        // The following line tries to replace [c value]text[/c] with text colored as per the value.
        // Since the color names are plain words like 'red', or 'blue', or 'steelgray' etc.,
        TAG_REPLACEMENTS.put(Pattern.compile("\\[c\\s(?<color>[a-z]+?)](?<content>.+?)\\[/c]"),
                "<span style='color:${color}'>${content}</span>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[sub](?<content>.+?)\\[/sub]"), "<sub>${content}</sub>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[sup](?<content>.+?)\\[/sup]"), "<sup>${content}</sup>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[u](?<content>.+?)\\[/u]"), "<span style='text-decoration:underline'>${content}</span>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[br]"), "<br/>");
        // ignore tag 'm" but "m1" to indent 1 level, "m2" to indent 2 level and "m3" and more to indent 3 level.
        TAG_REPLACEMENTS.put(Pattern.compile("\\[m1](?<content>.+?)\\[/m]"), "<p style=\"text-indent: 30px\">${content}</p>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[m2](?<content>.+?)\\[/m]"), "<p style=\"text-indent: 60px\">${content}</p>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[(m3|m4|m5|m6|m7|m8|m9)](?<content>.+?)\\[/m]"),
                "<p style=\\\"text-indent: 90px\">${content}</p>");
        TAG_REPLACEMENTS.put(Pattern.compile("\\[url](?<link>.+?)\\[/url]"), "<a href='${link}'>Link</a>");
        // The following line tries to replace a letter surrounded by ['][/'] tags (indicating stress)
        // with a red letter (the default behavior in Lingvo).
        TAG_REPLACEMENTS.put(Pattern.compile("\\['](?<content>.+?)\\[/']"), "<span style='color:red'>${content}</span>");
        // silently ignored these tags that can be arbitrary nested.
        String[] IGNORED_TAGS = {"\\*", "m", "com", "ex", "lang", "p", "preview", "ref", "s", "trn", "trn1", "trs", "!trs", "video"};
        for (String tag : IGNORED_TAGS) {
            TAG_REPLACEMENTS.put(Pattern.compile("\\[(?<tag>" + tag + ")](?<content>.+?)\\[/\\k<tag>]"), "${content}");
        }
    }
}
