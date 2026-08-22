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

package org.omegat.core.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Characterization of how fuzzy matching treats numbers today, as groundwork
 * for feature request #465 (consider numbers for fuzzy matches).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class FindMatchesNumbersTest {

    /** Resolved from the test classpath so tree reorganizations cannot break it. */
    private static final File TMX_NUMBERS = fixture("/data/tmx/test-numbers-en-cs.tmx");

    private static File fixture(String resource) {
        java.net.URL url = FindMatchesNumbersTest.class.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("fixture missing on test classpath: " + resource);
        }
        try {
            return new File(url.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
    }

    @Before
    public void setUp() throws Exception {
        Core.initializeConsole();
        Core.registerTokenizerClass(DefaultTokenizer.class);
        Core.registerTokenizerClass(LuceneEnglishTokenizer.class);
        TestPreferencesInitializer.init();
    }

    private List<NearString> search(String srcText) {
        return search(srcText, false);
    }

    private List<NearString> search(String srcText, boolean matchNumbers) {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("cs");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(true);
        prop.setMatchNumbersEnabled(matchNumbers);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IProject project = new FindMatchesTest.TestProject(prop, TMX_NUMBERS, null,
                new LuceneEnglishTokenizer(), new DefaultTokenizer(), segmenter);
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        return finder.search(srcText, false, iStopped, false);
    }

    private void assertScores(NearString near, int stem, int noStem, int adjusted) {
        assertEquals(stem, near.scores[0].score);
        assertEquals(noStem, near.scores[0].scoreNoStem);
        assertEquals(adjusted, near.scores[0].adjustedScore);
    }

    /**
     * The ticket's headline case. The word-token passes see no tokens at all
     * in a number-only segment, so the primary and secondary scores are 0; the
     * match only survives because the verbatim pass counts the shared
     * punctuation and year/month tokens.
     */
    @Test
    public void numberOnlyDateSegment() {
        List<NearString> result = search("2007-11-25");
        assertEquals(1, result.size());
        assertScores(result.get(0), 0, 0, 80);
        assertEquals("21. listopadu 2007", result.get(0).translation);
    }

    /**
     * A bare integer is a single verbatim token, so one differing digit kills
     * the whole score: no match at all, although the TM holds a number-only
     * entry whose translation shows the target-locale number format.
     */
    @Test
    public void numberOnlyIntegerSegment() {
        List<NearString> result = search("88888");
        assertTrue(result.isEmpty());
    }

    /**
     * Numbers are invisible to the word-token passes, so prose differing only
     * in the contained date still reports a 100% primary score.
     */
    @Test
    public void proseDifferingOnlyInNumber() {
        List<NearString> result = search("The meeting takes place on 2008-01-30 in the main hall.");
        assertEquals(1, result.size());
        assertScores(result.get(0), 100, 100, 87);
    }

    /**
     * A Roman numeral and its Arabic rendering carry the same value but never
     * match as tokens.
     */
    @Test
    public void romanNumeralAgainstSameChapter() {
        List<NearString> result = search("Chapter 12");
        assertEquals(1, result.size());
        assertScores(result.get(0), 50, 50, 66);
        assertEquals("Kapitola XII", result.get(0).translation);
    }

    // --- the same cases with the match-numbers project option enabled ------

    /**
     * With the option on, two number-only dates compare as numbers of the same
     * shape: full placeholder similarity minus the number-only penalty, and
     * the verbatim pass sees the differing values.
     */
    @Test
    public void numberOnlyDateSegmentWithOption() {
        List<NearString> result = search("2007-11-25", true);
        assertEquals(1, result.size());
        assertScores(result.get(0), 90, 90, 80);
        assertEquals("21. listopadu 2007", result.get(0).translation);
    }

    /** With the option on, a differing bare integer surfaces as a fuzzy match. */
    @Test
    public void numberOnlyIntegerSegmentWithOption() {
        List<NearString> result = search("88888", true);
        assertEquals(1, result.size());
        assertScores(result.get(0), 90, 90, 0);
        assertEquals("99 999", result.get(0).translation);
    }

    /**
     * With the option on, the word passes still ignore numbers, but the
     * verbatim pass now equates the value-identical Roman and Arabic
     * renderings.
     */
    @Test
    public void romanNumeralAgainstSameChapterWithOption() {
        List<NearString> result = search("Chapter 12", true);
        assertEquals(1, result.size());
        assertScores(result.get(0), 50, 50, 100);
        assertEquals("Kapitola XII", result.get(0).translation);
    }

    /** Prose scores are not touched by the option when the values differ. */
    @Test
    public void proseDifferingOnlyInNumberWithOption() {
        List<NearString> result = search("The meeting takes place on 2008-01-30 in the main hall.", true);
        assertEquals(1, result.size());
        assertScores(result.get(0), 100, 100, 87);
    }
}
