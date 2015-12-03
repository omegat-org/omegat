/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.custommonkey.xmlunit.XMLTestCase;
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
import org.omegat.util.RuntimePreferences;

import com.vlsolutions.swing.docking.Dockable;

/**
 * Core setup for unit tests.
 * 
 * @author Alexander_Buloichik
 */
public abstract class TestCore extends XMLTestCase {
    protected void setUp() throws Exception {
        File configDir = new File(System.getProperty("java.io.tmpdir"), "OmegaT test config");
        removeDir(configDir);

        RuntimePreferences.setConfigDir(configDir.getAbsolutePath());

        final JMenu menu = new JMenu();

        final IMainMenu mainMenu = new IMainMenu() {
            public JMenu getToolsMenu() {
                return menu;
            }

            public JMenu getProjectMenu() {
                return menu;
            }

            public JMenu getOptionsMenu() {
                return menu;
            }

            public JMenu getMachineTranslationMenu() {
                return menu;
            }

            public JMenu getGlossaryMenu() {
                return menu;
            }

			public JMenuItem getProjectRecentMenuItem() {
				return menu;
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
            
            public Cursor getCursor() {
                return null;
            }

            public void setCursor(Cursor cursor) {
            }

            public int showConfirmDialog(Object message, String title,
                    int optionType, int messageType) throws HeadlessException {
                return 0;
            }

            public void showMessageDialog(String message) {
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
        };
        Core.setEditor(new IEditor() {

            @Override
            public void windowDeactivated() {
            }

            @Override
            public void waitForCommit(int timeoutSeconds) {
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
            public IEditorSettings getSettings() {
                return editorSettings;
            }

            @Override
            public String getSelectedText() {
                return null;
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
            public void activateEntry() {
            }
        });
    }

    protected static void removeDir(File dir) {
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    removeDir(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
