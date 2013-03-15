/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel and Tiago Saboga
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2008 Andrzej Sawula
               2010-2013 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.util;

/**
 * Utilities for string processing.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StringUtil {

    /**
     * Check if string is empty, i.e. null or length==0
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Returns true if the input is lowercase.
     */
    public static boolean isLowerCase(final String input) {
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isLetter(current) && !Character.isLowerCase(current))
                return false;
        }
        return true;
    }

    /**
     * Returns true if the input is upper case.
     */
    public static boolean isUpperCase(final String input) {
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isLetter(current) && !Character.isUpperCase(current))
                return false;
        }
        return true;
    }

    /**
     * Returns true if the input is title case.
     */
    public static boolean isTitleCase(final String input) {
        if (input.length() > 1)
            return Character.isTitleCase(input.charAt(0)) && isLowerCase(input.substring(1));
        else
            return Character.isTitleCase(input.charAt(0));
    }

    /**
     * Returns first not null object from list, or null if all values is null.
     */
    public static <T> T nvl(T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }

    /**
     * Returns first non-zero object from list, or zero if all values is null.
     */
    public static long nvlLong(long... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {
                return values[i];
            }
        }
        return 0;
    }

    /**
     * Compare two values, which could be null.
     */
    public static <T> boolean equalsWithNulls(T v1, T v2) {
        if (v1 == null && v2 == null) {
            return true;
        } else if (v1 != null && v2 != null) {
            return v1.equals(v2);
        } else {
            return false;
        }
    }

    /**
     * Compare two values, which could be null.
     */
    public static <T extends Comparable<T>> int compareToWithNulls(T v1, T v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null && v2 != null) {
            return -1;
        } else if (v1 != null && v2 == null) {
            return 1;
        } else {
            return v1.compareTo(v2);
        }
    }

    /**
     * Extracts first N chars from string.
     */
    public static String firstN(String str, int len) {
        if (str.length() < len) {
            return str;
        } else {
            return str.substring(0, len) + "...";
        }
    }

    /**
     * Returns first letter in lowercase. Usually used for create tag shortcuts.
     */
    public static char getFirstLetterLowercase(CharSequence s) {
        char f = 0;

        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                f = Character.toLowerCase(s.charAt(i));
                break;
            }
        }

        return f;
    }

    /**
     * Checks if text contains substring after specified position.
     */
    public static boolean isSubstringAfter(String text, int pos, String substring) {
        if (pos + substring.length() > text.length()) {
            return false;
        }
        return substring.equals(text.substring(pos, pos + substring.length()));
    }

    /**
     * Checks if text contains substring before specified position.
     */
    public static boolean isSubstringBefore(String text, int pos, String substring) {
        if (pos - substring.length() < 0) {
            return false;
        }
        return substring.equals(text.substring(pos - substring.length(), pos));
    }
}
