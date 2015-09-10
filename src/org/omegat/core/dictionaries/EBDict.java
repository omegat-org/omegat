/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.util.Map;
import java.util.HashMap;

import fuku.eb4j.Book;
import fuku.eb4j.EBException;
import fuku.eb4j.Result;
import fuku.eb4j.SubBook;
import fuku.eb4j.Searcher;
import fuku.eb4j.hook.Hook;
import fuku.eb4j.hook.HookAdapter;

import org.omegat.util.Log;

/**
 * EPWING dictionary
 * @author Hiroshi Miura
 */
public class EBDict implements IDictionary {
    private String eBookDirectory = null;
    private Book eBookDictionary = null;
    private SubBook[] subBooks;

    private static void logEBError(EBException e) {
        switch (e.getErrorCode()) {
            case EBException.CANT_READ_DIR:
                Log.log("EPWING error: cannot read directory:" + e.getMessage());
                break;
            case EBException.DIR_NOT_FOUND:
                Log.log("EPWING error: cannot found directory:" + e.getMessage());
            default:
                Log.log("EPWING error: " + e.getMessage());
                break;
        }
    }
    
    public EBDict(File catalogFile) throws Exception {
        eBookDirectory = catalogFile.getParent();
        try {
            eBookDictionary = new Book(eBookDirectory);
        } catch (EBException e) {
            logEBError(e);
        }
        final int bookType = eBookDictionary.getBookType();
        if ( bookType != Book.DISC_EPWING ) {
            throw new Exception("EPWING: Invalid type of dictionary");
        }
        subBooks = eBookDictionary.getSubBooks();
    }

    /**
     * (non-Javadoc)
     * @see org.omegat.core.dictionaries.IDictionary#searchExactMatch(java.lang.String)
     *
     * returns Object that will be given to readArticle()'s second argument.
     */
    public Object searchExactMatch(String key) {
        Searcher sh;
        Result searchResult;
        Hook<String> hook;
        String article;
        Object result = null;

        for (SubBook sb: subBooks) {
            if (sb.hasExactwordSearch()) {
                try {
                    hook = new EBDictStringHook(sb);
                    sh = sb.searchExactword(key);
                    while ((searchResult = sh.getNextResult()) != null) {
                        article = searchResult.getText(hook);
                        result = addArticle(article, result);
                    }
                } catch (EBException e) {
                    logEBError(e);
               }
            }
        }
        return result;
    }

    /**
     * Add new article to result object. If article for this words was already read,
     * it create array with all articles instead one article,
     * and add new article to this array. It required to support multiple
     * translations for one word in dictionary.
     *
     * @param text
     *            translation article
     * @param result
     *            add to Object
     */
    private Object addArticle(final String text, final Object data) {
        if (data == null) {
            return text;
        } else {
            if (data instanceof String[]) {
                String[] dobj = (String[]) data;
                String[] d = new String[dobj.length + 1];
                System.arraycopy(dobj, 0, d, 0, dobj.length);
                d[d.length - 1] = text;
                return d;
            } else {
                String[] d = new String[2];
                d[0] = (String) data;
                d[1] = text;
                return d;
            }
        }
    }

    /**
     * Read article's text.
     * 
     * @param word
     *            acticle name from key from readHeader method
     * @param acticleData
     *            object from value from readHeader method
     * @return article text
     */
    @Override
    public String readArticle(String word, Object data) throws Exception {
            return (String) data;
    }

    public class EBDictStringHook extends HookAdapter<String> {
        private static final int MAX_LINES = 20;
        private StringBuffer output = new StringBuffer(2048);
        private SubBook subBook;
        private int lineNum;
        private boolean narrow = false;
        private int decType;

        public EBDictStringHook(SubBook book) {
            super();
            subBook = book;
            lineNum = 0;
        }

        /**
         * clear output line
         */
        @Override
        public void clear() {
            output.delete(0, output.length());
            lineNum = 0;
        }

        /*
         * get result string
         */
        @Override
        public String getObject() {
            return output.toString();
        }

        /*
         * Can accept more input?
         */
        @Override
        public boolean isMoreInput() {
            if (lineNum >= MAX_LINES) {
                return false;
            }
            return true;
        }

        /**
         * Append article text.
         *
         * @param text 
         */
        @Override
        public void append(String text) {
            if (narrow) {
                output.append(convertZen2Han(text));
            } else {
                output.append(text);
            }
        }

        /**
         * Append GAIJI text(bitmap)
         *
         * @param code  gaiji code
         *        referenced to bitmap gliff image
         */
        @Override
        public void append(int code) {
            // FIXME: implement me.
        }

        /**
         * begin roman alphabet
         */
        @Override
        public void beginNarrow() {
            narrow = true;
        }

        /**
         * end roman alphabet
         */
        @Override
        public void endNarrow() {
            narrow = false;
        }

        /**
         * begin subscript
         */
        @Override
        public void beginSubscript() {
            output.append("<sub>");
        }

        /**
         * end subscript
         */
        @Override
        public void endSubscript() {
            output.append("</sub>");
        }

        /**
         * begin super script
         */
        @Override
        public void beginSuperscript() {
            output.append("<sup>");
        }

        /**
         * end super script
         */
        @Override
        public void endSuperscript() {
            output.append("</sup>");
        }

        /**
         * set indent of line head
         * 
         * @param len 
         */
        @Override
        public void setIndent(int len) {
            for (int i = 0 ; i<len; i++ ) {
                output.append("&nbsp;");
            }
        }

        /**
         * insert new line.
         */
        @Override
        public void newLine() {
            output.append("<br>");
            lineNum++;
        }

        /**
         * set no break
         */
        @Override
        public void beginNoNewLine() {
            // FIXME: implement me.
        }
        @Override
        public void endNoNewLine() {
            // FIXME
        }

        /**
         * insert em tag
         */
        @Override
        public void beginEmphasis() {
            output.append("<em>");
        }
        @Override
        public void endEmphasis() {
            output.append("</em>");
        }

        /**
         * insert decoretion
         *
         * @param type decoration type
         * #BOLD
         * #ITALIC
         */
        @Override
        public void beginDecoration(int type) {
            this.decType = type;
            switch (decType) {
                case BOLD:
                    output.append("<i>");
                    break;
                case ITALIC:
                    output.append("<b>");
                    break;
                default:
                    output.append("<u>");
                    break;
            }
        }

        @Override
        public void endDecoration() {
             switch (decType) {
                case BOLD:
                    output.append("</i>");
                    break;
                case ITALIC:
                    output.append("</b>");
                    break;
                default:
                    output.append("</u>");
                    break;
            }
        }

        /**
         * convert Zenkaku alphabet to Hankaku
         *
         * convert (\uFF01 - \uFF5E) to (\u0021- \u007E)
         *
         * @param text
         * @return String converted
        */
        public String convertZen2Han(String text) {
            int i;
            StringBuilder result = new StringBuilder(text.length());
            for (i = 0; i < text.length(); i++ ) {
                char c = text.charAt(i);
                if (0xff00 < (int) c && (int) c  < 0xff5f) {
                    result.append(Character.toChars((int) c + 0x0021 - 0xff01));
                }else {
                    result.append(c);
                }
            }
            return result.toString();
        }
    }
}
