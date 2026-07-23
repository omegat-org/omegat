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
package org.omegat.gui.editor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.po.PoFilter;
import org.omegat.gui.editor.sort.SortBar;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;

/**
 * End-to-end resize-cost characterization for the editor pane with the sort
 * bar. Unlike the pure layout micro-benchmark in
 * {@code org.omegat.gui.editor.sort.SortBarResizePerformanceTest}, this drives
 * the real component stack (dockable pane, north bars, scroll pane, editor
 * document) through a pixel-wise width sweep and forces the text layout to
 * re-wrap at every width, as painting during an interactive window resize
 * does. The expanded sort bar must not make that sweep measurably more
 * expensive than the collapsed one; the dominant cost (re-wrapping the
 * document) is the same in both states.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorResizePerformanceTest extends TestCore {

    private static final int ENTRY_COUNT = 400;
    private static final int WARMUP_PASSES = 1;
    private static final int MEASURED_PASSES = 2;
    private static final int WIDTH_MIN = 600;
    private static final int WIDTH_MAX = 1400;
    private static final int WIDTH_STEP = 16;

    private EditorController editorController;
    private File projectRootDir;

    private final Language sourceLang = new Language("en");
    private final Language targetLang = new Language("pl");

    @BeforeClass
    public static void setUpBeforeClass() {
        assumeNotHeadless();
        org.junit.Assume.assumeTrue(
                "Skipping performance test: wall-clock timings are unreliable on CI runners",
                System.getenv("CI") == null && System.getenv("TF_BUILD") == null);
    }

    private static void assumeNotHeadless() {
        org.junit.Assume.assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
    }

    @Before
    public final void setUp() throws IOException {
        projectRootDir = Files.createTempDirectory("omegat").toFile();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(PoFilter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
    }

    @Test
    public void expandedSortBarDoesNotSlowDownResize() throws Exception {
        TestProjectProperties props = new TestProjectProperties();
        props.setProjectRoot(projectRootDir.getAbsolutePath());
        // Project-change listeners left registered by earlier tests in the
        // same JVM (e.g. a ProjectFilesListController) render these roots
        // while our LOAD event is dispatched; leaving them unset aborts the
        // event chain with an error dialog before the editor builds its
        // document.
        props.setSourceRoot(new File(projectRootDir, "source").getAbsolutePath() + File.separator);
        props.setTargetRoot(new File(projectRootDir, "target").getAbsolutePath() + File.separator);
        props.setGlossaryRoot(new File(projectRootDir, "glossary").getAbsolutePath() + File.separator);
        props.setWriteableGlossary(new File(new File(projectRootDir, "glossary"), "glossary.txt").getAbsolutePath());
        props.setTMRoot(new File(projectRootDir, "tm").getAbsolutePath() + File.separator);
        props.setDictRoot(new File(projectRootDir, "dictionary").getAbsolutePath() + File.separator);
        props.setSupportDefaultTranslations(false);
        props.setTargetTokenizer(DefaultTokenizer.class);
        TestCoreInitializer.initNotes(new EditorControllerTest.MyNotes());
        Core.setProject(new ManyEntriesProject(props));
        fireLoadProjectEvent();
        assertNotNull(editorController.editor.getOmDocument());

        Component top = topLevelAncestor(editorController.editor);
        SortBar bar = findSortBar(top);
        assertNotNull("sort bar must be part of the editor pane", bar);
        assertTrue("sort bar must be visible for a multi-segment document", bar.isVisible());

        // Swing lays out a container only when it is displayable, so host the
        // pane in a never-shown frame; addNotify creates the peers without
        // putting a window on screen. The whole measurement runs on the EDT,
        // where interactive resize layout happens too (and where the text
        // views' shared flow strategy is safe to drive).
        long[] ms = new long[2];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.add(top);
            frame.setSize(WIDTH_MIN, 900);
            frame.addNotify();
            frame.validate();
            assertTrue("editor must receive a real width from the layout, got "
                    + editorController.editor.getWidth(), editorController.editor.getWidth() > 0);
            System.out.println(String.format(Locale.ROOT,
                    "Editor resize fixture: editorWidth=%d, docLength=%d, loadedSegments=%d",
                    editorController.editor.getWidth(),
                    editorController.editor.getDocument().getLength(),
                    editorController.m_docSegList == null ? -1 : editorController.m_docSegList.length));
            try {
                ms[0] = sweep(frame, bar, false);
                ms[1] = sweep(frame, bar, true);
            } finally {
                frame.dispose();
            }
        });
        long collapsedMs = ms[0];
        long expandedMs = ms[1];

        System.out.println(String.format(Locale.ROOT,
                "Editor resize sweep (%d widths x %d passes, %d entries): collapsed=%d ms, expanded=%d ms",
                (WIDTH_MAX - WIDTH_MIN) / WIDTH_STEP + 1, MEASURED_PASSES, ENTRY_COUNT,
                collapsedMs, expandedMs));

        assertTrue("resizing with the expanded sort bar must cost about the same as collapsed"
                + " (collapsed=" + collapsedMs + " ms, expanded=" + expandedMs + " ms)",
                expandedMs <= Math.max(300, collapsedMs * 2));
    }

    private long sweep(JFrame frame, SortBar bar, boolean expanded) {
        bar.setExpanded(expanded);
        frame.validate();
        for (int i = 0; i < WARMUP_PASSES; i++) {
            resizePass(frame);
        }
        long t0 = System.nanoTime();
        for (int i = 0; i < MEASURED_PASSES; i++) {
            resizePass(frame);
        }
        return (System.nanoTime() - t0) / 1_000_000;
    }

    /**
     * One width sweep as a user dragging the window edge causes. Setting the
     * root view's size after each validate forces the editor's view hierarchy
     * to actually re-wrap at the new width (interactively, painting does
     * this), so the sweep carries the true per-resize document cost.
     */
    private void resizePass(JFrame frame) {
        for (int w = WIDTH_MIN; w <= WIDTH_MAX; w += WIDTH_STEP) {
            frame.setSize(w, 900);
            frame.validate();
            EditorTextArea3 editor = editorController.editor;
            editor.getUI().getRootView(editor).setSize(editor.getWidth(), editor.getHeight());
        }
    }

    private static Component topLevelAncestor(Component c) {
        Component top = c;
        while (top.getParent() != null) {
            top = top.getParent();
        }
        return top;
    }

    private static SortBar findSortBar(Component c) {
        if (c instanceof SortBar) {
            return (SortBar) c;
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                SortBar found = findSortBar(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void fireLoadProjectEvent() {
        CountDownLatch latch = new CountDownLatch(1);
        editorController.editor.addPropertyChangeListener("model", evt -> {
            if (editorController.editor.getOmDocument() != null) {
                latch.countDown();
            }
        });
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // Pass through when timeout, allow opportunistic test.
        }
    }

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }

    class TestProjectProperties extends ProjectProperties {

        @Override
        public void setProjectRoot(String projectRoot) {
            this.projectRootDir = new File(projectRoot);
        }

        @Override
        public Language getSourceLanguage() {
            return sourceLang;
        }

        @Override
        public Language getTargetLanguage() {
            return targetLang;
        }

        @Override
        public boolean isSentenceSegmentingEnabled() {
            return true;
        }
    }

    /** A single-file project with enough entries that resizing has real work to do. */
    protected static class ManyEntriesProject extends RealProject {

        private static final String SOURCE_FILE = "many.txt";

        private final List<FileInfo> files;

        public ManyEntriesProject(ProjectProperties props) {
            super(props);
            files = new ArrayList<>();
            FileInfo file = new FileInfo(SOURCE_FILE);
            for (int i = 1; i <= ENTRY_COUNT; i++) {
                String source = "Segment " + i + ": the quick brown fox number " + (i * 7)
                        + " jumps over the lazy dog and keeps running along the riverbank"
                        + " so that this line is long enough to wrap at narrow editor widths.";
                file.entries.add(new SourceTextEntry(new EntryKey(SOURCE_FILE, source, null, "", "", null),
                        i, null, null, Collections.emptyList()));
            }
            files.add(file);
        }

        @Override
        public ITokenizer getSourceTokenizer() {
            return new LuceneEnglishTokenizer();
        }

        @Override
        public ITokenizer getTargetTokenizer() {
            return new DefaultTokenizer();
        }

        @Override
        public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
            return Collections.emptyMap();
        }

        @Override
        public List<FileInfo> getProjectFiles() {
            return files;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            return new ArrayList<>(files.get(0).entries);
        }

        @Override
        public boolean isProjectLoaded() {
            return true;
        }
    }
}
