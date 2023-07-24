/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Platform;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.MenuExtender;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Core setup for unit tests.
 *
 * @author Alexander_Buloichik
 */
public abstract class TestCore {
    protected File configDir;

    @Before
    public final void setUpCore() throws Exception {
        configDir = Files.createTempDirectory("omegat").toFile();
        TestPreferencesInitializer.init(configDir.getAbsolutePath());

        final IMainMenu mainMenu = new IMainMenu() {
            private final JMenu projectMenu = new JMenu("Project");
            private final JMenu toolsMenu = new JMenu("Tools");
            private final JMenu gotoMenu = new JMenu("Goto");
            private final JMenu optionsMenu = new JMenu("Options");
            private final JMenu helpMenu = new JMenu("Help");
            private final JMenu machineTranslationMenu = new JMenu("MachineTranslate");
            private final JMenu glossaryMenu = new JMenu("Glossary");
            private final JMenu autoCompleteMenu = new JMenu("AutoComplete");

            public JMenu getToolsMenu() {
                if (toolsMenu.getItemCount() == 0) {
                    toolsMenu.add(new JMenuItem("toolsCheckIssuesMenuItem"));
                    toolsMenu.add(new JMenuItem("toolsCheckIssuesCurrentFileMenuItem"));
                    toolsMenu.add(new JMenuItem("toolsShowStatisticsStandardMenuItem"));
                    toolsMenu.add(new JMenuItem("toolsShowStatisticsMatchesMenuItem"));
                    toolsMenu.add(new JMenuItem("toolsShowStatisticsMatchesPerFileMenuItem"));
                    toolsMenu.addSeparator();
                    toolsMenu.add(new JMenuItem("toolsAlignFilesMenuItem"));
                }
                return toolsMenu;
            }

            public JMenu getProjectMenu() {
                if (projectMenu.getItemCount() == 0) {
                    projectMenu.add(new JMenuItem("New"));
                    projectMenu.add(new JMenuItem("TeamNew"));
                    projectMenu.add(new JMenuItem("Open"));
                    projectMenu.add(new JMenuItem("OpenRecent"));
                    projectMenu.add(new JMenuItem("Reload"));
                    projectMenu.add(new JMenuItem("Close"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("Save"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("Import"));
                    projectMenu.add(new JMenuItem("WikiImport"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("CommitSource"));
                    projectMenu.add(new JMenuItem("CommitTarget"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("Compile"));
                    projectMenu.add(new JMenuItem("SingleCompile"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("MedOpen"));
                    projectMenu.add(new JMenuItem("MedCreate"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("ProjectEdit"));
                    projectMenu.add(new JMenuItem("ViewFIleList"));
                    projectMenu.add(new JMenuItem("AccessProjectFiles"));
                    projectMenu.addSeparator();
                    projectMenu.add(new JMenuItem("Restart"));
                    // all except MacOSX
                    if (!Platform.isMacOSX()) {
                        projectMenu.add(new JMenuItem("Exit"));
                    }
                }
                return projectMenu;
            }

            public JMenu getOptionsMenu() {
                if (optionsMenu.getItemCount() == 0) {
                    if (!Platform.isMacOSX()) {
                        optionsMenu.add("Preference");
                        optionsMenu.addSeparator();
                    }
                    optionsMenu.add(machineTranslationMenu);
                    optionsMenu.add(glossaryMenu);
                    optionsMenu.add(new JMenuItem("Dictionary"));
                    optionsMenu.add(autoCompleteMenu);
                    optionsMenu.addSeparator();
                    optionsMenu.add(new JMenuItem("SetupFileFilters"));
                    optionsMenu.add(new JMenuItem("Sentseg"));
                    optionsMenu.add(new JMenuItem("Workflow"));
                    optionsMenu.addSeparator();
                    optionsMenu.add(new JMenuItem("AccessConfigDir"));
                    optionsMenu.addSeparator();
                }
                return optionsMenu;
            }

            public JMenu getMachineTranslationMenu() {
                return machineTranslationMenu;
            }

            public JMenu getGlossaryMenu() {
                return glossaryMenu;
            }

            public JMenu getAutoCompletionMenu() {
                return autoCompleteMenu;
            }

            @Override
            public JMenu getHelpMenu() {
                if (helpMenu.getItemCount() == 0) {
                    helpMenu.add(new JMenuItem("User manual"));
                    helpMenu.add(new JMenuItem("About"));
                    helpMenu.addSeparator();
                    helpMenu.add(new JMenuItem("item 3"));
                }
                return helpMenu;
            }

            @Override
            public JMenu getMenu(final MenuExtender.MenuKey marker) {
                switch (marker) {
                case PROJECT:
                    return getProjectMenu();
                case HELP:
                    return getHelpMenu();
                case OPTIONS:
                    return getOptionsMenu();
                case GOTO:
                    return getGotoMenu();
                case TOOLS:
                    return getToolsMenu();
                case EDIT:
                    return new JMenu();
                case VIEW:
                    return new JMenu();
                default:
                    return new JMenu();
                }
            }

            private JMenu getGotoMenu() {
                if (gotoMenu.getItemCount() == 0) {
                    gotoMenu.add(new JMenuItem("gotoNextUntranslatedMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoNextTranslatedMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoNextSegmentMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoPreviousSegmentMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoSegmentMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoNextNoteMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoPreviousNoteMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoNextUniqueMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoMatchSourceSegment"));
                    gotoMenu.addSeparator();
                    gotoMenu.add(new JMenu("gotoXEntrySubmenu"));
                    gotoMenu.addSeparator();
                    gotoMenu.add(new JMenuItem("gotoHistoryBackMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoHistoryForwardMenuItem"));
                    gotoMenu.addSeparator();
                    gotoMenu.add(new JMenuItem("gotoNotesPanelMenuItem"));
                    gotoMenu.add(new JMenuItem("gotoEditorPanelMenuItem"));
                }
                return gotoMenu;
            }

            public void invokeAction(String action, int modifiers) {
            }
        };

        Core.setMainWindow(new IMainWindow() {
            public void addDockable(Dockable pane) {
            }

            public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            }

            public Font getApplicationFont() {
                return new Font("Dialog", Font.PLAIN, 12);
            }

            public JFrame getApplicationFrame() {
                return new JFrame();
            }

            public void lockUI() {
            }

            public void showLengthMessage(String messageText) {
            }

            public void showProgressMessage(String messageText) {
            }

            public void showStatusMessageRB(String messageKey, Object... params) {
            }

            public void showTimedStatusMessageRB(String messageKey, Object... params) {
            }

            public void displayWarningRB(String warningKey, Object... params) {
            }

            public void displayWarningRB(String warningKey, String supercedesKey, Object... params) {
            }

            public void showErrorDialogRB(String title, String message, Object... args) {
            }

            public void unlockUI() {
            }

            public IMainMenu getMainMenu() {
                return mainMenu;
            }

            @Override
            public DockingDesktop getDesktop() {
                return null;
            }

            public Cursor getCursor() {
                return null;
            }

            public void setCursor(Cursor cursor) {
            }

            public int showConfirmDialog(Object message, String title, int optionType, int messageType)
                    throws HeadlessException {
                return 0;
            }

            public void showMessageDialog(String message) {
            }

            public void showLockInsertMessage(String messageText, String toolTip) {
            }
        });
        Core.setCurrentProject(new NotLoadedProject());

        final IEditorSettings editorSettings = new IEditorSettings() {

            @Override
            public boolean isUseTabForAdvance() {
                return false;
            }

            @Override
            public void setUseTabForAdvance(boolean useTabForAdvance) {
            }

            @Override
            public boolean isMarkTranslated() {
                return false;
            }

            @Override
            public void setMarkTranslated(boolean markTranslated) {
            }

            @Override
            public boolean isMarkUntranslated() {
                return false;
            }

            @Override
            public void setMarkUntranslated(boolean markUntranslated) {
            }

            @Override
            public boolean isMarkAutoPopulated() {
                return false;
            }

            @Override
            public void setMarkAutoPopulated(boolean markAutoPopulated) {
            }

            @Override
            public boolean isDisplaySegmentSources() {
                return false;
            }

            @Override
            public void setDisplaySegmentSources(boolean displaySegmentSources) {
            }

            @Override
            public boolean isMarkNonUniqueSegments() {
                return false;
            }

            @Override
            public void setMarkNonUniqueSegments(boolean markNonUniqueSegments) {
            }

            @Override
            public boolean isMarkNotedSegments() {
                return false;
            }

            @Override
            public void setMarkNotedSegments(boolean markNotedSegments) {
            }

            @Override
            public boolean isMarkNBSP() {
                return false;
            }

            @Override
            public void setMarkNBSP(boolean markNBSP) {
            }

            @Override
            public boolean isMarkWhitespace() {
                return false;
            }

            @Override
            public void setMarkWhitespace(boolean markWhitespace) {
            }

            @Override
            public boolean isMarkBidi() {
                return false;
            }

            @Override
            public void setMarkBidi(boolean markBidi) {
            }

            @Override
            public boolean isAutoSpellChecking() {
                return false;
            }

            @Override
            public void setAutoSpellChecking(boolean isNeedToSpell) {
            }

            @Override
            public boolean isDoFontFallback() {
                return false;
            }

            @Override
            public void setDoFontFallback(boolean doFallback) {
            }

            @Override
            public String getDisplayModificationInfo() {
                return null;
            }

            @Override
            public void setDisplayModificationInfo(String displayModificationInfo) {
            }

            @Override
            public void updateTagValidationPreferences() {
            }

            @Override
            public void updateViewPreferences() {
            }

            @Override
            public boolean isMarkLanguageChecker() {
                return false;
            }

            @Override
            public void setMarkLanguageChecker(boolean markLanguageChecker) {
            }

            @Override
            public boolean isMarkGlossaryMatches() {
                return false;
            }

            @Override
            public void setMarkGlossaryMatches(boolean markGlossaryMatches) {
            }

            @Override
            public void setMarkParagraphDelimitations(boolean mark) {
            }

            @Override
            public boolean isMarkParagraphDelimitations() {
                return false;
            }
        };
        TestCoreInitializer.initEditor(new IEditor() {

            @Override
            public void windowDeactivated() {
            }

            @Override
            public void undo() {
            }

            @Override
            public void setFilter(IEditorFilter filter) {
            }

            @Override
            public void setAlternateTranslationForCurrentEntry(boolean alternate) {
            }

            @Override
            public void requestFocus() {
            }

            @Override
            public void replaceEditTextAndMark(String text) {
            }

            @Override
            public void replaceEditText(String text) {
            }

            @Override
            public void replaceEditTextAndMark(final String text, final String origin) {
            }

            @Override
            public void removeFilter() {
            }

            @Override
            public void remarkOneMarker(String markerClassName) {
            }

            @Override
            public void registerUntranslated() {
            }

            @Override
            public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
            }

            @Override
            public void registerIdenticalTranslation() {
            }

            @Override
            public void registerEmptyTranslation() {
            }

            @Override
            public void refreshViewAfterFix(List<Integer> fixedEntries) {
            }

            @Override
            public void refreshView(boolean doCommit) {
            }

            @Override
            public void redo() {
            }

            @Override
            public void prevEntryWithNote() {
            }

            @Override
            public void prevEntry() {
            }

            @Override
            public void nextXAutoEntry() {
            }

            @Override
            public void prevXAutoEntry() {
            }

            @Override
            public void nextXEnforcedEntry() {
            }

            @Override
            public void prevXEnforcedEntry() {
            }

            @Override
            public void nextUntranslatedEntry() {
            }

            @Override
            public void nextUniqueEntry() {
            }

            @Override
            public void nextTranslatedEntry() {
            }

            @Override
            public void nextEntryWithNote() {
            }

            @Override
            public void nextEntry() {
            }

            @Override
            public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
                    String markerClassName) {
            }

            @Override
            public void insertText(String text) {
            }

            @Override
            public void insertTextAndMark(String text) {
            }

            @Override
            public void insertTag(String tag) {
            }

            @Override
            public void gotoHistoryForward() {
            }

            @Override
            public void gotoHistoryBack() {
            }

            @Override
            public void gotoFile(int fileIndex) {
            }

            @Override
            public void gotoEntryAfterFix(int fixedEntry, String fixedSource) {
            }

            @Override
            public void gotoEntry(String srcString, EntryKey key) {
            }

            @Override
            public void gotoEntry(int entryNum) {
            }

            @Override
            public void gotoEntry(int entryNum, CaretPosition pos) {
            }

            @Override
            public IEditorSettings getSettings() {
                return editorSettings;
            }

            @Override
            public String getSelectedText() {
                return null;
            }

            @Override
            public void selectSourceText() {
            }

            @Override
            public IEditorFilter getFilter() {
                return null;
            }

            @Override
            public String getCurrentTranslation() {
                return null;
            }

            @Override
            public String getCurrentTargetFile() {
                return null;
            }

            @Override
            public String getCurrentFile() {
                return null;
            }

            @Override
            public int getCurrentEntryNumber() {
                return 0;
            }

            @Override
            public SourceTextEntry getCurrentEntry() {
                return null;
            }

            @Override
            public IAutoCompleter getAutoCompleter() {
                return null;
            }

            @Override
            public void commitAndLeave() {
            }

            @Override
            public void commitAndDeactivate() {
            }

            @Override
            public void changeCase(CHANGE_CASE_TO newCase) {
            }

            @Override
            public void replaceEditText(final String text, final String origin) {

            }

            @Override
            public void activateEntry() {
            }
        });
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort());

    @After
    public final void tearDownCore() throws Exception {
        FileUtils.deleteDirectory(configDir);
    }
}
