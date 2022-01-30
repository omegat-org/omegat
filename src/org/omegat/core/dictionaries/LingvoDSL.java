/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               2021 Aaron Madlon-Kay, Dmitri Gabinski, Hiroshi Miura
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.eb4j.dsl.DslArticle;
import io.github.eb4j.dsl.DslDictionary;
import io.github.eb4j.dsl.DslResult;
import io.github.eb4j.dsl.visitor.DslVisitor;
import org.apache.commons.io.FilenameUtils;

/**
 * Dictionary implementation for Lingvo DSL format.
 * <p>
 * Lingvo DSL format described in Lingvo help. See also links below.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 * @see <a href="https://github.com/eb4j/dsl4j">DSL4j library</a>
 * @see <a href="http://lingvo.helpmax.net/en/troubleshooting/dsl-compiler/">DSL
 * Documentation (English)</a>
 * @see <a href="http://www.dsleditor.narod.ru/art_03.htm">DSL documentation
 * (Russian)</a>
 */
public class LingvoDSL implements IDictionaryFactory {

    @Override
    public final boolean isSupportedFile(final File file) {
        return file.getPath().endsWith(".dsl") || file.getPath().endsWith(".dsl.dz");
    }

    @Override
    public final IDictionary loadDict(final File file) throws Exception {
        Path dictPath = Paths.get(file.toURI());
        Path indexPath = Paths.get(dictPath + ".idx");
        return new LingvoDSLDict(dictPath, indexPath);
    }

    static class LingvoDSLDict implements IDictionary {
        protected final DslDictionary data;
        private final HtmlVisitor htmlVisitor;

        /**
         * Constructor of LingvoDSL Dictionary driver.
         * @param dictPath *.dsl file object.
         * @param indexPath index cache file.
         * @throws Exception when loading dictionary failed.
         */
        LingvoDSLDict(final Path dictPath, final Path indexPath) throws Exception {
            data = DslDictionary.loadDictionary(dictPath, indexPath);
            htmlVisitor = new HtmlVisitor(dictPath.getParent().toString());
        }

        /**
         * read article with exact match.
         * @param word
         *            The word to look up in the dictionary
         *
         * @return list of results.
         */
        @Override
        public List<DictionaryEntry> readArticles(final String word) throws IOException {
            return readEntries(data.lookup(word));
        }

        /**
         * read article with predictive match.
         * @param word
         *            The word to look up in the dictionary
         *
         * @return list of results.
         */
        @Override
        public List<DictionaryEntry> readArticlesPredictive(final String word) throws IOException {
            return readEntries(data.lookupPredictive(word));
        }

        private List<DictionaryEntry> readEntries(final DslResult dslResult) {
            List<DictionaryEntry> list = new ArrayList<>();
            for (Map.Entry<String, String> e : dslResult.getEntries(htmlVisitor)) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(e.getKey(), e.getValue());
                list.add(dictionaryEntry);
            }
            return list;
        }
    }

    /**
     * Simple HTML filter for LingvoDSL parser.
     */
    public static class HtmlVisitor extends DslVisitor<String> {

        private static final String[] IMAGE_EXTS = new String[] { "png", "jpg", "PNG", "JPG" };

        private StringBuilder sb;
        private boolean delayText;
        private String previousText;
        private final File basePath;

        /**
         * Constructor with media path.
         * @param dirPath media base path.
         * @throws IOException when given directory not found.
         */
        public HtmlVisitor(final String dirPath) throws IOException {
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                throw new IOException("Directory not found!");
            }
            basePath = dir;
            delayText = false;
        }

        /**
         * Start of accept.
         * <p>
         *     super#visit(ElementSequence) call this.
         * </p>
         */
        @Override
        public void start() {
            sb = new StringBuilder();
        }

        /**
         * End of accept.
         * <p>
         *     super#visit(ElementSequence) call this.
         * </p>
         */
        @Override
        public void finish() {
        }

        /**
         * Visit a tag.
         *
         * @param tag to visit.
         */
        @Override
        public void visit(final DslArticle.Tag tag) {
            if (tag.isTagName("b")) {
                sb.append("<strong>");
            } else if (tag.isTagName("br")) {
                sb.append("<br/>");
            } else if (tag.isTagName("u")) {
                sb.append("<span style='text-decoration:underline'>");
            } else if (tag.isTagName("i")) {
                sb.append("<span style='font-style: italic'>");
            } else if (tag.isTagName("sup")) {
                sb.append("<sup>");
            } else if (tag.isTagName("sub")) {
                sb.append("<sub>");
            } else if (tag.isTagName("m")) {
                sb.append("<p>");
            } else if (tag.isTagName("m1")) {
                sb.append("<p style=\"text-indent: 30px\">");
            } else if (tag.isTagName("m2")) {
                sb.append("<p style=\"text-indent: 60px\">");
            } else if (tag.isTagName("m3")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m4")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m5")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m6")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m7")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m8")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("m9")) {
                sb.append("<p style=\"text-indent: 90px\">");
            } else if (tag.isTagName("c")) {
                if (tag.hasAttribute()) {
                    sb.append("<span style=\"color: ").append(tag.getAttribute().getValue()).append("\">");
                } else {
                    sb.append("<span style=\"color: green\">");
                }
            } else if (tag.isTagName("'")) {
                sb.append("<span style=\"color: red\">");
            } else if (tag.isTagName("url") || tag.isTagName("s") || tag.isTagName("video")) {
                delayText = true;
            }
            // no output for t
        }

        private String getMediaUrl() {
            return new File(basePath, previousText).toURI().toString();
        }

        private boolean isMediaImage() {
            String ext = FilenameUtils.getExtension(previousText);
            for (String e : IMAGE_EXTS) {
                if (e.equals(ext)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Visit an EndTag.
         *
         * @param endTag to visit.
         */
        @Override
        public void visit(final DslArticle.EndTag endTag) {
            if (delayText) {
                if (previousText == null) {
                    return;
                }
                if (endTag.isTagName("video")) {
                    sb.append("<a href=\"").append(getMediaUrl()).append("\">").append(previousText).append("</a>");
                } else if (endTag.isTagName("s")) {
                    if (isMediaImage()) {
                        sb.append("<img src=\"").append(getMediaUrl()).append("\" />");
                    } else {  // sound and unknown files
                        sb.append("<a href=\"").append(getMediaUrl()).append("\" >").append(previousText).append("</a>");
                    }
                } else if (endTag.isTagName("url")) {
                    sb.append("<a href=\"").append(previousText).append("\">").append(previousText).append("</a>");
                }
                delayText = false;
                previousText = null;
            }
            if (endTag.isTagName("b")) {
                sb.append("</strong>");
            } else if (endTag.isTagName("u") || endTag.isTagName("i") ||
                    endTag.isTagName("c") || endTag.isTagName("'")) {
                sb.append("</span>");
            } else if (endTag.isTagName("t")) {
                sb.append("&nbsp;");
            } else if (endTag.isTagName("sup")) {
                sb.append("</sup>");
            } else if (endTag.isTagName("sub")) {
                sb.append("</sub>");
            } else if (endTag.isTagName("m")) {
                sb.append("</p>");
            }
        }

        /**
         * Return result.
         *
         * @return result.
         */
        @Override
        public String getObject() {
            if (sb == null) {
                // should not happened, but check null to avoid findbugs error.
                throw new RuntimeException();
            }
            return sb.toString();
        }

        /**
         * Visit a text.
         *
         * @param t Text object to process.
         */
        @Override
        public void visit(final DslArticle.Text t) {
            previousText = t.getText();
            if (!delayText) {
                sb.append(t);
            }
        }

        /**
         * Visit an Attribute.
         *
         * @param a Attribute object to visit.
         */
        @Override
        public void visit(final DslArticle.Attribute a) {
        }

        /**
         * Visit a NewLine.
         *
         * @param n NewLine object to visit.
         */
        @Override
        public void visit(final DslArticle.Newline n) {
            sb.append("\n");
        }
    }
}
