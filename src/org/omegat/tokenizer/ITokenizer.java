/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2013 Aaron Madlon-Kay
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

import java.util.Map;

import org.apache.lucene.util.Version;
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

    /** CLI parameter to specify source tokenizer */
    static final String CLI_PARAM_SOURCE = "ITokenizer";
    /** CLI parameter to specify target tokenizer */
    static final String CLI_PARAM_TARGET = "ITokenizerTarget";
    /** CLI parameter to specify source tokenizer behavior mode */
    static final String CLI_PARAM_SOURCE_BEHAVIOR = "ITokenizerBehavior";
    /** CLI parameter to specify target tokenizer behavior mode */
    static final String CLI_PARAM_TARGET_BEHAVIOR = "ITokenizerTargetBehavior";
    
    /**
     * Obtain a map indicating the Lucene {@link Version}s supported by this tokenizer.
     * @return A version-description map
     */
    public Map<Version, String> getSupportedBehaviors();

    /**
     * Obtain the actual Lucene {@link Version} to use for this tokenizer.
     * @return Preferred version
     */
    public Version getBehavior();
    /**
     * Set the actual Lucene {@link Version} to use for this tokenizer.
     * @param behavior Preferred version
     */
    public void setBehavior(Version behavior);
    /**
     * Obtain the default Lucene {@link Version} to use with this tokenizer.
     * @return Default versions
     */
    public Version getDefaultBehavior();

    /**
     * Breaks a string into word-only tokens. Numbers, tags, and other non-word
     * tokens are NOT included in the result. Stemming can be used if possible,
     * depends of StemmingMode.
     * 
     * This method used to find 'fizzy matches' entries and to find glossary
     * entries.
     * 
     * Results can be cached for better performance.
     */
    Token[] tokenizeWords(String str, StemmingMode stemmingMode);

    /**
     * Breaks a string into word-only tokens. Numbers, tags, and other non-word
     * tokens are NOT included in the result. Stemming must NOT be used.
     * 
     * This method used to tokenize string to check spelling and to switch case.
     * 
     * There is no sense to cache results.
     */
    Token[] tokenizeWordsForSpelling(String str);

    /**
     * Breaks a string into tokens. Numbers, tags, and other non-word tokens are
     * included in the result. Stemming must NOT be used.
     * 
     * This method used to mark string differences on UI and to tune similarity.
     * 
     * There is no sense to cache results.
     */
    Token[] tokenizeAllExactly(String str);
}
