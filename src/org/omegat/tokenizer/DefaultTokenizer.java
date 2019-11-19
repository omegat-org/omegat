/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Didier Briel, Zoltan Bartko
               2008 Alex Buloichik
               2015 Didier Briel, Aaron Madlon-Kay
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Methods for tokenize string.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Alex Buloichik
 * @author Aaron Madlon-Kay
 */
public class DefaultTokenizer implements ITokenizer {

    /**
     * Contains a list of tokens for each *unique* string. By not storing a list
     * of tokens for every string, memory is saved. Token lists are not saved
     * when all tokens are requested. Again to save memory.
     */
    private static Map<String, Token[]> tokenCache = new HashMap<String, Token[]>(5000);

    public static final Token[] EMPTY_TOKENS_LIST = new Token[0];
    public static final String[] EMPTY_STRINGS_LIST = new String[0];

    public DefaultTokenizer() {
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                // clear cache
                synchronized (tokenCache) {
                    tokenCache.clear();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Token[] tokenizeWords(final String strOrig, final StemmingMode stemmingMode) {
        if (StringUtil.isEmpty(strOrig)) {
            return EMPTY_TOKENS_LIST;
        }

        Token[] result;
        synchronized (tokenCache) {
            result = tokenCache.get(strOrig);
        }
        if (result != null) {
            return result;
        }

        result = tokenizeTextNoCache(strOrig, false);

        // put result in the cache
        synchronized (tokenCache) {
            tokenCache.put(strOrig, result);
        }
        return result;
    }

    @Override
    public String[] tokenizeWordsToStrings(String str, StemmingMode stemmingMode) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRINGS_LIST;
        }
        return tokenizeTextToStringsNoCache(str, false);
    }

    @Override
    public Token[] tokenizeVerbatim(final String strOrig) {
        return tokenizeTextNoCache(strOrig, true);
    }

    @Override
    public String[] tokenizeVerbatimToStrings(String str) {
        return tokenizeTextToStringsNoCache(str, true);
    }

    /**
     * Breaks a string into tokens.
     * <p>
     * Examples:
     * <ul>
     * <li>This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
     * <li>Fine, thanks, and you? -> "fine", "thanks", "and", "you"
     * <li>C&all this action -> "call", "this", "action" ('&' is eaten)
     * </ul>
     * <p>
     * OmegaT tags and other non-word tokens are skipped if the parameter "all"
     * is false.
     *
     * @param str
     *            string to tokenize
     * @param all
     *            If true, numbers, tags, and other non-word tokens are included
     *            in the list
     * @return array of tokens (all)
     */
    private static Token[] tokenizeTextNoCache(final String strOrig, final boolean all) {
        if (StringUtil.isEmpty(strOrig)) {
            // fixes bug nr. 1382810 (StringIndexOutOfBoundsException)
            return EMPTY_TOKENS_LIST;
        }

        // create a new token list
        List<Token> tokens = new ArrayList<Token>(64);

        // get a word breaker
        BreakIterator breaker = getWordBreaker();
        breaker.setText(strOrig);

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
            String tokenStr = strOrig.substring(start, end);
            if (all) {
                // Accepting all tokens
                tokens.add(new Token(tokenStr, start));
                continue;
            }
            // Accepting only words that aren't OmegaT tags
            boolean word = false;
            for (int cp, i = 0; i < tokenStr.length(); i += Character.charCount(cp)) {
                cp = tokenStr.codePointAt(i);
                if (Character.isLetter(cp)) {
                    word = true;
                    break;
                }
            }
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()) {
                tokens.add(new Token(tokenStr, start));
            }
        }

        return tokens.toArray(new Token[tokens.size()]);
    }

    private static String[] tokenizeTextToStringsNoCache(String str, boolean all) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRINGS_LIST;
        }

        // create a new token list
        List<String> tokens = new ArrayList<String>(64);

        // get a word breaker
        BreakIterator breaker = getWordBreaker();
        breaker.setText(str);

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
            String tokenStr = str.substring(start, end);
            if (all) {
                // Accepting all tokens
                tokens.add(tokenStr);
                continue;
            }
            // Accepting only words that aren't OmegaT tags
            boolean word = false;
            for (int cp, i = 0; i < tokenStr.length(); i += Character.charCount(cp)) {
                cp = tokenStr.codePointAt(i);
                if (Character.isLetter(cp)) {
                    word = true;
                    break;
                }
            }
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()) {
                tokens.add(tokenStr);
            }
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    /** Returns an iterator to break sentences into words. */
    public static BreakIterator getWordBreaker() {
        // if (wordBreaker==null)
        // wordBreaker = new WordIterator();
        // return wordBreaker;

        return new WordIterator();

        // HP: This is a fix for bug 1589484. If you use only one
        // WordIterator instance, it will lead to problems when
        // using multiple threads, as OmegaT does. Sometimes, in
        // the middle of breaking a string, another thread may set
        // a different text, and then you get index out of bounds
        // exceptions. By returning a new WordIterator each time
        // one is requested, this problem is solved, and it doesn't
        // hurt performance either.
    }

    /**
     * Check if array contains token.
     */
    public static boolean isContains(Token[] tokensList, Token tokenForCheck) {
        return search(tokensList, tokenForCheck, 0) != -1;
    }

    /**
     * Search a haystack for an token equal to the needle and return the token from the haystack.
     * <p>
     * This makes no sense! Why are we returning an object equal to the one we already have? Because the Token class is
     * bizarrely implemented to consider two tokens equal based on only their hash field, and in this case we are
     * explicitly interested in getting the other token with the same hash but different other fields.
     *
     * @param haystack
     *            Array of tokens to search
     * @param needle
     *            The token with the hash we want to match
     * @return The matching token, or null if not found
     */
    private static int search(Token[] haystack, Token needle, int start) {
        for (int i = start; i < haystack.length; i++) {
            if (Objects.equals(needle, haystack[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if the {@code listForFind} tokens are present in {@code tokensList}.
     *
     * @param tokensList
     *            a list of tokens to be searched
     * @param listForFind
     *            a list of tokens to search in {@code tokensList}
     * @param notExact
     *            is true if the tokens in {@code listForFind} can be non-contiguous or in a different order in the
     *            {@code tokensList}. If false, tokens must be exactly the same.
     * @return true if the tokens in {@code listForFind} are found in {@code tokensList}
     */
    public static boolean isContainsAll(Token[] tokensList, Token[] listForFind, boolean notExact) {
        return notExact ? containsAllInexact(tokensList, listForFind) : containsAllExact(tokensList, listForFind);
    }

    /**
     * Find and return all tokens in {@code tokensList} that match the tokens in {@code listForFind}.
     *
     * @param tokensList
     *            a list of tokens to be searched
     * @param listForFind
     *            a list of tokens to search in tokensList
     * @param notExact
     *            is true if the tokens in listForFind can be non-contiguous or in a different order in the tokensList.
     *            If false, tokens must be exactly the same.
     * @return A list containing each hit of the matched tokens. Each token array represents a different instance of
     *         {@code listForFind} that was found in {@code tokensList}.
     */
    public static List<Token[]> searchAll(Token[] tokensList, Token[] listForFind, boolean notExact) {
        return notExact ? searchAllInexact(tokensList, listForFind) : searchAllExact(tokensList, listForFind);
    }

    /**
     * Check if all elements of {@code needles} are present in {@code haystack} in any order.
     *
     * @param haystack
     *            a list of tokens to be searched
     * @param needles
     *            a list of tokens to search in {@code haystack}
     * @return Whether or not the {@code needles} were found
     */
    private static boolean containsAllInexact(Token[] haystack, Token[] needles) {
        for (Token n : needles) {
            if (search(haystack, n, 0) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if all elements of {@code needles} are present in {@code haystack}, and if so, return all matching elements
     * of {@code haystack}.
     *
     * @param haystack
     *            a list of tokens to be searched
     * @param needles
     *            a list of tokens to search in {@code haystack}
     * @return A list of one array containing the unique hits of the matched tokens.
     */
    private static List<Token[]> searchAllInexact(Token[] haystack, Token[] needles) {
        List<Token> result = null;
        for (Token n : needles) {
            boolean found = false;
            for (int i = 0; (i = search(haystack, n, i)) != -1; i++) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                found = true;
                if (!contains(result, haystack[i])) {
                    result.add(haystack[i]);
                }
            }
            if (!found) {
                return Collections.emptyList();
            }
        }
        if (result.size() < needles.length) {
            return Collections.emptyList();
        }
        // We expect to filter results later, so we can't use Collections.singletonList here
        List<Token[]> ret = new ArrayList<>();
        ret.add(result.toArray(new Token[result.size()]));
        return ret;
    }

    /*
     * This is required instead of List#contains because token equality only compares hashes but we want "deep" equality
     * here.
     */
    private static boolean contains(List<Token> list, Token token) {
        for (Token tok : list) {
            if (tok.deepEquals(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if {@code needles} is found contiguously in {@code haystack}.
     *
     * @param haystack
     *            a list of tokens to be searched
     * @param needles
     *            a list of tokens to search in tokensList
     * @return Whether or not the {@code needles} were found
     */
    private static boolean containsAllExact(Token[] haystack, Token[] needles) {
        return searchExact(haystack, needles, 0) != -1;
    }

    /**
     * Check if {@code needles} is found contiguously in {@code haystack}.
     *
     * @param haystack
     *            a list of tokens to be searched
     * @param needles
     *            a list of tokens to search in tokensList
     * @return The contiguous portions of {@code haystack} matching {@code needles}, or an empty list if not found
     */
    private static List<Token[]> searchAllExact(Token[] haystack, Token[] needles) {
        int i = searchExact(haystack, needles, 0);
        if (i == -1) {
            return Collections.emptyList();
        }
        List<Token[]> result = new ArrayList<>();
        result.add(Arrays.copyOfRange(haystack, i, i + needles.length));
        while ((i = searchExact(haystack, needles, i + needles.length)) != -1) {
            result.add(Arrays.copyOfRange(haystack, i, i + needles.length));
        }
        return result;
    }

    /**
     * Check if {@code needles} is found contiguously in {@code haystack}.
     *
     * @param haystack
     *            a list of tokens to be searched
     * @param needles
     *            a list of tokens to search in {@code haystack}
     * @return The index of the start of the match
     */
    private static int searchExact(Token[] haystack, Token[] needles, int start) {
        if (needles.length == 0) {
            return -1;
        }
        for (int i = start; i < haystack.length; i++) {
            if (StaticUtils.arraysMatchAt(needles, haystack, i)) {
                return i;
            }
        }
        return -1;
    }



    @Override
    public String[] getSupportedLanguages() {
        return new String[0];
    }
}
