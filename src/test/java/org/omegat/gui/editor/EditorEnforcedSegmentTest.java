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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;

import com.vlsolutions.swing.docking.Dockable;

/**
 * Leaving a segment whose translation is enforced from a tm/enforce/ memory
 * must not raise the "reverting enforced segment" warning unless the user
 * actually changed the translation. A mere deactivation - navigating away or
 * the editor view refresh triggered by a preferences/colour change - used to
 * raise the warning, and via {@code refreshView} even twice (SF bug #1171).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorEnforcedSegmentTest extends TestCore {

    private static final String SOURCE = "This is enforced.";
    private static final String ENFORCED_TRANSLATION = "To jest wymuszone.";

    private EditorController editorController;
    private final List<String> warnings = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        assumeFalse("Skipping test: headless environment", GraphicsEnvironment.isHeadless());
    }

    @Before
    public final void setUpProject() throws IOException {
        TestCoreState.getInstance().setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class));
        TestCoreState.getInstance().setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));

        TestCoreInitializer.initNotes(new RecordingNotes());
        File projectRootDir = Files.createTempDirectory("omegat-enforce").toFile();
        EnforcedProjectProperties props = new EnforcedProjectProperties();
        props.setProjectRoot(projectRootDir.getAbsolutePath());
        props.setSourceTokenizer(LuceneEnglishTokenizer.class);
        props.setTargetTokenizer(DefaultTokenizer.class);
        EnforcedProject project = new EnforcedProject(props);
        TestCoreState.getInstance().setProject(project);
        project.seedEnforcedTranslation();
        fireLoadProjectEvent();
    }

    @Test
    public void viewRefreshOnEnforcedSegmentDoesNotWarn() throws Exception {
        runOnEdt(() -> editorController.refreshView(true));
        assertTrue("a plain view refresh of an enforced segment must not warn", warnings.isEmpty());
    }

    @Test
    public void changingAnEnforcedSegmentStillWarnsExactlyOnce() throws Exception {
        // Emptying the enforced translation is a genuine attempt to change it
        // and must still raise the warning - exactly once.
        runOnEdt(() -> editorController.registerEmptyTranslation());
        assertEquals("changing an enforced translation must still warn", 1, warnings.size());
        assertEquals("EC_WARNING_REVERT_ENFORCED_SEGMENT", warnings.get(0));
    }

    private void fireLoadProjectEvent() {
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        long deadline = System.currentTimeMillis() + 5000;
        while (editorController.editor.getOmDocument() == null
                && System.currentTimeMillis() < deadline) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                });
                Thread.sleep(20);
            } catch (InterruptedException | InvocationTargetException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void runOnEdt(Runnable r) throws Exception {
        SwingUtilities.invokeAndWait(r);
    }

    @Override
    protected void initEditor(IMainWindow mainWindow) {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }

    /**
     * Main window mock that records the warning keys the editor raises.
     */
    @Override
    protected IMainWindow getMainWindow() {
        final IMainMenu mainMenu = getMainMenu();
        return new ConsoleWindow() {
            @Override
            public void addDockable(Dockable pane) {
            }

            @Override
            public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            }

            @Override
            public void displayWarningRB(String warningKey, Object... params) {
                warnings.add(warningKey);
            }

            @Override
            public void displayWarningRB(String warningKey, String supercedesKey, Object... params) {
                warnings.add(warningKey);
            }

            @Override
            public Font getApplicationFont() {
                return new Font("Dialog", Font.PLAIN, 12);
            }

            @Override
            public JFrame getApplicationFrame() {
                return new JFrame();
            }

            @Override
            public void showLengthMessage(String messageText) {
            }

            @Override
            public void showProgressMessage(String messageText) {
            }

            @Override
            public IMainMenu getMainMenu() {
                return mainMenu;
            }
        };
    }

    private static final class RecordingNotes implements org.omegat.gui.notes.INotes {
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
        }

        @Override
        public void redo() {
        }

        @Override
        public void requestFocus() {
        }
    }

    private final class EnforcedProjectProperties extends ProjectProperties {
        @Override
        public void setProjectRoot(String projectRoot) {
            super.setProjectRoot(projectRoot);
        }

        @Override
        public Language getSourceLanguage() {
            return new Language("en");
        }

        @Override
        public Language getTargetLanguage() {
            return new Language("pl");
        }

        @Override
        public boolean isSentenceSegmentingEnabled() {
            return true;
        }

        @Override
        public boolean isSupportDefaultTranslations() {
            return true;
        }
    }

    private static final class EnforcedProject extends RealProject {

        private static final String SOURCE_FILE = "source.txt";
        private final List<FileInfo> files = new ArrayList<>();
        private final SourceTextEntry entry;

        EnforcedProject(ProjectProperties props) {
            super(props);
            FileInfo file = new FileInfo(SOURCE_FILE);
            entry = new SourceTextEntry(new EntryKey(SOURCE_FILE, SOURCE, null, "", "", null), 1, null,
                    null, Collections.emptyList());
            file.entries.add(entry);
            files.add(file);
        }

        void seedEnforcedTranslation() {
            PrepareTMXEntry prep = new PrepareTMXEntry();
            prep.source = SOURCE;
            prep.translation = ENFORCED_TRANSLATION;
            setTranslation(entry, prep, true, TMXEntry.ExternalLinked.xENFORCED);
        }

        @Override
        public List<FileInfo> getProjectFiles() {
            return files;
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            return Collections.singletonList(entry);
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
        public boolean isProjectLoaded() {
            return true;
        }
    }
}
