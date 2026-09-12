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
import static org.junit.Assume.assumeFalse;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.RenderBenchmarkTests;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 * Offscreen render cost measurement of the editor with the segment metadata
 * gutter (SF feature requests 420 and 1028): paints the loaded editor and
 * the gutter into an image with the clip sizes that occur in practice and
 * prints one summary line per scenario.
 *
 * The numbers guide the render performance work and give before/after
 * comparisons; the assertions only guard that every scenario still paints,
 * so differing machine speeds cannot make the test flaky.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
@Category(RenderBenchmarkTests.class)
public class EditorRenderBenchmarkTest extends TestCore {

    /** Segments in the benchmark file; enough for a busy viewport. */
    private static final int SEGMENTS = 300;
    private static final int VIEW_WIDTH = 900;
    private static final int VIEW_HEIGHT = 700;
    private static final int WARMUP = 15;
    private static final int RUNS = 40;

    private EditorController editorController;
    private File projectRoot;

    @BeforeClass
    public static void setUpBeforeClass() {
        assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
    }

    @After
    public final void tearDownProject() {
        FileUtils.deleteQuietly(projectRoot);
    }

    @Before
    public final void setUp() throws IOException {
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
    }

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }

    @Test
    public void measureRenderScenarios() throws Exception {
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER, true);
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA, false);
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_GRID, false);
        loadBenchmarkProject();

        // The views belong to the event dispatch thread: layout and paint
        // must run there, like they do in the application.
        Throwable[] failure = new Throwable[1];
        java.awt.EventQueue.invokeAndWait(() -> {
            try {
                runScenarios();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        if (failure[0] instanceof Exception) {
            throw (Exception) failure[0];
        }
        if (failure[0] != null) {
            throw new AssertionError(failure[0]);
        }
    }

    private void runScenarios() {
        EditorTextArea3 editor = editorController.editor;
        assertNotNull(editor.getOmDocument());
        editor.setSize(VIEW_WIDTH, VIEW_HEIGHT);
        editor.setSize(VIEW_WIDTH, Math.max(VIEW_HEIGHT, editor.getPreferredSize().height));
        SegmentMetadataGutter gutter = new SegmentMetadataGutter(editorController, editor);
        gutter.setSize(Math.max(60, gutter.getPreferredSize().width), editor.getHeight());

        BufferedImage image = new BufferedImage(VIEW_WIDTH, VIEW_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        int lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();

        report("editor full viewport, decorations off",
                measure(image, editor, VIEW_HEIGHT));
        report("editor one line, decorations off", measure(image, editor, lineHeight));
        report("gutter full viewport", measure(image, gutter, VIEW_HEIGHT));

        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA, true);
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_GRID, true);
        report("editor full viewport, zebra+grid",
                measure(image, editor, VIEW_HEIGHT));
        report("editor one line, zebra+grid", measure(image, editor, lineHeight));
        report("gutter full viewport, zebra+grid",
                measure(image, gutter, VIEW_HEIGHT));

        Preferences.setPreference(Preferences.EDITOR_LAYOUT_STACKED, false);
        SegmentColumnsView columnsView = editor.columnsView();
        if (columnsView != null) {
            columnsView.relayout();
        }
        report("editor full viewport, zebra+grid, side by side",
                measure(image, editor, VIEW_HEIGHT));

        // What one keystroke repaints while grid or zebra is on: the full
        // editor viewport plus the full gutter.
        double keystroke = measure(image, editor, VIEW_HEIGHT)[0]
                + measure(image, gutter, VIEW_HEIGHT)[0];
        System.out.println(String.format(Locale.ENGLISH,
                "RENDERBENCH keystroke repaint (editor+gutter, zebra+grid): %.2f ms",
                keystroke));
        assertTrue("keystroke repaint measured", keystroke > 0);
    }

    /** Paints the component RUNS times with the clip height; {avg, max} ms. */
    private double[] measure(BufferedImage image, javax.swing.JComponent component,
            int clipHeight) {
        for (int i = 0; i < WARMUP; i++) {
            paintOnce(image, component, clipHeight);
        }
        double total = 0;
        double max = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            paintOnce(image, component, clipHeight);
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            total += ms;
            max = Math.max(max, ms);
        }
        return new double[] { total / RUNS, max };
    }

    private void paintOnce(BufferedImage image, javax.swing.JComponent component,
            int clipHeight) {
        Graphics2D g = image.createGraphics();
        try {
            // A one line clip sits where the active segment is edited; the
            // full clip mirrors a viewport-wide repaint.
            g.setClip(0, 0, VIEW_WIDTH, clipHeight);
            component.paint(g);
        } finally {
            g.dispose();
        }
    }

    private void report(String scenario, double[] avgMax) {
        System.out.println(String.format(Locale.ENGLISH,
                "RENDERBENCH %s: avg %.2f ms, max %.2f ms", scenario, avgMax[0], avgMax[1]));
        assertTrue(scenario + " painted", avgMax[0] >= 0);
    }

    private void loadBenchmarkProject() {
        try {
            projectRoot = Files.createTempDirectory("omegat-renderbench").toFile();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        BenchProjectProperties props = new BenchProjectProperties();
        props.setProjectRoot(projectRoot.getAbsolutePath());
        props.setSupportDefaultTranslations(false);
        props.setTargetTokenizer(DefaultTokenizer.class);
        Core.setProject(new BenchProject(props));

        CountDownLatch latch = new CountDownLatch(1);
        editorController.editor.addPropertyChangeListener("document", evt -> {
            if (editorController.editor.getOmDocument() != null) {
                latch.countDown();
            }
        });
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        boolean loaded;
        try {
            loaded = latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        assertTrue("editor document did not load", loaded);
    }

    static class BenchProjectProperties extends ProjectProperties {
        private final Language sourceLang = new Language("en");
        private final Language targetLang = new Language("de");

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

    static class BenchProject extends RealProject {
        private static final String SOURCE_FILE = "manual.txt";
        private final List<FileInfo> files;

        BenchProject(ProjectProperties props) {
            super(props);
            FileInfo file = new FileInfo(SOURCE_FILE);
            for (int i = 1; i <= SEGMENTS; i++) {
                // Realistic manual style sentences of varying length, so the
                // paragraphs wrap like a real translation project.
                String source = String.format(Locale.ENGLISH,
                        "Step %d: tighten the retaining bolt of assembly %d to the torque"
                                + " listed in row %d of the maintenance table before"
                                + " reconnecting the sensor cable.",
                        i, (i % 7) + 1, (i % 23) + 1);
                file.entries.add(new SourceTextEntry(
                        new EntryKey(SOURCE_FILE, source, "bench" + i, "", "", null), i,
                        null, null, Collections.emptyList()));
            }
            files = new ArrayList<>();
            files.add(file);
        }

        @Override
        public ITokenizer getSourceTokenizer() {
            return new DefaultTokenizer();
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
            return files.get(0).entries;
        }

        @Override
        public boolean isProjectLoaded() {
            return true;
        }
    }
}
