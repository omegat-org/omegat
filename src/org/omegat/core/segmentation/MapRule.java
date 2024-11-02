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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import tokyo.northside.logging.ILogger;

import org.omegat.util.Log;
import org.omegat.util.StringUtil;

/**
 * A class representing the language rules and their mapping to the segmentation
 * rules for each particular language.
 *
 * @author Maxym Mykhalchuk
 */
public class MapRule implements Serializable {

    private static final long serialVersionUID = -5868132953113679291L;
    private static final ILogger LOGGER = Log.getLogger(MapRule.class);

    /** Language Name */
    private String languageCode;

    /**
     * Creates a new empty MapRule.
     * <p>
     * When SRX.loadSrxFile loads segmentation.conf, java.beans.XMLDecoder
     * create an empty object, then calls setLanguage and setPattern methods.
     * </p>
     */
    public MapRule() {
    }

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
        this.setLanguage(code != null ? code : language);
        this.setPattern(pattern);
        this.setRules(rules);
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
        String res = LanguageCodes.getLanguageName(languageCode);
        return StringUtil.isEmpty(res) ? languageCode : res;
    }

    /** Sets Language Code */
    public void setLanguage(String code) {
        /*
         * setLanguage method is called from XmlDecoder of a Java beans library
         * when migrating from "segmentation.conf" beans file. An argument will
         * be localized name of language. When the object is created from a
         * standard SRX file, the argument will be standard language name,
         * defined as "LanguageCodes.*_CODE". The behavior was changed in OmegaT
         * 6.0.0 release in 2023. We first detect whether the argument is
         * standard code. If the code is not a standard code, then try to find a
         * localized name of the language name. When you believe all the OmegaT
         * 4.x and 5.x users are migrated to OmegaT 6.x or later, you may want
         * to remove the workaround here.
         */
        if (!LanguageCodes.isLanguageCodeKnown(code)) {
            String alt = LanguageCodes.getLanguageCodeByName(code);
            if (alt != null) {
                languageCode = alt;
                return;
            } else {
                LOGGER.atDebug().setMessage("Unknown languagerulename '{}'").addArgument(code).log();
            }
        }
        languageCode = code;
    }

    /**
     * Returns Language Code for programmatic usage.
     */
    public String getLanguage() {
        return languageCode;
    }

    /*
     * Pattern for the language/country ISO code (of a form LL-CC). It is like
     * "EN.*".
     */
    private Pattern pattern;

    /**
     * Returns Pattern for the language/country ISO code (of a form LL-CC).
     */
    public String getPattern() {
        if (pattern != null) {
            return pattern.pattern();
        } else {
            return null;
        }
    }

    /**
     * Returns Compiled Pattern for the language/country ISO code (of a form
     * LL-CC).
     */
    public Pattern getCompiledPattern() {
        return pattern;
    }

    /**
     * Sets Pattern for the language/country ISO code (of a form LL-CC).
     * 
     * @param pattern
     *            pattern string such as "EN.*"
     */
    public void setPattern(String pattern) throws PatternSyntaxException {
        // Fix for bug [1643500]
        // language code in segmentation rule is a case-sensitive
        // Correction contributed by Tiago Saboga.
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Deep copy of the object, mandatory for java beans.
     * 
     * @return new MapRule object
     */
    public MapRule copy() {
        MapRule result = new MapRule();
        result.languageCode = languageCode;
        result.pattern = pattern;
        result.rules = new ArrayList<Rule>(rules.size());
        for (Rule rule : rules) {
            result.rules.add(rule.copy());
        }
        return result;
    }

    /** List of rules (of class {@link Rule}) for the language */
    private List<Rule> rules;

    /** Returns List of rules (of class {@link Rule}) for the language */
    public List<Rule> getRules() {
        return rules;
    }

    /** Sets List of rules (of class {@link Rule}) for the language */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Indicates whether some other MapRule is "equal to" this one.
     */
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
    public int hashCode() {
        return this.getPattern().hashCode() + this.getLanguage().hashCode() + this.getRules().hashCode();
    }

    /**
     * Returns a string representation of the MapRule for debugging purposes.
     */
    public String toString() {
        return getLanguage() + " (" + getPattern() + ") " + getRules().toString();
    }
}
