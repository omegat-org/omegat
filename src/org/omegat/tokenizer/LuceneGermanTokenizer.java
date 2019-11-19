/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik (alex73mail@gmail.com)
               2013 Aaron Madlon-Kay
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "de" }, isDefault = true)
public class LuceneGermanTokenizer extends BaseTokenizer {

    @SuppressWarnings("resource")
    @Override
    protected TokenStream getTokenStream(final String strOrig, final boolean stemsAllowed,
            final boolean stopWordsAllowed) throws IOException {
        if (stemsAllowed) {
            CharArraySet stopWords = stopWordsAllowed ? GermanAnalyzer.getDefaultStopSet() : CharArraySet.EMPTY_SET;
            return new Lucene30GermanAnalyzer(stopWords).tokenStream("", new StringReader(strOrig));
        } else {
            return getStandardTokenStream(strOrig);
        }
    }

    /**
     * A German analyzer that recreates the behavior of the GermanAnalyzer in
     * Lucene 3.0 and earlier.
     *
     * @see <a href=
     *      "https://groups.yahoo.com/neo/groups/OmegaT/conversations/messages/28395">
     *      User group discussion</a>
     * @see <a href=
     *      "https://github.com/apache/lucene-solr/blob/e8e4245d9b36123446546ff15967ac95429ea2b0/lucene/analysis/common/src/java/org/apache/lucene/analysis/de/GermanAnalyzer.java#L172">
     *      Behavior before version branching was removed</a>
     */
    private static class Lucene30GermanAnalyzer extends Analyzer {
        private final CharArraySet stopWords;

        Lucene30GermanAnalyzer(CharArraySet stopWords) {
            this.stopWords = stopWords;
        }

        @Override
        protected TokenStreamComponents createComponents(String arg0) {
            final org.apache.lucene.analysis.Tokenizer source = new StandardTokenizer();
            TokenStream result = new StandardFilter(source);
            result = new LowerCaseFilter(result);
            result = new StopFilter(result, stopWords);
            result = new SetKeywordMarkerFilter(result, CharArraySet.EMPTY_SET);
            result = new GermanStemFilter(result);
            return new TokenStreamComponents(source, result);
        }

    }
}
