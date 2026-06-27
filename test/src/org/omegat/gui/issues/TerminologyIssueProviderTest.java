/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 fung911
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

package org.omegat.gui.issues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.omegat.gui.glossary.GlossaryEntry;

/**
 * Tests for TerminologyIssueProvider.
 *
 * Verifies that glossary entries with an empty target term are correctly
 * identified and excluded from the terminology check (bug #1315).
 */
public class TerminologyIssueProviderTest {

    /**
     * Helper: invoke the private static hasNonEmptyTargetTerms() via reflection.
     */
    private boolean hasNonEmptyTargetTerms(GlossaryEntry entry) throws Exception {
        Method m = TerminologyIssueProvider.class
                .getDeclaredMethod("hasNonEmptyTargetTerms", GlossaryEntry.class);
        m.setAccessible(true);
        return (Boolean) m.invoke(null, entry);
    }

    /**
     * A glossary entry whose target term is empty (e.g. "source\t\tcomment" in
     * a TSV file) must return false — it should be excluded from the check.
     */
    @Test
    public void testEmptyTargetTermReturnsFalse() throws Exception {
        GlossaryEntry entry = new GlossaryEntry("backend", "", "comment", false, "glossary.txt");
        assertFalse(hasNonEmptyTargetTerms(entry));
    }

    /**
     * A glossary entry with a real target term must return true — it should be
     * included in the terminology check.
     */
    @Test
    public void testNonEmptyTargetTermReturnsTrue() throws Exception {
        GlossaryEntry entry = new GlossaryEntry("server", "serveur", "", true, "glossary.txt");
        assertTrue(hasNonEmptyTargetTerms(entry));
    }

    /**
     * A glossary entry where every target term is an empty string (multi-target
     * case) must return false.
     */
    @Test
    public void testAllTargetTermsEmptyReturnsFalse() throws Exception {
        GlossaryEntry entry = new GlossaryEntry(
                "source",
                new String[]{"", ""},
                new String[]{"comment1", "comment2"},
                new boolean[]{false, false},
                new String[]{"glossary.txt", "glossary.txt"});
        assertFalse(hasNonEmptyTargetTerms(entry));
    }

    /**
     * A glossary entry where at least one target term is non-empty must return
     * true, even if other target terms are empty.
     */
    @Test
    public void testPartiallyEmptyTargetTermsReturnsTrue() throws Exception {
        GlossaryEntry entry = new GlossaryEntry(
                "source",
                new String[]{"", "serveur"},
                new String[]{"", ""},
                new boolean[]{false, false},
                new String[]{"glossary.txt", "glossary.txt"});
        assertTrue(hasNonEmptyTargetTerms(entry));
    }
}
