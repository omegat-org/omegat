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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.MatchEquivalence;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Character equivalence classes in fuzzy matching (feature request #1681):
 * segments differing only in quote, apostrophe, dash, space or invisible
 * format variants match at 100% with the default class set, and keep their
 * distance when the class is disabled.
 *
 * @author Stephan Pakebusch
 */
public class FindMatchesEquivalenceTest {

    /** Resolved from the test classpath so tree reorganizations cannot break it. */
    private static final File TMX_EQUIVALENCE = fixture("/data/tmx/test-equivalence-en-de.tmx");

    private static File fixture(String resource) {
        java.net.URL url = FindMatchesEquivalenceTest.class.getResource(resource);
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

    private List<NearString> search(String srcText, Set<MatchEquivalence> disabled) {
        ProjectProperties prop = new ProjectProperties(tmpDir.toFile());
        prop.setSourceLanguage("en");
        prop.setTargetLanguage("de");
        prop.setSupportDefaultTranslations(true);
        prop.setSentenceSegmentingEnabled(true);
        prop.setDisabledMatchEquivalences(disabled);
        Segmenter segmenter = new Segmenter(Preferences.getSRX());
        IProject project = new FindMatchesTest.TestProject(prop, TMX_EQUIVALENCE, null,
                new LuceneEnglishTokenizer(), new DefaultTokenizer(), segmenter);
        IStopped iStopped = () -> false;
        FindMatches finder = new FindMatches(project, segmenter, OConsts.MAX_NEAR_STRINGS, false, 30);
        return finder.search(srcText, false, iStopped, false);
    }

    private NearString bestMatch(String srcText, Set<MatchEquivalence> disabled) {
        List<NearString> result = search(srcText, disabled);
        assertTrue("no match found for: " + srcText, !result.isEmpty());
        return result.get(0);
    }

    private static final Set<MatchEquivalence> NONE_DISABLED = EnumSet.noneOf(MatchEquivalence.class);

    private void assertScores(NearString near, int stem, int noStem, int adjusted) {
        assertEquals(stem, near.scores[0].score);
        assertEquals(noStem, near.scores[0].scoreNoStem);
        assertEquals(adjusted, near.scores[0].adjustedScore);
    }

    /** Straight double quotes match the TM entry with curly quotes (#1681). */
    @Test
    public void straightQuotesMatchCurlyQuotes() {
        NearString near = bestMatch("Select the \"Save\" command from the menu.", NONE_DISABLED);
        assertEquals("Wählen Sie den Befehl „Speichern“ im Menü.", near.translation);
        assertScores(near, 100, 100, 100);
    }

    /** With the quotes class disabled, the verbatim pass sees the difference. */
    @Test
    public void disabledQuotesClassKeepsDistance() {
        NearString near = bestMatch("Select the \"Save\" command from the menu.",
                EnumSet.of(MatchEquivalence.QUOTES));
        assertEquals(100, near.scores[0].score);
        assertTrue("adjusted score must drop below 100 with quotes class disabled",
                near.scores[0].adjustedScore < 100);
    }

    /** Single quotes do not match the double-quoted TM entry (#1681). */
    @Test
    public void singleQuotesDoNotMatchDoubleQuotes() {
        NearString near = bestMatch("Select the 'Save' command from the menu.", NONE_DISABLED);
        assertEquals(100, near.scores[0].score);
        assertTrue("single quotes must not fold onto double quotes",
                near.scores[0].adjustedScore < 100);
    }

    /**
     * The typewriter apostrophe matches the curly one, also in the word-token
     * passes where the apostrophe sits inside the token.
     */
    @Test
    public void straightApostropheMatchesCurlyApostrophe() {
        NearString near = bestMatch("It's John's house near the river.", NONE_DISABLED);
        assertScores(near, 100, 100, 100);
    }

    @Test
    public void disabledQuotesClassKeepsApostropheDistance() {
        // Apostrophe folding is part of the quotes class. The word-token
        // passes still match: the Lucene analyzer normalizes possessives
        // itself, so the distance shows in the verbatim pass.
        NearString near = bestMatch("It's John's house near the river.",
                EnumSet.of(MatchEquivalence.QUOTES));
        assertTrue("adjusted score must drop with apostrophe folding disabled",
                near.scores[0].adjustedScore < 100);
    }

    @Test
    public void hyphenMatchesEnDash() {
        NearString near = bestMatch("See pages 3-4 for details.", NONE_DISABLED);
        assertScores(near, 100, 100, 100);
    }

    @Test
    public void plainSpaceMatchesNoBreakSpace() {
        NearString near = bestMatch("The discount is 10 % this week.", NONE_DISABLED);
        assertScores(near, 100, 100, 100);
    }

    @Test
    public void softHyphenIsIgnored() {
        NearString near = bestMatch("More information is available online.", NONE_DISABLED);
        assertScores(near, 100, 100, 100);
    }
}
