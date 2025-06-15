/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2011 Didier Briel, Martin Fleurke
               2012 Didier Briel, Martin Fleurke
               2013 Didier Briel, Alex Buloichik
               2017 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

package org.omegat.util;

import org.omegat.util.html.EntityUtil;

import java.util.Collection;

public final class HTMLUtils {

    private static EntityUtil entityUtil = null;

    private static EntityUtil getEntityUtil() {
        if (entityUtil == null) {
            entityUtil = new EntityUtil();
        }
        return entityUtil;
    }

    private HTMLUtils() {
    }

    /** Returns true if a char is a latin letter */
    public static boolean isLatinLetter(int ch) {
        return getEntityUtil().isLatinLetter(ch);
    }

    /** Returns true if a char is a decimal digit */
    public static boolean isDecimalDigit(int ch) {
        return getEntityUtil().isDecimalDigit(ch);
    }

    /** Returns true if a char is a hex digit */
    public static boolean isHexDigit(int ch) {
        return getEntityUtil().isHexDigit(ch);
    }

    /**
     * Converts HTML entities in the given input string to their corresponding
     * characters. This handles numeric and named entities, resolving them to
     * their appropriate Unicode representations. If an entity is unresolvable
     * or malformed, it is left unchanged in the output string.
     *
     * @param input
     *            the input string that may contain HTML entities to be
     *            converted
     * @return a string with HTML entities replaced by their corresponding
     *         characters
     */
    public static String entitiesToChars(String input) {
        return getEntityUtil().entitiesToChars(input);
    }

    /**
     * Converts characters that must be converted (&lt; &gt; &amp; '&nbsp;'
     * (nbsp)) into HTML entities.
     *
     * @param str
     *            The string to convert
     * @param encoding
     *            The output encoding. May be null (all characters are assumed
     *            to be supported).
     * @param shortcuts
     *            A collection of tag-like strings ({@code <foo>}) that will be
     *            "protected", i.e. they will not be escaped as entities.
     */
    public static String charsToEntities(String str, String encoding, Collection<String> shortcuts) {
        return getEntityUtil().charsToEntities(str, encoding, shortcuts);
    }

    public static String getSpacePrefix(String input, boolean compressWhitespace) {
        int size = input.length();
        for (int cp, i = 0; i < size; i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (!Character.isWhitespace(cp)) {
                return input.substring(0,
                        compressWhitespace ? Math.min(i, input.offsetByCodePoints(0, 1)) : i);
            }
        }
        return "";
    }

    public static String getSpacePostfix(String input, boolean compressWhitespace) {
        int size = input.length();
        int i = size;
        while (i > 0) {
            int cp = input.codePointBefore(i);
            if (!Character.isWhitespace(cp)) {
                if (i == size) {
                    return "";
                } else {
                    if (compressWhitespace) {
                        return input.substring(i, Math.min(input.offsetByCodePoints(i, 1), size));
                    }
                    return input.substring(i, size);
                }
            }
            i -= Character.charCount(cp);
        }
        return "";
    }
}
