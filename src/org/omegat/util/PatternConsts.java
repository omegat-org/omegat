/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel, Zoltan Bartko
               2008 Martin Fleurke
               2009 Didier Briel
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
public class PatternConsts
{
    /**
     * Compiled pattern to extract the encoding from XML file, if any.
     * Found encoding is stored in group #1.
     */
    public static final Pattern XML_ENCODING = Pattern.compile(
            "<\\?xml.*?encoding\\s*=\\s*\"(\\S+?)\".*?\\?>");                       
    
    /** compiled pattern to match XML header */
    public static final Pattern XML_HEADER = Pattern.compile(
            "(<\\?xml.*?\\?>)");                                                    
    
    /**
     * Compiled pattern to extract the DOCTYPE declaration from XML file, if any.
     * Groups:
     * <br>
     * #1 - DOCTYPE name
     * <br>
     * #3 - PUBLIC DOCTYPE URL
     * <br>
     * #5 - SYSTEM DOCTYPE URL
     */
    public static final Pattern XML_DOCTYPE = Pattern.compile(
            "<\\!DOCTYPE\\s+(\\w+)\\s+(PUBLIC\\s+\"(-//.*)\"\\s+)?");//(SYSTEM\\s+)?\"(.*?)\"\\s+>");                       
    
    /**
     * Compiled pattern to extract the root tag from XML file, if any.
     * Group #1 should contain the root tag.
     */
    public static final Pattern XML_ROOTTAG = Pattern.compile(
            "<(\\w+)");                       
    
    /** compiled pattern to extract the encoding from HTML file, if any */
    public static final Pattern HTML_ENCODING = Pattern.compile(
            "<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"'].*?/?\\s*>",  
            Pattern.CASE_INSENSITIVE);
    
    /** compiled pattern to look for HTML file HEAD declaration */
    public static final Pattern HTML_HEAD = Pattern.compile(
            "<head.*?>",                                                            
            Pattern.CASE_INSENSITIVE);
    
    /** compiled pattern to look for HTML file HTML declaration */
    public static final Pattern HTML_HTML = Pattern.compile(
            "<html.*?>",                                                            
            Pattern.CASE_INSENSITIVE);

    /** Pattern for detecting html &ltBR&gt; tags */
    public static final Pattern HTML_BR = Pattern.compile(
            "<BR>", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern that matches full string containing in full and only
     * omegat-specific tag (without leading &lt; and trailing &gt;).
     */
    public static final Pattern OMEGAT_TAG_ONLY = Pattern.compile(
            "^\\/?[a-zA-Z]+[0-9]+\\/?$");                                       
    
    /**
     * Pattern that matches omegat-specific tags
     * (with leading &lt; and trailing &gt; in any place of a string).
     */
    public static final Pattern OMEGAT_TAG = Pattern.compile(
            "<\\/?[a-zA-Z]+[0-9]+\\/?>");                                       
    
    /**
     * Pattern that matches omegat-specific tags 
     * (with leading &lt; and trailing &gt; in any place of a string)
     * and decompiles them into pieces:
     * <ol>
     * <li>leading /, if any
     * <li>tag shortcut
     * <li>tag number
     * <li>trailing /, if any
     * </ol>
     * Call <code>matcher.group(n)</code> to get each piece.
     */
    public static final Pattern OMEGAT_TAG_DECOMPILE = Pattern.compile(
            "<(\\/?)([a-zA-Z]+)([0-9]+)(\\/?)>");                               
    
    /** 
     * Pattern that matches OmegaT-specific tags in the HTML form
     * i.e, with &amp;lt; instead of &lt; and &amp;gt instead of &gt;.
     * The pattern is also a capturing group.
    */
    public static final Pattern OMEGAT_HTML_TAG = Pattern.compile(              
            "(&lt;\\/?[a-zA-Z]+[0-9]+\\/?&gt;)");  
    
    /** Pattern that detects space-only regular expressions. */
    public static final Pattern SPACY_REGEX = Pattern.compile(
            "((\\s|\\\\n|\\\\t|\\\\s)(\\+|\\*)?)+");                            
    
    /** Pattern that detects language and country. */
    public static final Pattern LANG_AND_COUNTRY = Pattern.compile(
            "([A-Za-z]{1,8})(?:(?:-|_)([A-Za-z0-9]{1,8}))?");                   
    
    /** Pattern for detecting remote dictionary file archives */
    public static final Pattern DICTIONARY_ZIP = Pattern.compile(
//          "\"([a-z]{1,8})(_([A-Z]{1,8})?)?\\.zip\"");
            // Hardcoded pattern to get the French dictionary
            // (fr_FR_1-3-2.zip) in addition to the others
            // The initial pattern is above.
            // [ 2138846 ] French dictionary cannot be downloaded and installed
            "\"([a-z]{1,8})(_([A-Z]{1,8})?)(_1-3-2)?\\.zip\"");
    
    /** Pattern for detecting the placeholders in a printf-function string
     *  which can occur in languages like php, C and others. 
     *  placeholder ::= "%" [ARGUMENTSWAPSPECIFIER] [SIGNSPECIFIER] [PADDINGSPECIFIER] [ALIGNMENTSPECIFIER] [WIDTHSPECIFIER] [PRECISIONSPECIFIER] TYPESPECIFIER
     *  NUMBER ::= { "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" }
     *  ARGUMENTSWAPSPECIFIER = NUMBER "$"
     *  SIGNSPECIFIER ::= "+" | "-"
     *  PADDINGSPECIFIER ::= " " | "0" | "'" CHARACTER
     *  ALIGNMENTSPECIFIER ::= "" | "-"
     *  WIDTHSPECIFIER ::= NUMBER
     *  PRECISIONSPECIFIER ::= "." NUMBER
     *  TYPESPECIFIER ::= "b" | "c" | "d" | "e" | "E" | "f" | "F" | "g" | "G" | "i" | "n" | "o" | "p" | "s" | "u" | "x" | "X" | "%"
     *  //c++: [cdieEfgGosuxXpn%]
     *  //php: [bcdeufFosxX%]
     *  NB: Because having space as paddingspecifier leads to many false matches 
     *  in regular text, and space being the default padding specifier in php, 
     *  and being able to have space or 0 as padding specifier by prefixing it 
     *  with ', and having the padding specifier not being used frequently in 
     *  most cases, the regular expression only corresponds with quote+paddingspecifier. 
     *  NB2: The argument swap specifier gives explicit ordering of variables, 
     *  without it, the ordering is implicit (first in sequence is first in order)
     *  Example in code: <code>echo printf(gettext("%s is very %s"), "OmegaT", "great");</code>*/
    public static final Pattern PRINTF_VARS = Pattern.compile(
            "%([1-9]+\\$)?([+-])?('.)?(-)?([0-9]*)(\\.[0-9]*)?[bcdeEfFgGinopsuxX%]", Pattern.CASE_INSENSITIVE);
    /**
     * Pattern for detecting the placeholders in a printf-function string. 
     * It detects only simple placeholders, without SIGN-, PADDING-, ALIGNMENT- and WIDTH specifier.
     * @see PRINTF_VARS
     */
    public static final Pattern SIMPLE_PRINTF_VARS = Pattern.compile(
            "%([1-9]+\\$)?([0-9]*)(\\.[0-9]*)?[bcdeEfFgGinopsuxX%]", Pattern.CASE_INSENSITIVE);

}
