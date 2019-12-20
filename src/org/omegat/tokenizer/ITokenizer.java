/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2013, 2015 Aaron Madlon-Kay
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

package org.omegat.tokenizer;

import org.omegat.util.Token;

/**
 * Interface for tokenize string engine.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public interface ITokenizer {
    enum StemmingMode {
        NONE, MATCHING, GLOSSARY
    }

    /**
     * Breaks a string into word-only tokens. Numbers, tags, and other non-word
     * tokens are NOT included in the result. Stemming can be used depending on
     * the supplied {@link StemmingMode}.
     * <p>
     * This method is used to find fuzzy matches and glossary entries.
     * <p>
     * Results can be cached for better performance.
     */
    Token[] tokenizeWords(String str, StemmingMode stemmingMode);

    /**
     * Breaks a string into word-only strings. Numbers, tags, and other non-word
     * tokens are NOT included in the result. Stemming can be used depending on
     * the supplied {@link StemmingMode}.
     * <p>
     * When stemming is used, both the original word and its stem may be
     * included in the results, if they differ. (The stem will come first.)
     * <p>
     * This method used for dictionary lookup.
     * <p>
     * Results are not cached.
     */
    String[] tokenizeWordsToStrings(String str, StemmingMode stemmingMode);

    /**
     * Breaks a string into tokens. Numbers, tags, and other non-word tokens are
     * included in the result. Stemming is NOT used.
     * <p>
     * This method is used to mark string differences in the UI and to tune similarity.
     * <p>
     * Results are not cached.
     */
    Token[] tokenizeVerbatim(String str);

    /**
     * Breaks a string into strings. Numbers, tags, and other non-word tokens are
     * included in the result. Stemming is NOT used.
     * <p>
     * This method is used to mark string differences in the UI and for debugging
     * purposes.
     * <p>
     * Results are not cached.
     */
    String[] tokenizeVerbatimToStrings(String str);

    /**
     * Return an array of language strings (<code>xx-yy</code>) indicating the tokenizer's
     * supported languages. Meant for tokenizers for which the supported languages
     * can only be determined at runtime, like the {@link HunspellTokenizer}.
     * <p>
     * Indicate that this should be used by setting the {@link Tokenizer} annotation
     * to contain only {@link Tokenizer#DISCOVER_AT_RUNTIME}.
     */
    String[] getSupportedLanguages();
}
