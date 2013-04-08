/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2009 Didier Briel, Alex Buloichik
               2012 Thomas Cordonnier
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.statistics;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * Save project statistic into text file.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
public class Statistics {

    protected static final int PERCENT_EXACT_MATCH = 101;
    protected static final int PERCENT_REPETITIONS = 102;

    /**
     * Pre-builds a map with external TMX and orphaned translations
     * in order to eliminate dupplicates
     **/
    public static Map<String, Token[]> buildExternalSourceTexts(final Map<String, Token[]> tokensCache) {
        final Map<String, Token[]> res = new java.util.HashMap<String, Token[]> ();

        final IProject project = Core.getProject();
        
        /* Travel by default orphaned. */
        project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
            public void iterate(String source, TMXEntry en) {
                res.put (en.source, tokenizeExactlyWithCache(tokensCache, en.source));
            }
        });
        /* Travel by alternative orphaned. */
        project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
            public void iterate(EntryKey source, TMXEntry en) {
                res.put (en.source, tokenizeExactlyWithCache(tokensCache, en.source));
            }
        });

        /* Travel by TMs. */
        for (ExternalTMX tmFile : project.getTransMemories().values()) {
            for (int i = 0; i < tmFile.getEntries().size(); i++) {
                TMXEntry tm = tmFile.getEntries().get(i);
                res.put (tm.source, tokenizeExactlyWithCache(tokensCache, tm.source));
            }
        }
        
        return res;
    }

    /**
     * Get tokens from cache or tokenize and cache it.
     * 
     * @param tokensCache
     *            cache
     * @param str
     *            string to tokenize
     * @return tokens
     */
    public static Token[] tokenizeExactlyWithCache(final Map<String, Token[]> tokensCache, final String str) {
        Token[] result = tokensCache.get(str);
        if (result == null) {
            result = Core.getProject().getSourceTokenizer().tokenizeAllExactly(str);
            tokensCache.put(str, result);
        }
        return result;
    }

    /**
     * Computes the number of characters excluding spaces in a string. Special
     * char for tag replacement not calculated.
     */
    public static int numberOfCharactersWithoutSpaces(String str) {
        int chars = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != StaticUtils.TAG_REPLACEMENT && !Character.isSpaceChar(c)) {
                chars++;
            }
        }
        return chars;
    }

    /**
     * Computes the number of characters with spaces in a string. Special char
     * for tag replacement not calculated.
     */
    public static int numberOfCharactersWithSpaces(String str) {
        int chars = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != StaticUtils.TAG_REPLACEMENT) {
                chars++;
            }
        }
        return chars;
    }

    /** Computes the number of words in a string. */
    public static int numberOfWords(String str) {
        int len = str.length();
        if (len == 0)
            return 0;
        int nTokens = 0;
        BreakIterator breaker = DefaultTokenizer.getWordBreaker();
        breaker.setText(str);

        String tokenStr = new String();

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
            tokenStr = str.substring(start, end);
            boolean word = false;
            for (int i = 0; i < tokenStr.length(); i++) {
                char ch = tokenStr.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    word = true;
                    break;
                }
            }
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()) {
                nTokens++;
            }
        }
        return nTokens;
    }

    /**
     * Write text to file.
     * 
     * @param filename
     * @param data
     */
    public static void writeStat(String filename, String text) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename), OConsts.UTF8);
            try {
                out.write(DateFormat.getInstance().format(new Date()) + "\n");
                out.write(text);
                out.flush();
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
}
