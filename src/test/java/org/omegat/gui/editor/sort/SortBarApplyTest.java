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
package org.omegat.gui.editor.sort;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.EditorSettingsStub;
import org.omegat.gui.editor.EditorStub;
import org.omegat.gui.editor.IEditorSorter;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Language;

/**
 * The apply path of the sort bar: plain sorts are applied synchronously;
 * numeric sorts first run the background preparation pass (the progress-bar
 * scan of all entry texts) and apply the sort when it finishes.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SortBarApplyTest extends TestCore {

    private static final String FILE = "file.txt";

    private volatile IEditorSorter applied;
    private final CountDownLatch appliedLatch = new CountDownLatch(1);

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editor = new EditorStub(new EditorSettingsStub()) {
            @Override
            public void setSort(IEditorSorter sorter) {
                applied = sorter;
                appliedLatch.countDown();
            }

            @Override
            public String getCurrentFile() {
                return FILE;
            }
        };
        TestCoreInitializer.initEditor(editor);
    }

    private void setStubProject() {
        Core.setProject(new StubProject());
    }

    @Test
    public void plainApplyAlsoPreparesInBackground() throws Exception {
        setStubProject();
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);

        SwingUtilities.invokeAndWait(bar::applyPending);
        assertTrue("plain text sorts run the same preparation pass (collation keys)",
                appliedLatch.await(15, TimeUnit.SECONDS));
        assertNotNull(applied);
    }

    @Test
    public void numericApplyPreparesInBackgroundThenSetsSort() throws Exception {
        setStubProject();
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectDir(0, true, true); // numeric ascending

        SwingUtilities.invokeAndWait(bar::applyPending);
        assertTrue("the prepared sort must be applied when the worker finishes",
                appliedLatch.await(15, TimeUnit.SECONDS));

        MultiKeySorter sorter = (MultiKeySorter) applied;
        assertTrue(sorter.getKeys().get(0).numeric);
    }

    @Test
    public void randomApplyMaterializesTheSeedAndShowsTheSymbol() throws Exception {
        setStubProject();
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectRandomDir(0, true, null); // seeded mode with an empty field

        SwingUtilities.invokeAndWait(bar::applyPending);
        assertTrue("the prepared sort must be applied when the worker finishes",
                appliedLatch.await(15, TimeUnit.SECONDS));
        drainEdt();

        MultiKeySorter sorter = (MultiKeySorter) applied;
        assertTrue(sorter.getKeys().get(0).random);
        assertNotNull("an empty seed must be drawn and persisted on apply", sorter.getKeys().get(0).seed);
        assertFalse("the drawn seed must be written back into the field", bar.seedText(0).isEmpty());
        assertTrue("the collapsed summary must mark the seeded random mode",
                bar.buildSummary().endsWith(" ~°"));
    }

    /**
     * The applied latch opens inside setSort, which the worker's done() calls
     * BEFORE finishApply stores the applied keys - all on the EDT. An empty
     * invokeAndWait afterwards both waits for that tail work and establishes
     * the happens-before edge for reading the bar's state from this thread
     * (without it the summary assertions race the EDT and fail on slow CI).
     */
    private static void drainEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    @Test
    public void romanFreeNumericApplyCarriesModeAndSummarySymbol() throws Exception {
        setStubProject();
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectDir(0, true, true, true); // numeric ascending without Roman numerals

        SwingUtilities.invokeAndWait(bar::applyPending);
        assertTrue("the prepared sort must be applied when the worker finishes",
                appliedLatch.await(15, TimeUnit.SECONDS));
        drainEdt();

        MultiKeySorter sorter = (MultiKeySorter) applied;
        assertTrue(sorter.getKeys().get(0).numeric);
        assertTrue(sorter.getKeys().get(0).ignoreRoman);
        assertTrue("the collapsed summary must show the Roman-free numeric symbol",
                bar.buildSummary().endsWith(" 1↑"));
    }

    @Test
    public void applyWithoutProjectIsANoop() {
        Core.setProject(new NotLoadedProject());
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);

        bar.applyPending();

        assertNull(applied);
    }

    /** A loaded project with one file of three entries, enough for the prepare pass. */
    private static class StubProject extends NotLoadedProject {

        private final ProjectProperties props = new StubProps();
        private final List<FileInfo> files = new ArrayList<>();

        StubProject() {
            IProject.FileInfo fi = new IProject.FileInfo(FILE);
            for (int i = 1; i <= 3; i++) {
                fi.entries.add(new SourceTextEntry(
                        new EntryKey(FILE, "Chapter " + i, null, "", "", null), i, null, null,
                        Collections.emptyList()));
            }
            files.add(fi);
        }

        @Override
        public boolean isProjectLoaded() {
            return true;
        }

        @Override
        public ProjectProperties getProjectProperties() {
            return props;
        }

        @Override
        public List<FileInfo> getProjectFiles() {
            return files;
        }
    }

    private static class StubProps extends ProjectProperties {
        @Override
        public Language getSourceLanguage() {
            return new Language("en");
        }

        @Override
        public Language getTargetLanguage() {
            return new Language("de");
        }
    }
}
