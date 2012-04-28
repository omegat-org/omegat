/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009 Didier Briel, Alex Buloichik
               2010 Wildrich Fourie, Didier Briel
               2012 Wildrich Fourie, Guido Leenders, Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.main;

import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.KnownException;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.ExternalTMXMatchesDialog;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.gui.dialogs.SaveOptionsDialog;
import org.omegat.gui.dialogs.SpellcheckerConfigurationDialog;
import org.omegat.gui.dialogs.TagValidationOptionsDialog;
import org.omegat.gui.dialogs.TeamOptionsDialog;
import org.omegat.gui.dialogs.UserPassDialog;
import org.omegat.gui.dialogs.ViewOptionsDialog;
import org.omegat.gui.dialogs.WorkflowOptionsDialog;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.help.HelpFrame;
import org.omegat.gui.search.SearchWindow;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
 * @author Didier Briel
 * @author Wildrich Fourie
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
        ProjectUICommands.projectCreate();
    }

    /**
     * Create new team project.
     */
    public void projectTeamNewMenuItemActionPerformed() {
        ProjectUICommands.projectTeamCreate();
    }

    /**
     * Open project.
     */
    public void projectOpenMenuItemActionPerformed() {
        ProjectUICommands.projectOpen(null);
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
        ProjectUICommands.projectReload();
    }

    /**
     * Close project.
     */
    public void projectCloseMenuItemActionPerformed() {
        ProjectUICommands.projectClose();
    }

    /**
     * Save project.
     */
    public void projectSaveMenuItemActionPerformed() {
        ProjectUICommands.projectSave();
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed() {
        ProjectUICommands.projectCompile();
    }

    /** Edits project's properties */
    public void projectEditMenuItemActionPerformed() {
        ProjectUICommands.projectEditProperties();
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

    /**
     * Empties the exported segments.
     */
    private void flushExportedSegments() {
        FileUtil.writeScriptFile("", OConsts.SOURCE_EXPORT);
        FileUtil.writeScriptFile("", OConsts.TARGET_EXPORT);
    }

    /** Quits OmegaT */
    public void projectExitMenuItemActionPerformed() {
        boolean projectModified = false;
        if (Core.getProject().isProjectLoaded())
            projectModified = Core.getProject().isProjectModified();

        // RFE 1302358
        // Add Yes/No Warning before OmegaT quits
        if (projectModified || Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT)) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(mainWindow,
                    OStrings.getString("MW_QUIT_CONFIRM"), OStrings.getString("CONFIRM_DIALOG_TITLE"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }

        flushExportedSegments();

        new SwingWorker<Object, Void>() {
            protected Object doInBackground() throws Exception {
                if (Core.getProject().isProjectLoaded()) {
                    // Save the list of learned and ignore words
                    ISpellChecker sc = Core.getSpellChecker();
                    sc.saveWordLists();
                    try {
                        Core.getProject().saveProject();
                    } catch (KnownException ex) {
                        // hide exception on shutdown
                    }
                }

                CoreEvents.fireApplicationShutdown();

                return null;
            }

            protected void done() {
                try {
                    get();

                    MainWindowUI.saveScreenLayout(mainWindow);

                    Preferences.save();

                    System.exit(0);
                } catch (Exception ex) {
                    Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                    Core.getMainWindow().displayErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                }
            }
        }.execute();
    }

    public void editUndoMenuItemActionPerformed() {
        Core.getEditor().undo();
    }

    public void editRedoMenuItemActionPerformed() {
        Core.getEditor().redo();
    }

    public void editOverwriteTranslationMenuItemActionPerformed() {
        mainWindow.doRecycleTrans();
    }

    public void editInsertTranslationMenuItemActionPerformed() {
        mainWindow.doInsertTrans();
    }

    public void editOverwriteMachineTranslationMenuItemActionPerformed() {
        String tr = Core.getMachineTranslatePane().getDisplayedTranslation();
        if (!StringUtil.isEmpty(tr)) {
            Core.getEditor().replaceEditText(tr);
        }
    }

    /**
     * replaces entire edited segment text with a the source text of a segment at cursor position
     */
    public void editOverwriteSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded())
            return;

        Core.getEditor().replaceEditText(Core.getEditor().getCurrentEntry().getSrcText());
    }

    /** inserts the source text of a segment at cursor position */
    public void editInsertSourceMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded())
            return;

        Core.getEditor().insertText(Core.getEditor().getCurrentEntry().getSrcText());
    }

    public void editExportSelectionMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded())
            return;

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

        FileUtil.writeScriptFile(selection, OConsts.SELECTION_EXPORT);
    }

    public void editCreateGlossaryEntryMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded())
            return;

        Core.getGlossary().showCreateGlossaryEntryDialog();
    }

    public void editFindInProjectMenuItemActionPerformed() {
        if (!Core.getProject().isProjectLoaded())
            return;

        String selection = Core.getEditor().getSelectedText();
        if (selection != null)
            selection.trim();

        SearchWindow search = new SearchWindow(mainWindow, selection);
        search.setVisible(true);
        mainWindow.addSearchWindow(search);
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

    public void editMultipleDefaultActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(false);
    }
    
    public void editMultipleAlternateActionPerformed() {
        Core.getEditor().setAlternateTranslationForCurrentEntry(true);
    }

    public void cycleSwitchCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.CYCLE);
    }

    public void titleCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.TITLE);
    }

    public void upperCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.UPPER);
    }

    public void lowerCaseMenuItemActionPerformed() {
        Core.getEditor().changeCase(IEditor.CHANGE_CASE_TO.LOWER);
    }

    public void gotoNextUntranslatedMenuItemActionPerformed() {
        Core.getEditor().nextUntranslatedEntry();
    }

    public void gotoNextSegmentMenuItemActionPerformed() {
        Core.getEditor().nextEntry();
    }

    public void gotoPreviousSegmentMenuItemActionPerformed() {
        Core.getEditor().prevEntry();
    }

    public void gotoNextNoteMenuItemActionPerformed() {
        Core.getEditor().nextEntryWithNote();
    }

    public void gotoPreviousNoteMenuItemActionPerformed() {
        Core.getEditor().prevEntryWithNote();
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public void gotoSegmentMenuItemActionPerformed() {
        // Create a dialog for input
        final JOptionPane input = new JOptionPane(OStrings.getString("MW_PROMPT_SEG_NR_MSG"),
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION); // create
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

                        int maxNr = Core.getProject().getAllEntries().size();

                        // Check if the user entered a value at all
                        if ((inputValue == null) || (inputValue.trim().length() == 0)) {
                            // Show error message
                            displayErrorMessage(maxNr);
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
                            displayErrorMessage(maxNr);
                            return;
                        }

                        // Check if the segment number is within bounds
                        if (segmentNr < 1 || segmentNr > maxNr) {
                            // Tell the user he has to enter a number within
                            // certain bounds
                            displayErrorMessage(maxNr);
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

            private void displayErrorMessage(int maxNr) {
                JOptionPane.showMessageDialog(dialog,
                        StaticUtils.format(OStrings.getString("MW_SEGMENT_NUMBER_ERROR"), maxNr),
                        OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show the input dialog
        dialog.pack(); // make it look good
        dialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame()); // center it on the main window
        dialog.setVisible(true); // show it

        // Get the input value, if any
        Object inputValue = input.getInputValue();
        if ((inputValue != null) && !inputValue.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            // Go to the segment the user requested
            try {
                Core.getEditor().gotoEntry(Integer.parseInt((String) inputValue));
            } catch (ClassCastException e) {
                // Shouldn't happen, but still... Just eat silently.
            } catch (NumberFormatException e) {
            }
        }
    }

    public void gotoHistoryBackMenuItemActionPerformed() {
        Core.getEditor().gotoHistoryBack();
    }

    public void gotoHistoryForwardMenuItemActionPerformed() {
        Core.getEditor().gotoHistoryForward();
    }

    public void viewMarkTranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setMarkTranslated(mainWindow.menu.viewMarkTranslatedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkUntranslatedSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor()
                .getSettings()
                .setMarkUntranslated(
                        mainWindow.menu.viewMarkUntranslatedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewDisplaySegmentSourceCheckBoxMenuItemActionPerformed() {
        Core.getEditor()
                .getSettings()
                .setDisplaySegmentSources(
                        mainWindow.menu.viewDisplaySegmentSourceCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNonUniqueSegmentsCheckBoxMenuItemActionPerformed() {
        Core.getEditor()
                .getSettings()
                .setMarkNonUniqueSegments(
                        mainWindow.menu.viewMarkNonUniqueSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNotedSegmentsCheckBoxMenuItemActionPerformed(){
        Core.getEditor()
                .getSettings()
                .setMarkNotedSegments(
                        mainWindow.menu.viewMarkNotedSegmentsCheckBoxMenuItem.isSelected());
    }

    public void viewMarkNBSPCheckBoxMenuItemActionPerformed(){
        Core.getEditor()
                .getSettings()
                .setMarkNBSP(
                        mainWindow.menu.viewMarkNBSPCheckBoxMenuItem.isSelected());
    }

    public void viewDisplayModificationInfoNoneRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE);
    }

    public void viewDisplayModificationInfoSelectedRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED);
    }

    public void viewDisplayModificationInfoAllRadioButtonMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setDisplayModificationInfo(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL);
    }

    public void toolsValidateTagsMenuItemActionPerformed() {
        Core.getTagValidation().validateTags();
    }

    /**
     * Identify all the placeholders in the source text and automatically inserts them into the target text.
     */
    public void editTagPainterMenuItemActionPerformed() {

        String sourceText = Core.getEditor().getCurrentEntry().getSrcText();
        String placeholderString = StaticUtils.buildPaintPlaceholderList(sourceText);

        if (!placeholderString.equals("")) {
            Core.getEditor().insertText(placeholderString);
        }
    }

    public void toolsShowStatisticsStandardMenuItemActionPerformed() {
        new StatisticsWindow(StatisticsWindow.STAT_TYPE.STANDARD).setVisible(true);
    }

    public void toolsShowStatisticsMatchesMenuItemActionPerformed() {
        new StatisticsWindow(StatisticsWindow.STAT_TYPE.MATCHES).setVisible(true);
    }
    
    
    public void toolsRemoveTagsCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.REMOVE_TAGS, 
                mainWindow.menu.toolsRemoveTagsCheckBoxMenuItem.isSelected());
        // Ask for reload.
        int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"),
                OStrings.getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION)        
            ProjectUICommands.projectReload();
    }

    

    public void optionsTabAdvanceCheckBoxMenuItemActionPerformed() {
        Core.getEditor().getSettings()
                .setUseTabForAdvance(mainWindow.menu.optionsTabAdvanceCheckBoxMenuItem.isSelected());
    }

    public void optionsAlwaysConfirmQuitCheckBoxMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.ALWAYS_CONFIRM_QUIT,
                mainWindow.menu.optionsAlwaysConfirmQuitCheckBoxMenuItem.isSelected());
    }

    public void optionsTransTipsEnableMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.TRANSTIPS,
                mainWindow.menu.optionsTransTipsEnableMenuItem.isSelected());
    }

    public void optionsTransTipsExactMatchMenuItemActionPerformed() {
        Preferences.setPreference(Preferences.TRANSTIPS_EXACT_SEARCH,
                mainWindow.menu.optionsTransTipsExactMatchMenuItem.isSelected());
    }

    /**
     * Displays the font dialog to allow selecting the font for source, target text (in main window) and for
     * match and glossary windows.
     */
    public void optionsFontSelectionMenuItemActionPerformed() {
        FontSelectionDialog dlg = new FontSelectionDialog(Core.getMainWindow().getApplicationFrame(), Core
                .getMainWindow().getApplicationFont());
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == FontSelectionDialog.RET_OK_CHANGED) {
            mainWindow.setApplicationFont(dlg.getSelectedFont());
        }
    }

    /**
     * Displays the filters setup dialog to allow customizing file filters in detail.
     */
    public void optionsSetupFileFiltersMenuItemActionPerformed() {
        FiltersCustomizer dlg = new FiltersCustomizer(mainWindow, false);
        dlg.setVisible(true);
        if (dlg.result != null) {
            // saving config
            FilterMaster.getInstance().setConfig(dlg.result);
            FilterMaster.getInstance().saveConfig();

            if (Core.getProject().isProjectLoaded() ) {
                if (Core.getProject().getFilterMaster() != null) {
                    //project specific filters are in place. No need to reload project when non-project-specific filters are changed
                    return;
                }
                // asking to reload a project
                int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"),
                        OStrings.getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION)
                    ProjectUICommands.projectReload();
            }
        }
    }

    /**
     * Displays the segmentation setup dialog to allow customizing the segmentation rules in detail.
     */
    public void optionsSentsegMenuItemActionPerformed() {
        SegmentationCustomizer segment_window = new SegmentationCustomizer(mainWindow, false, null);
        segment_window.setVisible(true);

        if (segment_window.getReturnStatus() == SegmentationCustomizer.RET_OK
                && Core.getProject().isProjectLoaded() && Core.getProject().getSRX()==null) {
            // asking to reload a project
            int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"),
                    OStrings.getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
                ProjectUICommands.projectReload();
        }
    }

    /**
     * Opens the spell checking window
     */
    public void optionsSpellCheckMenuItemActionPerformed() {
        Language currentLanguage = null;
        if (Core.getProject().isProjectLoaded()) {
            currentLanguage = Core.getProject().getProjectProperties().getTargetLanguage();
        } else {
            currentLanguage = new Language(Preferences.getPreference(Preferences.TARGET_LOCALE));
        }
        SpellcheckerConfigurationDialog sd = new SpellcheckerConfigurationDialog(mainWindow, currentLanguage);

        sd.setVisible(true);

        if (sd.getReturnStatus() == SpellcheckerConfigurationDialog.RET_OK) {
            boolean isNeedToSpell = Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
            if (isNeedToSpell && Core.getProject().isProjectLoaded()) {
                ISpellChecker sc = Core.getSpellChecker();
                sc.destroy();
                sc.initialize();
            }
            Core.getEditor().getSettings().setAutoSpellChecking(isNeedToSpell);
        }
    }

    /**
     * Displays the workflow setup dialog to allow customizing the diverse workflow options.
     */
    public void optionsWorkflowMenuItemActionPerformed() {
        new WorkflowOptionsDialog(mainWindow).setVisible(true);
    }

    /**
     * Displays the tag validation setup dialog to allow customizing the diverse tag validation options.
     */
    public void optionsTagValidationMenuItemActionPerformed() {
        TagValidationOptionsDialog tagValidationOptionsDialog = new TagValidationOptionsDialog(mainWindow);
        tagValidationOptionsDialog.setVisible(true);
        
        if (tagValidationOptionsDialog.getReturnStatus() == TagValidationOptionsDialog.RET_OK
                && Core.getProject().isProjectLoaded()) {
            // Redisplay according to new view settings
            Core.getEditor().getSettings().updateTagValidationPreferences();
        }
        
    }

    /**
     * Displays the team options dialog to allow customizing the diverse team options.
     */
    public void optionsTeamMenuItemActionPerformed() {
        new TeamOptionsDialog(mainWindow).setVisible(true);
    }

    /**
     * Displays the external TMX dialog to allow customizing the external TMX options.
     */
    public void optionsExtTMXMenuItemActionPerformed() {

        ExternalTMXMatchesDialog externalTMXOptions = new ExternalTMXMatchesDialog(mainWindow);
        externalTMXOptions.setVisible(true);

        if (externalTMXOptions.getReturnStatus() == ExternalTMXMatchesDialog.RET_OK
                && Core.getProject().isProjectLoaded()) {
            // asking to reload a project
            int res = JOptionPane.showConfirmDialog(mainWindow, OStrings.getString("MW_REOPEN_QUESTION"),
                    OStrings.getString("MW_REOPEN_TITLE"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
                ProjectUICommands.projectReload();
        }

    }

    /**
     * Displays the view options dialog to allow customizing the view options.
     */
    public void optionsViewOptionsMenuItemActionPerformed() {

        ViewOptionsDialog viewOptions = new ViewOptionsDialog(mainWindow);
        viewOptions.setVisible(true);

        if (viewOptions.getReturnStatus() == ViewOptionsDialog.RET_OK
                && Core.getProject().isProjectLoaded()) {
            // Redisplay according to new view settings
            Core.getEditor().getSettings().updateViewPreferences();
        }

    }

   /**
    * Display the save options dialog to allow setting the save interval
    */
    public void optionsSaveOptionsMenuItemActionPerformed() {
        SaveOptionsDialog saveOptions = new SaveOptionsDialog(mainWindow);
        saveOptions.setVisible(true);
    }

    /**
     * Restores defaults for all dockable parts. May be expanded in the future to reset the entire GUI to its
     * defaults.
     */
    public void optionsRestoreGUIMenuItemActionPerformed() {
        MainWindowUI.resetDesktopLayout(mainWindow);
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

    /**
     * Displays the dialog to set login and password for proxy.
     */
    public void optionsViewOptionsMenuLoginItemActionPerformed() {
        UserPassDialog proxyOptions = new UserPassDialog(mainWindow);

        String encodedUser = (Preferences.getPreference(Preferences.PROXY_USER_NAME));
        String encodedPassword = (Preferences.getPreference(Preferences.PROXY_PASSWORD));

        BASE64Decoder dec = new BASE64Decoder();
        try {
            proxyOptions.userText.setText(new String(dec.decodeBuffer(encodedUser)));
            proxyOptions.passwordField.setText(new String(dec.decodeBuffer(encodedPassword)));
        } catch (IOException ex) {
            Log.logErrorRB("LOG_DECODING_ERROR");
            Log.log(ex);
        }

        proxyOptions.setTitle(OStrings.getString("PROXY_LOGIN_DIALOG")); // NOI18N
        proxyOptions.descriptionTextArea.setText(OStrings.getString("PROXY_LOGIN_DESCRIPTION"));
        proxyOptions.setVisible(true);

        if (proxyOptions.getReturnStatus() == UserPassDialog.RET_OK) {
            BASE64Encoder enc = new BASE64Encoder();
            encodedUser = enc.encode(proxyOptions.userText.getText().getBytes());
            encodedPassword = enc.encode(new String(proxyOptions.passwordField.getPassword()).getBytes());

            Preferences.setPreference(Preferences.PROXY_USER_NAME, encodedUser);
            Preferences.setPreference(Preferences.PROXY_PASSWORD, encodedPassword);
        }
    }
}
