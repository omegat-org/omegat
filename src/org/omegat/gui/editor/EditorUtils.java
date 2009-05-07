/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.gui.editor;

/**
 * Some utilities methods.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EditorUtils {
    /**
     * Check if language is RTL.
     * 
     * @param language
     *            ISO-639-2 language code
     * @return true if language is RTL
     */
    public static boolean isRTL(final String language) {
        return "ar".equalsIgnoreCase(language)
                || "iw".equalsIgnoreCase(language)
                || "he".equalsIgnoreCase(language)
                || "fa".equalsIgnoreCase(language)
                || "ur".equalsIgnoreCase(language);
    }

    /**
     * Remove LTR/RTL direction chars(u202A,u202B,u202C) from beginning and
     * ending of string.
     * 
     * @param str
     *            input string
     * @return string without direction chars
     */
    public static String removeDirection(final String str) {
        String result = str;
        if (result.length() > 0) {
            if (isDirectionChar(result.charAt(0))) {
                result = result.substring(1);
            }
        }
        if (result.length() > 0) {
            int last = result.length() - 1;
            if (isDirectionChar(result.charAt(last))) {
                result = result.substring(0, last);
            }
        }
        return result;
    }

    /**
     * Check if char is direction char(u202A,u202B,u202C).
     * 
     * @param ch
     *            char to check
     * @return true if it's direction char
     */
    private static boolean isDirectionChar(final char ch) {
        return ch == '\u202A' || ch == '\u202B' || ch == '\u202C';
    }
}
