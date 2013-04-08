/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Some utilities methods.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class EditorUtils {
    /**
     * Check if language is Right-To-Left oriented.
     * 
     * @param language
     *            ISO-639-2 language code
     * @return true if language is RTL
     */
    public static boolean isRTL(final String language) {
        return "ar".equalsIgnoreCase(language) || "iw".equalsIgnoreCase(language)
                || "he".equalsIgnoreCase(language) || "fa".equalsIgnoreCase(language)
                || "ur".equalsIgnoreCase(language) || "ug".equalsIgnoreCase(language)
                || "ji".equalsIgnoreCase(language) || "yi".equalsIgnoreCase(language);
    }

    /**
     * Check if locale is Right-To-Left oriented.
     * @return true if locale is Right-To-Left oriented.
     */
    public static boolean localeIsRTL() {
        String language = Locale.getDefault().getLanguage().toLowerCase();
        return EditorUtils.isRTL(language);
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
    public static int getWordStart(JTextComponent c, int offs) throws BadLocationException {
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
    public static int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
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
        return ch == '\u202A' || ch == '\u202B' || ch == '\u202C';
    }

    /**
     * Remove invisible direction chars from string.
     * 
     * @param text
     *            string with direction chars
     * @return string without direction chars
     */
    public static String removeDirectionChars(String text) {
        return text.replaceAll("[\u202A\u202B\u202C]", "");
    }
}
