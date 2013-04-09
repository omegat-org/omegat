/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
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

package org.omegat.convert.v20to21.data;

import java.io.Serializable;

/**
 * Options for (X)HTML filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * HTML filter would have the following options ([+] means default on).<br>
 * Add or rewrite encoding declaration in HTML and XHTML files:
 * <ul>
 * <li>[] Always
 * <li>[+] Only if HTML file has a header
 * <li>[] Only if HTML file has an encoding declaration
 * <li>[] Never
 * </ul>
 * Translatable attributes:
 * <ul>
 * <li>[+] href
 * <li>[+] src
 * <li>[+] lang
 * <li>[+] hreflang
 * <li>[+] value
 * <li>[+] value (of buttons)
 * </ul>
 * Start a new paragraph on breaks (&lt;br&gt;) []<br>
 * Skip text matching regExp []<br>
 * Skip content of meta-tag when any of the given attibutename-value pairs is
 * present in the tag
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Martin Fleurke
 */
public class HTMLOptions implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /** (X)HTML filter should always add/rewrite encoding declaration. */
    public static final int REWRITE_ALWAYS = 1;
    /**
     * Default. (X)HTML filter should rewrite encoding declaration if HTML file
     * has a header.
     */
    public static final int REWRITE_IFHEADER = 2;
    /**
     * (X)HTML filter should rewrite encoding declaration meta-tag if HTML file
     * has one.
     */
    public static final int REWRITE_IFMETA = 3;
    /** (X)HTML filter should never rewrite encoding declaration. */
    public static final int REWRITE_NEVER = 4;

    /** Holds value of property. */
    private int rewriteEncoding = REWRITE_IFHEADER;

    /**
     * Returns whether and when (X)HTML filter adds/rewrites encoding
     * declaration.
     * 
     * @return One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER},
     *         {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public int getRewriteEncoding() {
        return this.rewriteEncoding;
    }

    /**
     * Sets when (X)HTML filter should add/rewrite encoding declaration.
     * 
     * @param rewriteEncoding
     *            One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER},
     *            {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public void setRewriteEncoding(int rewriteEncoding) {
        this.rewriteEncoding = rewriteEncoding;
    }

    /** Hold value of properties. */
    private boolean translateHref = true;
    private boolean translateSrc = true;
    private boolean translateLang = true;
    private boolean translateHreflang = true;
    private boolean translateValue = true;
    private boolean translateButtonValue = true;
    private boolean paragraphOnBr = false;

    private String skipRegExp = "";
    private String skipMeta = "http-equiv=refresh," + "name=robots," + "name=revisit-after,"
            + "http-equiv=expires," + "http-equiv=content-style-type," + "http-equiv=content-script-type";

    /**
     * Returns whether href attributes should be translated.
     */
    public boolean getTranslateHref() {
        return this.translateHref;
    }

    /**
     * Sets whether href attributes should be translated.
     */
    public void setTranslateHref(boolean translateHref) {
        this.translateHref = translateHref;
    }

    /**
     * Returns whether src attributes should be translated.
     */
    public boolean getTranslateSrc() {
        return this.translateSrc;
    }

    /**
     * Sets whether src attributes should be translated.
     */
    public void setTranslateSrc(boolean translateSrc) {
        this.translateSrc = translateSrc;
    }

    /**
     * Returns whether lang attributes should be translated.
     */
    public boolean getTranslateLang() {
        return this.translateLang;
    }

    /**
     * Sets whether lang attributes should be translated.
     */
    public void setTranslateLang(boolean translateLang) {
        this.translateLang = translateLang;
    }

    /**
     * Returns whether hreflang attributes should be translated.
     */
    public boolean getTranslateHreflang() {
        return this.translateHreflang;
    }

    /**
     * Sets whether hreflang attributes should be translated.
     */
    public void setTranslateHreflang(boolean translateHreflang) {
        this.translateHreflang = translateHreflang;
    }

    /**
     * Sets whether value attributes should be translated.
     */
    public void setTranslateValue(boolean translateValue) {
        this.translateValue = translateValue;
    }

    /**
     * Returns whether value attributes should be translated.
     */
    public boolean getTranslateValue() {
        return this.translateValue;
    }

    /**
     * Sets whether button value attributes should be translated.
     */
    public void setTranslateButtonValue(boolean translateButtonValue) {
        this.translateButtonValue = translateButtonValue;
    }

    /**
     * Returns whether button value attributes should be translated.
     */
    public boolean getTranslateButtonValue() {
        return this.translateButtonValue;
    }

    /**
     * Returns whether a new paragraph should be started on BR.
     */
    public boolean getParagraphOnBr() {
        return this.paragraphOnBr;
    }

    /**
     * Sets whether a new paragraph should be started on BR.
     */
    public void setParagraphOnBr(boolean paragraphOnBr) {
        this.paragraphOnBr = paragraphOnBr;
    }

    /**
     * Returns the regular expression that matches text not to be translated
     */
    public String getSkipRegExp() {
        return this.skipRegExp;
    }

    /**
     * Sets the regular expression that matches text not to be translated
     */
    public void setSkipRegExp(String skipRegExp) {
        this.skipRegExp = skipRegExp;
    }

    /**
     * Returns the meta-tag attribute key-value pairs of which meta-tags should
     * not be translated
     */
    public String getSkipMeta() {
        return this.skipMeta;
    }

    /**
     * Sets the meta-tag attribute key-value pairs of which meta-tags should not
     * be translated
     */
    public void setSkipMeta(String skipMeta) {
        this.skipMeta = skipMeta;
    }

}
