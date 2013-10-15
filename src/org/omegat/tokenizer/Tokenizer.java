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

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.tokenizer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to indicate the languages for which
 * a tokenizer is intended for use.
 * 
 * @author Aaron Madlon-Kay
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Tokenizer {

    /**
     * Value for the {@link #languages()} member that indicates that the
     * supported languages should be determined at runtime via
     * {@link BaseTokenizer#getSupportedLanguages()}.
     */
    public static final String DISCOVER_AT_RUNTIME = "discoverAtRuntime";

    /**
     * The languages supported by the tokenizer.
     * E.g. LuceneCJKTokenizer supports { zh, ja, ko }.
     */
    String[] languages();

    /** 
     * When multiple tokenizers support the same language,
     * this indicates the one that should be preferred above the others.
     */
    boolean isDefault() default false;
}
