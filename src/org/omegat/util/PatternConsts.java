/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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
 * Constant patterns, used in dirrerent other classes.
 *
 * @author Maxym Mykhalchuk
 */
public class PatternConsts
{
    /**
     * Compiled pattern to extract the encoding from XML file, if any.
     * Found encoding is stored in group #1.
     */
    public static final Pattern XML_ENCODING = Pattern.compile(
            "<\\?xml.*?encoding\\s*=\\s*\"(\\S+?)\".*?\\?>");                       // NOI18N
    
    /** compiled pattern to match XML header */
    public static final Pattern XML_HEADER = Pattern.compile(
            "(<\\?xml.*?\\?>)");                                                    // NOI18N
    
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
            "<\\!DOCTYPE\\s+(\\w+)\\s+(PUBLIC\\s+\"(-//.*)\"\\s+)?");//(SYSTEM\\s+)?\"(.*?)\"\\s+>");                       // NOI18N
    
    /**
     * Compiled pattern to extract the root tag from XML file, if any.
     * Group #1 should contain the root tag.
     */
    public static final Pattern XML_ROOTTAG = Pattern.compile(
            "<(\\w+)");                       // NOI18N
    
    /** compiled pattern to extract the encoding from HTML file, if any */
    public static final Pattern HTML_ENCODING = Pattern.compile(
            "<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"'].*?/?\\s*>",  // NOI18N
            Pattern.CASE_INSENSITIVE);
    
    /** compiled pattern to look for HTML file HEAD declaration */
    public static final Pattern HTML_HEAD = Pattern.compile(
            "<head.*?>",                                                            // NOI18N
            Pattern.CASE_INSENSITIVE);
    
    /** compiled pattern to look for HTML file HTML declaration */
    public static final Pattern HTML_HTML = Pattern.compile(
            "<html.*?>",                                                            // NOI18N
            Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern that matches full string containing in full and only
     * omegat-specific tag (without leading &lt; and trailing &gt;).
     */
    public static final Pattern OMEGAT_TAG_ONLY = Pattern.compile(
            "^\\/?[a-zA-Z]+[0-9]+\\/?$");                                       // NOI18N
    
    /**
     * Pattern that matches omegat-specific tags
     * (with leading &lt; and trailing &gt; in any place of a string).
     */
    public static final Pattern OMEGAT_TAG = Pattern.compile(
            "<\\/?[a-zA-Z]+[0-9]+\\/?>");                                       // NOI18N
    
    /** Pattern that detects space-only regular expressions. */
    public static final Pattern SPACY_REGEX = Pattern.compile(
            "((\\s|\\\\n|\\\\t|\\\\s)(\\+|\\*)?)+");                            // NOI18N
    
    /** Pattern that detects language and country. */
    public static final Pattern LANG_AND_COUNTRY = Pattern.compile(
            "([A-Za-z]{1,8})(?:(?:-|_)([A-Za-z0-9]{1,8}))?");                   // NOI18N
    
}
