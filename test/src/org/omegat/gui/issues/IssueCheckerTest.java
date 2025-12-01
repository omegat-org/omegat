/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.data.CoreState;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.data.TMXEntryFactoryForTest;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.tagvalidation.ITagValidation;
import org.omegat.core.data.TestCoreState;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Functional tests for IssueChecker aggregation and filtering behavior.
 *
 * We verify:
 * - Aggregation of Tag issues and Provider issues
 * - File pattern filtering
 * - Duplicate filtering when filterDuplicates flag is enabled
 */
public class IssueCheckerTest {

    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";

    private static List<SourceTextEntry> entries;
    private static Map<SourceTextEntry, TMXEntry> translations;

    /**
     * Simple provider that emits one TestingIssue per translated entry.
     */
    private static class TestProvider implements IIssueProvider {
        @Override
        public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
            return tmxEntry.isTranslated() ? List.of(new TestingIssue(sourceEntry, tmxEntry)) : List.of();
        }

        @Override
        public String getId() {
            return "testprovider";
        }

        @Override
        public String getName() {
            return "Test Provider";
        }
    }

    /**
     * Fake tag validation that returns exactly one tag error for the first entry in FILE2
     * when the pattern matches that file.
     */
    private static class FakeTagValidation implements ITagValidation {
        @Override
        public List<ErrorReport> listInvalidTags() {
            return listInvalidTags(".*");
        }

        @Override
        public List<ErrorReport> listInvalidTags(String sourcePattern) {
            Pattern p = Pattern.compile(sourcePattern);
            // Find first entry from FILE2
            for (SourceTextEntry ste : entries) {
                if (FILE2.equals(ste.getKey().file) && p.matcher(FILE2).find()) {
                    TMXEntry tmx = translations.get(ste);
                    return List.of(new ErrorReport(ste, tmx));
                }
            }
            return Collections.emptyList();
        }

        @Override
        public boolean checkInvalidTags(SourceTextEntry ste) {
            return true;
        }

        @Override
        public void logTagValidationErrors(List<ErrorReport> invalidTagsEntries) {
            // no-op for tests
        }
    }

    /**
     * Minimal project that serves a fixed list of entries and translations.
     */
    private static class StubProject extends NotLoadedProject {
        @Override
        public boolean isProjectLoaded() {
            return true;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            return entries;
        }

        @Override
        public TMXEntry getTranslationInfo(SourceTextEntry ste) {
            return translations.get(ste);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        TestPreferencesInitializer.init();
        // Disable built-in providers to keep test deterministic
        String disabled = SpellingIssueProvider.class.getCanonicalName() + ","
                + TerminologyIssueProvider.class.getCanonicalName();
        org.omegat.util.Preferences.setPreference(org.omegat.util.Preferences.ISSUE_PROVIDERS_DISABLED, disabled);

        // Reset core state and install our stubs
        TestCoreState.resetState();

        // Build entries
        entries = new ArrayList<>();
        translations = new HashMap<>();

        // Entry 1: FILE1, source "HELLO"
        SourceTextEntry e1 = new SourceTextEntry(new EntryKey(FILE1, "HELLO", "id1", "", "", null), 1,
                null, null, Collections.emptyList());
        TMXEntry t1 = new TMXEntryFactoryForTest().setSource("HELLO").setTranslation("Bonjour").build();
        translations.put(e1, t1);
        entries.add(e1);

        // Entry 2: FILE1, source "WORLD"
        SourceTextEntry e2 = new SourceTextEntry(new EntryKey(FILE1, "WORLD", "id2", "", "", null), 2,
                null, null, Collections.emptyList());
        TMXEntry t2 = new TMXEntryFactoryForTest().setSource("WORLD").setTranslation("Monde").build();
        translations.put(e2, t2);
        entries.add(e2);

        // Entry 3: FILE2, source "DUP" (FIRST of duplicate)
        SourceTextEntry e3 = new SourceTextEntry(new EntryKey(FILE2, "DUP", "id3", "", "", null), 3,
                null, null, Collections.emptyList());
        TMXEntry t3 = new TMXEntryFactoryForTest().setSource("DUP").setTranslation("Dup1").build();
        translations.put(e3, t3);
        entries.add(e3);

        // Entry 4: FILE2, source "DUP" (NEXT duplicate) – mark as NEXT via reflection
        SourceTextEntry e4 = new SourceTextEntry(new EntryKey(FILE2, "DUP", "id4", "", "", null), 4,
                null, null, Collections.emptyList());
        // Mark as duplicate of e3 by setting firstInstance via reflection
        try {
            java.lang.reflect.Field fi = SourceTextEntry.class.getDeclaredField("firstInstance");
            fi.setAccessible(true);
            fi.set(e4, e3);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        TMXEntry t4 = new TMXEntryFactoryForTest().setSource("DUP").setTranslation("Dup2").build();
        translations.put(e4, t4);
        entries.add(e4);

        // Install project and tag validation
        CoreState.getInstance().setProject(new StubProject());
        CoreState.getInstance().setTagValidation(new FakeTagValidation());

        // Register test provider
        IssueProviders.addIssueProvider(new TestProvider());
    }

    @Test
    public void testCollectIssuesAggregatesTagAndProvider() {
        List<IIssue> issues = IssueChecker.collectIssues(".*", false);

        // Provider issues should equal number of translated entries (4)
        long providerCount = issues.stream().filter(i -> i instanceof TestingIssue).count();
        // Tag issues should be exactly 1 from FakeTagValidation
        long tagCount = issues.stream().filter(i -> i instanceof TagIssue).count();

        assertEquals(4, providerCount);
        assertEquals(1, tagCount);
        assertEquals(5, issues.size());
    }

    @Test
    public void testFilePatternFiltersEntries() {
        // Only FILE1 issues: 2 provider issues; no tag issue since FakeTagValidation reports for FILE2
        String pattern = Pattern.quote(FILE1);
        List<IIssue> issues = IssueChecker.collectIssues(pattern, false);

        long providerCount = issues.stream().filter(i -> i instanceof TestingIssue).count();
        long tagCount = issues.stream().filter(i -> i instanceof TagIssue).count();

        assertEquals(2, providerCount);
        assertEquals(0, tagCount);
        assertEquals(2, issues.size());
    }

    @Test
    public void testDuplicateFiltering() {
        // Without duplicate filtering, provider issues = 4 (includes e4 which is NEXT duplicate)
        List<IIssue> allIssues = IssueChecker.collectIssues(".*", false);
        long providerAll = allIssues.stream().filter(i -> i instanceof TestingIssue).count();

        // With duplicate filtering on, provider issues should drop by 1 (exclude e4)
        List<IIssue> filteredIssues = IssueChecker.collectIssues(".*", true);
        long providerFiltered = filteredIssues.stream().filter(i -> i instanceof TestingIssue).count();

        assertEquals(4, providerAll);
        assertEquals(3, providerFiltered);

        // Tag issues unaffected by duplicate filtering (still 1 for FILE2)
        long tagAll = allIssues.stream().filter(i -> i instanceof TagIssue).count();
        long tagFiltered = filteredIssues.stream().filter(i -> i instanceof TagIssue).count();
        assertTrue(tagAll == 1 && tagFiltered == 1);
    }
}
