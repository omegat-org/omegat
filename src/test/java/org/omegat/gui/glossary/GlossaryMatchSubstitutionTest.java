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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import org.omegat.tokenizer.DefaultTokenizer;

/**
 * The glossary repair of an inserted fuzzy match (feature requests #1566,
 * #369): source differences whose both sides are glossary terms swap their
 * target terms in the translation; everything else stays untouched.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class GlossaryMatchSubstitutionTest {

    private static final List<GlossaryEntry> GLOSSARY = Arrays.asList(
            entry("behaviour", "comportamiento"), entry("conduct", "conducta"),
            entry("cakes", "los gateaux"), entry("bananas", "las bananas"));

    private static GlossaryEntry entry(String src, String target) {
        return new GlossaryEntry(src, target, "", false, "test");
    }

    private static String substitute(String currentSource, String matchSource, String matchTranslation) {
        return GlossaryMatchSubstitution.substituteIntoMatch(currentSource, matchSource, matchTranslation,
                GLOSSARY, Locale.ENGLISH, Locale.of("es"), new DefaultTokenizer());
    }

    /** The ticket's headline case. */
    @Test
    public void repairsTheDifferingGlossaryTerm() {
        assertEquals("Código de comportamiento",
                substitute("Code of Behaviour", "Code of Conduct", "Código de conducta"));
    }

    /** #369's example: sentence context around the differing term. */
    @Test
    public void repairsInsideProse() {
        assertEquals("j'aime las bananas",
                substitute("I like bananas", "I like cakes", "j'aime los gateaux"));
    }

    /** A difference without glossary coverage inserts the match unchanged. */
    @Test
    public void unresolvedDifferenceLeavesTranslationUntouched() {
        assertEquals("Código de conducta",
                substitute("Code of Practice", "Code of Conduct", "Código de conducta"));
    }

    /** Identical sources need no repair. */
    @Test
    public void identicalSourcesStayUntouched() {
        assertEquals("Código de conducta",
                substitute("Code of Conduct", "Code of Conduct", "Código de conducta"));
    }

    /** Case differences on the source side still find the glossary term. */
    @Test
    public void sourceLookupIsCaseInsensitive() {
        assertEquals("Código de comportamiento",
                substitute("Code of BEHAVIOUR", "Code of Conduct", "Código de conducta"));
    }

    /** Two independent differences repair independently. */
    @Test
    public void repairsSeveralDifferences() {
        assertEquals("comportamiento y las bananas",
                substitute("behaviour and bananas", "conduct and cakes", "conducta y los gateaux"));
    }

    /** A sentence-initial old target keeps its capitalization on the new term. */
    @Test
    public void replacementMatchesCapitalization() {
        assertEquals("Comportamiento del equipo",
                substitute("team behaviour", "team conduct", "Conducta del equipo"));
    }

    /** The old target only matches as a whole word, never inside another one. */
    @Test
    public void replacementRespectsWordBoundaries() {
        // "conducta" must not match inside "conductas" - nothing to repair.
        assertEquals("Las conductas",
                substitute("the behaviour", "the conduct", "Las conductas"));
    }

    /** Pure insertions or deletions have no counterpart and change nothing. */
    @Test
    public void insertionOnlyDifferenceChangesNothing() {
        assertEquals("Código de conducta",
                substitute("Code of Conduct today", "Code of Conduct", "Código de conducta"));
    }
}
