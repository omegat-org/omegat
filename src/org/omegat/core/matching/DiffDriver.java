/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Aaron Madlon-Kay
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

package org.omegat.core.matching;

import java.util.LinkedList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.util.Token;

import bmsi.util.Diff;

/**
 * Drives a diff engine to produce rendered textual diff output.
 * Uses GNU Diff for Java: http://bmsi.com/java/#diff
 * 
 * @author Aaron Madlon-Kay
 */
public class DiffDriver {

    public enum Type {
        INSERT, DELETE
    }

    /**
     * Given two strings, perform a diff comparison and return a Render object.
     *
     * @param original Original string
     * @param revised Revised string for comparison
     * @return Render object
     */
    public static Render render(String original, String revised) {

        String[] originalStrings = tokenize(original);
        String[] revisedStrings = tokenize(revised);

        // Get "change script", a linked list of Diff.changes.
        Diff diff = new Diff(originalStrings, revisedStrings);
        Diff.change script = diff.diff_2(false);
        assert (validate(script, originalStrings, revisedStrings));

        Render result = new Render();
        StringBuffer rawText = new StringBuffer();

        // Walk original token strings past the last index in
        // case there was an insertion at the end.
        for (int n = 0; n <= originalStrings.length; n++) {

            Diff.change c = search(n, script);

            if (c == null) {
                // No change for this token.
                if (n < originalStrings.length) {
                    rawText.append(originalStrings[n]);
                }
                continue;
            } else {
            	// Next time, start search from the next change.
            	script = c.link;
            }

            // Handle deletions
            if (c.deleted > 0) {

                int start = rawText.length();

                //rawText.append("-[");
                for (int m = 0; m < c.deleted; m++) {
                    rawText.append(originalStrings[n + m]);
                }
                //rawText.append("]");

                n += c.deleted - 1;

                result.addRun(start, rawText.length() - start, Type.DELETE);
            }

            // Handle insertions
            if (c.inserted > 0) {

                int start = rawText.length();

                //rawText.append("+[");
                for (int m = 0; m < c.inserted; m++) {
                    rawText.append(revisedStrings[c.line1 + m]);
                }
                //rawText.append("]");

                result.addRun(start, rawText.length() - start, Type.INSERT);

                // If this was an insert only (no deleted lines), we should
                // add the original token in as well.
                if (c.deleted == 0 && n < originalStrings.length) {
                    rawText.append(originalStrings[n]);
                }
            }
        }

        result.text = rawText.toString();

        return result;
    }

    /**
     * Recurse through a change script until we find a change at the given index.
     *
     * @param i Index to seek
     * @param script Change script
     * @return Element at index i, or null if not found
     */
    private static Diff.change search(int i, Diff.change script) {
    	// Give up when we reach the end of the list,
    	// OR if we've passed the desired index (list is sorted in increasing order).
        if (script == null || script.line0 > i) {
            return null;
        }

        if (script.line0 == i) {
            return script;
        }

        return search(i, script.link);
    }

    /**
     * Double check some assumptions made about change scripts.
     * Only meant to be called in debug, via assert.
     * 
     * @param script Linked list of Diff.change elements
     * @param original Original strings
     * @param revised Revised strings
     * @return Whether or not the change script is valid
     */
    private static boolean validate(Diff.change script, String[] original, String[] revised) {
    	Diff.change prev = null;
        for (Diff.change c = script; c != null; c = c.link) {
            // Script is sorted in increasing order of string line number.
            if (prev != null && (c.line0 <= prev.line0 || c.line1 <= prev.line1)) {
            	return false;
            }
            // All changes will be accounted for by walking c.line0 in range [0, original.length].
            if (c.line0 < 0 || c.line0 > original.length) {
            	return false;
            }
            prev = c;
        }
        return true;
    }

    /**
     * Use the project's source tokenizer to split a string into token strings.
     *
     * @param input String to tokenize
     * @return Array of String tokens
     */
    private static String[] tokenize(String input) {
        ITokenizer tokenizer = Core.getProject().getSourceTokenizer();

        Token[] tokens = tokenizer.tokenizeAllExactly(input);
        String[] strings = new String[tokens.length];

        for (int n = 0; n < tokens.length; n++) {
            strings[n] = tokens[n].getTextFromString(input);
        }

        return strings;
    }

    /**
     * Represents the output of a string-string diff comparison. Contains the raw text for display, as well as
     * formatting information.
     *
     * @author aaron.madlon-kay
     */
    public static class Render {

        public List<TextRun> formatting = new LinkedList<TextRun>();
        public String text;

        public void addRun(int start, int length, Type type) {
            formatting.add(new TextRun(start, length, type));
        }
    }

    /**
     * Indicates formatting of a text run for diff display purposes.
     *
     * @author aaron.madlon-kay
     */
    public static class TextRun {

        public int start;
        public int length;
        public Type type;

        public TextRun(final int start, final int length, final Type type) {
            assert (start >= 0);
            assert (length >= 1);
            assert (type != null);

            this.start = start;
            this.length = length;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%s: %d +%d", type, start, length);
        }
    }
}
