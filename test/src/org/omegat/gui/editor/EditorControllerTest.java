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
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.mozlang.MozillaLangFilter;
import org.omegat.filters2.po.PoFilter;
import org.omegat.filters4.xml.xliff.Xliff1Filter;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.notes.INotes;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;

import javax.swing.text.Document;
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

    private final Language sourceLang = new Language("en");
    private final Language targetLang = new Language("pl");

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
        props.setSupportDefaultTranslations(false);
        props.setTargetTokenizer(DefaultTokenizer.class);
        TestCoreInitializer.initNotes(new MyNotes());
        Core.setProject(new RealProjectWithTMX(props));
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
        assertNotNull(editorController.editor.getOmDocument());
        assertTrue(editorController.isOrientationAllLtr());
        assertNotNull(editorController.getCurrentFile());
        assertEquals(1, editorController.getCurrentEntryNumber());
        assertEquals(31, editorController.editor.getOmDocument().getTranslationEnd());
        assertEquals(31, editorController.editor.getOmDocument().getTranslationStart());
    }

    @Test
    public void testEditorControllerLoadSimpleProjectWithCaretEvent() {
        setSimpleProject();
        fireLoadProjectEvent();
        Document doc = editorController.editor.getOmDocument();
        assertNotNull(doc);
        assertTrue(doc.getLength() > 0);
        fireCaretEvent(editorController.editor, 0);
        assertEquals(31, editorController.editor.getOmDocument().getTranslationEnd());
        assertEquals(31, editorController.editor.getOmDocument().getTranslationStart());
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
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // Pass through when timeout, allow opportunistic test.
        }
    }

    private void fireCaretEvent(JTextComponent component, int position) {
        CountDownLatch latch = new CountDownLatch(1);
        component.addCaretListener(e -> {
            latch.countDown();
        });
        component.setCaretPosition(position);
        try {
            latch.await(5, TimeUnit.SECONDS);
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

    protected static class RealProjectWithTMX extends RealProject {
        public RealProjectWithTMX(ProjectProperties props) {
            super(props);
            projectTMX = new ProjectTMX();
            files = new ArrayList<>();
            FileInfo file1 = new FileInfo();
            file1.filePath = "source.txt";
            file1.entries = new ArrayList<>();
            file1.entries.add(new SourceTextEntry(new EntryKey("source.txt", "XXX", null, "", "", null),
                    1, null, null, Collections.emptyList()));
            files.add(file1);
            FileInfo file2 = new FileInfo();
            file2.filePath = "website/download.html";
            file2.entries = new ArrayList<>();
            file2.entries.add(new SourceTextEntry(new EntryKey("website/download.html", "Other", "id",
                    "For installation on Linux.",
                    "For installation on other operating systems (such as FreeBSD and Solaris).&lt;br0/>",
                    null), 1, null, "Other", Collections.emptyList()));
            files.add(file2);
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

        public ProjectTMX getTMX() {
            return projectTMX;
        }

        private final List<FileInfo> files;

        @Override
        public List<FileInfo> getProjectFiles() {
            return files;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            List<SourceTextEntry> ste = new ArrayList<>();
            ste.add(files.get(0).entries.get(0));
            ste.add(files.get(1).entries.get(0));
            return ste;
        }

        @Override
        public boolean isProjectLoaded() {
            return true;
        }
    }

    static class MyNotes implements INotes {
        private String note;
        @Override
        public String getNoteText() {
            return note;
        }

        @Override
        public void setNoteText(String note) {
            this.note = note;
        }

        @Override
        public void clear() {
            note = null;
        }

        @Override
        public void undo() {
            // do nothing
        }

        @Override
        public void redo() {
            // do nothing
        }

        @Override
        public void requestFocus() {
            // do nothing
        }
    }
}
