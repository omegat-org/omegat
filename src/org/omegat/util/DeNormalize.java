/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT. The real license is reproduced below.

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

/*
 Copyright (c) 2013, Johns Hopkins University
 All rights reserved.

 BSD 2-clause license
 http://opensource.org/licenses/BSD-2-Clause

 Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of Johns Hopkins University.
*/

package org.omegat.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Denormalize a(n English) string in a collection of ways listed below.
 * <UL>
 * <LI>Capitalize the first character in the string</LI>
 * <LI>Detokenize</LI>
 * <UL>
 * <LI>Delete whitespace in front of periods and commas</LI>
 * <LI>Join contractions</LI>
 * <LI>Capitalize name titles (Mr Ms Miss Dr etc.)</LI>
 * <LI>TODO: Handle surrounding characters ([{&lt;"''"&gt;}])</LI>
 * <LI>TODO: Join multi-period abbreviations (e.g. M.Phil. i.e.)</LI>
 * <LI>TODO: Handle ambiguities like "st.", which can be an abbreviation for
 * both "Saint" and "street"</LI>
 * <LI>TODO: Capitalize both the title and the name of a person, e.g. Mr. Morton
 * (named entities should be demarcated).</LI>
 * </UL>
 * </UL>
 * <bold>N.B.</bold> These methods all assume that every translation result that
 * will be denormalized has the following format:
 * <UL>
 * <LI>There is only one space between every pair of tokens</LI>
 * <LI>There is no whitespace before the first token</LI>
 * <LI>There is no whitespace after the final token</LI>
 * <LI>Standard spaces are the only type of whitespace</LI>
 * </UL>
 * </UL>
 */

public final class DeNormalize {

    private DeNormalize() {
    }

    /**
     * Apply all the denormalization methods to the normalized input line.
     *
     * @param normalized
     * @return
     */
    public static String processSingleLine(String normalized) {
        // The order in which the methods are applied could matter in some
        // situations. E.g., a token to
        // be matched is "phd", but if it is the first token in the line, it
        // might have already been
        // capitalized to "Phd" by the capitalizeFirstLetter method, and because
        // the "phd" token won't
        // match, "Phd" won't be corrected to "PhD".
        String deNormalized = normalized;
        deNormalized = capitalizeNameTitleAbbrvs(deNormalized);
        deNormalized = replaceBracketTokens(deNormalized);
        deNormalized = joinPunctuationMarks(deNormalized);
        deNormalized = joinHyphen(deNormalized);
        deNormalized = joinContractions(deNormalized);
        deNormalized = capitalizeLineFirstLetter(deNormalized);
        return deNormalized;
    }

    /**
     * Capitalize the first letter of a line. This should be the last
     * denormalization step applied to a line.
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String capitalizeLineFirstLetter(String line) {
        String result = null;
        // U+00A1 INVERTED EXCLAMATION MARK
        // U+00BF INVERTED QUESTION MARK
        Pattern regexp = Pattern.compile("[^\\p{Punct}\\p{Space}\u00a1\u00bf]");
        Matcher matcher = regexp.matcher(line);
        if (matcher.find()) {
            String match = matcher.group(0);
            result = line.replaceFirst(match, match.toUpperCase());
        } else {
            result = line;
        }
        return result;
    }

    /**
     * Scanning from left-to-right, a comma or period preceded by a space will
     * become just the comma/period.
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String joinPunctuationMarks(String line) {
        String result = line;
        result = result.replace(" ,", ",");
        result = result.replace(" .", ".");
        result = result.replace(" !", "!");
        result = result.replace("\u00a1 ", "\u00a1"); // U+00A1 INVERTED
                                                      // EXCLAMATION MARK
        result = result.replace(" ?", "?");
        result = result.replace("\u00bf ", "\u00bf"); // U+00BF INVERTED
                                                      // QUESTION MARK
        result = result.replace(" )", ")");
        result = result.replace(" ]", "]");
        result = result.replace(" }", "}");
        result = result.replace("( ", "(");
        result = result.replace("[ ", "[");
        result = result.replace("{ ", "{");
        return result;
    }

    /**
     * Scanning from left-to-right, a hyphen surrounded by a space before and
     * after it will become just the hyphen.
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String joinHyphen(String line) {
        return line.replace(" - ", "-");
    }

    /**
     * Scanning the line from left-to-right, a contraction suffix preceded by a
     * space will become just the contraction suffix. <br>
     * <br>
     * I.e., the preceding space will be deleting, joining the prefix to the
     * suffix. <br>
     * <br>
     * E.g.
     *
     * <pre>
     * wo n't
     * </pre>
     *
     * becomes
     *
     * <pre>
     * won't
     * </pre>
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String joinContractions(String line) {
        String result = line;
        for (String suffix : new String[] { "'d", "'ll", "'m", "n't", "'re", "'s", "'ve", }) {
            result = result.replace(" " + suffix, suffix);
        }
        return result;
    }

    /**
     * Capitalize the first character of the titles of names: Mr Mrs Ms Miss Dr
     * Prof
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String capitalizeNameTitleAbbrvs(String line) {
        String result = line;

        // Capitalize only the first character of certain name titles.
        for (String title : new String[] { "dr", "miss", "mr", "mrs", "ms", "prof" }) {
            result = result.replaceAll("\\b" + title + "\\b",
                    Character.toUpperCase(title.charAt(0)) + title.substring(1));
        }
        // Capitalize the relevant characters of certain name titles.
        result = result.replaceAll("\\b" + "phd" + "\\b", "PhD");
        result = result.replaceAll("\\b" + "mphil" + "\\b", "MPhil");
        return result;
    }

    public static String capitalizeI(String line) {
        // Capitalize only the first character of certain name titles.
        return line.replaceAll("\\b" + "i" + "\\b", "I");
    }

    /**
     * Case-insensitively replace all of the character sequences that represent
     * a bracket character.
     *
     * Keys are token representations of abbreviations of titles for names that
     * capitalize more than just the first letter.<br>
     * Bracket token sequences: -lrb- -rrb- -lsb- -rsb- -lcb- -rcb- <br>
     * <br>
     * See http://www.cis.upenn.edu/~treebank/tokenization.html
     *
     * @param line
     *            The single-line input string
     * @return The input string modified as described above
     */
    public static String replaceBracketTokens(String line) {
        String result = line;
        result = result.replaceAll("(?iu)" + "-lrb-", "(");
        result = result.replaceAll("(?iu)" + "-rrb-", ")");
        result = result.replaceAll("(?iu)" + "-lsb-", "[");
        result = result.replaceAll("(?iu)" + "-rsb-", "]");
        result = result.replaceAll("(?iu)" + "-lcb-", "{");
        result = result.replaceAll("(?iu)" + "-rcb-", "}");
        return result;
    }

}
