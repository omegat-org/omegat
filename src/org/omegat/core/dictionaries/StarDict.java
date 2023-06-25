/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015-2016 Hiroshi Miura, Aaron Madlon-Kay
               2020 Suguru Oho, Aaron Madlon-Kay
               2022 Hiroshi Miura
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

package org.omegat.core.dictionaries;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.github.eb4j.stardict.StarDictDictionary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 * StarDict Dictionary support.
 * <p>
 * StarDict format described on
 * https://github.com/huzheng001/stardict-3/blob/master/dict/doc/StarDictFileFormat
 * <p>
 * A StarDict dictionary plugin uses stardict4j access library. Every dictionary
 * consists of these files:
 * <ol>
 * <li>somedict.ifo
 * <li>somedict.idx or somedict.idx.gz
 * <li>somedict.dict or somedict.dict.dz
 * <li>somedict.syn (optional)
 * </ol>
 *
 * This driver handle entry types 'm': mean, 'h': html and 't': phonetics. Other
 * entries such as MediaWiki markup are not shown.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 * @author Suguru Oho
 */
public class StarDict implements IDictionaryFactory {

    /**
     * Plugin loader.
     */
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new StarDictApplicationEventListener());
    }

    /**
     * Plugin unloader.
     */
    public static void unloadPlugins() {
    }

    /**
     * registration of dictionary factory.
     */
    static class StarDictApplicationEventListener implements IApplicationEventListener {
        @Override
        public void onApplicationStartup() {
            Core.getDictionaries().addDictionaryFactory(new StarDict());
        }

        @Override
        public void onApplicationShutdown() {
        }
    }

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".ifo");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return loadDict(file, new Language(Locale.getDefault()));
    }

    @Override
    public IDictionary loadDict(final File file, final Language language) throws Exception {
        return new StarDictDict(file, language);
    }

    static class StarDictDict implements IDictionary {

        protected final StarDictDictionary dictionary;
        protected final Language language;

        StarDictDict(final File file, Language language) throws Exception {
            dictionary = StarDictDictionary.loadDictionary(file, 1_000, Duration.ofMinutes(30));
            // Max cache size to 1,000 items and expiry to 30 min.
            this.language = language;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            List<StarDictDictionary.Entry> result = dictionary.readArticles(word);
            if (result.isEmpty()) {
                result = dictionary.readArticles(word.toLowerCase(language.getLocale()));
            }
            return result.stream().filter(StarDictDict::useEntry).map(StarDictDict::convertEntry)
                    .collect(Collectors.toList());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            List<StarDictDictionary.Entry> result = dictionary.readArticlesPredictive(word);
            if (result.isEmpty()) {
                result = dictionary.readArticlesPredictive(word.toLowerCase(language.getLocale()));
            }
            return result.stream().filter(StarDictDict::useEntry).map(StarDictDict::convertEntry)
                    .collect(Collectors.toList());
        }

        private static boolean useEntry(StarDictDictionary.Entry entry) {
            StarDictDictionary.EntryType type = entry.getType();
            return type == StarDictDictionary.EntryType.MEAN || type == StarDictDictionary.EntryType.PHONETIC
                    || type == StarDictDictionary.EntryType.HTML || type == StarDictDictionary.EntryType.XDXF;
        }

        private static final String CONDENSED_SPAN = "<span class=\"paragraph-start\">&nbsp;"
                + "\u00b6</span><span>";

        private static DictionaryEntry convertEntry(StarDictDictionary.Entry entry) {
            boolean condensed = Preferences.isPreferenceDefault(Preferences.DICTIONARY_CONDENSED_VIEW, false);
            StringBuilder sb = new StringBuilder();
            if (entry.getType().equals(StarDictDictionary.EntryType.MEAN)) {
                String[] lines = entry.getArticle().split("\n");
                if (condensed) {
                    for (int i = 0; i < lines.length; i++) {
                        if (i > 0) {
                            sb.append(CONDENSED_SPAN);
                        } else {
                            sb.append("<span>");
                        }
                        sb.append(lines[i]).append("</span>");
                    }
                } else {
                    for (String line : lines) {
                        sb.append("<div>").append(line).append("</div>");
                    }
                }
            } else if (entry.getType().equals(StarDictDictionary.EntryType.PHONETIC)) {
                sb.append("<span>(").append(entry.getArticle()).append(")</span>");
            } else if (entry.getType().equals(StarDictDictionary.EntryType.HTML)) {
                sb.append(entry.getArticle());
            } else if (entry.getType().equals(StarDictDictionary.EntryType.XDXF)) {
                Document document = Jsoup.parse(entry.getArticle());
                // Process XDXF specific tags
                document.select("k").remove();
                Elements c = document.select("c");
                for (Element e : c) {
                    String color = e.attr("c");
                    e.tagName("span");
                    e.removeAttr("c");
                    e.attr("style", "color: " + color + ";");
                }
                Elements su = document.select("su");
                su.tagName("div");
                su.attr("class", "details");
                Elements ex = document.select("ex");
                ex.tagName("span");
                ex.attr("style", "color: blue;");
                Elements co = document.select("co");
                co.tagName("span");
                co.attr("style", "color: gray;");
                Safelist safelist = new Safelist()
                        .addTags("sup", "sub", "i", "b", "tt", "big", "small", "span");
                safelist.addAttributes("span", "style");
                Elements kref = document.select("kref");
                kref.tagName("span");
                kref.attr("style", "font-style: italic;");
                kref.removeAttr("idref");
                Elements iref = document.select("iref");
                iref.tagName("a");
                Elements rref = document.select("rref");
                for (Element e: rref) {
                    String type = e.attr("type");
                    if (type.isEmpty()) {
                        e.remove();
                        continue;
                    }
                    String resource = e.attr("lctn");
                    e.removeAttr("lctn");
                    if (type.startsWith("audio")) {
                        e.tagName("a");
                        e.removeAttr("start");
                        e.removeAttr("size");
                        e.attr("href", resource);
                        e.text("Play");
                    } else if (type.startsWith("image")) {
                        e.tagName("img");
                        e.attr("src", resource);
                    } else if (type.startsWith("video")) {
                        e.tagName("video");
                        e.appendChild(new Element("source").attr("src", resource).attr("type", type));
                        e.removeAttr("type");
                    } else {
                        e.remove();
                    }
                }
                if (!condensed) {
                    safelist.addTags("blockquote");
                    Cleaner cleaner = new Cleaner(safelist);
                    document = cleaner.clean(document);
                    Elements q = document.select("blockquote");
                    q.attr("style", "display: block;margin-left: 20px;");
                    Elements definitionElements = document.select("def");
                    if (definitionElements.size() > 0) {
                        definitionElements.forEach(
                                e -> sb.append("<div>").append(e.html()).append("</div>"));
                    } else {
                        sb.append("<div>").append(document.body().html()).append("</div>");
                    }
                } else {
                    document.select("k").remove();
                    document.select("su").remove();
                    Cleaner cleaner = new Cleaner(safelist);
                    document = cleaner.clean(document);
                    Elements definitionElements = document.select("def");
                    if (definitionElements.size() > 0) {
                        for (int i = 0; i < definitionElements.size(); i++) {
                            if (i > 0) {
                                sb.append(CONDENSED_SPAN);
                            } else {
                                sb.append("<span>");
                            }
                            sb.append(definitionElements.get(i).html()).append("</span>");
                        }
                    } else {
                        sb.append("<span>").append(document.body().html()).append("</span>");
                    }
                }
            }
            return new DictionaryEntry(entry.getWord(), sb.toString());
        }
    }
}
