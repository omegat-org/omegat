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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import tokyo.northside.stardict.StarDictDictionary;

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
        protected final File dictionaryFile;
        protected StarDictDictionary dictionary;
        protected final Language language;

        StarDictDict(final File file, Language language) {
            dictionaryFile = file;
            // Max cache size to 1,000 items and expiry to 30 min.
            this.language = language;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) {
            try {
                loadDictionary();
            } catch (Exception e) {
                return Collections.emptyList();
            }
            List<StarDictDictionary.Entry> result = dictionary.readArticles(word);
            if (result.isEmpty()) {
                result = dictionary.readArticles(word.toLowerCase(language.getLocale()));
            }
            return result.stream().filter(StarDictDict::useEntry).map(StarDictDict::convertEntry)
                    .collect(Collectors.toList());
        }

        private void loadDictionary() throws Exception {
            dictionary = StarDictDictionary.loadDictionary(dictionaryFile, 1_000, Duration.ofMinutes(30));
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            if (dictionary == null) {
                try {
                    loadDictionary();
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            }
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
                    || type == StarDictDictionary.EntryType.HTML || type == StarDictDictionary.EntryType.XDXF
                    || type == StarDictDictionary.EntryType.PANGO;
        }

        private static final String CONDENSED_SPAN = "<span class=\"paragraph-start\">&nbsp;"
                + "\u00b6</span><span>";

        private static DictionaryEntry convertEntry(StarDictDictionary.Entry entry) {
            boolean condensed = Preferences.isPreferenceDefault(Preferences.DICTIONARY_CONDENSED_VIEW, false);
            String result;
            switch (entry.getType()) {
            case MEAN:
                result = processMeanEntry(entry, condensed);
                break;
            case PHONETIC:
                result = processPhoneticEntry(entry);
                break;
            case HTML:
                result = processHtmlEntry(entry);
                break;
            case PANGO:
                result = processPangoEntry(entry);
                break;
            case XDXF:
                result = processXdxfEntry(entry, condensed);
                break;
            default:
                throw new IllegalArgumentException("Unsupported EntryType: " + entry.getType());
            }

            return new DictionaryEntry(entry.getWord(), result);
        }

        private static String processMeanEntry(StarDictDictionary.Entry entry, boolean condensed) {
            StringBuilder contentBuilder = new StringBuilder();
            String[] lines = entry.getArticle().split("\n");
            if (condensed) {
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        contentBuilder.append(CONDENSED_SPAN);
                    } else {
                        contentBuilder.append("<span>");
                    }
                    contentBuilder.append(lines[i]).append("</span>");
                }
            } else {
                for (String line : lines) {
                    contentBuilder.append("<div>").append(line).append("</div>");
                }
            }
            return contentBuilder.toString();
        }

        private static String processPhoneticEntry(StarDictDictionary.Entry entry) {
            return "<span>(" + entry.getArticle() + ")</span>";
        }

        private static String processHtmlEntry(StarDictDictionary.Entry entry) {
            Document document = Jsoup.parse(entry.getArticle());
            Cleaner cleaner = new Cleaner(Safelist.relaxed());
            document = cleaner.clean(document);
            return document.body().html();
        }

        private static String processPangoEntry(StarDictDictionary.Entry entry) {
            Document document = Jsoup.parse(entry.getArticle());
            Cleaner cleaner = new Cleaner(
                    new Safelist().addTags("sup", "sub", "i", "b", "u", "tt", "big", "small", "span"));
            document = cleaner.clean(document);
            return document.body().html();
        }

        private static String processXdxfEntry(StarDictDictionary.Entry entry, boolean condensed) {
            Document document = Jsoup.parse(entry.getArticle());
            cleanXdxfSpecificTags(document);

            Safelist safelist = new Safelist().addTags("sup", "sub", "i", "b", "tt", "big", "small", "span")
                    .addAttributes("span", "style");

            if (!condensed) {
                return handleExpandedXdxf(document, safelist);
            } else {
                return handleCondensedXdxf(document, safelist);
            }
        }

        private static void cleanXdxfSpecificTags(Document document) {
            document.select("k").remove();
            document.select("c").forEach(e -> {
                String color = e.attr("c");
                e.tagName("span").removeAttr("c").attr("style", "color: " + color + ";");
            });
            document.select("su").tagName("div").attr("class", "details");
            document.select("ex").tagName("span").attr("style", "color: blue;");
            document.select("co").tagName("span").attr("style", "color: gray;");
            document.select("kref").tagName("span").attr("style", "font-style: italic;").removeAttr("idref");
            document.select("iref").tagName("a");
            document.select("rref").forEach(StarDictDict::processRrefTag);
        }

        private static void processRrefTag(Element e) {
            String type = e.attr("type");
            String resource = e.attr("lctn");
            e.removeAttr("lctn");
            switch (type.split("/")[0]) {
            case "audio":
                e.tagName("a").attr("href", resource).text("Play").removeAttr("start").removeAttr("size");
                break;
            case "image":
                e.tagName("img").attr("src", resource);
                break;
            case "video":
                e.tagName("video").appendChild(new Element("source").attr("src", resource).attr("type", type))
                        .removeAttr("type");
                break;
            default:
                e.remove();
                break;
            }
        }

        private static String handleExpandedXdxf(Document document, Safelist safelist) {
            safelist.addTags("blockquote");
            Cleaner cleaner = new Cleaner(safelist);
            document = cleaner.clean(document);
            document.select("blockquote").attr("style", "display: block;margin-left: 20px;");
            return processDefinitionElements(document);
        }

        private static String handleCondensedXdxf(Document document, Safelist safelist) {
            document.select("k, su").remove();
            Cleaner cleaner = new Cleaner(safelist);
            document = cleaner.clean(document);
            return processDefinitionElementsCondensed(document);
        }

        private static String processDefinitionElements(Document document) {
            StringBuilder contentBuilder = new StringBuilder();
            Elements definitionElements = document.select("def");
            if (definitionElements.isEmpty()) {
                contentBuilder.append("<div>").append(document.body().html()).append("</div>");
            } else {
                definitionElements
                        .forEach(e -> contentBuilder.append("<div>").append(e.html()).append("</div>"));
            }
            return contentBuilder.toString();
        }

        private static String processDefinitionElementsCondensed(Document document) {
            StringBuilder contentBuilder = new StringBuilder();
            Elements definitionElements = document.select("def");
            if (definitionElements.isEmpty()) {
                contentBuilder.append("<span>").append(document.body().html()).append("</span>");
            } else {
                for (int i = 0; i < definitionElements.size(); i++) {
                    if (i > 0) {
                        contentBuilder.append(CONDENSED_SPAN);
                    } else {
                        contentBuilder.append("<span>");
                    }
                    contentBuilder.append(definitionElements.get(i).html()).append("</span>");
                }
            }
            return contentBuilder.toString();
        }
    }
}
