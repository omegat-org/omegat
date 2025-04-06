/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.mozlang.MozillaLangFilter;
import org.omegat.filters2.po.PoFilter;
import org.omegat.filters4.xml.xliff.Xliff1Filter;
import org.omegat.util.Language;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

public class EditorControllerTest extends TestCore {

    private EditorController editorController;
    private File projectRootDir;

    private Language sourceLang = new Language("en");
    private Language targetLang = new Language("pl");

    @BeforeClass
    public static void setUpBeforeClass() {
        assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
    }

    @Before
    public final void setUp() throws IOException {
        projectRootDir = Files.createTempDirectory("omegat").toFile();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(PoFilter.class, MozillaLangFilter.class, Xliff1Filter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
    }


    private void setEmptyProject() {
        TestProjectProperties props = new TestProjectProperties();
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }

            @Override
            public List<SourceTextEntry> getAllEntries() {
                return Collections.emptyList();
            }
        });
    }

    private void setSimpleProject() {
        TestProjectProperties props = new TestProjectProperties();
        props.setProjectRoot(projectRootDir.getAbsolutePath());
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }

            @Override
            public List<FileInfo> getProjectFiles() {
                List<FileInfo> files = new ArrayList<>();
                FileInfo file1 = new FileInfo();
                file1.filePath = "source.txt";
                files.add(file1);
                FileInfo file2 = new FileInfo();
                file2.filePath = "website/download.html";
                return files;
            }

            @Override
            public List<SourceTextEntry> getAllEntries() {
                List<SourceTextEntry> ste = new ArrayList<>();
                ste.add(new SourceTextEntry(new EntryKey("source.txt", "XXX", null, "", "", null),
                        1, null, null, Collections.emptyList()));
                ste.add(new SourceTextEntry(new EntryKey("website/download.html", "Other", "id",
                        "For installation on Linux.",
                        "For installation on other operating systems (such as FreeBSD and Solaris).&lt;br0/>",
                        null), 1, null, "Other", Collections.emptyList()));
                return ste;
            }

            @Override
            public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
                return Collections.emptyMap();
            }
        });
    }

    @Test
    public void testEditorControllerDefaults() {
        assertNotNull(editorController);
        assertNotNull(editorController.editor);
        assertEquals(0, editorController.displayedFileIndex);
    }

    @Test
    public void testEditorControllerLoadEmptyProject() {
        setEmptyProject();
        fireLoadProjectEvent();
        assertTrue(editorController.isOrientationAllLtr());
        assertNull(editorController.editor.getOmDocument());
    }

    @Test
    public void testEditorControllerLoadSimpleProject() {
        setSimpleProject();
        fireLoadProjectEvent();
        assertTrue(editorController.isOrientationAllLtr());
        assertNotNull(editorController.editor.getOmDocument());
        assertEquals(0, editorController.getCurrentEntryNumber());
        assertEquals(0, editorController.editor.getOmDocument().getTranslationEnd());
        assertEquals(0, editorController.editor.getOmDocument().getTranslationStart());
    }

    @Test
    public void testEditorControllerLoadSimpleProjectWithCarretEvent() {
        setSimpleProject();
        fireLoadProjectEvent();
        assertNotNull(editorController.editor.getOmDocument());
        editorController.lastLoaded = 2;
        fireCarretEvent(editorController.editor, 1);
        assertEquals(0, editorController.editor.getOmDocument().getTranslationEnd());
        assertEquals(0, editorController.editor.getOmDocument().getTranslationStart());
    }

    private void fireLoadProjectEvent() {
        CountDownLatch latch = new CountDownLatch(1);
        CoreEvents.registerProjectChangeListener(event -> {
            if (Core.getProject().isProjectLoaded()) {
                latch.countDown();
            }
        });
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private void fireCarretEvent(JTextComponent component, int position) {
        CountDownLatch latch = new CountDownLatch(1);
        component.addCaretListener(e -> {
            latch.countDown();
        });
        component.setCaretPosition(position);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    protected void initEditor() {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }

    private class TestProjectProperties extends ProjectProperties {
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
}
