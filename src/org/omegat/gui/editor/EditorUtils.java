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

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

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
                || "ur".equalsIgnoreCase(language)
                || "ug".equalsIgnoreCase(language);
    }

    /**
     * Determines the start of a word for the given model location. This method
     * skips direction char.
     * 
     * TODO: change to use document's locale
     * 
     * @param c
     * @param offs
     * @return
     * @throws BadLocationException
     */
    public static int getWordStart(JTextComponent c, int offs)
            throws BadLocationException {
        int result = Utilities.getWordStart(c, offs);
        char ch = c.getDocument().getText(result, 1).charAt(0);
        if (isDirectionChar(ch)) {
            result++;
        }
        return result;
    }

    /**
     * Determines the end of a word for the given model location. This method
     * skips direction char.
     * 
     * TODO: change to use document's locale
     * 
     * @param c
     * @param offs
     * @return
     * @throws BadLocationException
     */
    public static int getWordEnd(JTextComponent c, int offs)
            throws BadLocationException {
        int result = Utilities.getWordEnd(c, offs);
        if (result > 0) {
            char ch = c.getDocument().getText(result - 1, 1).charAt(0);
            if (isDirectionChar(ch)) {
                result--;
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
        return ch == '\u202A' || ch == '\u202B' || ch == '\u202C'|| ch=='\u200B';
    }

    /**
     * Remove invisible direction chars from string.
     * 
     * @param text
     *            string with direction chars
     * @return string without direction chars
     */
    public static String removeDirectionChars(String text) {
        return text.replaceAll("[\u202A|\u202B|\u202C|\u200B]", "");
    }
}
