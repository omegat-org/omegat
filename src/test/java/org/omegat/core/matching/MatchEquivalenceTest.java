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

package org.omegat.core.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Folding behavior of the character equivalence classes (feature request
 * #1681).
 *
 * @author Stephan Pakebusch
 */
public class MatchEquivalenceTest {

    private static String foldAll(String text) {
        return MatchEquivalence.fold(text, MatchEquivalence.buildFoldMap(MatchEquivalence.all()));
    }

    private static String fold(String text, Set<MatchEquivalence> active) {
        return MatchEquivalence.fold(text, MatchEquivalence.buildFoldMap(active));
    }

    @Test
    public void doubleQuoteVariantsFoldTogether() {
        String expected = foldAll("Select \"Save\" from the menu.");
        assertEquals(expected, foldAll("Select “Save” from the menu."));
        assertEquals(expected, foldAll("Select „Save“ from the menu."));
        assertEquals(expected, foldAll("Select «Save» from the menu."));
        assertEquals(expected, foldAll("Select 「Save」 from the menu."));
    }

    @Test
    public void singleQuoteVariantsFoldTogether() {
        String expected = foldAll("a 'word' here");
        assertEquals(expected, foldAll("a ‘word’ here"));
        assertEquals(expected, foldAll("a ‹word› here"));
    }

    /** #1681 asks for two groups: single quotes never match double quotes. */
    @Test
    public void singleAndDoubleQuotesStaySeparate() {
        assertNotEquals(foldAll("a \"word\" here"), foldAll("a 'word' here"));
    }

    /** Primes are measurement marks, not quotation marks; they stay as is. */
    @Test
    public void primesAreNotQuotes() {
        assertNotEquals(foldAll("5′10″"), foldAll("5'10\""));
    }

    @Test
    public void apostropheVariantsFoldInsideWords() {
        Set<MatchEquivalence> only = EnumSet.of(MatchEquivalence.QUOTES);
        assertEquals(fold("l'ananas", only), fold("l’ananas", only));
        assertEquals(fold("l'ananas", only), fold("lʼananas", only));
    }

    @Test
    public void dashVariantsFoldTogether() {
        String expected = foldAll("pages 3-4");
        assertEquals(expected, foldAll("pages 3–4"));
        assertEquals(expected, foldAll("pages 3—4"));
        assertEquals(expected, foldAll("pages 3−4"));
    }

    @Test
    public void spaceVariantsFoldTogether() {
        String expected = foldAll("10 %");
        assertEquals(expected, foldAll("10\u00A0%"));
        assertEquals(expected, foldAll("10\u202F%"));
        assertEquals(expected, foldAll("10\u3000%"));
    }

    @Test
    public void invisibleFormattingCharactersAreRemoved() {
        assertEquals(foldAll("information"), foldAll("infor\u00ADmation"));
        assertEquals(foldAll("ab"), foldAll("a\u200Eb"));
        assertEquals(foldAll("ab"), foldAll("a\u200Bb"));
        // ZWNJ and ZWJ are meaning-bearing in Persian and Indic scripts and
        // are not folded away.
        assertNotEquals(foldAll("ab"), foldAll("a\u200Cb"));
        assertNotEquals(foldAll("ab"), foldAll("a\u200Db"));
    }

    /** Canonical normalization applies even with every class disabled. */
    @Test
    public void canonicalNormalizationIsUnconditional() {
        Set<MatchEquivalence> none = EnumSet.noneOf(MatchEquivalence.class);
        assertEquals(fold("caf\u00E9", none), fold("cafe\u0301", none));
    }

    @Test
    public void disabledClassKeepsVariantsApart() {
        Set<MatchEquivalence> withoutQuotes = EnumSet.complementOf(EnumSet.of(MatchEquivalence.QUOTES));
        assertNotEquals(fold("a \"word\"", withoutQuotes), fold("a “word”", withoutQuotes));
    }

    @Test
    public void idListRoundTrip() {
        Set<MatchEquivalence> classes = EnumSet.of(MatchEquivalence.QUOTES, MatchEquivalence.SPACES);
        assertEquals(classes, MatchEquivalence.fromIdList(MatchEquivalence.toIdList(classes)));
        // Unknown ids from newer versions are ignored instead of failing.
        assertEquals(EnumSet.of(MatchEquivalence.DASHES),
                MatchEquivalence.fromIdList("dashes,ligatures"));
    }

    /** Apostrophe variants belong to the quotes class (shared code points). */
    @Test
    public void apostrophesBelongToQuotes() {
        Map<Integer, String> map = MatchEquivalence.buildFoldMap(EnumSet.of(MatchEquivalence.QUOTES));
        assertEquals("’", map.get((int) '\''));
        assertEquals("’", map.get((int) 'ʼ'));
    }

    private static boolean regexFinds(String needle, String haystack, Set<MatchEquivalence> active) {
        return java.util.regex.Pattern.compile(MatchEquivalence.globToRegex(needle, active))
                .matcher(haystack).find();
    }

    /** Search patterns match every variant of the active classes (#1681). */
    @Test
    public void searchRegexMatchesVariants() {
        Set<MatchEquivalence> all = MatchEquivalence.all();
        assertTrue(regexFinds("\"Save\"", "W\u00e4hlen Sie \u201eSave\u201c im Men\u00fc.", all));
        assertTrue(regexFinds("3-4", "pages 3\u20134", all));
        assertTrue(regexFinds("10 %", "Rabatt 10\u00a0%", all));
        // invisible formatting characters tolerated in the searched text
        assertTrue(regexFinds("information", "infor\u00admation", all));
        // and dropped from the needle
        assertTrue(regexFinds("infor\u00admation", "information", all));
        assertFalse(regexFinds("'Save'", "\u201eSave\u201c", all));
    }

    @Test
    public void searchRegexEscapesMetacharacters() {
        Set<MatchEquivalence> all = MatchEquivalence.all();
        assertFalse(regexFinds("a.b", "axb", all));
        assertTrue(regexFinds("a.b", "a.b", all));
        assertTrue(regexFinds("f*o", "fooo", all));
        assertFalse(regexFinds("f*o", "f o", all));
        assertTrue(regexFinds("t?p", "tip", all));
    }

    /**
     * The needle is NFC-normalized, so literals with a differing canonical
     * decomposition alternate both forms: composed and decomposed text is
     * found either way, with any class configuration.
     */
    @Test
    public void searchRegexMatchesBothNormalizationForms() {
        Set<MatchEquivalence> none = EnumSet.noneOf(MatchEquivalence.class);
        assertTrue(regexFinds("caf\u00E9", "caf\u00E9", none));
        assertTrue(regexFinds("caf\u00E9", "cafe\u0301", none));
        assertTrue(regexFinds("cafe\u0301", "caf\u00E9", none));
        assertTrue(regexFinds("cafe\u0301", "cafe\u0301", none));
    }

    /**
     * A needle of nothing but invisible formatting characters must not
     * collapse to an empty pattern that finds (and in replace mode rewrites)
     * everything; it is searched literally instead.
     */
    @Test
    public void invisibleOnlyNeedleStaysLiteral() {
        Set<MatchEquivalence> all = MatchEquivalence.all();
        assertFalse(MatchEquivalence.globToRegex("\u00AD", all).isEmpty());
        assertFalse(regexFinds("\u00AD", "plain text", all));
        assertTrue(regexFinds("\u00AD", "soft\u00ADhyphen", all));
    }

    @Test
    public void searchRegexWithoutClassesIsPlain() {
        Set<MatchEquivalence> none = EnumSet.noneOf(MatchEquivalence.class);
        assertFalse(regexFinds("\"Save\"", "\u201eSave\u201c", none));
        assertTrue(regexFinds("\"Save\"", "\"Save\"", none));
    }

    @Test
    public void membersListedForGui() {
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            assertTrue(eq.getId(), eq.getMembers().size() >= 3);
        }
        assertTrue(MatchEquivalence.QUOTES.getMembers().containsKey(0x1F676));
    }
}
