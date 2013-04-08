/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.tokenizer;

import java.io.IOException;
import java.io.StringReader;

import net.moraleboost.io.BasicCodePointReader;
import net.moraleboost.io.CodePointReader;
import net.moraleboost.tinysegmenter.TinySegmenter;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

/**
 * A tokenizer based on <a href="http://chasen.org/~taku/software/TinySegmenter/">TinySegmenter by Taku Kudo</a>.
 * 
 * This implementation uses the <a href="http://code.google.com/p/cmecab-java/source/browse/trunk/src/net/moraleboost/tinysegmenter/">
 * Java port by Kohei Taketa</a>.
 * 
 * For stopword filtering we borrow the facilities provided
 * by {@link CJKAnalyzer} and {@link StopFilter}.
 * 
 * @author Aaron Madlon-Kay
 *
 */
@Tokenizer(languages = { "ja" })
public class TinySegmenterJapaneseTokenizer extends BaseTokenizer {

    public TinySegmenterJapaneseTokenizer() {
        super();
        shouldDelegateTokenizeExactly = false;
    }

    @Override
    protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed,
            boolean stopWordsAllowed) {
        
        TokenStream ts = new TokenStreamWrapper(new BasicCodePointReader(new StringReader(strOrig)));
        
        if (stemsAllowed) {
            String[] stopWords = stopWordsAllowed ? CJKAnalyzer.STOP_WORDS
                    : EMPTY_STOP_WORDS_LIST;
            return new StopFilter(Version.LUCENE_36, ts, StopFilter.makeStopSet(stopWords));
        }
        
        return ts;
    }

    /**
     * Wrap a {@link TinySegmenter} to behave like a {@link TokenStream}.
     */
    public class TokenStreamWrapper extends TokenStream {
        private TinySegmenter ts;
        private CharTermAttribute termAttr;
        private OffsetAttribute offAttr;
        
        public TokenStreamWrapper(CodePointReader reader) {
            ts = new TinySegmenter(reader);
            termAttr = addAttribute(CharTermAttribute.class);
            offAttr = addAttribute(OffsetAttribute.class);
        }
        
        public boolean incrementToken() throws IOException {
            TinySegmenter.Token token = ts.next();
            if (token == null) return false;
            termAttr.setEmpty();
            termAttr.append(token.str);
            offAttr.setOffset((int) token.start, (int) token.end);
            return true;
        }
    }
}
