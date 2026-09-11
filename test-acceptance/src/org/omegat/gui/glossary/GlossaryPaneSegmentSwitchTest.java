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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

/**
 * End-to-end guard for the glossary pane half of SF bug #981 ("5.2.0
 * painfully slow"): a segment switch against a glossary that yields hundreds
 * of matches must search and render the pane through the real GUI and EDT
 * within a time budget. Since 5.x every entry was inserted separately into
 * the live StyledDocument, which grew superlinearly with the match count
 * (400 matches = 1.1 s, 800 = 4.5 s); the fix renders into an offline
 * document and swaps it in whole. The searcher and tokenization budgets are
 * guarded unit-level by {@code GlossarySearchLoadTest}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class GlossaryPaneSegmentSwitchTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    /** Match count a long segment reaches against a 12 MB glossary file. */
    private static final int ERROR_MATCHES = 800;
    /** Match count of the second segment, so both switch directions render. */
    private static final int SENTENCE_MATCHES = 400;
    /**
     * Budget for one end-to-end segment switch (glossary search plus pane
     * rendering), best of three rounds, with headroom for loaded CI runners;
     * the unfixed rendering needed 4.5 s for the same match count.
     */
    private static final long SWITCH_RENDER_BUDGET_MS = 1_000;

    @Test
    public void glossaryPaneFollowsSegmentSwitchesWithManyMatches() throws Exception {
        // Merging would collapse the same-source variants into one rendered
        // entry; the reported freeze scales with the rendered entry count.
        Preferences.setPreference(Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS, false);
        openSampleProjectWaitGlossary(prepareProjectWithLargeGlossary());
        robot().waitForIdle();
        assertNotNull(window);

        GlossaryTextArea pane = (GlossaryTextArea) Core.getGlossary();
        // last_entry.properties activates the "Error {0}: {1}" segment
        assertEquals("initially active segment must show every matching variant", ERROR_MATCHES,
                pane.getDisplayedEntries().size());

        int errorEntry = entryNumberContaining("Error {0}");
        int sentenceEntry = entryNumberContaining("sentence one");

        long best = Long.MAX_VALUE;
        for (int round = 0; round < 3; round++) {
            switchAndWaitMs(pane, sentenceEntry);
            assertEquals("pane must follow to the other segment's matches", SENTENCE_MATCHES,
                    pane.getDisplayedEntries().size());
            best = Math.min(best, switchAndWaitMs(pane, errorEntry));
            assertEquals("pane must follow back to the many-match segment", ERROR_MATCHES,
                    pane.getDisplayedEntries().size());
        }

        String text = window.textBox("glossary_text_area").text();
        assertNotNull(text);
        assertTrue("pane must render the variants", text.contains("Error = variant 0"));
        assertTrue("pane must render every variant",
                text.contains("variant " + (ERROR_MATCHES - 1)));

        System.out.println(String.format(Locale.ROOT,
                "SF-981 end-to-end segment switch: %d rendered matches, best of 3 rounds = %d ms",
                ERROR_MATCHES, best));
        assertTrue("one segment switch rendered " + ERROR_MATCHES + " glossary matches in " + best
                + " ms end to end; time budget is " + SWITCH_RENDER_BUDGET_MS + " ms (SF bug #981)",
                best <= SWITCH_RENDER_BUDGET_MS);

        closeProject();
    }

    /**
     * Copy of the sample project whose glossary yields SF-981-magnitude match
     * counts: many target variants of terms the sample segments contain.
     */
    private static Path prepareProjectWithLargeGlossary() throws IOException {
        Path fixture = Files.createTempDirectory("omegat-glossary-load-");
        FileUtils.copyDirectory(PROJECT_PATH.toFile(), fixture.toFile());
        FileUtils.forceDeleteOnExit(fixture.toFile());
        StringBuilder glossary = new StringBuilder("# Glossary in tab-separated format\n");
        for (int i = 0; i < ERROR_MATCHES; i++) {
            glossary.append("Error\tvariant ").append(i).append('\n');
        }
        for (int i = 0; i < SENTENCE_MATCHES; i++) {
            glossary.append("sentence\tphrase ").append(i).append('\n');
        }
        Files.writeString(fixture.resolve("glossary").resolve("glossary.txt"), glossary.toString(),
                StandardCharsets.UTF_8);
        return fixture;
    }

    /** Entry number of the first segment whose source contains the text. */
    private static int entryNumberContaining(String text) {
        for (SourceTextEntry ste : Core.getProject().getAllEntries()) {
            if (ste.getSrcText().contains(text)) {
                return ste.entryNum();
            }
        }
        throw new AssertionError("no segment contains: " + text);
    }

    /**
     * Activates the given segment in the real editor and waits until the
     * glossary pane rendered the segment's matches; the elapsed time is the
     * user-visible switch latency (background search plus EDT rendering).
     */
    private long switchAndWaitMs(GlossaryTextArea pane, int entryNum) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        PropertyChangeListener listener = evt -> latch.countDown();
        pane.addPropertyChangeListener("entries", listener);
        try {
            long t0 = System.nanoTime();
            SwingUtilities.invokeLater(() -> Core.getEditor().gotoEntry(entryNum));
            assertTrue("glossary pane must follow the segment switch",
                    latch.await(timeout, TimeUnit.SECONDS));
            // drain the one-time link refresh the document swap queued
            robot().waitForIdle();
            return (System.nanoTime() - t0) / 1_000_000;
        } finally {
            pane.removePropertyChangeListener("entries", listener);
        }
    }
}
