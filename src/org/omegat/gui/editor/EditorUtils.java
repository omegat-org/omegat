/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Didier Briel
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.editor;

import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.omegat.core.Core;
import org.omegat.gui.editor.IEditor.CHANGE_CASE_TO;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Some utilities methods.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
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
    
    /**
     * perform the case change. Lowercase becomes titlecase, titlecase becomes uppercase, uppercase becomes
     * lowercase. if the text matches none of these categories, it is uppercased.
     * 
     * @param input
     *            : the string to work on
     * @param toWhat
     *            : one of the CASE_* values - except for case CASE_CYCLE.
     */
    public static String doChangeCase(String input, CHANGE_CASE_TO toWhat) {
        // tokenize the selection
        Token[] tokenList = Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(input);

        if (toWhat == CHANGE_CASE_TO.CYCLE) {
            int lower = 0;
            int upper = 0;
            int title = 0;
            int ambiguous = 0; // Maybe title, maybe upper
            int mixed = 0;

            for (Token token : tokenList) {
                String word = token.getTextFromString(input);
                if (StringUtil.isLowerCase(word)) {
                    lower++;
                    continue;
                }
                boolean isTitle = StringUtil.isTitleCase(word);
                boolean isUpper = StringUtil.isUpperCase(word);
                if (isTitle && isUpper) {
                    ambiguous++;
                    continue;
                }
                if (isTitle) {
                    title++;
                    continue;
                }
                if (isUpper) {
                    upper++;
                    continue;
                }
                if (StringUtil.isMixedCase(word)) {
                    mixed++;
                }
                // Ignore other tokens as they should be caseless text
                // such as CJK ideographs or symbols only.
            }
            
            if (lower == 0 && title == 0 && upper == 0 && mixed == 0 && ambiguous == 0) {
                return input; // nothing to do here
            }

            toWhat = determineTargetCase(lower, upper, title, mixed, ambiguous);
        }

        StringBuilder buffer = new StringBuilder(input);
        int lengthIncrement = 0;
        Locale locale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
        
        for (Token token : tokenList) {
            // find out the case and change to the selected
            String tokText = token.getTextFromString(input);
            String result = toWhat == CHANGE_CASE_TO.LOWER ? tokText.toLowerCase(locale)
                    : toWhat == CHANGE_CASE_TO.UPPER ? tokText.toUpperCase(locale)
                    : toWhat == CHANGE_CASE_TO.TITLE ? StringUtil.toTitleCase(tokText, locale)
                    : tokText;

            // replace this token
            buffer.replace(token.getOffset() + lengthIncrement, token.getLength() + token.getOffset()
                    + lengthIncrement, result);

            lengthIncrement += result.length() - token.getLength();
        }
        
        return buffer.toString();
    }
    
    private static CHANGE_CASE_TO determineTargetCase(int lower, int upper, int title, int mixed, int ambiguous) {
        int presentCaseTypes = 0;
        if (lower > 0) {
            presentCaseTypes++;
        }
        if (upper > 0) {
            presentCaseTypes++;
        }
        if (title > 0) {
            presentCaseTypes++;
        }
        if (mixed > 0) {
            presentCaseTypes++;
        }
        
        if (mixed > 0 || presentCaseTypes > 1) {
            return CHANGE_CASE_TO.UPPER;
        }

        if (lower > 0) {
            return CHANGE_CASE_TO.TITLE;
        }

        if (title > 0) {
            return CHANGE_CASE_TO.UPPER;
        }

        if (upper > 0) {
            return CHANGE_CASE_TO.LOWER;
        }

        if (ambiguous > 0) {
            // If we only have ambiguous tokens then we must go to lower so that we
            // get binary upper/lower switching instead of trinary upper/lower/title.
            return CHANGE_CASE_TO.LOWER;
        }
        
        // This should only happen if no cases are present, so it doesn't even matter.
        return CHANGE_CASE_TO.UPPER;
    }
}
