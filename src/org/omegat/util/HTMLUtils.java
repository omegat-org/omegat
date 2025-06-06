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

import org.omegat.util.html.HTMLUtilsImpl;

import java.nio.charset.Charset;
import java.util.Collection;

public final class HTMLUtils {

    private static  final HTMLUtilsImpl IMPL = new HTMLUtilsImpl();

    private HTMLUtils() {
    }

    /** Returns true if a char is a latin letter */
    public static boolean isLatinLetter(int ch) {
        return IMPL.isLatinLetter(ch);
    }

    /** Returns true if a char is a decimal digit */
    public static boolean isDecimalDigit(int ch) {
        return IMPL.isDecimalDigit(ch);
    }

    /** Returns true if a char is a hex digit */
    public static boolean isHexDigit(int ch) {
        return IMPL.isHexDigit(ch);
    }

    /**
     * Converts HTML entities in the given input string to their corresponding characters.
     * This handles numeric and named entities, resolving them to their appropriate Unicode representations.
     * If an entity is unresolvable or malformed, it is left unchanged in the output string.
     *
     * @param input the input string that may contain HTML entities to be converted
     * @return a string with HTML entities replaced by their corresponding characters
     */
    public static String entitiesToChars(String input) {
        return IMPL.entitiesToChars(input);
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
        return IMPL.charsToEntities(str, encoding, shortcuts);

    }

    public static String getSpacePrefix(String input, boolean compressWhitespace) {
        return IMPL.getSpacePrefix(input, compressWhitespace);
    }

    public static String getSpacePostfix(String input, boolean compressWhitespace) {
        return IMPL.getSpacePostfix(input, compressWhitespace);
    }

    /**
     * Returns the reader of the underlying file in the correct encoding.
     *
     * <p>
     * We can detect the following:
     * <ul>
     * <li>UTF-16 with BOM (byte order mark)
     * <li>UTF-8 with BOM (byte order mark)
     * <li>Any other encoding with 8-bit Latin symbols (e.g. Windows-1251, UTF-8
     * etc), if it is specified using XML/HTML-style encoding declarations.
     * </ul>
     * <p>
     * Note that we cannot detect UTF-16 encoding, if there's no BOM!
     */
    public static Charset detectEncoding(String fileName, String defaultEncoding) {
        return IMPL.detectEncoding(fileName, defaultEncoding);
    }
}
