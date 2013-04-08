/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Wildrich Fourie, Alex Buloichik, Didier Briel
               2011 Didier Briel
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import org.omegat.util.Preferences;

/**
 * Underlines all the terms in the SourceTextEntry that has matches in the
 * Glossary.
 * 
 * @author W. Fourie
 * @author Alex Buloichik
 * @author Didier Briel
 */
public class TransTips {
    // List containing the characters that may be found at the end of a word,
    // that
    // might result in the word not being underlined
    private static List<Character> afterExcludeCases = new ArrayList<Character>();
    static {
        afterExcludeCases.add('.');
        afterExcludeCases.add('!');
        afterExcludeCases.add('?');
        afterExcludeCases.add(':');
        afterExcludeCases.add(',');
        afterExcludeCases.add(';');
        afterExcludeCases.add('<');
        afterExcludeCases.add(')');
        afterExcludeCases.add('-');
    };

    // List containing the characters that may be found at the beginning of a
    // word, that
    // might result in the word not being underlined
    private static List<Character> beforeExcludeCases = new ArrayList<Character>();
    static {
        beforeExcludeCases.add('>');
        beforeExcludeCases.add('(');
        beforeExcludeCases.add(' ');
    };

    /**
     * Search for a word and returns the offset of the first occurrence.
     * Highlights are added for all occurrences found.
     * 
     * @param glossaryEntry
     *            To be searched
     * @param start
     *            Starting position
     * @param end
     *            Ending position
     * @return The offset of the first occurrence
     */
    public static void search(String sourceText, GlossaryEntry glossaryEntry, Search callback) {
        int firstOffset = -1;

        String word = glossaryEntry.getSrcText();
        // Test for invalid word.
        if (word == null || word.equals("")) {
            return;
        }

        String content = sourceText;

        if (!word.equals(word.toUpperCase())) { // The glossary word is not in full uppercase
            // We're comparing with lower case,
            // and the source text has to be in lower case too.
            content = sourceText.toLowerCase();
            word = word.toLowerCase();
        } // Otherwise, if the glossary word is in full uppercase,
          // we only apply transtips to source words that are in full uppercase too


        int lastIndex = 0;
        int wordSize = word.length();

        // Test for BadLocation
        while (((lastIndex = content.indexOf(word, lastIndex)) != -1)) {
            int endIndex = lastIndex + wordSize;
            if (Preferences.isPreference(Preferences.TRANSTIPS_EXACT_SEARCH)) {
                if (isWordAlone(content, lastIndex, word)) {
                    callback.found(glossaryEntry, lastIndex, endIndex);

                    if (firstOffset == -1)
                        firstOffset = lastIndex;
                }
            } else {
                callback.found(glossaryEntry, lastIndex, endIndex);

                if (firstOffset == -1)
                    firstOffset = lastIndex;

            }

            lastIndex = endIndex;
        }
        return;
    }

    /**
     * Determines if the word is surrounded by whitespace characters.
     * 
     * @param sourceText
     * @param testIndex
     * @param glosSrc
     * @return True if the word is alone; False if the word is contained within
     *         another word
     */
    public static boolean isWordAlone(String sourceText, int testIndex, String glosSrc) {
        // Remove directional characters
        sourceText = sourceText.replaceAll("[\u202A|\u202B|\u202C]", " ");

        // Check that word stands alone
        char before;
        try {
            before = sourceText.toLowerCase().charAt(testIndex - 1);
        } catch (Exception ex) {
            before = ' ';
        }

        // Test the end of the string
        char after;
        try {
            after = sourceText.toLowerCase().charAt(testIndex + glosSrc.length());
        } catch (Exception cp) {
            after = ' ';
        }

        // After Exclude Cases.
        if (afterExcludeCases.contains(after))
            after = ' ';

        // Before Exclude Cases
        if (beforeExcludeCases.contains(before))
            before = ' ';

        if ((before == ' ') && (after == ' ')) {
            return true;
        } else {
            return false;
        }
    }

    public interface Search {
        void found(GlossaryEntry glossaryEntry, int start, int end);
    }
}
