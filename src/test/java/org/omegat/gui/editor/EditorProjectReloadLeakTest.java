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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Language;

/**
 * GUI-side companion of
 * {@link org.omegat.core.data.ProjectReloadLeakTest}: with a real
 * {@link EditorController} attached, every open/close cycle must still leave
 * the closed {@link RealProject} unreachable. The editor is the biggest
 * per-project consumer (its document and segment builders reference all
 * entries of the displayed file), so anything in the editor or its helpers
 * that survives the close event shows up here.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorProjectReloadLeakTest extends TestCore {

    private static final int CYCLES = 3;

    private EditorController editorController;
    private File projectRoot;

    @BeforeClass
    public static void setUpBeforeClass() {
        org.junit.Assume.assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
    }

    @Before
    public final void setUpProject() throws Exception {
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));

        projectRoot = Files.createTempDirectory("omegat-leak").toFile();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("Segment number ").append(i).append(" with some text payload.\n\n");
        }
        ProjectProperties props = makeProps();
        props.autocreateDirectories();
        Files.write(new File(props.getSourceRoot(), "source.txt").toPath(),
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private ProjectProperties makeProps() throws Exception {
        ProjectProperties props = new ProjectProperties(projectRoot);
        props.setSourceLanguage(new Language("en"));
        props.setTargetLanguage(new Language("de"));
        props.setSentenceSegmentingEnabled(true);
        props.setSupportDefaultTranslations(true);
        // Project-level filter config: loadProject re-reads filters from the
        // preferences, which know nothing about the test's filter classes.
        props.setProjectFilters(FilterMaster.createDefaultFiltersConfig());
        return props;
    }

    @Test
    public void closedProjectsMustBecomeUnreachableWithEditorAttached() throws Exception {
        List<WeakReference<RealProject>> closed = new ArrayList<>();
        // The project object being collectable is not enough: the editor held
        // the closed project's whole entry graph (via the old document) even
        // after the RealProject itself was gone. So track an entry too.
        List<WeakReference<org.omegat.core.data.SourceTextEntry>> closedEntries = new ArrayList<>();

        for (int i = 0; i < CYCLES; i++) {
            ProjectFactory.loadProject(makeProps(), false);
            assertTrue("fixture project must load", Core.getProject().isProjectLoaded());
            closed.add(new WeakReference<>((RealProject) Core.getProject()));
            closedEntries.add(new WeakReference<>(Core.getProject().getAllEntries().get(0)));

            fireProjectEventAndWait(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
            waitForDocument();

            Core.getProject().closeProject();
            fireProjectEventAndWait(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
            Core.setProject(new NotLoadedProject());
        }

        for (int i = 0; i < closed.size(); i++) {
            if (!becomesUnreachable(closed.get(i))) {
                fail("closed project of cycle " + i + " is still strongly reachable with the editor"
                        + " attached - some editor/GUI state keeps the project alive after close");
            }
            if (!becomesUnreachable(closedEntries.get(i))) {
                fail("the entries of closed project " + i + " are still strongly reachable - the"
                        + " editor (document, undo history) keeps the closed project's segment"
                        + " graph alive after close");
            }
        }
    }

    /**
     * The editor builds its document via a nested invokeLater after the LOAD
     * event; without the document (and its segment builders) the leak this
     * test guards against cannot occur, so wait until it is really there.
     */
    private void waitForDocument() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 15_000;
        while (editorController.editor.getOmDocument() == null
                && System.currentTimeMillis() < deadline) {
            CountDownLatch latch = new CountDownLatch(1);
            javax.swing.SwingUtilities.invokeLater(latch::countDown);
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(20);
        }
        assertTrue("editor document must be built after project load",
                editorController.editor.getOmDocument() != null);
    }

    private void fireProjectEventAndWait(IProjectEventListener.PROJECT_CHANGE_TYPE type)
            throws InterruptedException {
        CoreEvents.fireProjectChange(type);
        // Project events are dispatched on the EDT; an empty EDT task after
        // them means the editor finished (un)loading the document.
        CountDownLatch latch = new CountDownLatch(1);
        javax.swing.SwingUtilities.invokeLater(latch::countDown);
        assertTrue("EDT must drain", latch.await(15, TimeUnit.SECONDS));
    }

    private boolean becomesUnreachable(WeakReference<?> ref) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            System.gc();
            if (ref.get() == null) {
                return true;
            }
            // Brief backoff: releasing can lag behind close by a few
            // milliseconds (monitor threads finish, EDT drains).
            byte[][] pressure = new byte[64][];
            for (int i = 0; i < pressure.length; i++) {
                pressure[i] = new byte[1024 * 1024];
            }
            Thread.sleep(10);
        }
        return ref.get() == null;
    }

    /**
     * The project-files window is part of every real session and holds the
     * file/entry lists of the shown project; create it like the GUI bootstrap
     * does so its close handling is under test as well.
     */
    @Before
    public final void setUpProjectFilesWindow() {
        new org.omegat.gui.filelist.ProjectFilesListController();
    }

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }
}
