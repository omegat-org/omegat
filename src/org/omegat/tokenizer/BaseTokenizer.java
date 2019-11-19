/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik (alex73mail@gmail.com)
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

import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.comments.ICommentProvider;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Base class for Lucene-based tokenizers.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public abstract class BaseTokenizer implements ITokenizer {

    protected static final String[] EMPTY_STRING_LIST = new String[0];
    protected static final Token[] EMPTY_TOKENS_LIST = new Token[0];
    protected static final int DEFAULT_TOKENS_COUNT = 64;

    private final Map<String, Token[]> tokenCacheNone = new ConcurrentHashMap<>(2500);
    private final Map<String, Token[]> tokenCacheMatching = new ConcurrentHashMap<>(2500);
    private final Map<String, Token[]> tokenCacheGlossary = new ConcurrentHashMap<>(2500);

    /**
     * Indicates that {@link #tokenizeVerbatim(String)} should use OmegaT's
     * {@link WordIterator} to tokenize "exactly" for display.
     * <p>
     * For language-specific tokenizers that maintain the property that
     * <code>(the concatenation of all tokens).equals(original string) == true</code>,
     * set this to false to use the language-specific tokenizer for everything.
     */
    protected boolean shouldDelegateTokenizeExactly = true;

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
        Token[] result = cache.get(strOrig);
        if (result != null) {
            return result;
        }
        result = tokenize(strOrig,
                stemmingMode == StemmingMode.GLOSSARY || stemmingMode == StemmingMode.MATCHING,
                stemmingMode == StemmingMode.MATCHING,
                stemmingMode != StemmingMode.GLOSSARY,
                true);

        // put result in the cache
        cache.put(strOrig, result);
        return result;
    }

    @Override
    public String[] tokenizeWordsToStrings(String str, StemmingMode stemmingMode) {
        return tokenizeToStrings(str,
                stemmingMode == StemmingMode.GLOSSARY || stemmingMode == StemmingMode.MATCHING,
                stemmingMode == StemmingMode.MATCHING,
                stemmingMode != StemmingMode.GLOSSARY,
                true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeVerbatim(final String strOrig) {
        if (StringUtil.isEmpty(strOrig)) {
            return EMPTY_TOKENS_LIST;
        }

        if (!shouldDelegateTokenizeExactly) {
            return tokenize(strOrig, false, false, false, false);
        }

        List<Token> result = new ArrayList<Token>(DEFAULT_TOKENS_COUNT);

        WordIterator iterator = new WordIterator();
        iterator.setText(strOrig);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end,
                end = iterator.next()) {
            String tokenStr = strOrig.substring(start, end);
            result.add(new Token(tokenStr, start));
        }

        return result.toArray(new Token[result.size()]);
    }

    @Override
    public String[] tokenizeVerbatimToStrings(String str) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRING_LIST;
        }

        if (!shouldDelegateTokenizeExactly) {
            return tokenizeToStrings(str, false, false, false, false);
        }

        List<String> result = new ArrayList<String>(DEFAULT_TOKENS_COUNT);

        WordIterator iterator = new WordIterator();
        iterator.setText(str);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end,
                end = iterator.next()) {
            String tokenStr = str.substring(start, end);
            result.add(tokenStr);
        }

        return result.toArray(new String[result.size()]);
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

    protected String[] tokenizeByCodePointToStrings(String strOrig) {
        // See http://www.ibm.com/developerworks/library/j-unicode/#1-5
        // Example 1-5 appears to be faster than 1-6 for us (because our strings are short?)
        String[] tokens = new String[strOrig.codePointCount(0, strOrig.length())];
        for (int cp, i = 0, j = 0; i < strOrig.length(); i += Character.charCount(cp)) {
            cp = strOrig.codePointAt(i);
            tokens[j++] = String.valueOf(Character.toChars(cp));
        }
        return tokens;
    }

    protected Token[] tokenize(final String strOrig, final boolean stemsAllowed, final boolean stopWordsAllowed,
            final boolean filterDigits, final boolean filterWhitespace) {
        if (StringUtil.isEmpty(strOrig)) {
            return EMPTY_TOKENS_LIST;
        }

        List<Token> result = new ArrayList<Token>(64);

        try (TokenStream in = getTokenStream(strOrig, stemsAllowed, stopWordsAllowed)) {
            in.addAttribute(CharTermAttribute.class);
            in.addAttribute(OffsetAttribute.class);

            CharTermAttribute cattr = in.getAttribute(CharTermAttribute.class);
            OffsetAttribute off = in.getAttribute(OffsetAttribute.class);

            in.reset();
            while (in.incrementToken()) {
                String tokenText = cattr.toString();
                if (acceptToken(tokenText, filterDigits, filterWhitespace)) {
                    result.add(new Token(tokenText, off.startOffset(), off.endOffset() - off.startOffset()));
                }
            }
            in.end();
        } catch (IOException ex) {
            Log.log(ex);
        }
        return result.toArray(new Token[result.size()]);
    }

    protected String[] tokenizeToStrings(String str, boolean stemsAllowed, boolean stopWordsAllowed,
            boolean filterDigits, boolean filterWhitespace) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRING_LIST;
        }

        List<String> result = new ArrayList<String>(64);

        try (TokenStream in = getTokenStream(str, stemsAllowed, stopWordsAllowed)) {
            in.addAttribute(CharTermAttribute.class);
            in.addAttribute(OffsetAttribute.class);

            CharTermAttribute cattr = in.getAttribute(CharTermAttribute.class);
            OffsetAttribute off = in.getAttribute(OffsetAttribute.class);

            Locale loc = stemsAllowed ? getEffectiveLanguage().getLocale() : null;

            in.reset();
            while (in.incrementToken()) {
                String tokenText = cattr.toString();
                if (acceptToken(tokenText, filterDigits, filterWhitespace)) {
                    result.add(tokenText);
                    if (stemsAllowed) {
                        String origText = str.substring(off.startOffset(), off.endOffset());
                        if (!origText.toLowerCase(loc).equals(tokenText.toLowerCase(loc))) {
                            result.add(origText);
                        }
                    }
                }
            }
            in.end();
        } catch (IOException ex) {
            Log.log(ex);
        }
        return result.toArray(new String[result.size()]);
    }

    private boolean acceptToken(String token, boolean filterDigits, boolean filterWhitespace) {
        if (StringUtil.isEmpty(token)) {
            return false;
        }
        if (!filterDigits && !filterWhitespace) {
            return true;
        }
        boolean isWhitespaceOnly = true;
        for (int i = 0, cp; i < token.length(); i += Character.charCount(cp)) {
            cp = token.codePointAt(i);
            if (filterDigits && Character.isDigit(cp)) {
                return false;
            }
            if (filterWhitespace && !StringUtil.isWhiteSpace(cp)) {
                isWhitespaceOnly = false;
            }
        }
        return !(filterWhitespace && isWhitespaceOnly);
    }

    protected abstract TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed)
            throws IOException;

    /**
     * Minimal implementation that returns the default implementation
     * corresponding to all false parameters. Subclasses should override this to
     * handle true parameters.
     */
    protected TokenStream getStandardTokenStream(String strOrig) throws IOException {
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(strOrig));
        return tokenizer;
    }

    @Override
    public String[] getSupportedLanguages() {
        return getAnnotationLanguages();
    }

    private String[] getAnnotationLanguages() {
        Tokenizer ann = getClass().getAnnotation(Tokenizer.class);
        if (ann == null) {
            throw new RuntimeException(getClass().getName() + " must have a "
                    + Tokenizer.class.getName() + " annotation available at runtime.");
        }
        String[] languages = ann.languages();
        if (languages.length == 0) {
            throw new RuntimeException(getClass().getName() + " must have a non-empty " + Tokenizer.class.getName()
                    + " annotation available at runtime.");
        }
        return languages;
    }

    protected Language getEffectiveLanguage() {
        String[] languages = getAnnotationLanguages();
        if (languages.length == 1) {
            if (languages[0].equals(Tokenizer.DISCOVER_AT_RUNTIME)) {
                return getProjectLanguage();
            } else {
                return new Language(languages[0]);
            }
        }
        return getProjectLanguage();
    }

    protected Language getProjectLanguage() {
        IProject proj = Core.getProject();
        if (proj == null) {
            throw new RuntimeException("This tokenizer's language can only be "
                    + "determined in the context of a project, but project is null.");
        } else if (proj.getSourceTokenizer() == this) {
            return proj.getProjectProperties().getSourceLanguage();
        } else if (proj.getTargetTokenizer() == this) {
            return proj.getProjectProperties().getTargetLanguage();
        } else {
            throw new RuntimeException("This tokenizer's language can only be "
                    + "determined in the context of a project, but is not assigned " + "to current project.");
        }
    }

    protected String test(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append('\n');
        for (String input : args) {
            sb.append("Input:\n");
            sb.append(input).append("\n");
            sb.append("tokenizeVerbatim:\n");
            sb.append(printTest(tokenizeVerbatimToStrings(input), input));
            sb.append("tokenize:\n");
            sb.append(printTest(tokenizeToStrings(input, false, false, false, true), input));
            sb.append("tokenize (stemsAllowed):\n");
            sb.append(printTest(tokenizeToStrings(input, true, false, false, true), input));
            sb.append("tokenize (stemsAllowed stopWordsAllowed):\n");
            sb.append(printTest(tokenizeToStrings(input, true, true, false, true), input));
            sb.append("tokenize (stemsAllowed stopWordsAllowed filterDigits) (=tokenizeWords(MATCHING)):\n");
            sb.append(printTest(tokenizeToStrings(input, true, true, true, true), input));
            sb.append("tokenize (stemsAllowed filterDigits) (=tokenizeWords(GLOSSARY)):\n");
            sb.append(printTest(tokenizeToStrings(input, true, false, true, true), input));
            sb.append("tokenize (filterDigits) (=tokenizeWords(NONE)):\n");
            sb.append(printTest(tokenizeToStrings(input, false, false, true, true), input));
            sb.append("----------------------------------\n");
        }
        return sb.toString();
    }

    protected String printTest(String[] strings, String input) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.join(strings, ", ")).append('\n');
        sb.append("Is verbatim: ").append(StringUtils.join(strings, "").equals(input)).append('\n');
        return sb.toString();
    }

    public static final ICommentProvider TOKENIZER_DEBUG_PROVIDER = new ICommentProvider() {
        @Override
        public String getComment(SourceTextEntry newEntry) {
            return ((BaseTokenizer) Core.getProject().getSourceTokenizer()).test(newEntry.getSrcText());
        }
    };
}
