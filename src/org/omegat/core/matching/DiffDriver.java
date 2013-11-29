/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Aaron Madlon-Kay
               2013 Zoltan Bartko
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

package org.omegat.core.matching;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.tokenizer.ITokenizer;
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
        INSERT, DELETE, NOCHANGE
    }
    
    public static final Pattern DIFF_MERGEABLE_DELIMITER_PATTERN = Pattern.compile("[ :;,.()]+");

    /**
     * Given two strings, perform a diff comparison and return a Render object.
     *
     * @param original Original string
     * @param revised Revised string for comparison
     * @return Render object
     */
    public static Render render(String original, String revised, boolean optimize) {

        Render result = new Render();
        
        String[] originalStrings = tokenize(original);
        String[] revisedStrings = tokenize(revised);
        
        if (originalStrings == null || revisedStrings == null) {
            return result;
        }

        // Get "change script", a linked list of Diff.changes.
        Diff diff = new Diff(originalStrings, revisedStrings);
        Diff.change script = diff.diff_2(false);
        assert (validate(script, originalStrings, revisedStrings));

        StringBuilder rawText = new StringBuilder();

        // Walk original token strings past the last index in
        // case there was an insertion at the end.
        for (int n = 0; n <= originalStrings.length; n++) {

            Diff.change c = search(n, script);

            if (c == null) {
                // No change for this token.
                if (n < originalStrings.length) {
                    if (optimize) result.addRun(rawText.length(), originalStrings[n].length(), Type.NOCHANGE);  
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
                    if (optimize) result.addRun(rawText.length(), originalStrings[n].length(), Type.NOCHANGE); 
                    rawText.append(originalStrings[n]);
                }
            }
        }

        result.text = rawText.toString();
        if (optimize) {
            Render optimized = optimizeRender(result,0);
            return (optimized.formatting.size() < result.formatting.size()) ? optimized : result;
        } else {
            return result;
        }
    }

    private static Render optimizeRender(Render render, int level) {
        if (level > 3) 
            return render;
        
        StringBuilder rawText = new StringBuilder();
        Render result = new Render();
        List<TextRun> fList = render.formatting;
        
        // try to merge <deletion><insertion><space><deletion><insertion> patterns
        if (fList.size() < 5)
            return render;
        
        for (int i = 0; i < fList.size(); i++) {
            TextRun r0 = fList.get(i);
            if (i < fList.size()-4) {
                TextRun r1 = fList.get(i+1);
                TextRun r2 = fList.get(i+2);
                TextRun r3 = fList.get(i+3);
                TextRun r4 = fList.get(i+4);

                if (r0.type == Type.DELETE 
                        && r1.type == Type.INSERT 
                        && r2.type == Type.NOCHANGE 
                        && DIFF_MERGEABLE_DELIMITER_PATTERN.matcher(
                                render.text.substring(r2.start, r2.start + r2.length)).matches() 
                        && r3.type == Type.DELETE 
                        && r4.type == Type.INSERT 
                        ) {
                    StringBuilder buff = new StringBuilder();
                    //merge deletes
                    buff.append(render.getRunText(r0));
                    buff.append(render.getRunText(r2));
                    buff.append(render.getRunText(r3));
                    
                    result.addRun(rawText.length(), buff.length(), Type.DELETE);
                    rawText.append(buff);

                    buff.delete(0,buff.length());

                    //merge inserts
                    buff.append(render.getRunText(r1));
                    buff.append(render.getRunText(r2));
                    buff.append(render.getRunText(r4));
                    
                    result.addRun(rawText.length(), buff.length(), Type.INSERT);
                    rawText.append(buff);

                    i = i+4;
                    continue;
                }    
            }
            result.addRun(rawText.length(), r0.length, r0.type);
            rawText.append(render.getRunText(r0));
        }
        
        result.text = rawText.toString();
        
        Render optimized = optimizeRender(result,level+1);
        return (optimized.formatting.size() < result.formatting.size()) ? optimized : result;
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
        
        if (tokenizer == null) {
            // Project has probably been closed.
            return null;
        }

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
        
        /**
         * Get the text corresponding to the run 
         * @param run
         * @return 
         */
        public String getRunText(TextRun run) {
            return text.substring(run.start, run.start + run.length);
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
