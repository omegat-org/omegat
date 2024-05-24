/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
package org.omegat.gui.main;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.LogDialog;
import org.omegat.gui.editor.EditorUtils;
import org.omegat.gui.editor.SegmentExportImport;
import org.omegat.gui.exttrans.MachineTranslationInfo;
import org.omegat.gui.filelist.IProjectFilesList;
import org.omegat.gui.filters2.FiltersCustomizerController;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.EditingBehaviorController;
import org.omegat.gui.segmentation.SegmentationCustomizerController;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

public class TestMainWindowMenuHandler extends BaseMainWindowMenuHandler {

    IMainWindow mainWindow;

    public TestMainWindowMenuHandler(IMainWindow mw) {
        this.mainWindow = mw;
    }

    /**
     * Create a new project.
     */
    public void projectNewMenuItemActionPerformed() {
        ProjectUICommands.projectCreate();
    }

    public void projectExitMenuItemActionPerformed() {
        mainWindow.getApplicationFrame().setVisible(false);
        mainWindow.getApplicationFrame().setEnabled(false);
    }

    public void viewFileListMenuItemActionPerformed() {
        IProjectFilesList projWin = Core.getProjectFilesList();
        if (projWin == null) {
            return;
        }
        projWin.setActive(!projWin.isActive());
    }

    public void editUndoMenuItemActionPerformed() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().undo();
        } else {
            Core.getEditor().undo();
        }
    }

    public void editRedoMenuItemActionPerformed() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused == Core.getNotes()) {
            Core.getNotes().redo();
        } else {
            Core.getEditor().redo();
        }
    }

    public void editOverwriteTranslationMenuItemActionPerformed() {
    }

    public void editInsertTranslationMenuItemActionPerformed() {
    }

    public void editOverwriteMachineTranslationMenuItemActionPerformed() {
        MachineTranslationInfo tr = Core.getMachineTranslatePane().getDisplayedTranslation();
        if (tr == null) {
            Core.getMachineTranslatePane().forceLoad();
        } else if (!StringUtil.isEmpty(tr.result)) {
            Core.getEditor().replaceEditText(tr.result, String.format("MT:[%s]", tr.translatorName));
        }
    }

    /**
     * replaces entire edited segment text with a the source text of a segment
     * at cursor position
     */
    public void editOverwriteSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
        if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
            toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
        }
        Core.getEditor().replaceEditText(toInsert);
    }

    /** inserts the source text of a segment at cursor position */
    public void editInsertSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String toInsert = Core.getEditor().getCurrentEntry().getSrcText();
        if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
            toInsert = EditorUtils.replaceGlossaryEntries(toInsert);
        }
        Core.getEditor().insertText(toInsert);
    }

    /** select the source text of the current segment */
    public void editSelectSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getEditor().selectSourceText();
    }

    public void editExportSelectionMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String selection = Core.getEditor().getSelectedText();
        if (selection == null) {
            SourceTextEntry ste = Core.getEditor().getCurrentEntry();
            TMXEntry te = Core.getProject().getTranslationInfo(ste);
            if (te.isTranslated()) {
                selection = te.translation;
            } else {
                selection = ste.getSrcText();
            }
        }
        SegmentExportImport.exportCurrentSelection(selection);
    }

    public void editSearchDictionaryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        String selection = Core.getEditor().getSelectedText();
        if (selection == null) {
            SourceTextEntry ste = Core.getEditor().getCurrentEntry();
            selection = ste.getSrcText();
        }
        Core.getDictionaries().searchText(selection);
    }

    public void editCreateGlossaryEntryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getGlossary().showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
    }

    public void editFindInProjectMenuItemActionPerformed() {
    }

    void findInProjectReuseLastWindow() {
    }

    public void editReplaceInProjectMenuItemActionPerformed() {
    }

    private String getTrimmedSelectedTextInMainWindow() {
        String selection = null;
        Component component = mainWindow.getApplicationFrame().getMostRecentFocusOwner();
        if (component instanceof JTextComponent) {
            selection = ((JTextComponent) component).getSelectedText();
            if (!StringUtil.isEmpty(selection)) {
                selection = EditorUtils.removeDirectionChars(selection);
                selection = selection.trim();
            }
        }
        return selection;
    }

    /** Set active match to #1. */
    public void editSelectFuzzy1MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(0);
    }

    /** Set active match to #2. */
    public void editSelectFuzzy2MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(1);
    }

    /** Set active match to #3. */
    public void editSelectFuzzy3MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(2);
    }

    /** Set active match to #4. */
    public void editSelectFuzzy4MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(3);
    }

    /** Set active match to #5. */
    public void editSelectFuzzy5MenuItemActionPerformed() {
        Core.getMatcher().setActiveMatch(4);
    }

    /** Set active match to the next one */
    public void editSelectFuzzyNextMenuItemActionPerformed() {
        Core.getMatcher().setNextActiveMatch();
    }

    /** Set active match to the previous one */
    public void editSelectFuzzyPrevMenuItemActionPerformed() {
        Core.getMatcher().setPrevActiveMatch();
    }

    public void insertCharsLRMActionPerformed() {
        Core.getEditor().insertText("\u200E");
    }

    public void insertCharsRLMActionPerformed() {
        Core.getEditor().insertText("\u200F");
    }

    public void insertCharsLREActionPerformed() {
        Core.getEditor().insertText("\u202A");
    }

    public void insertCharsRLEActionPerformed() {
        Core.getEditor().insertText("\u202B");
    }

    public void insertCharsPDFActionPerformed() {
        Core.getEditor().insertText("\u202C");
    }

    public void editMultipleDefaultActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(false);
    }

    public void editMultipleAlternateActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(true);
    }

    public void editRegisterUntranslatedMenuItemActionPerformed() {
        Core.getEditor().registerUntranslated();
    }

    public void editRegisterEmptyMenuItemActionPerformed() {
        Core.getEditor().registerEmptyTranslation();
    }

    public void editRegisterIdenticalMenuItemActionPerformed() {
        Core.getEditor().registerIdenticalTranslation();
    }

    public void toolsShowStatisticsStandardMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.STANDARD)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(), StatisticsWindow.STAT_TYPE.MATCHES)
                .setVisible(true);
    }

    public void toolsShowStatisticsMatchesPerFileMenuItemActionPerformed() {
        new StatisticsWindow(Core.getMainWindow().getApplicationFrame(),
                StatisticsWindow.STAT_TYPE.MATCHES_PER_FILE).setVisible(true);
    }

    public void optionsAutoCompleteShowAutomaticallyItemActionPerformed() {
        /*
         * Preferences.setPreference(Preferences.
         * AC_SHOW_SUGGESTIONS_AUTOMATICALLY,
         * mainWindow.menu.optionsAutoCompleteShowAutomaticallyItem.isSelected()
         * );
         */ }

    public void optionsAutoCompleteHistoryCompletionMenuItemActionPerformed() {
        /*
         * Preferences.setPreference(Preferences.AC_HISTORY_COMPLETION_ENABLED,
         * mainWindow.menu.optionsAutoCompleteHistoryCompletionMenuItem.
         * isSelected());
         */ }

    public void optionsAutoCompleteHistoryPredictionMenuItemActionPerformed() {
        /*
         * Preferences.setPreference(Preferences.AC_HISTORY_PREDICTION_ENABLED,
         * mainWindow.menu.optionsAutoCompleteHistoryPredictionMenuItem.
         * isSelected());
         */ }

    public void optionsMTAutoFetchCheckboxMenuItemActionPerformed() {
        /*
         * boolean enabled =
         * mainWindow.menu.optionsMTAutoFetchCheckboxMenuItem.isSelected();
         * Preferences.setPreference(Preferences.MT_AUTO_FETCH, enabled);
         */ }

    public void optionsGlossaryFuzzyMatchingCheckBoxMenuItemActionPerformed() {
        /*
         * Preferences.setPreference(Preferences.GLOSSARY_STEMMING,
         * mainWindow.menu.optionsGlossaryFuzzyMatchingCheckBoxMenuItem.
         * isSelected()); Preferences.save();
         */ }

    public void optionsDictionaryFuzzyMatchingCheckBoxMenuItemActionPerformed() {
        /*
         * Preferences.setPreference(Preferences.DICTIONARY_FUZZY_MATCHING,
         * mainWindow.menu.optionsDictionaryFuzzyMatchingCheckBoxMenuItem.
         * isSelected()); Preferences.save();
         */ }

    /**
     * Displays the filters setup dialog to allow customizing file filters in
     * detail.
     */
    public void optionsSetupFileFiltersMenuItemActionPerformed() {
        new PreferencesWindowController().show(mainWindow.getApplicationFrame(),
                FiltersCustomizerController.class);
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the
     * segmentation rules in detail.
     */
    public void optionsSentsegMenuItemActionPerformed() {
        new PreferencesWindowController().show(mainWindow.getApplicationFrame(),
                SegmentationCustomizerController.class);
    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse
     * workflow options.
     */
    public void optionsWorkflowMenuItemActionPerformed() {
        new PreferencesWindowController().show(mainWindow.getApplicationFrame(),
                EditingBehaviorController.class);
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     */
    public void viewRestoreGUIMenuItemActionPerformed() {
    }

    public void optionsAccessConfigDirMenuItemActionPerformed() {
        openFile(new File(StaticUtils.getConfigDir()));
    }

    /**
     * Show log
     */
    public void helpLogMenuItemActionPerformed() {
        new LogDialog(mainWindow.getApplicationFrame()).setVisible(true);
    }

    public void helpAboutMenuItemActionPerformed() {
        JDialog aboutDialog = new AboutDialog(mainWindow.getApplicationFrame());
        aboutDialog.setVisible(true);
    }

    private void openFile(File file) {
        Log.log("TestMainWindowMenuHanlder.openFile called with " + file.toString());
    }
}
