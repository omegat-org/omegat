/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Repairs a fuzzy match with glossary terms (feature requests #1566, #369):
 * where the match source and the current source differ and both sides of the
 * difference are glossary terms, the match translation gets the old target
 * term replaced with the new one. "Code of Conduct" -&gt; "Código de conducta"
 * inserted into "Code of Behaviour" becomes "Código de comportamiento" when
 * the glossary maps conduct -&gt; conducta and behaviour -&gt; comportamiento.
 * The repair is conservative: only whole-word occurrences of the old target
 * term are replaced (first occurrence), a difference merged with an adjacent
 * insertion looks up the whole span and silently stays when the glossary does
 * not carry it, and later spans search the already-repaired text.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class GlossaryMatchSubstitution {

    private GlossaryMatchSubstitution() {
    }

    /**
     * The match translation with glossary substitutions applied for every
     * source difference the glossary can resolve; unchanged where it cannot.
     *
     * @param currentSource
     *            source text of the segment being translated
     * @param matchSource
     *            source text of the fuzzy match
     * @param matchTranslation
     *            translation of the fuzzy match
     * @param entries
     *            glossary entries of the project (source terms match the
     *            differing spans exactly, case-insensitively; stemmed forms
     *            deliberately do not take part)
     * @param srcLocale
     *            locale of the source language, for case-insensitive compares
     * @param trgLocale
     *            locale of the target language, for capitalization matching
     * @param tokenizer
     *            source-language tokenizer
     */
    public static String substituteIntoMatch(String currentSource, String matchSource,
            String matchTranslation, List<GlossaryEntry> entries, Locale srcLocale, Locale trgLocale,
            ITokenizer tokenizer) {
        Token[] matchTokens = tokenizer.tokenizeWords(matchSource, StemmingMode.NONE);
        Token[] currentTokens = tokenizer.tokenizeWords(currentSource, StemmingMode.NONE);
        List<Replacement> replacements = diffReplacements(matchSource, matchTokens, currentSource,
                currentTokens, srcLocale);
        String result = matchTranslation;
        for (Replacement replacement : replacements) {
            String oldTarget = targetFor(entries, replacement.oldTerm, srcLocale);
            String newTarget = targetFor(entries, replacement.newTerm, srcLocale);
            if (oldTarget == null || newTarget == null) {
                continue;
            }
            // Whole-word, case-insensitive occurrence in the original string:
            // matching on a lowercased copy would shift indexes for the code
            // points whose lower case grows (Turkish dotted I).
            Pattern occurrence = Pattern.compile(
                    "(?<![\\p{L}\\p{Nd}])" + Pattern.quote(oldTarget) + "(?![\\p{L}\\p{Nd}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = occurrence.matcher(result);
            if (!matcher.find()) {
                continue;
            }
            String replacementText = StringUtil.matchCapitalization(newTarget, matcher.group(),
                    trgLocale);
            result = result.substring(0, matcher.start()) + replacementText
                    + result.substring(matcher.end());
        }
        return result;
    }

    /** One differing span: the old source words and the new source words. */
    private static final class Replacement {
        private final String oldTerm;
        private final String newTerm;

        Replacement(String oldTerm, String newTerm) {
            this.oldTerm = oldTerm;
            this.newTerm = newTerm;
        }
    }

    /**
     * Replaced spans between the two token streams: a longest common
     * subsequence anchors the equal tokens, every region where both sides
     * carry leftover tokens is one replacement (pure insertions or deletions
     * have no counterpart to look up and are skipped).
     */
    private static List<Replacement> diffReplacements(String oldText, Token[] oldTokens, String newText,
            Token[] newTokens, Locale locale) {
        int n = oldTokens.length;
        int m = newTokens.length;
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (sameToken(oldText, oldTokens[i], newText, newTokens[j], locale)) {
                    lcs[i][j] = lcs[i + 1][j + 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }
        List<Replacement> replacements = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < n || j < m) {
            if (i < n && j < m && sameToken(oldText, oldTokens[i], newText, newTokens[j], locale)) {
                i++;
                j++;
                continue;
            }
            int oldStart = i;
            int newStart = j;
            while (i < n || j < m) {
                if (i < n && j < m && sameToken(oldText, oldTokens[i], newText, newTokens[j], locale)) {
                    break;
                }
                if (j >= m || (i < n && lcs[i + 1][j] >= lcs[i][j + 1])) {
                    i++;
                } else {
                    j++;
                }
            }
            if (i > oldStart && j > newStart) {
                replacements.add(new Replacement(spanText(oldText, oldTokens, oldStart, i),
                        spanText(newText, newTokens, newStart, j)));
            }
        }
        return replacements;
    }

    private static boolean sameToken(String oldText, Token oldToken, String newText, Token newToken,
            Locale locale) {
        return oldToken.getTextFromString(oldText).toLowerCase(locale)
                .equals(newToken.getTextFromString(newText).toLowerCase(locale));
    }

    /** The original text between the first and the last token of the span. */
    private static String spanText(String text, Token[] tokens, int from, int toExclusive) {
        Token first = tokens[from];
        Token last = tokens[toExclusive - 1];
        return text.substring(first.getOffset(), last.getOffset() + last.getLength());
    }

    /** First target term of the entry whose source term equals the given span. */
    private static @Nullable String targetFor(List<GlossaryEntry> entries, String term, Locale locale) {
        String needle = term.toLowerCase(locale);
        for (GlossaryEntry entry : entries) {
            if (entry.getSrcText().toLowerCase(locale).equals(needle)) {
                return entry.getLocText();
            }
        }
        return null;
    }
}
