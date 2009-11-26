/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.html2;

import java.util.Map;

import org.omegat.filters2.AbstractOptions;

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
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class HTMLOptions extends AbstractOptions {

    enum REWRITE_MODE {
        /** (X)HTML filter should always add/rewrite encoding declaration. */
        ALWAYS,
        /**
         * Default. (X)HTML filter should rewrite encoding declaration if HTML
         * file has a header.
         */
        IFHEADER,
        /**
         * (X)HTML filter should rewrite encoding declaration meta-tag if HTML
         * file has one.
         */
        IFMETA,
        /** (X)HTML filter should never rewrite encoding declaration. */
        NEVER
    };

    public static final String OPTION_REWRITE_ENCODING = "rewriteEncoding";
    public static final String OPTION_TRANSLATE_HREF = "translateHref";
    public static final String OPTION_TRANSLATE_SRC = "translateSrc";
    public static final String OPTION_TRANSLATE_LANG = "translateLang";
    public static final String OPTION_TRANSLATE_HREFLANG = "translateHreflang";
    public static final String OPTION_TRANSLATE_VALUE = "translateValue";
    public static final String OPTION_TRANSLATE_BUTTONVALUE = "translateButtonValue";
    public static final String OPTION_PARAGRAPH_ONBR = "paragraphOnBr";
    public static final String OPTION_SKIP_REGEXP = "skipRegExp";
    public static final String OPTION_SKIP_META = "skipMeta";

    public HTMLOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * Returns whether and when (X)HTML filter adds/rewrites encoding
     * declaration.
     * 
     * @return One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER},
     *         {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public REWRITE_MODE getRewriteEncoding() {
        return getEnum(REWRITE_MODE.class, OPTION_REWRITE_ENCODING,
                REWRITE_MODE.IFHEADER);
    }

    /**
     * Sets when (X)HTML filter should add/rewrite encoding declaration.
     * 
     * @param rewriteEncoding
     *            One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER},
     *            {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public void setRewriteEncoding(REWRITE_MODE rewriteEncoding) {
        setEnum(OPTION_REWRITE_ENCODING, rewriteEncoding);
    }

    /**
     * Returns whether href attributes should be translated.
     */
    public boolean getTranslateHref() {
        return getBoolean(OPTION_TRANSLATE_HREF, true);
    }

    /**
     * Sets whether href attributes should be translated.
     */
    public void setTranslateHref(boolean translateHref) {
        setBoolean(OPTION_TRANSLATE_HREF, translateHref);
    }

    /**
     * Returns whether src attributes should be translated.
     */
    public boolean getTranslateSrc() {
        return getBoolean(OPTION_TRANSLATE_SRC, true);
    }

    /**
     * Sets whether src attributes should be translated.
     */
    public void setTranslateSrc(boolean translateSrc) {
        setBoolean(OPTION_TRANSLATE_SRC, translateSrc);
    }

    /**
     * Returns whether lang attributes should be translated.
     */
    public boolean getTranslateLang() {
        return getBoolean(OPTION_TRANSLATE_LANG, true);
    }

    /**
     * Sets whether lang attributes should be translated.
     */
    public void setTranslateLang(boolean translateLang) {
        setBoolean(OPTION_TRANSLATE_LANG, translateLang);
    }

    /**
     * Returns whether hreflang attributes should be translated.
     */
    public boolean getTranslateHreflang() {
        return getBoolean(OPTION_TRANSLATE_HREFLANG, true);
    }

    /**
     * Sets whether hreflang attributes should be translated.
     */
    public void setTranslateHreflang(boolean translateHreflang) {
        setBoolean(OPTION_TRANSLATE_HREFLANG, translateHreflang);
    }

    /**
     * Sets whether value attributes should be translated.
     */
    public void setTranslateValue(boolean translateValue) {
        setBoolean(OPTION_TRANSLATE_VALUE, translateValue);
    }

    /**
     * Returns whether value attributes should be translated.
     */
    public boolean getTranslateValue() {
        return getBoolean(OPTION_TRANSLATE_VALUE, true);
    }

    /**
     * Sets whether button value attributes should be translated.
     */
    public void setTranslateButtonValue(boolean translateButtonValue) {
        setBoolean(OPTION_TRANSLATE_BUTTONVALUE, translateButtonValue);
    }

    /**
     * Returns whether button value attributes should be translated.
     */
    public boolean getTranslateButtonValue() {
        return getBoolean(OPTION_TRANSLATE_BUTTONVALUE, true);
    }

    /**
     * Returns whether a new paragraph should be started on BR.
     */
    public boolean getParagraphOnBr() {
        return getBoolean(OPTION_PARAGRAPH_ONBR, false);
    }

    /**
     * Sets whether a new paragraph should be started on BR.
     */
    public void setParagraphOnBr(boolean paragraphOnBr) {
        setBoolean(OPTION_PARAGRAPH_ONBR, paragraphOnBr);
    }

    /**
     * Returns the regular expression that matches text not to be translated
     */
    public String getSkipRegExp() {
        return getString(OPTION_SKIP_REGEXP, "");
    }

    /**
     * Sets the regular expression that matches text not to be translated
     */
    public void setSkipRegExp(String skipRegExp) {
        setString(OPTION_SKIP_REGEXP, skipRegExp);
    }

    /**
     * Returns the meta-tag attribute key-value pairs of which meta-tags should
     * not be translated
     */
    public String getSkipMeta() {
        return getString(OPTION_SKIP_META, "http-equiv=refresh,"
                + "name=robots," + "name=revisit-after,"
                + "http-equiv=expires," + "http-equiv=content-style-type,"
                + "http-equiv=content-script-type");
    }

    /**
     * Sets the meta-tag attribute key-value pairs of which meta-tags should not
     * be translated
     */
    public void setSkipMeta(String skipMeta) {
        setString(OPTION_SKIP_META, skipMeta);
    }
}
