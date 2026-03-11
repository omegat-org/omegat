/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2024 Hiroshi Miura
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

package org.omegat.core.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import gen.core.segmentation.Languagemap;

import org.omegat.util.StringUtil;

/**
 * A class representing the language rules and their mapping to the segmentation
 * rules for each particular language.
 *
 * @author Maxym Mykhalchuk
 */
public class MapRule {

    /** Language Name */
    private final String languageCode;

    /**
     * Create initialized MapRule object.
     * 
     * @param language
     *            localized language name (from segmentation.conf), or language
     *            code (from SRX)
     * @param pattern
     *            language pattern such as "EN.*" or ".*"
     * @param rules
     *            segmentation rules.
     */
    public MapRule(String language, String pattern, List<Rule> rules) {
        String code = LanguageCodes.getLanguageCodeByPattern(pattern);
        this.languageCode = LanguageCodes.getLanguageCode(code != null ? code : language);
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.rules = rules;
    }

    /**
     * Create initialized MapRule object from segmentation.srx.
     *
     * @param languagemap
     *            language map from segmentation.srx.
     * @param rules
     *            segmentation rules.
     */
    public MapRule(Languagemap languagemap, List<Rule> rules) {
        this(languagemap.getLanguagerulename(), languagemap.getLanguagepattern(), rules);
    }

    /** Returns Language Name (to display it in a dialog). */
    public String getLanguageName() {
        /*
         * When there has already migrated a SRX file store, languageCode fields
         * has a name defined as "LanguageCodes.*_CODE". Otherwise, MapRule
         * object is created from "segmentation.conf" java beans file, so it is
         * localized name of language. We first assume the latter. If res is
         * empty, the object is created from a SRX file, then return
         * languageCode itself.
         */
        String res = LanguageCodes.getLanguageName(getLanguage());
        return StringUtil.isEmpty(res) ? languageCode : res;
    }

    /**
     * Returns Language Code for programmatic usage.
     */
    public String getLanguage() {
        if (pattern != null) {
            String code = LanguageCodes.getLanguageCodeByPattern(pattern.pattern());
            if (code != null) {
                return code;
            }
        }
        return languageCode;
    }

    /*
     * Pattern for the language/country ISO code (of a form LL-CC). It is like
     * "EN.*".
     */
    private final Pattern pattern;

    /**
     * Returns Pattern for the language/country ISO code (of a form LL-CC).
     */
    public String getPattern() {
        return pattern.pattern();
    }

    /**
     * Returns Compiled Pattern for the language/country ISO code (of a form
     * LL-CC).
     */
    public Pattern getCompiledPattern() {
        return pattern;
    }

    /**
     * Copy of the object.
     * 
     * @return new MapRule object
     */
    public MapRule copy() {
        return new MapRule(languageCode, pattern.pattern(), new ArrayList<>(rules));
    }

    /** List of rules (of class {@link Rule}) for the language */
    private final List<Rule> rules;

    /** Returns List of rules (of class {@link Rule}) for the language */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Indicates whether some other MapRule is "equal to" this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MapRule)) {
            return false;
        }
        MapRule that = (MapRule) obj;
        return this.getPattern().equals(that.getPattern()) && this.getLanguage().equals(that.getLanguage())
                && this.getRules().equals(that.getRules());
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return this.getPattern().hashCode() + this.getLanguage().hashCode() + this.getRules().hashCode();
    }

    /**
     * Returns a string representation of the MapRule for debugging purposes.
     */
    @Override
    public String toString() {
        return getLanguage() + " (" + getPattern() + ") " + getRules().toString();
    }
}
