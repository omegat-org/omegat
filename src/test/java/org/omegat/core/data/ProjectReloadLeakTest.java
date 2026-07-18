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
package org.omegat.core.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.util.Language;

/**
 * Reproduction guard for the memory exhaustion seen after repeatedly opening
 * and closing large projects: every open/close cycle must leave the previous
 * {@link RealProject} unreachable, or the retained heaps of large projects add
 * up until the VM dies. The cycle here is the core data-engine path (load,
 * publish via {@link Core#setProject}, close, replace); if an instance
 * survives garbage collection, something static or long-lived still points at
 * the closed project.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ProjectReloadLeakTest extends TestCore {

    private static final int CYCLES = 3;

    private File projectRoot;

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
    public void closedProjectsMustBecomeUnreachable() throws Exception {
        List<WeakReference<RealProject>> closed = new ArrayList<>();

        for (int i = 0; i < CYCLES; i++) {
            // The same sequence the UI uses to open and close a project.
            ProjectFactory.loadProject(makeProps(), false);
            assertTrue("fixture project must load", Core.getProject().isProjectLoaded());
            closed.add(new WeakReference<>((RealProject) Core.getProject()));

            Core.getProject().closeProject();
            Core.setProject(new NotLoadedProject());
        }

        for (int i = 0; i < closed.size(); i++) {
            WeakReference<RealProject> ref = closed.get(i);
            if (!becomesUnreachable(ref)) {
                fail("closed project of cycle " + i + " is still strongly reachable "
                        + "- a listener, cache, or singleton keeps the project alive after close");
            }
        }
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
}
