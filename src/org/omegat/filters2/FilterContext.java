/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.filters2;

import org.omegat.core.data.ProjectProperties;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * Context for filter calls.
 */
public class FilterContext {

    private final Language sourceLang;

    private final Language targetLang;

    private String inEncoding;

    private String outEncoding;

    private boolean sentenceSegmentingEnabled;

    private boolean isRemoveAllTags;

    private Class<?> sourceTokenizerClass;

    private Class<?> targetTokenizerClass;

    public FilterContext(ProjectProperties props) {
        this.sourceLang = props.getSourceLanguage();
        this.targetLang = props.getTargetLanguage();
        this.sentenceSegmentingEnabled = props.isSentenceSegmentingEnabled();
        this.isRemoveAllTags = props.isRemoveTags();
        this.sourceTokenizerClass = props.getSourceTokenizer();
        this.targetTokenizerClass = props.getTargetTokenizer();
    }

    public FilterContext(Language sourceLang, Language targetLang, boolean sentenceSegmentingEnabled) {
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.sentenceSegmentingEnabled = sentenceSegmentingEnabled;
        this.isRemoveAllTags = false;
    }

    /** Source language of project. */
    public Language getSourceLang() {
        return sourceLang;
    }

    /** Target language of project. */
    public Language getTargetLang() {
        return targetLang;
    }

    /** Source file encoding, but can be 'null'. */
    public String getInEncoding() {
        return inEncoding;
    }

    public FilterContext setInEncoding(String inEncoding) {
        this.inEncoding = inEncoding;
        return this;
    }

    /** Target file encoding, but can be 'null'. */
    public String getOutEncoding() {
        return outEncoding;
    }

    public FilterContext setOutEncoding(String outEncoding) {
        this.outEncoding = outEncoding;
        return this;
    }

    /** Is sentence segmenting enabled. */
    public boolean isSentenceSegmentingEnabled() {
        return sentenceSegmentingEnabled;
    }

    /** Should all tags be removed from segments */
    public boolean isRemoveAllTags() {
        return isRemoveAllTags;
    }

    public FilterContext setRemoveAllTags(boolean isRemoveAllTags) {
        this.isRemoveAllTags = isRemoveAllTags;
        return this;
    }

    public FilterContext setSourceTokenizerClass(Class<?> sourceTokenizerClass) {
        this.sourceTokenizerClass = sourceTokenizerClass;
        return this;
    }

    public ITokenizer getSourceTokenizer() {
        try {
            return (ITokenizer) sourceTokenizerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public FilterContext setTargetTokenizerClass(Class<?> targetTokenizerClass) {
        this.targetTokenizerClass = targetTokenizerClass;
        return this;
    }

    public ITokenizer getTargetTokenizer() {
        try {
            return (ITokenizer) targetTokenizerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
