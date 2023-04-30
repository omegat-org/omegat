/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik (alex73mail@gmail.com)
               2013, 2015 Aaron Madlon-Kay
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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.HMMChineseTokenizer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.omegat.util.Token;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "zh" }, isDefault = true)
public class LuceneSmartChineseTokenizer extends BaseTokenizer {

    /*
     * The SmartChineseAnalyzer/HMMChineseTokenizer can't be used in verbatim
     * scenarios because it replaces all punctuation with `,`. See
     * https://sourceforge.net/p/omegat/feature-requests/1602/#24f1/daa9/a82f/6568
     *
     * However the default tokenizeVerbatim{,ToStrings} implementation will only
     * break at script-change boundaries, which is much too coarse for most
     * uses. Hence we tokenize by code point.
     */
    @Override
    public Token[] tokenizeVerbatim(String strOrig) {
        return tokenizeByCodePoint(strOrig);
    }

    @Override
    public String[] tokenizeVerbatimToStrings(String strOrig) {
        return tokenizeByCodePointToStrings(strOrig);
    }

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(stopWordsAllowed);
            return analyzer.tokenStream("", new StringReader(strOrig));
        } else {
            HMMChineseTokenizer tokenizer = new HMMChineseTokenizer();
            tokenizer.setReader(new StringReader(strOrig));
            return tokenizer;
        }
    }
}
