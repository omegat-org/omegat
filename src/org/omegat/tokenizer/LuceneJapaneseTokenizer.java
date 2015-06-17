/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2013, 2015 Aaron Madlon-Kay
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
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.omegat.util.PatternConsts;

/**
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "ja" }, isDefault = true)
public class LuceneJapaneseTokenizer extends BaseTokenizer {

    public LuceneJapaneseTokenizer() {
        super();
        shouldDelegateTokenizeExactly = false;
    }

    @Override
    protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed) {
        if (stemsAllowed) {
            // Blank out tags when stemming only
            strOrig = blankOutTags(strOrig);
            CharArraySet stopWords = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopSet()
                    : new CharArraySet(getBehavior(), 0, false);
            Set<String> stopTags = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopTags()
                    : Collections.EMPTY_SET;
            return new JapaneseAnalyzer(getBehavior(), null, Mode.SEARCH, stopWords, stopTags)
                    .tokenStream("", new StringReader(strOrig));
        } else {
            return new TagJoiningFilter(new JapaneseTokenizer(new StringReader(strOrig), null, false, Mode.NORMAL));
        }
    }
    
    /**
     * Replace all instances of OmegaT-style tags (&lt;x0>, etc.) with blank spaces
     * of equal length. This is done because
     * <ul><li>This tokenizer will turn "&lt;x0>" into [x, 0], leaving the alphabetical part
     * intact (other tokenizers are expected to produce [x0], which is suppressed by digit filtering)
     * <li>Instead of merely removing the tags, they are replaced with spaces so that
     * the tokens produced correctly line up with the original, unmodified string.
     * </ul>
     */
    private String blankOutTags(String text) {
        StringBuilder buffer = new StringBuilder(text);
        Matcher m = PatternConsts.OMEGAT_TAG.matcher(text);
        while (m.find()) {
            for (int i = m.start(), end = m.end(); i < end; i++) {
                buffer.replace(i, i + 1, " ");
            }
        }
        return buffer.toString();
    }
    
    /**
     * This filter will reassemble OmegaT-style tags (&lt;x0>, etc.) that this tokenizer
     * has broken apart. It is only meant to recognize "OmegaT-style tags". It has limited
     * recovery capability when encountering false positives.
     */
    private static class TagJoiningFilter extends TokenFilter {

        private static final int BUFFER_INITIAL_SIZE = 5;
        
        private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
        private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
        
        private StringBuilder buffer = new StringBuilder(BUFFER_INITIAL_SIZE);
        
        private int startOffset = -1;
        
        private boolean buffering = false;
        private boolean doReplay = false;

        private final ArrayDeque<CachedToken> stack = new ArrayDeque<CachedToken>();
        
        protected TagJoiningFilter(TokenStream input) {
            super(input);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (doReplay) {
                replayCachedToken();
                doReplay = !stack.isEmpty();
                return true;
            }
            while (input.incrementToken()) {
                char[] chars = termAtt.buffer();
                int len = termAtt.length();
                if (buffering) {
                    if (finishBuffering(chars, len)) {
                        return true;
                    }
                    if (cancelBuffering(chars, len)) {
                        return true;
                    }
                    cacheToken(chars, len);
                    buffer.append(chars, 0, len);
                    continue;
                }
                if (startBuffering(chars, len)) {
                    continue;
                }
                return true;
            }
            return finishToken();
        }
        
        private boolean startBuffering(char[] chars, int len) {
            if (len > 2 || !isTagOpen(chars[0])) {
                return false;
            }
            buffer.append(chars, 0, len);
            startOffset = offsetAtt.startOffset();
            cacheToken(chars, len);
            buffering = true;
            return true;
        }
        
        private boolean isTagOpen(char c) {
            return c == '<' || c == '{';
        }
        
        private boolean cancelBuffering(char[] chars, int len) {
            for (int i = 0; i < len; i++) {
                if (!isTagContent(chars[i])) {
                    cacheToken(chars, len);
                    replayCachedToken();
                    clearBuffer();
                    doReplay = true;
                    return true;
                }
            }
            return false;
        }
        
        private boolean isTagContent(char c) {
            return c == '/' || Character.isLetterOrDigit(c);
        }
        
        private void replayCachedToken() {
            CachedToken t = stack.poll();
            termAtt.copyBuffer(t.chars, 0, t.chars.length);
            termAtt.setLength(t.chars.length);
            offsetAtt.setOffset(t.startOffset, t.startOffset + t.chars.length);
        }
        
        private boolean finishBuffering(char[] chars, int len) {
            if (len > 2 || !isTagClose(chars[len - 1])) {
                return false;
            }
            buffer.append(chars, 0, len);
            return finishToken();
        }
        
        private boolean isTagClose(char c) {
            char open = buffer.charAt(0);
            return (open == '<' && c == '>')
                    || (open == '{' && c == '}');
        }
        
        private boolean finishToken() {
            if (buffer.length() == 0) {
                return false;
            }
            String token = buffer.toString();
            termAtt.copyBuffer(token.toCharArray(), 0, token.length());
            termAtt.setLength(token.length());
            offsetAtt.setOffset(startOffset, offsetAtt.endOffset());
            clearBuffer();
            stack.clear();
            return true;
        }
        
        private void clearBuffer() {
            buffer = new StringBuilder(BUFFER_INITIAL_SIZE);
            buffering = false;
        }
        
        private void cacheToken(char[] chars, int len) {
            stack.add(new CachedToken(Arrays.copyOf(chars, len), offsetAtt.startOffset()));
        }
        
        private static class CachedToken {
            public final char[] chars;
            public final int startOffset;
            
            public CachedToken(char[] chars, int startOffset) {
                this.chars = chars;
                this.startOffset = startOffset;
            }
        }
    }
}
