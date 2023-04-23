/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013, 2015 Aaron Madlon-Kay
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
package org.omegat.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
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

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed)
            throws IOException {
        if (stemsAllowed) {
            // Blank out tags when stemming only
            strOrig = blankOutTags(strOrig);
            CharArraySet stopWords = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            Set<String> stopTags = stopWordsAllowed ? JapaneseAnalyzer.getDefaultStopTags() : Collections.emptySet();
            return new JapaneseAnalyzer(null, Mode.SEARCH, stopWords, stopTags).tokenStream("",
                    new StringReader(strOrig));
        } else {
            JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, Mode.NORMAL);
            tokenizer.setReader(new StringReader(strOrig));
            return new TagJoiningFilter(tokenizer);
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
                buffer.setCharAt(i, ' ');
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

        private final ArrayDeque<CachedToken> inputStack = new ArrayDeque<CachedToken>();
        private final ArrayDeque<CachedToken> outputStack = new ArrayDeque<CachedToken>();
        private final ArrayDeque<CachedToken> recoveryStack = new ArrayDeque<CachedToken>();

        protected TagJoiningFilter(TokenStream input) {
            super(input);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (!outputStack.isEmpty()) {
                replayToken(outputStack.poll());
                return true;
            }
            while (getNextInput()) {
                char[] chars = termAtt.buffer();
                int len = termAtt.length();
                if (buffering) {
                    if (finishBuffering(chars, len)) {
                        return true;
                    }
                    if (cancelBuffering(chars, len)) {
                        return true;
                    }
                    cacheRecoveryToken(chars, len);
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

        private boolean getNextInput() throws IOException {
            if (!inputStack.isEmpty()) {
                replayToken(inputStack.poll());
                return true;
            }
            return input.incrementToken();
        }

        private boolean startBuffering(char[] chars, int len) {
            for (int i = 0; i < len; i++) {
                if (isTagOpen(chars[i])) {
                    if (i > 0) {
                        // return true for content up to start, then replay start
                        cacheInputToken(Arrays.copyOfRange(chars, i, len), offsetAtt.startOffset() + i);
                        truncateToken(i);
                        return false;
                    } else {
                        buffer.append(chars, i, len);
                        startOffset = offsetAtt.startOffset();
                        cacheRecoveryToken(chars, len);
                        buffering = true;
                        return true;
                    }
                }
            }
            return false;
        }

        private void truncateToken(int end) {
            termAtt.setLength(end);
            offsetAtt.setOffset(offsetAtt.startOffset(), offsetAtt.startOffset() + end);
        }

        private boolean isTagOpen(char c) {
            return c == '<' || c == '{';
        }

        private boolean cancelBuffering(char[] chars, int len) {
            for (int i = 0; i < len; i++) {
                if (!isTagContent(chars[i])) {
                    cacheRecoveryToken(chars, len);
                    outputStack.addAll(recoveryStack);
                    recoveryStack.clear();
                    replayToken(outputStack.poll());
                    clearBuffer();
                    return true;
                }
            }
            return false;
        }

        private boolean isTagContent(char c) {
            return c == '/' || Character.isLetterOrDigit(c);
        }

        private void replayToken(CachedToken t) {
            termAtt.copyBuffer(t.chars, 0, t.chars.length);
            termAtt.setLength(t.chars.length);
            offsetAtt.setOffset(t.startOffset, t.startOffset + t.chars.length);
        }

        private boolean finishBuffering(char[] chars, int len) {
            for (int i = 0; i < len; i++) {
                if (isTagClose(chars[i])) {
                    if (i < len - 1) {
                        // replay remainder afterwards
                        cacheInputToken(Arrays.copyOfRange(chars, i + 1, len), offsetAtt.startOffset() + i + 1);
                    }
                    buffer.append(chars, 0, i + 1);
                    return finishToken();
                }
            }
            return false;
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
            offsetAtt.setOffset(startOffset, startOffset + token.length());
            clearBuffer();
            recoveryStack.clear();
            return true;
        }

        private void clearBuffer() {
            buffer = new StringBuilder(BUFFER_INITIAL_SIZE);
            buffering = false;
        }

        private void cacheInputToken(char[] chars, int start) {
            inputStack.add(new CachedToken(chars, start));
        }

        private void cacheRecoveryToken(char[] chars, int len) {
            recoveryStack.add(new CachedToken(Arrays.copyOf(chars, len), offsetAtt.startOffset()));
        }

        private static class CachedToken {
            public final char[] chars;
            public final int startOffset;

            CachedToken(char[] chars, int startOffset) {
                this.chars = chars;
                this.startOffset = startOffset;
            }
        }
    }
}
