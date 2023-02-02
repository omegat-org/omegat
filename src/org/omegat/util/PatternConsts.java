/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel, Zoltan Bartko
               2008 Martin Fleurke
               2009 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2012 Martin Fleurke
               2015 Didier Briel
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

package org.omegat.util;

import java.util.regex.Pattern;

/**
 * Constant patterns, used in different other classes.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Martin Fleurke
 */
public final class PatternConsts {

    private PatternConsts() {
    }

    private static final String RE_OMEGAT_TAG = "<\\/?[a-zA-Z]+[0-9]+\\/?>";
    private static final String RE_PRINTF_VARS = "%([1-9]+\\$)?([+-])?('.)?(-)?([0-9]*)(\\.[0-9]*)?[bcdeEfFgGinopsuxX%]";
    private static final String RE_SIMPLE_PRINTF_VARS = "%([1-9]+\\$)?([0-9]*)(\\.[0-9]*)?[bcdeEfFgGinopsuxX%]";
    private static final String RE_SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS = "\\{([0-9])+\\}";

    /** Tag Validation Option: check user defined tags according to regexp.*/
    public static final String CHECK_CUSTOM_PATTERN_DEFAULT = "\\d+";

    /**
     * Compiled pattern to extract the encoding from XML file, if any. Found
     * encoding is stored in group #1.
     */
    public static final Pattern XML_ENCODING = Pattern
            .compile("<\\?xml.*?encoding\\s*=\\s*\"(\\S+?)\".*?\\?>");

    /** compiled pattern to match XML header */
    public static final Pattern XML_HEADER = Pattern.compile("(<\\?xml.*?\\?>)");

    /**
     * Compiled pattern to extract the DOCTYPE declaration from XML file, if
     * any. Groups: <br>
     * #1 - DOCTYPE name <br>
     * #3 - PUBLIC DOCTYPE URL <br>
     * #5 - SYSTEM DOCTYPE URL
     */
    public static final Pattern XML_DOCTYPE = Pattern
            .compile("<\\!DOCTYPE\\s+(\\w+)\\s+(PUBLIC\\s+\"(-//.*)\"\\s+)?"); // (SYSTEM\\s+)?\"(.*?)\"\\s+>");

    /**
     * Compiled pattern to extract the root tag from XML file, if any. Group #1
     * should contain the root tag.
     */
    public static final Pattern XML_ROOTTAG = Pattern.compile("<(\\w+)");

    /**
     * Compiled pattern to extract the xlmns declaration from an XML file, if
     * any. Group #2 should contain the xmlns declaration. E.g.,
     * http://www.w3.org/2001/XMLSchema-instance
     */
    public static final Pattern XML_XMLNS = Pattern.compile("xmlns(:\\w+)?=\"(.*?)\"");

    /** compiled pattern to extract the encoding from HTML file, if any */
    public static final Pattern HTML_ENCODING = Pattern.compile(
            "<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"'].*?/?\\s*>",
            Pattern.CASE_INSENSITIVE);

    /** compiled pattern to extract the encoding from HTML5 file, if any */
    public static final Pattern HTML5_ENCODING = Pattern.compile(
            "<meta.*?charset\\s*=\\s*[\"'](\\S+?)[\"'].*?/?\\s*>",
            Pattern.CASE_INSENSITIVE);

    /** Compiled pattern to look for HTML file HEAD declaration.
     Using [^e] instead of . prevents confusing &lt;head&gt; with &lt;header&gt; from HTML 5*/
    public static final Pattern HTML_HEAD = Pattern.compile("<head[^e]*?>", Pattern.CASE_INSENSITIVE);

    /** compiled pattern to look for HTML file HTML declaration */
    public static final Pattern HTML_HTML = Pattern.compile("<html.*?>", Pattern.CASE_INSENSITIVE);

    /** Pattern for detecting html &lt;BR&gt; tags */
    public static final Pattern HTML_BR = Pattern.compile("<BR>", Pattern.CASE_INSENSITIVE);

    /**
     * Pattern that matches full string containing in full and only
     * omegat-specific tag (without leading &lt; and trailing &gt;).
     */
    public static final Pattern OMEGAT_TAG_ONLY = Pattern.compile("^\\/?[a-zA-Z]+[0-9]+\\/?$");

    /**
     * Pattern that matches omegat-specific tags (with leading &lt; and trailing
     * &gt; in any place of a string).
     */
    public static final Pattern OMEGAT_TAG = Pattern.compile(RE_OMEGAT_TAG);

    /**
     * Pattern that matches omegat-specific tags (with leading &lt; and trailing
     * &gt; in any place of a string) plus a space after it.
     */
    public static final Pattern OMEGAT_TAG_SPACE = Pattern.compile("<\\/?[a-zA-Z]+[0-9]+\\/?>\\s");

    /**
     * Pattern that matches omegat-specific tags (with leading &lt; and trailing
     * &gt; in any place of a string) with a space before it.
     */
    public static final Pattern SPACE_OMEGAT_TAG = Pattern.compile("\\s<\\/?[a-zA-Z]+[0-9]+\\/?>");

    /**
     * Pattern that matches omegat-specific tags (with leading &lt; and trailing
     * &gt; in any place of a string) and decompiles them into pieces:
     * <ol>
     * <li>leading /, if any
     * <li>tag shortcut
     * <li>tag number
     * <li>trailing /, if any
     * </ol>
     * Call <code>matcher.group(n)</code> to get each piece.
     */
    public static final Pattern OMEGAT_TAG_DECOMPILE = Pattern.compile("<(\\/?)([a-zA-Z]+)([0-9]+)(\\/?)>");

    /**
     * Pattern that matches paired tag in protected parts
     * <ol>
     * <li>opening tag
     * <li>text between tags
     * <li>closing tag
     * </ol>
     * Call <code>matcher.group(n)</code> to get each piece.
     */
    public static final Pattern PROTECTED_PARTS_PAIRED_TAG_DECOMPILE = Pattern.compile("^(<.+?>)([^<]+?)(<\\/.+?>)");

    /**
     * Pattern that matches an equiv-text attribute as used in XLIFF inline codes.
     */
    public static final Pattern EQUIV_TEXT_ATTRIBUTE_DECOMPILE = Pattern.compile("equiv-text=\"([^\"]+)\"");

    /** Pattern that detects space-only regular expressions. */
    public static final Pattern SPACY_REGEX = Pattern.compile("((\\s|\\\\n|\\\\t|\\\\s)(\\+|\\*)?)+");

    /** Pattern that detects language and country, with an optionnal script in the middle. */
    public static final Pattern LANG_AND_COUNTRY = Pattern
            .compile("([A-Za-z]{1,8})(?:(?:-|_)(?:[A-Za-z]{4}(?:-|_))?([A-Za-z0-9]{1,8}))?");

    /** Pattern for detecting remote dictionary file archives */
    public static final Pattern DICTIONARY_ZIP = Pattern.compile(
    // "\"([a-z]{1,8})(_([A-Z]{1,8})?)?\\.zip\"");
            // Hardcoded pattern to get the French dictionary
            // (fr_FR_1-3-2.zip) in addition to the others
            // The initial pattern is above.
            // [ 2138846 ] French dictionary cannot be downloaded and installed
            "\"([a-z]{1,8})(_([A-Z]{1,8}))?(_1-3-2)?\\.zip\"");

    public static final Pattern SPACE_TAB = Pattern.compile("( |\t)+");

    /** Pattern for regular expression variable : $n, where n is a number, but should not be preceded by backslash */
    public static final Pattern REGEX_VARIABLE = Pattern.compile("(?<!\\\\)((?:\\\\\\\\)*)\\$(\\d+)");

    /** compiled pattern to match line ending win/mac/linux */
    public static final Pattern LINE_ENDING = Pattern.compile("\r?\n|\r[^\n]");

    /**
     * Pattern for detecting the placeholders in a printf-function string which
     * can occur in languages like php, C and others. placeholder ::= "%"
     * [ARGUMENTSWAPSPECIFIER] [SIGNSPECIFIER] [PADDINGSPECIFIER]
     * [ALIGNMENTSPECIFIER] [WIDTHSPECIFIER] [PRECISIONSPECIFIER] TYPESPECIFIER
     * NUMBER ::= { "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" }
     * ARGUMENTSWAPSPECIFIER = NUMBER "$" SIGNSPECIFIER ::= "+" | "-"
     * PADDINGSPECIFIER ::= " " | "0" | "'" CHARACTER ALIGNMENTSPECIFIER ::= ""
     * | "-" WIDTHSPECIFIER ::= NUMBER PRECISIONSPECIFIER ::= "." NUMBER
     * TYPESPECIFIER ::= "b" | "c" | "d" | "e" | "E" | "f" | "F" | "g" | "G" |
     * "i" | "n" | "o" | "p" | "s" | "u" | "x" | "X" | "%" //c++:
     * [cdieEfgGosuxXpn%] //php: [bcdeufFosxX%] NB: Because having space as
     * paddingspecifier leads to many false matches in regular text, and space
     * being the default padding specifier in php, and being able to have space
     * or 0 as padding specifier by prefixing it with ', and having the padding
     * specifier not being used frequently in most cases, the regular expression
     * only corresponds with quote+paddingspecifier. NB2: The argument swap
     * specifier gives explicit ordering of variables, without it, the ordering
     * is implicit (first in sequence is first in order) Example in code:
     * <code>echo printf(gettext("%s is very %s"), "OmegaT", "great");</code>
     */
    public static final Pattern PRINTF_VARS = Pattern
            .compile(RE_PRINTF_VARS);
    /**
     * Pattern for detecting the placeholders in a printf-function string. It
     * detects only simple placeholders, without SIGN-, PADDING-, ALIGNMENT- and
     * WIDTH specifier.
     *
     * @see #PRINTF_VARS
     */
    public static final Pattern SIMPLE_PRINTF_VARS = Pattern.compile(RE_SIMPLE_PRINTF_VARS);

    public static final Pattern SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS = Pattern
            .compile(RE_SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS);

    /**
     * Pattern for detecting OmegaT-tags and other placeholders (extended sprintf-variant) in texts
     */
    public static final Pattern SIMPLE_PLACEHOLDERS = Pattern.compile(RE_OMEGAT_TAG + "|" + RE_PRINTF_VARS);

    /**
     * combined pattern for all placeholder tags
     */
    private static Pattern placeholders;
    /**
     * pattern for text that should be removed from translation. Can be null!
     */
    private static Pattern remove;
    /**
     * Pattern for text that should be considered a custom tag. Can be null!
     */
    private static Pattern customTags;

    /**
     * Returns the placeholder pattern (OmegaT tags, printf tags, java
     * MessageFomat tags, custom tags, combined according to user configuration)
     *
     * @return the pattern
     * @see #updatePlaceholderPattern()
     */
    public static Pattern getPlaceholderPattern() {
        if (placeholders == null) {
            String regexp = RE_OMEGAT_TAG;
            if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_ALL_PRINTF_TAGS))) {
                regexp += "|" + RE_PRINTF_VARS;
            } else if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS))) {
                regexp += "|" + RE_SIMPLE_PRINTF_VARS;
            }
            if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_JAVA_PATTERN_TAGS))) {
                regexp += "|" + RE_SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS;
            }
            // assume: customRegExp has already been validated.
            String customRegExp = Preferences.getPreferenceDefault(Preferences.CHECK_CUSTOM_PATTERN, CHECK_CUSTOM_PATTERN_DEFAULT);
            if (!"".equalsIgnoreCase(customRegExp)) {
                regexp += "|" + customRegExp;
            }
            placeholders = Pattern.compile(regexp);
        }
        return placeholders;
    }

    /**
     * Resets the placeholder pattern. Use it when the user has changed tagvalidation configuration.
     */
    public static void updatePlaceholderPattern() {
        placeholders = null;
    }

    public static Pattern getRemovePattern() {
        if (remove == null) {
            String removeRegExp = Preferences.getPreference(Preferences.CHECK_REMOVE_PATTERN);
            if (!"".equalsIgnoreCase(removeRegExp)) {
                remove = Pattern.compile(removeRegExp);
            }
        }
        return remove;
    }

    /**
     * Resets the remove pattern. Use it when the user has changed tagvalidation configuration.
     */
    public static void updateRemovePattern() {
        remove = null;
    }

    public static Pattern getCustomTagPattern() {
        if (customTags == null) {
            String customTagsRegex = Preferences.getPreferenceDefault(Preferences.CHECK_CUSTOM_PATTERN, CHECK_CUSTOM_PATTERN_DEFAULT);
            if (!"".equalsIgnoreCase(customTagsRegex)) {
                customTags = Pattern.compile(customTagsRegex);
            }
        }
        return customTags;
    }

    /**
     * Resets the remove pattern. Use it when the user has changed tagvalidation configuration.
     */
    public static void updateCustomTagPattern() {
        customTags = null;
    }
}
