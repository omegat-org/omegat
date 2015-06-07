/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2008 Alex Buloichik (alex73mail@gmail.com)
               2013, 2015 Aaron Madlon-Kay
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
package org.omegat.tokenizer;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.comments.ICommentProvider;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Base class for Lucene-based tokenizers.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public abstract class BaseTokenizer implements ITokenizer {
    private static final Map<String, Token[]> tokenCacheNone = new HashMap<String, Token[]>(
            5000);
    private static final Map<String, Token[]> tokenCacheMatching = new HashMap<String, Token[]>(
            5000);
    private static final Map<String, Token[]> tokenCacheGlossary = new HashMap<String, Token[]>(
            5000);

    /**
     * A map indicating which {@link Version}s should be used with this tokenizer,
     * with user-facing strings that describe the versions.
     * <p>
     * By default it is populated with all members of the {@link Version} enum;
     * individual tokenizers should remove inappropriate versions or overwrite version
     * descriptions with an explanatory string (e.g. noting the algorithm used in that version).
     * <p>
     * See {@link LuceneGermanTokenizer} for an example class that modifies this map.
     */
    protected static final Map<Version, String> supportedBehaviors = new LinkedHashMap<Version, String>(
            Version.values().length);

    protected static final String[] EMPTY_STOP_WORDS_LIST = new String[0];
    protected static final Token[] EMPTY_TOKENS_LIST = new Token[0];
    protected static final int DEFAULT_TOKENS_COUNT = 64;

    /**
     * Indicates that {@link #tokenizeAllExactly(String)} should use OmegaT's
     * {@link WordIterator} to tokenize "exactly" for display.
     * <p>
     * For language-specific tokenizers that maintain the property that 
     * <code>(the concatenation of all tokens).equals(original string) == true</code>,
     * set this to false to use the language-specific tokenizer for everything.
     */
    protected boolean shouldDelegateTokenizeExactly = true;

    /**
     * Indicates the default behavior to use for the tokenizer.
     * Each tokenizer may override this with the version most suitable for that language.
     */
    protected Version defaultBehavior = Version.LUCENE_CURRENT;

    protected Version currentBehavior = null;

    public BaseTokenizer() {
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                if (eventType == PROJECT_CHANGE_TYPE.CLOSE) {
                    synchronized (tokenCacheNone) {
                        tokenCacheNone.clear();
                    }
                    synchronized (tokenCacheMatching) {
                        tokenCacheMatching.clear();
                    }
                    synchronized (tokenCacheGlossary) {
                        tokenCacheGlossary.clear();
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Version, String> getSupportedBehaviors() {
        return supportedBehaviors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Version getBehavior() {
        return currentBehavior == null ? defaultBehavior : currentBehavior;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBehavior(Version behavior) {
        currentBehavior = behavior;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Version getDefaultBehavior() {
        return defaultBehavior;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeWordsForSpelling(String str) {
        return tokenize(str, false, false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeWords(final String strOrig, final StemmingMode stemmingMode) {
        Map<String, Token[]> cache;
        switch (stemmingMode) {
        case NONE:
            cache = tokenCacheNone;
            break;
        case GLOSSARY:
            cache = tokenCacheGlossary;
            break;
        case MATCHING:
            cache = tokenCacheMatching;
            break;
        default:
            throw new RuntimeException("No cache for specified stemming mode");
        }
        Token[] result;
        synchronized (cache) {
            result = cache.get(strOrig);
        }
        if (result != null) {
            return result;
        }
        result = tokenize(strOrig, stemmingMode == StemmingMode.GLOSSARY
                || stemmingMode == StemmingMode.MATCHING,
                stemmingMode == StemmingMode.MATCHING,
                true);

        // put result in the cache
        synchronized (cache) {
            cache.put(strOrig, result);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeAllExactly(final String strOrig) {

        if (!shouldDelegateTokenizeExactly) {
            return tokenize(strOrig, false, false, false);
        }

        if (strOrig.isEmpty()) {
            return EMPTY_TOKENS_LIST;
        }

        List<Token> result = new ArrayList<Token>(DEFAULT_TOKENS_COUNT);

        WordIterator iterator = new WordIterator();
        iterator.setText(strOrig.toLowerCase());

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end,
                end = iterator.next()) {
            String tokenStr = strOrig.substring(start, end).toLowerCase();
            result.add(new Token(tokenStr, start));
        }

        return result.toArray(new Token[result.size()]);
    }
    
    protected Token[] tokenizeByCodePoint(String strOrig) {
        // See http://www.ibm.com/developerworks/library/j-unicode/#1-5
        // Example 1-5 appears to be faster than 1-6 for us (because our strings are short?)
        Token[] tokens = new Token[strOrig.codePointCount(0, strOrig.length())];
        for (int cp, i = 0, j = 0; i < strOrig.length(); i += Character.charCount(cp)) {
            cp = strOrig.codePointAt(i);
            tokens[j++] = new Token(String.valueOf(Character.toChars(cp)), i);
        }
        return tokens;
    }

    protected Token[] tokenize(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed, final boolean filterDigits) {
        if (StringUtil.isEmpty(strOrig)) {
            return EMPTY_TOKENS_LIST;
        }

        List<Token> result = new ArrayList<Token>(64);

        final TokenStream in = getTokenStream(strOrig, stemsAllowed, stopWordsAllowed);
        in.addAttribute(CharTermAttribute.class);
        in.addAttribute(OffsetAttribute.class);
        
        CharTermAttribute cattr = in.getAttribute(CharTermAttribute.class);
        OffsetAttribute off = in.getAttribute(OffsetAttribute.class);

        try {
            in.reset();
            while (in.incrementToken()) {
                String tokenText = cattr.toString();
                if (filterDigits) {
                    for (int i = 0, cp; i < tokenText.length(); i += Character.charCount(cp)) {
                        cp = tokenText.codePointAt(i);
                        if (Character.isDigit(cp)) {
                            tokenText = null;
                            break;
                        }
                    }
                }
                if (tokenText != null) {
                    result.add(new Token(tokenText, off.startOffset(),
                            off.endOffset() - off.startOffset()));
                }
            }
            in.end();
            in.close();
        } catch (IOException ex) {
            // shouldn't happen
        }
        return result.toArray(new Token[result.size()]);
    }

    protected abstract TokenStream getTokenStream(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed);

    @Override
    public String[] getSupportedLanguages() {
        Tokenizer ann = getClass().getAnnotation(Tokenizer.class);
        return ann == null ? new String[0] : ann.languages();
    }

    protected String test(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append('\n');
        for (String input : args) {
            sb.append("Input:\n");
            sb.append(input);
            sb.append("tokenizeAllExactly:\n");
            sb.append(printTest(tokenizeAllExactly(input), input));
            sb.append("tokenize:\n");
            sb.append(printTest(tokenize(input, false, false, false), input));
            sb.append("tokenize (stemsAllowed):\n");
            sb.append(printTest(tokenize(input, true, false, false), input));
            sb.append("tokenize (stemsAllowed stopWordsAllowed):\n");
            sb.append(printTest(tokenize(input, true, true, false), input));
            sb.append("tokenize (stemsAllowed stopWordsAllowed filterDigits) (=tokenizeWords(MATCHING)):\n");
            sb.append(printTest(tokenize(input, true, true, true), input));
            sb.append("tokenize (stemsAllowed filterDigits) (=tokenizeWords(GLOSSARY)):\n");
            sb.append(printTest(tokenize(input, true, false, true), input));
            sb.append("tokenize (filterDigits) (=tokenizeWords(NONE) tokenizeWordsForSpelling):\n");
            sb.append(printTest(tokenize(input, false, false, true), input));
            sb.append("----------------------------------\n");
        }
        return sb.toString();
    }
    
    protected String printTest(Token[] tokens, String input) {
        StringBuilder sb = new StringBuilder();
        String[] strings = Token.getTextsFromString(tokens, input);
        sb.append(StringUtils.join(strings, ", ")).append('\n');
        sb.append("Is verbatim: ").append(StringUtils.join(strings, "").equals(input)).append('\n');
        return sb.toString();
    }
    
    public static ICommentProvider TOKENIZER_DEBUG_PROVIDER = new ICommentProvider() {
        @Override
        public String getComment(SourceTextEntry newEntry) {
            return ((BaseTokenizer) Core.getProject().getSourceTokenizer()).test(newEntry.getSrcText());
        }
    };
    
    static {
        for (Version v : Version.values()) {
            StringBuilder b = new StringBuilder();
            b.append(v.toString().charAt(0));
            b.append(v.toString().substring(1).toLowerCase().replace('_', ' '));
            if (Character.isDigit(b.charAt(b.length() - 1))) b.insert(b.length() - 1, '.');
            supportedBehaviors.put(v, b.toString());
        }
    }
}
