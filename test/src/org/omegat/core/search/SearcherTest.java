/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022-2025 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.omegat.core.search.SearchExpression.SearchExpressionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;
import org.omegat.util.TestPreferencesInitializer;

public class SearcherTest {

    private File tempDir;
    private RealProjectWithTMX proj;
    private IProject.FileInfo fi;

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Before
    public void preUp() throws Exception {
        tempDir = Files.createTempDirectory("omegat").toFile();
        TestPreferencesInitializer.init();
        ProjectProperties props = new ProjectProperties(tempDir);
        props.setSupportDefaultTranslations(true);
        props.setTargetTokenizer(DefaultTokenizer.class);
        proj = new RealProjectWithTMX(props);
        Core.setProject(proj);
        fi = new IProject.FileInfo();
        proj.getProjectFilesList().add(fi);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testSearchStringExactMatch() throws Exception {
        addSTE(fi, "id1", "OmegaT is great", null);
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT is great"));
        assertFalse(searcher.searchString("omegat is great")); // case-sensitive
    }

    @Test
    public void testSearchStringKeywordMatch() throws Exception {
        addSTE(fi, "id1", "OmegaT is great software", null);
        SearchExpression s = createSearchExpression("great software", SearchExpressionType.KEYWORD, false, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("great software"));
        assertTrue(searcher.searchString("OmegaT is great software"));
        assertFalse(searcher.searchString("OmegaT is average software"));
    }

    @Test
    public void testSearchReplaceExactMatch() throws Exception {
        SearchExpression s = createSearchExpression("great", SearchExpressionType.EXACT, false, false);
        s.mode = SearchMode.REPLACE;
        s.replacement = "awesome";
        List<SearchMatch> matches = executeSearchReplace("Great things are great indeed.", s);
        assertEquals(2, matches.size());
        assertEquals("awesome", matches.get(0).getReplacement());
        assertEquals("awesome", matches.get(1).getReplacement());
    }

    @Test
    public void testSearchReplaceRegexMatch() throws Exception {
        SearchExpression s = createSearchExpression("(\\d+) apples", SearchExpressionType.REGEXP,
                false, false);
        s.mode = SearchMode.REPLACE;
        s.replacement = "$1 bananas";
        List<SearchMatch> matches = executeSearchReplace("I have 5 apples and 10 apples.", s);
        assertEquals(2, matches.size());
        assertEquals("5 bananas", matches.get(0).getReplacement());
        assertEquals("10 bananas", matches.get(1).getReplacement());
    }

    @Test
    public void testSearchReplaceKeywordNotSupported() throws Exception {
        SearchExpression s = createSearchExpression("great", SearchExpressionType.KEYWORD, false, false);
        s.mode = SearchMode.REPLACE;
        s.replacement = "awesome";
        List<SearchMatch> matches = executeSearchReplace("Great things are great indeed.", s);
        assertEquals(2, matches.size());
    }

    private List<SearchMatch> executeSearchReplace(String inputText, SearchExpression s) throws Exception {
        Searcher searcher = new Searcher(proj, s);
        searcher.setThread(new SearchTestThread());
        searcher.search();
        searcher.searchString(inputText);
        return searcher.getFoundMatches();
    }

    @Test
    public void testSearchStringRegexMatch() throws Exception {
        addSTE(fi, "id1", "OmegaT version 4.3.2", null);
        SearchExpression s = createSearchExpression("version \\d+\\.\\d+\\.\\d+", SearchExpressionType.REGEXP, false, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT version 4.3.2"));
        assertFalse(searcher.searchString("OmegaT version 4.3")); // incomplete match
    }

    @Test
    public void testSearchStringWidthInsensitive() throws Exception {
        addSTE(fi, "id1", "OmegaT\u2009is\u2009great", null); // Using narrow no-break spaces
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, false, true); // width-insensitive
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT is great")); // width-insensitive match
    }

    @Test
    public void testSearch() throws Exception {
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s");
        SearchExpression s = createSearchExpression("list", SearchExpressionType.KEYWORD, false, true);
        Searcher searcher = startSearcher(s);
        List<SearchResultEntry> result = searcher.getSearchResults();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetExpressionExactMatch() throws Exception {
        SearchExpression expression = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(expression);
        assertSame(expression, searcher.getExpression());
    }

    @Test
    public void testGetExpressionKeywordMatch() throws Exception {
        SearchExpression expression = createSearchExpression("great software", SearchExpressionType.KEYWORD, false, false);
        Searcher searcher = startSearcher(expression);
        assertSame(expression, searcher.getExpression());
    }

    @Test
    public void testGetExpressionRegexMatch() throws Exception {
        SearchExpression expression = createSearchExpression("version \\d+\\.\\d+\\.\\d+", SearchExpressionType.REGEXP, false, true);
        Searcher searcher = startSearcher(expression);
        assertSame(expression, searcher.getExpression());
    }

    @Test
    public void testSearchStringEmptyInput() throws Exception {
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);
        assertFalse(searcher.searchString("")); // Should return false for empty input
    }

    @Test
    public void testSearchStringNullInput() throws Exception {
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);
        assertFalse(searcher.searchString(null)); // Should return false for null input
    }

    @Test
    public void testSearchStringNoMatch() throws Exception {
        addSTE(fi, "id1", "OmegaT is fantastic", null);
        SearchExpression s = createSearchExpression("awesome", SearchExpressionType.EXACT, false, false);
        Searcher searcher = startSearcher(s);
        assertFalse(searcher.searchString("OmegaT is fantastic")); // No match expected
    }

    @Test
    public void testSearchStringPartialRegexMatch() throws Exception {
        addSTE(fi, "id1", "OmegaT version 4.3.2-beta", null);
        SearchExpression s = createSearchExpression("version \\d+\\.\\d+\\.\\d+", SearchExpressionType.REGEXP, false, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT version 4.3.2-beta")); // Partial version match is valid
    }

    @Test
    public void testSearchStringMultipleMatches() throws Exception {
        addSTE(fi, "id1", "OmegaT is great, OmegaT helps you translate", null);
        SearchExpression s = createSearchExpression("OmegaT", SearchExpressionType.KEYWORD, false, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT is great, OmegaT helps you translate"));
        assertEquals(2, searcher.getFoundMatches().size()); // Two matches expected
    }

    @Test
    public void testSearchStringCollapseResults() throws Exception {
        addSTE(fi, "id1", "OmegaT OmegaT OmegaT", null);
        SearchExpression s = createSearchExpression("OmegaT", SearchExpressionType.KEYWORD, false, false);
        Searcher searcher = startSearcher(s);
        assertTrue(searcher.searchString("OmegaT OmegaT OmegaT", true)); // Ensure collapseResults is true
        assertEquals(3, searcher.getFoundMatches().size()); // All matches should collapse into one
    }

    @Test
    public void testGetSearchResultsEmpty() throws Exception {
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);
        List<SearchResultEntry> results = searcher.getSearchResults();
        assertTrue(results.isEmpty()); // No search has been executed yet
    }

    @Test
    public void testGetSearchResultsExactMatch() throws Exception {
        setTranslation(addSTE(fi, "id1", "OmegaT is great", "OmegaT est génial"));
        setTranslation(addSTE(fi, "id2", "OmegaT is useful", "OmegaT est utile"));
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);

        List<SearchResultEntry> results = searcher.getSearchResults();
        assertEquals(2, results.size());
        assertNull(results.get(0).getPreamble());
        assertEquals("OmegaT is great", results.get(0).getSrcText());
        assertEquals("OmegaT est génial", results.get(0).getTranslation());
        assertEquals("Orphan segments", results.get(1).getPreamble());
        assertEquals("OmegaT is great", results.get(1).getSrcText());
    }

    @Test
    public void testGetSearchResultsKeywordMatch() throws Exception {
        setTranslation(addSTE(fi, "id1", "OmegaT is great software", "OmegaT est un génial logiciel"));
        setTranslation(addSTE(fi, "id2", "Great tools are appreciated", "Les bons outils sont appréciés"));
        SearchExpression s = createSearchExpression("great software", SearchExpressionType.KEYWORD, false, false);
        Searcher searcher = startSearcher(s);

        List<SearchResultEntry> results = searcher.getSearchResults();
        assertEquals(2, results.size());
        assertEquals("OmegaT is great software", results.get(0).getSrcText());
        assertEquals("OmegaT est un génial logiciel", results.get(0).getTranslation());
        assertEquals(OStrings.getString("CT_ORPHAN_STRINGS"), results.get(1).getPreamble());
        assertEquals("OmegaT is great software", results.get(1).getSrcText());
    }

    @Test
    public void testGetSearchResultsAfterModification() throws Exception {
        setTranslation(addSTE(fi, "id1", "OmegaT is great", "OmegaT est génial"));
        SearchExpression s = createSearchExpression("OmegaT is great", SearchExpressionType.EXACT, true, false);
        Searcher searcher = startSearcher(s);
        searcher.search();

        List<SearchResultEntry> initialResults = searcher.getSearchResults();
        assertEquals(2, initialResults.size());

        setTranslation(addSTE(fi, "id2", "OmegaT is fantastic", "OmegaT est fantastique"));
        searcher.search();

        List<SearchResultEntry> updatedResults = searcher.getSearchResults();
        assertEquals(2, updatedResults.size());
    }

    @Test
    public void testGetSearchResultsHandlesDuplicates() throws Exception {
        setTranslation(addSTE(fi, "id1", "Duplicate entry", "Entrée dupliquée"));
        setTranslation(addSTE(fi, "id2", "Duplicate entry", "Entrée dupliquée"));

        SearchExpression s = createSearchExpression("Duplicate entry", SearchExpressionType.EXACT, true, false,
                false);
        Searcher searcher = startSearcher(s);
        searcher.search();

        // Verify results
        List<SearchResultEntry> results = searcher.getSearchResults();
        assertEquals(2, results.size());
        assertEquals(OStrings.getString("SW_NR_MATCHES", 2), results.get(0).getPreamble());
        assertEquals("Duplicate entry", results.get(0).getSrcText());
        assertEquals(OStrings.getString("CT_ORPHAN_STRINGS"), results.get(1).getPreamble());
        assertEquals("Duplicate entry", results.get(1).getSrcText());
    }

    private static @NotNull SearchExpression createSearchExpression(String text, SearchExpressionType type,
                                                                    boolean caseSensitive, boolean widthInsensitive) {
        return createSearchExpression(text, type, caseSensitive, widthInsensitive, true);
    }

    private static @NotNull SearchExpression createSearchExpression(String text, SearchExpressionType type,
                                                                    boolean caseSensitive, boolean widthInsensitive,
                                                                    boolean allResults) {
        SearchExpression s = new SearchExpression();
        s.text = text;
        s.searchExpressionType = type;
        s.mode = SearchMode.SEARCH;
        s.glossary = false;
        s.memory = true;
        s.tm = true;
        s.allResults = allResults;
        s.fileNames = true;
        s.caseSensitive = caseSensitive;
        s.spaceMatchNbsp = false;
        s.searchSource = true;
        s.searchTarget = true;
        s.searchTranslated = true;
        s.searchUntranslated = true;
        s.widthInsensitive = widthInsensitive;
        s.excludeOrphans = false;
        s.replacement = null;
        return s;
    }

    private @NotNull Searcher startSearcher(SearchExpression s) throws Exception {
        Searcher searcher = new Searcher(proj, s);
        searcher.setThread(new SearchTestThread());
        searcher.search();
        return searcher;
    }

    private SourceTextEntry addSTE(IProject.FileInfo fi, String id, String source, String translation) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size() + 1, null, translation,
                new ArrayList<>());
        ste.setSourceTranslationFuzzy(false);
        fi.entries.add(ste);
        proj.getAllEntries().add(ste);
        return ste;
    }

    private void setTranslation(SourceTextEntry ste) {
        PrepareTMXEntry prepareTMXEntry = new PrepareTMXEntry();
        prepareTMXEntry.source = ste.getSrcText();
        prepareTMXEntry.translation = ste.getSourceTranslation();
        proj.setTranslation(ste, prepareTMXEntry, true, null);
    }

    protected static class RealProjectWithTMX extends RealProject {
        public RealProjectWithTMX(ProjectProperties props) {
            super(props);
        }

        public ProjectTMX getTMX() {
            return projectTMX;
        }

        public List<FileInfo> getProjectFilesList() {
            return projectFilesList;
        }
    }

    static class SearchTestThread extends LongProcessThread {
        @Override
        public void run() {
            try {
                checkInterrupted();
            } catch (Exception ignored) {
                // do not raise exception for the test
            }
        }
    }
}
