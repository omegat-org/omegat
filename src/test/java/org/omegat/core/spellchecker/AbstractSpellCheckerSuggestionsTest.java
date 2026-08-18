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

package org.omegat.core.spellchecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;

/**
 * The suggestions cache of the spell checker: a suggestion lookup runs once
 * per word (the editor tooltips ask on every hover), and every event that
 * can change the suggestions empties the cache.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
public class AbstractSpellCheckerSuggestionsTest {

    private File projectDir;

    /** Counts the lookups, so the tests see cache hits and misses. */
    static final class CountingProvider implements ISpellCheckerProvider {
        private int suggestCalls;

        @Override
        public boolean isCorrect(String word) {
            return false;
        }

        @Override
        public List<String> suggest(String word) {
            suggestCalls++;
            return Arrays.asList(word + "-first", word + "-second");
        }

        @Override
        public void learnWord(String word) {
        }

        @Override
        public void destroy() {
        }
    }

    /** Checkers created by the running test, made inert afterwards. */
    private static final List<TestSpellChecker> CREATED = new ArrayList<>();

    static final class TestSpellChecker extends AbstractSpellChecker {
        private final CountingProvider provider = new CountingProvider();
        private volatile boolean inert;

        TestSpellChecker() {
            CREATED.add(this);
        }

        @Override
        protected Optional<ISpellCheckerProvider> initializeWithLanguage(String language) {
            return Optional.of(provider);
        }

        @Override
        public boolean initialize() {
            // The constructor registers project listeners that cannot be
            // unregistered: a leaked instance must stay inert, or it acts
            // on the projects of the following tests and breaks them.
            if (inert) {
                return false;
            }
            return super.initialize();
        }
    }

    @Before
    public final void setUp() throws IOException {
        TestPreferencesInitializer.init();
        projectDir = Files.createTempDirectory("omegat-suggesttest").toFile();
        ProjectProperties props = new ProjectProperties() {
            @Override
            public Language getTargetLanguage() {
                return new Language("de");
            }

            @Override
            public String getProjectInternal() {
                return projectDir.getAbsolutePath();
            }
        };
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
    }

    @After
    public final void tearDown() {
        for (TestSpellChecker checker : CREATED) {
            checker.inert = true;
        }
        FileUtils.deleteQuietly(projectDir);
    }

    @Test
    public void suggestionsAreCachedPerWord() {
        TestSpellChecker checker = new TestSpellChecker();
        assertTrue(checker.initialize());

        List<String> first = checker.suggest("wrod");
        assertEquals(Arrays.asList("wrod-first", "wrod-second"), first);
        assertEquals(first, checker.suggest("wrod"));
        assertEquals("the second lookup of the same word hits the cache", 1,
                checker.provider.suggestCalls);

        checker.suggest("teh");
        assertEquals("a new word runs a new lookup", 2, checker.provider.suggestCalls);
    }

    @Test
    public void wordListChangesEmptyTheCache() {
        TestSpellChecker checker = new TestSpellChecker();
        assertTrue(checker.initialize());

        checker.suggest("wrod");
        checker.learnWord("wordly");
        checker.suggest("wrod");
        assertEquals("a learned word may change the suggestions of its neighbours", 2,
                checker.provider.suggestCalls);

        checker.ignoreWord("wroddy");
        checker.suggest("wrod");
        assertEquals("an ignored word empties the cache too", 3, checker.provider.suggestCalls);
    }

    @Test
    public void destroyEmptiesTheCacheAndStopsSuggesting() {
        TestSpellChecker checker = new TestSpellChecker();
        assertTrue(checker.initialize());

        checker.suggest("wrod");
        checker.destroy();
        assertEquals("without a provider there is nothing to suggest", new ArrayList<String>(),
                checker.suggest("wrod"));
    }
}
