/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Didier Briel, Zoltan Bartko
               2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.matching;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.util.PatternConsts;
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
 */
public class Tokenizer {

    /**
      * Contains a list of tokens for each *unique* string.
      * By not storing a list of tokens for every string,
      * memory is saved. Token lists are not saved when
      * all tokens are requested. Again to save memory.
      */
    private static Map<String, Token[]> tokenCache = new HashMap<String, Token[]>(5000);
    
    private static final Token[] EMPTY_TOKENS_LIST = new Token[0];

    /** Removes all token lists from the cache. */
    public static void clearTokenCache() {
        synchronized (tokenCache) {
            tokenCache.clear();
        }
    }

    /**
      * Breaks a string into tokens.
      * <p>
      * Examples:
      * <ul>
      * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
      * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
      * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
      * </ul>
      * <p>
      * Also skips OmegaT tags.
      *
      * @param str string to tokenize
      * @return List of all tokens (words only)
      */
    public static Token[] tokenizeText(String str) {
        return tokenizeTextWithCache(str);
    }

    /**
     * Breaks a string into tokens (see
     * {@link #tokenizeTextNoCache(String, boolean)}) and don't cache results.
     */
    public static Token[] tokenizeTextNoCache(String str) {
        return tokenizeTextNoCache(str, false);
    }

    /**
     * Breaks a string into tokens (see {@link #tokenizeTextNoCache(String, boolean)}) and cache results.
     * 
     * Check if we've already tokenized this string, because no sense in retokenizing identical strings.
     * 
     * Don't check if the caller wants all tokens.
     * 
     * @param strOrig
     * @return
     */
    public static Token[] tokenizeTextWithCache(final String strOrig) {
        Token[] result;
        synchronized (tokenCache) {
            result = tokenCache.get(strOrig);
        }
        if (result != null)
            return result;

        result = tokenizeTextNoCache(strOrig, false);

        // put result in the cache
        synchronized (tokenCache) {
            tokenCache.put(strOrig, result);
        }
        return result;
    }

    /**
     * Breaks a string into tokens.
     * <p>
     * Examples:
     * <ul>
     * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
     * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
     * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
     * </ul>
     * <p>
     * OmegaT tags and other non-word tokens are skipped if the parameter "all"
     * is false.
     * 
     * @param str
     *                string to tokenize
     * @param all
     *                If true, numbers, tags, and other non-word tokens are
     *                included in the list
     * @return array of tokens (all)
     */
    public static Token[] tokenizeTextNoCache(final String strOrig, final boolean all) {
        if (strOrig.length()==0) {
            // fixes bug nr. 1382810 (StringIndexOutOfBoundsException)
            return EMPTY_TOKENS_LIST;
        }

        // create a new token list        
        List<Token> tokens = new ArrayList<Token>(64);

        // get a word breaker
        String str = strOrig.toLowerCase(); // HP: possible error, this makes "A" and "a" match, CHECK AND FIX
        BreakIterator breaker = getWordBreaker();
        breaker.setText(str);

        int start = breaker.first();
        for (int end = breaker.next();
             end!=BreakIterator.DONE; 
             start = end, end = breaker.next())
        {
            String tokenStr = str.substring(start,end);
            boolean word = false;
            for (int i=0; i<tokenStr.length(); i++)
            {
                char ch = tokenStr.charAt(i);
                if (Character.isLetter(ch))
                {
                    word = true;
                    break;
                }
            }

            if (all || (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()))
            {
                Token token = new Token(tokenStr, start);
                tokens.add(token);
            }
        }
        
        return tokens.toArray(new Token[tokens.size()]);
    }
    
    /** Returns an iterator to break sentences into words. */
    public static BreakIterator getWordBreaker()
    {
        //if (wordBreaker==null)
        //    wordBreaker = new WordIterator();
        //return wordBreaker;

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
        for (Token t : tokensList) {
            if (tokenForCheck.equals(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if array contains other array.
     */
    public static boolean isContainsAll(Token[] tokensList, Token[] listForFind) {
        for (Token t : listForFind) {
            if (!isContains(tokensList, t)) {
                return false;
            }
        }
        return true;
    }
}
