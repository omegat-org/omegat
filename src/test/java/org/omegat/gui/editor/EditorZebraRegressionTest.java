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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Preferences;

/**
 * Regression guards for the render performance work on the editor with the
 * segment metadata gutter: the editor must stay opaque (blit scrolling and
 * small dirty regions depend on it) while the alternating backgrounds still
 * show beneath the text, and the stripes must survive the highlight resets
 * of the marker machinery.
 *
 * The stripes are asserted on rendered pixels in the left text margin, where
 * no glyph interferes: an odd segment band differs from the plain editor
 * background, an even one does not.
 *
 * @author Stephan Pakebusch stephan.pakebusch at zollsoft.de
 */
public class EditorZebraRegressionTest extends TestCore {

    private static final int VIEW_WIDTH = 700;
    private static final int VIEW_HEIGHT = 500;

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
    public void zebraPaintsUnderTextAndSurvivesHighlightReset() throws Exception {
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER, true);
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA, true);
        loadProject();

        Throwable[] failure = new Throwable[1];
        java.awt.EventQueue.invokeAndWait(() -> {
            try {
                assertZebraBehaviour();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        if (failure[0] != null) {
            throw new AssertionError(failure[0]);
        }
    }

    private void assertZebraBehaviour() throws Exception {
        EditorTextArea3 editor = editorController.editor;
        assertNotNull(editor.getOmDocument());

        // The editor must stay opaque with the alternating backgrounds on:
        // making it non-opaque disables viewport blit scrolling and blows
        // small repaints up, which is exactly the jank this work removed.
        assertTrue("editor must stay opaque with zebra on", editor.isOpaque());

        editor.setSize(VIEW_WIDTH, VIEW_HEIGHT);
        editor.setSize(VIEW_WIDTH, Math.max(VIEW_HEIGHT, editor.getPreferredSize().height));

        int background = editor.getBackground().getRGB();
        // Segment indexes count from 0: odd rows carry the band.
        assertNotEquals("odd segment shows a band", background, bandPixel(editor, 1));
        assertEquals("even segment shows the plain background", background,
                bandPixel(editor, 2));

        // The marker machinery clears all highlights on document rebuilds;
        // the stripes live in the highlighter subclass and must survive.
        editor.getHighlighter().removeAllHighlights();
        assertNotEquals("band survives a highlight reset", background,
                bandPixel(editor, 1));

        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA, false);
        assertEquals("no band without the zebra preference", background,
                bandPixel(editor, 1));
    }

    /**
     * Renders the editor and samples a pixel of the segment's zebra band:
     * at the right edge, just above the first text line — in the separator
     * gap, where neither glyphs nor the character background attributes of
     * the text (source shading, active segment colours) can interfere.
     */
    private int bandPixel(EditorTextArea3 editor, int segmentIndex) throws Exception {
        SegmentBuilder builder = editorController.m_docSegList[segmentIndex];
        Rectangle2D rect = editor.modelToView2D(builder.getStartPosition());
        assertNotNull(rect);
        // A viewport sized window around the band: rendering the whole
        // document into a full height image would cost dozens of megabytes
        // per probe.
        int top = Math.max(0, (int) rect.getY() - VIEW_HEIGHT / 2);
        BufferedImage image = new BufferedImage(VIEW_WIDTH, VIEW_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.translate(0, -top);
            g.setClip(0, top, VIEW_WIDTH, VIEW_HEIGHT);
            editor.paint(g);
        } finally {
            g.dispose();
        }
        return image.getRGB(VIEW_WIDTH - 3, (int) rect.getY() - 2 - top);
    }

    private void loadProject() {
        try {
            projectRoot = Files.createTempDirectory("omegat-zebratest").toFile();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        EditorRenderBenchmarkTest.BenchProjectProperties props =
                new EditorRenderBenchmarkTest.BenchProjectProperties();
        props.setProjectRoot(projectRoot.getAbsolutePath());
        props.setSupportDefaultTranslations(false);
        props.setTargetTokenizer(DefaultTokenizer.class);
        Core.setProject(new EditorRenderBenchmarkTest.BenchProject(props));

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
}
