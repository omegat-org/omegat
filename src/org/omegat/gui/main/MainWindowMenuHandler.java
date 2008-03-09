/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.Core;
import org.omegat.core.ProjectProperties;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.DialogThread;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.HelpFrame;
import org.omegat.gui.SearchWindow;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.gui.dialogs.SpellcheckerConfigurationDialog;
import org.omegat.gui.dialogs.WorkflowOptionsDialog;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.Styles;

/**
 * Handler for main menu items.
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindowMenuHandler {
    private final MainWindow mainWindow;

    public MainWindowMenuHandler(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Create new project.
     */
    public void projectNewMenuItemActionPerformed() {
        mainWindow.doCreateProject();
    }

    /**
     * Open project.
     */
    public void projectOpenMenuItemActionPerformed() {
        mainWindow.doLoadProject();
    }

    /**
     * Imports the file/files/folder into project's source files.
     * 
     * @author Kim Bruning
     * @author Maxym Mykhalchuk
     */
    public void projectImportMenuItemActionPerformed() {
        mainWindow.doImportSourceFiles();
    }

    public void projectWikiImportMenuItemActionPerformed() {
        mainWindow.doWikiImport();
    }

    public void projectReloadMenuItemActionPerformed() {
        mainWindow.doReloadProject();
    }

    /**
     * Close project.
     */
    public void projectCloseMenuItemActionPerformed() {
        mainWindow.doCloseProject();
    }

    /**
     * Save project.
     */
    public void projectSaveMenuItemActionPerformed() {
        // commit the current entry first
        mainWindow.commitEntry(true);
        mainWindow.activateEntry();
        mainWindow.doSave();
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed() {
        if (!mainWindow.isProjectLoaded())
            return;

        try {
            Core.getDataEngine().compileProject();
        } catch (IOException e) {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), e);
        } catch (TranslationException te) {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), te);
        }
    }

    /** Edits project's properties */
    public void projectEditMenuItemActionPerformed() {
        ProjectProperties config = CommandThread.core.getProjectProperties();
        boolean changed = false;
        try {
            changed = config.editProject(mainWindow);
        } catch (IOException ioe) {
            mainWindow.displayWarning(OStrings.getString("MW_ERROR_PROJECT_NOT_EDITABLE"), ioe);
        }

        if (changed) {
            int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"), OStrings
                    .getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
                mainWindow.doReloadProject();
        }
    }

    public void viewFileListMenuItemActionPerformed() {
        if (mainWindow.m_projWin == null) {
            mainWindow.menu.viewFileListMenuItem.setSelected(false);
            return;
        }

        // if the project window is not shown or in the background, show it
        if (!mainWindow.m_projWin.isActive()) {
            mainWindow.m_projWin.buildDisplay();
            mainWindow.m_projWin.setVisible(true);
            mainWindow.m_projWin.toFront();
        }
        // otherwise hide it
        else {
            mainWindow.m_projWin.setVisible(false);
        }
    }

    /** Quits OmegaT */
    public void projectExitMenuItemActionPerformed() {
        boolean projectModified = false;
        if (mainWindow.isProjectLoaded())
            projectModified = CommandThread.core.isProjectModified();

        // RFE 1302358
        // Add Yes/No Warning before OmegaT quits
        if (projectModified || Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT)) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(mainWindow, OStrings
                    .getString("MW_QUIT_CONFIRM"), OStrings.getString("CONFIRM_DIALOG_TITLE"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }

        MainWindowUI.saveScreenLayout(mainWindow);
        Preferences.save();

        if (mainWindow.isProjectLoaded())
            mainWindow.doSave();

        // shut down
        if (CommandThread.core != null)
            CommandThread.core.interrupt();

        // waiting for CommandThread to finish for 1 minute
        for (int i = 0; i < 600 && CommandThread.core != null; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        System.exit(0);
    }

    public void editUndoMenuItemActionPerformed() {
        try {
            synchronized (mainWindow.editor) {
                mainWindow.editor.undoOneEdit();
            }
        } catch (CannotUndoException cue) {
        }
    }

    public void editRedoMenuItemActionPerformed() {
        try {
            synchronized (mainWindow.editor) {
                mainWindow.editor.redoOneEdit();
            }
        } catch (CannotRedoException cue) {
        }
    }

    public void editOverwriteTranslationMenuItemActionPerformed() {
        mainWindow.doRecycleTrans();
    }

    public void editInsertTranslationMenuItemActionPerformed() {
        mainWindow.doInsertTrans();
    }

    /**
     * replaces entire edited segment text with a the source text of a segment
     * at cursor position
     */
    public void editOverwriteSourceMenuItemActionPerformed() {
        synchronized (mainWindow) {
            if (!mainWindow.isProjectLoaded())
                return;

            mainWindow.doReplaceEditText(mainWindow.m_curEntry.getSrcText());
        }
    }

    /** inserts the source text of a segment at cursor position */
    public void editInsertSourceMenuItemActionPerformed() {
        synchronized (mainWindow) {
            if (!mainWindow.isProjectLoaded())
                return;

            mainWindow.doInsertText(mainWindow.m_curEntry.getSrcText());
        }
    }

    public void editFindInProjectMenuItemActionPerformed() {
        if (!mainWindow.isProjectLoaded())
            return;

        synchronized (mainWindow.editor) {
            String selection = mainWindow.editor.getSelectedText();
            if (selection != null)
                selection.trim();

            // SearchThread srch = new SearchThread(this, selection);
            // srch.start();
            SearchWindow search = new SearchWindow(mainWindow, selection);
            DialogThread dt = new DialogThread(search);
            dt.start();
            mainWindow.m_searches.add(search);
        }
    }

    /** Set active match to #1. */
    public void editSelectFuzzy1MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(0);
    }

    /** Set active match to #2. */
    public void editSelectFuzzy2MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(1);
    }

    /** Set active match to #3. */
    public void editSelectFuzzy3MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(2);
    }

    /** Set active match to #4. */
    public void editSelectFuzzy4MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(3);
    }

    /** Set active match to #5. */
    public void editSelectFuzzy5MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(4);
    }

    public void cycleSwitchCaseMenuItemActionPerformed() {
        synchronized (mainWindow.editor) {
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.CYCLE);
        }
    }

    public void titleCaseMenuItemActionPerformed() {
        synchronized (mainWindow.editor) {
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.TITLE);
        }
    }

    public void upperCaseMenuItemActionPerformed() {
        synchronized (mainWindow.editor) {
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.UPPER);
        }
    }

    public void lowerCaseMenuItemActionPerformed() {
        synchronized (mainWindow.editor) {
            Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.LOWER);
        }
    }

    public void gotoNextUntranslatedMenuItemActionPerformed() {
        mainWindow.doNextUntranslatedEntry();
    }

    public void gotoNextSegmentMenuItemActionPerformed() {
        mainWindow.doNextEntry();
    }

    public void gotoPreviousSegmentMenuItemActionPerformed() {
        mainWindow.doPrevEntry();
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public void gotoSegmentMenuItemActionPerformed() {
        synchronized (mainWindow) {
            // Create a dialog for input
            final JOptionPane input = new JOptionPane(OStrings.getString("MW_PROMPT_SEG_NR_MSG"),
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION); // create
            // option
            // pane
            input.setWantsInput(true); // make it require input
            final JDialog dialog = new JDialog(mainWindow, OStrings.getString("MW_PROMPT_SEG_NR_TITLE"), true); // create
            // dialog
            dialog.setContentPane(input); // add option pane to dialog

            // Make the dialog verify the input
            input.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent event) {
                    // Handle the event
                    if (dialog.isVisible() && (event.getSource() == input)) {
                        // If user pressed Enter or OK, check the input
                        String property = event.getPropertyName();
                        Object value = input.getValue();

                        // Don't do the checks if no option has been selected
                        if (value == JOptionPane.UNINITIALIZED_VALUE)
                            return;

                        if (property.equals(JOptionPane.INPUT_VALUE_PROPERTY)
                                || (property.equals(JOptionPane.VALUE_PROPERTY) && ((Integer) value).intValue() == JOptionPane.OK_OPTION)) {
                            // Prevent the checks from being done twice
                            input.setValue(JOptionPane.UNINITIALIZED_VALUE);

                            // Get the value entered by the user
                            String inputValue = (String) input.getInputValue();

                            // Check if the user entered a value at all
                            if ((inputValue == null) || (inputValue.trim().length() == 0)) {
                                // Show error message
                                displayErrorMessage();
                                return;
                            }

                            // Check if the user really entered a number
                            int segmentNr = -1;
                            try {
                                // Just parse it. If parsed, it's a number.
                                segmentNr = Integer.parseInt(inputValue);
                            } catch (NumberFormatException e) {
                                // If the exception is thrown, the user didn't
                                // enter a number
                                // Show error message
                                displayErrorMessage();
                                return;
                            }

                            // Check if the segment number is within bounds
                            if (segmentNr < 1 || segmentNr > CommandThread.core.numEntries()) {
                                // Tell the user he has to enter a number within
                                // certain bounds
                                displayErrorMessage();
                                return;
                            }
                        }

                        // If we're here, the user has either pressed
                        // Cancel/Esc,
                        // or has entered a valid number. In all cases, close
                        // the dialog.
                        dialog.setVisible(false);
                    }
                }

                private void displayErrorMessage() {
                    JOptionPane.showMessageDialog(dialog, StaticUtils.format(OStrings
                            .getString("MW_SEGMENT_NUMBER_ERROR"), new Object[] { new Integer(CommandThread.core
                            .numEntries()) }), OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                }
            });

            // Show the input dialog
            dialog.pack(); // make it look good
            dialog.setLocationRelativeTo(mainWindow); // center it on the main
            // window
            dialog.setVisible(true); // show it

            // Get the input value, if any
            Object inputValue = input.getInputValue();
            if ((inputValue != null) && !inputValue.equals(JOptionPane.UNINITIALIZED_VALUE)) {
                // Go to the segment the user requested
                try {
                    mainWindow.doGotoEntry((String) inputValue);
                } catch (ClassCastException e) {
                    // Shouldn't happen, but still... Just eat silently.
                }
            }
        }
    }

    public void gotoHistoryBackMenuItemActionPerformed() {
        synchronized (mainWindow) {
            int prevValue = mainWindow.history.back();
            if (prevValue != -1)
                mainWindow.doGotoEntry(prevValue + 1);
        }
    }

    public void gotoHistoryForwardMenuItemActionPerformed() {
        synchronized (mainWindow) {
            int nextValue = mainWindow.history.forward();
            if (nextValue != -1)
                mainWindow.doGotoEntry(nextValue + 1);
        }
    }

    public void viewMarkTranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.MARK_TRANSLATED_SEGMENTS,
                mainWindow.menu.viewMarkTranslatedSegmentsCheckBoxMenuItem.isSelected());
        if (mainWindow.menu.viewMarkTranslatedSegmentsCheckBoxMenuItem.isSelected())
            mainWindow.m_translatedAttributeSet = Styles.TRANSLATED;
        else
            mainWindow.m_translatedAttributeSet = Styles.PLAIN;

        mainWindow.commitEntry(false);
        mainWindow.loadDocument();
        mainWindow.activateEntry();
    }

    public void viewMarkUntranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS,
                mainWindow.menu.viewMarkUntranslatedSegmentsCheckBoxMenuItem.isSelected());
        if (mainWindow.menu.viewMarkUntranslatedSegmentsCheckBoxMenuItem.isSelected())
            mainWindow.m_unTranslatedAttributeSet = Styles.UNTRANSLATED;
        else
            mainWindow.m_unTranslatedAttributeSet = Styles.PLAIN;

        mainWindow.commitEntry(false);
        mainWindow.loadDocument();
        mainWindow.activateEntry();
    }

    public void viewDisplaySegmentSourceCheckBoxMenuItemActionPerformed() {
        mainWindow.commitEntry(false);

        mainWindow.m_displaySegmentSources = mainWindow.menu.viewDisplaySegmentSourceCheckBoxMenuItem.isSelected();

        Preferences.setPreference(Preferences.DISPLAY_SEGMENT_SOURCES, mainWindow.m_displaySegmentSources);

        mainWindow.loadDocument();
        mainWindow.activateEntry();
    }

    public void toolsValidateTagsMenuItemActionPerformed() {
        Core.getTagValidation().validateTags();
    }

    public void optionsTabAdvanceCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.USE_TAB_TO_ADVANCE, mainWindow.menu.optionsTabAdvanceCheckBoxMenuItem
                .isSelected());
        if (mainWindow.menu.optionsTabAdvanceCheckBoxMenuItem.isSelected())
            mainWindow.m_advancer = KeyEvent.VK_TAB;
        else
            mainWindow.m_advancer = KeyEvent.VK_ENTER;
    }

    public void optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.ALWAYS_CONFIRM_QUIT,
                mainWindow.menu.optionsAlwaysConfirmQuitCheckBoxMenuItem.isSelected());
    }

    /**
     * Displays the font dialog to allow selecting the font for source, target
     * text (in main window) and for match and glossary windows.
     */
    public void optionsFontSelectionMenuItemActionPerformed() {
        FontSelectionDialog dlg = new FontSelectionDialog(mainWindow, mainWindow.m_font);
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == FontSelectionDialog.RET_OK_CHANGED) {
            // fonts have changed
            // first commit current translation
            mainWindow.commitEntry(false); // part of fix for bug 1409309
            mainWindow.m_font = dlg.getSelectedFont();
            synchronized (mainWindow.editor) {
                mainWindow.editor.setFont(mainWindow.m_font);
            }
            mainWindow.matches.setFont(mainWindow.m_font);
            mainWindow.glossary.setFont(mainWindow.m_font);
            Core.getTagValidation().setFont(mainWindow.m_font);
            if (mainWindow.m_projWin != null)
                mainWindow.m_projWin.setFont(mainWindow.m_font);

            Preferences.setPreference(OConsts.TF_SRC_FONT_NAME, mainWindow.m_font.getName());
            Preferences.setPreference(OConsts.TF_SRC_FONT_SIZE, mainWindow.m_font.getSize());
            mainWindow.activateEntry();
        }
    }

    /**
     * Displays the filters setup dialog to allow customizing file filters in
     * detail.
     */
    public void optionsSetupFileFiltersMenuItemActionPerformed() {
        FiltersCustomizer dlg = new FiltersCustomizer(mainWindow);
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == FiltersCustomizer.RET_OK) {
            // saving config
            FilterMaster.getInstance().saveConfig();

            if (mainWindow.isProjectLoaded()) {
                // asking to reload a project
                int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"), OStrings
                        .getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION)
                    mainWindow.doReloadProject();
            }
        } else {
            // reloading config from disk
            FilterMaster.getInstance().loadConfig();
        }
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the
     * segmentation rules in detail.
     */
    public void optionsSentsegMenuItemActionPerformed() {
        SegmentationCustomizer segment_window = new SegmentationCustomizer(mainWindow);
        segment_window.setVisible(true);

        if (segment_window.getReturnStatus() == SegmentationCustomizer.RET_OK && mainWindow.isProjectLoaded()) {
            // asking to reload a project
            int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"), OStrings
                    .getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
                mainWindow.doReloadProject();
        }
    }

    /**
     * Opens the spell checking window
     */
    public void optionsSpellCheckMenuItemActionPerformed() {
        SpellcheckerConfigurationDialog sd = new SpellcheckerConfigurationDialog(mainWindow, Core.getDataEngine()
                .getProjectProperties().getTargetLanguage());
        sd.setVisible(true);
        if (sd.getReturnStatus() == SpellcheckerConfigurationDialog.RET_OK) {
            mainWindow.m_autoSpellChecking = Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
            if (mainWindow.m_autoSpellChecking) {
                SpellChecker sc = CommandThread.core.getSpellchecker();
                sc.destroy();
                sc.initialize();
            }
            mainWindow.commitEntry(false);
            mainWindow.loadDocument();
            mainWindow.activateEntry();
        }
    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse
     * workflow options.
     */
    public void optionsWorkflowMenuItemActionPerformed() {
        new WorkflowOptionsDialog(mainWindow).setVisible(true);
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     * 
     * Note: The current implementation is just a quick hack, due to
     * insufficient knowledge of the docking framework library.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public void optionsRestoreGUIMenuItemActionPerformed() {
        try {
            String layout = "60#63#120#109#108#32#118#101#114#115#105#111#110#61#34#49#46#48#34#63#62#10#60#68#111#99#107#105#110#103#68#101#115#107#116#111#112#32#118#101#114#115#105#111#110#61#34#50#46#48#34#62#10#60#68#111#99#107#105#110#103#80#97#110#101#108#62#10#60#83#112#108#105#116#32#111#114#105#101#110#116#97#116#105#111#110#61#34#49#34#32#108#111#99#97#116#105#111#110#61#34#48#46#53#57#53#51#48#55#57#49#55#56#56#56#53#54#51#49#34#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#69#68#73#84#79#82#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#83#112#108#105#116#32#111#114#105#101#110#116#97#116#105#111#110#61#34#48#34#32#108#111#99#97#116#105#111#110#61#34#48#46#54#57#55#52#53#50#50#50#57#50#57#57#51#54#51#34#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#77#65#84#67#72#69#83#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#68#111#99#107#97#98#108#101#62#10#60#75#101#121#32#100#111#99#107#78#97#109#101#61#34#71#76#79#83#83#65#82#89#34#47#62#10#60#47#68#111#99#107#97#98#108#101#62#10#60#47#83#112#108#105#116#62#10#60#47#83#112#108#105#116#62#10#60#47#68#111#99#107#105#110#103#80#97#110#101#108#62#10#60#84#97#98#71#114#111#117#112#115#62#10#60#47#84#97#98#71#114#111#117#112#115#62#10#60#47#68#111#99#107#105#110#103#68#101#115#107#116#111#112#62#10";
            byte[] bytes = StaticUtils.uudecode(layout);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            mainWindow.desktop.readXML(in);
            in.close();
        } catch (Exception exception) {
            // eat silently, probably a bug in the docking framework
        }
    }

    /**
     * Show help.
     */
    public void helpContentsMenuItemActionPerformed() {
        HelpFrame hf = HelpFrame.getInstance();
        hf.setVisible(true);
        hf.toFront();
    }

    /**
     * Shows About dialog
     */
    public void helpAboutMenuItemActionPerformed() {
        new AboutDialog(mainWindow).setVisible(true);
    }
}
