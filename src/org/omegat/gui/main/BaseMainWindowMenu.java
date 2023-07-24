/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
 *                         Benjamin Siband, and Kim Bruning
 *               2007 Zoltan Bartko
 *               2008 Andrzej Sawula, Alex Buloichik
 *               2009 Didier Briel, Alex Buloichik
 *               2010 Wildrich Fourie, Didier Briel
 *               2011 Didier Briel
 *               2012 Wildrich Fourie, Guido Leenders, Martin Fleurke, Didier Briel
 *               2013 Zoltan Bartko, Didier Briel, Yu Tang
 *               2014 Aaron Madlon-Kay
 *               2015 Didier Briel, Yu Tang
 *               2017 Didier Briel
 *               2023 Hiroshi Miura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.main;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.openide.awt.Mnemonics;

import org.omegat.CLIParameters;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.RecentProjects;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.MenuExtender;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.Styles;

/**
 * Base class for create main menu and handle main menu events.
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
 * @author Martin Fleurke
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 */

/**
 * Add newly created MenuItem items to
 * /src/org/omegat/gui/main/MainMenuShortcuts.properties and
 * /src/org/omegat/gui/main/MainMenuShortcuts.mac.properties with the proper
 * shortcuts if set.
 */
public abstract class BaseMainWindowMenu implements ActionListener, MenuListener, IMainMenu {

    private static final Logger LOGGER = Logger.getLogger(BaseMainWindowMenu.class.getName());

    /** MainWindow instance. */
    protected final MainWindow mainWindow;

    /** menu bar instance */
    protected final JMenuBar mainMenu = new JMenuBar();

    /** MainWindow menu handler instance. */
    protected final MainWindowMenuHandler mainWindowMenuHandler;

    private final Map<MenuExtender.MenuKey, JMenu> menus = new EnumMap<>(MenuExtender.MenuKey.class);

    public BaseMainWindowMenu(final MainWindow mainWindow,
            final MainWindowMenuHandler mainWindowMenuHandler) {
        this.mainWindow = mainWindow;
        this.mainWindowMenuHandler = mainWindowMenuHandler;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        // Get item name from actionCommand.
        String action = evt.getActionCommand();

        Log.logInfoRB("LOG_MENU_CLICK", action);

        invokeAction(action, evt.getModifiers());
    }

    /**
     * Code for dispatching events from components to event handlers.
     *
     * @param evt
     *            event info
     */
    @Override
    public void menuSelected(MenuEvent evt) {
        // Item what perform event.
        JMenu menu = (JMenu) evt.getSource();

        // Get item name from actionCommand.
        String action = menu.getActionCommand();

        Log.logInfoRB("LOG_MENU_CLICK", action);

        // Find method by item name.
        String methodName = action + "MenuSelected";
        Method method = null;
        try {
            method = mainWindowMenuHandler.getClass().getMethod(methodName, JMenu.class);
        } catch (NoSuchMethodException ex) {
            // method not declared
            return;
        }

        // Call ...MenuMenuSelected method.
        try {
            method.invoke(mainWindowMenuHandler, menu);
        } catch (IllegalAccessException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error execute method", ex);
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }
    }

    public void menuCanceled(MenuEvent e) {
    }

    public void menuDeselected(MenuEvent e) {
    }

    /**
     * Initialize menu items.
     * <p>
     * Call order should not be changed.
     */
    void initComponents() {
        createComponents();
        setActionCommands();
        constructMenu();
        createMenuBar();
        PropertiesShortcuts.getMainMenuShortcuts().bindKeyStrokes(mainMenu);
        configureActions();
    }

    abstract void createMenuBar();

    protected void createComponents() {
        projectMenu = createMenu("TF_MENU_FILE", MenuExtender.MenuKey.PROJECT);
        editMenu = createMenu("TF_MENU_EDIT", MenuExtender.MenuKey.EDIT);
        gotoMenu = createMenu("MW_GOTOMENU", MenuExtender.MenuKey.GOTO);
        viewMenu = createMenu("MW_VIEW_MENU", MenuExtender.MenuKey.VIEW);
        toolsMenu = createMenu("TF_MENU_TOOLS", MenuExtender.MenuKey.TOOLS);
        optionsMenu = createMenu("MW_OPTIONSMENU", MenuExtender.MenuKey.OPTIONS);
        helpMenu = createMenu("TF_MENU_HELP", MenuExtender.MenuKey.HELP);

        projectNewMenuItem = createMenuItem("TF_MENU_FILE_CREATE");
        projectTeamNewMenuItem = createMenuItem("TF_MENU_FILE_TEAM_CREATE");
        projectOpenMenuItem = createMenuItem("TF_MENU_FILE_OPEN");
        projectOpenRecentMenuItem = createMenu("TF_MENU_FILE_OPEN_RECENT");
        projectClearRecentMenuItem = createMenuItem("TF_MENU_FILE_CLEAR_RECENT");

        projectReloadMenuItem = createMenuItem("TF_MENU_PROJECT_RELOAD");
        projectCloseMenuItem = createMenuItem("TF_MENU_FILE_CLOSE");
        projectSaveMenuItem = createMenuItem("TF_MENU_FILE_SAVE");
        projectImportMenuItem = createMenuItem("TF_MENU_FILE_IMPORT");
        projectWikiImportMenuItem = createMenuItem("TF_MENU_WIKI_IMPORT");
        projectCommitSourceFiles = createMenuItem("TF_MENU_FILE_COMMIT");
        projectCommitTargetFiles = createMenuItem("TF_MENU_FILE_TARGET");
        projectCompileMenuItem = createMenuItem("TF_MENU_FILE_COMPILE");
        projectSingleCompileMenuItem = createMenuItem("TF_MENU_FILE_SINGLE_COMPILE");
        projectMedOpenMenuItem = createMenuItem("TF_MENU_FILE_MED_OPEN");
        projectMedCreateMenuItem = createMenuItem("TF_MENU_FILE_MED_CREATE");
        projectEditMenuItem = createMenuItem("MW_PROJECTMENU_EDIT");
        viewFileListMenuItem = createMenuItem("TF_MENU_FILE_PROJWIN");

        projectAccessProjectFilesMenu = createMenu("TF_MENU_FILE_ACCESS_PROJECT_FILES");
        projectAccessRootMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_ROOT");
        projectAccessDictionaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_DICTIONARY");
        projectAccessGlossaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_GLOSSARY");
        projectAccessSourceMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_SOURCE");
        projectAccessTargetMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_TARGET");
        projectAccessTMMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_TM");
        projectAccessExportTMMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_EXPORT_TM");
        projectAccessCurrentSourceDocumentMenuItem = createMenuItem(
                "TF_MENU_FILE_ACCESS_CURRENT_SOURCE_DOCUMENT");
        projectAccessCurrentTargetDocumentMenuItem = createMenuItem(
                "TF_MENU_FILE_ACCESS_CURRENT_TARGET_DOCUMENT");
        projectAccessWritableGlossaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_WRITEABLE_GLOSSARY");

        projectAccessRootMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_ROOT");
        projectAccessDictionaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_DICTIONARY");
        projectAccessGlossaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_GLOSSARY");
        projectAccessSourceMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_SOURCE");
        projectAccessTargetMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_TARGET");
        projectAccessTMMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_TM");
        projectAccessExportTMMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_EXPORT_TM");
        projectAccessCurrentSourceDocumentMenuItem = createMenuItem(
                "TF_MENU_FILE_ACCESS_CURRENT_SOURCE_DOCUMENT");
        projectAccessCurrentTargetDocumentMenuItem = createMenuItem(
                "TF_MENU_FILE_ACCESS_CURRENT_TARGET_DOCUMENT");
        projectAccessWritableGlossaryMenuItem = createMenuItem("TF_MENU_FILE_ACCESS_WRITEABLE_GLOSSARY");
        projectRestartMenuItem = createMenuItem("TF_MENU_FILE_RESTART");
        projectExitMenuItem = createMenuItem("TF_MENU_FILE_QUIT");

        editUndoMenuItem = createMenuItem("TF_MENU_EDIT_UNDO");
        editRedoMenuItem = createMenuItem("TF_MENU_EDIT_REDO");
        editOverwriteTranslationMenuItem = createMenuItem("TF_MENU_EDIT_RECYCLE");
        editInsertTranslationMenuItem = createMenuItem("TF_MENU_EDIT_INSERT");
        editOverwriteSourceMenuItem = createMenuItem("TF_MENU_EDIT_SOURCE_OVERWRITE");
        editInsertSourceMenuItem = createMenuItem("TF_MENU_EDIT_SOURCE_INSERT");
        editSelectSourceMenuItem = createMenuItem("TF_MENU_EDIT_SOURCE_SELECT");
        editOverwriteMachineTranslationMenuItem = createMenuItem(
                "TF_MENU_EDIT_OVERWRITE_MACHITE_TRANSLATION");
        editTagPainterMenuItem = createMenuItem("TF_MENU_EDIT_TAGPAINT");
        editTagNextMissedMenuItem = createMenuItem("TF_MENU_EDIT_TAG_NEXT_MISSED");
        editExportSelectionMenuItem = createMenuItem("TF_MENU_EDIT_EXPORT_SELECTION");
        editCreateGlossaryEntryMenuItem = createMenuItem("TF_MENU_EDIT_CREATE_GLOSSARY_ENTRY");
        editFindInProjectMenuItem = createMenuItem("TF_MENU_EDIT_FIND");
        editReplaceInProjectMenuItem = createMenuItem("TF_MENU_EDIT_REPLACE");
        editSearchDictionaryMenuItem = createMenuItem("TF_MENU_EDIT_SEARCH_DICTIONARY");
        switchCaseSubMenu = createMenu("TF_EDIT_MENU_SWITCH_CASE");
        selectFuzzySubMenu = createMenu("TF_MENU_EDIT_COMPARE");

        editSelectFuzzyPrevMenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_PREV");
        editSelectFuzzyNextMenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_NEXT");
        editSelectFuzzy1MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_1");
        editSelectFuzzy2MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_2");
        editSelectFuzzy3MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_3");
        editSelectFuzzy4MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_4");
        editSelectFuzzy5MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_5");

        insertCharsSubMenu = createMenu("TF_MENU_EDIT_INSERT_CHARS");
        insertCharsLRM = createMenuItem("TF_MENU_EDIT_INSERT_CHARS_LRM");
        insertCharsRLM = createMenuItem("TF_MENU_EDIT_INSERT_CHARS_RLM");
        insertCharsLRE = createMenuItem("TF_MENU_EDIT_INSERT_CHARS_LRE");
        insertCharsRLE = createMenuItem("TF_MENU_EDIT_INSERT_CHARS_RLE");
        insertCharsPDF = createMenuItem("TF_MENU_EDIT_INSERT_CHARS_PDF");

        editMultipleDefault = createMenuItem("MULT_MENU_DEFAULT");
        editMultipleAlternate = createMenuItem("MULT_MENU_MULTIPLE");
        editRegisterUntranslatedMenuItem = createMenuItem("TF_MENU_EDIT_UNTRANSLATED_TRANSLATION");
        editRegisterEmptyMenuItem = createMenuItem("TF_MENU_EDIT_EMPTY_TRANSLATION");
        editRegisterIdenticalMenuItem = createMenuItem("TF_MENU_EDIT_IDENTICAL_TRANSLATION");

        lowerCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_LOWER");
        upperCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_UPPER");
        titleCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_TITLE");
        sentenceCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_SENTENCE");
        cycleSwitchCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_CYCLE");

        gotoNextUntranslatedMenuItem = createMenuItem("TF_MENU_EDIT_UNTRANS");
        gotoNextTranslatedMenuItem = createMenuItem("TF_MENU_EDIT_TRANS");
        gotoNextSegmentMenuItem = createMenuItem("TF_MENU_EDIT_NEXT");
        gotoPreviousSegmentMenuItem = createMenuItem("TF_MENU_EDIT_PREV");
        gotoSegmentMenuItem = createMenuItem("TF_MENU_EDIT_GOTO");
        gotoNextNoteMenuItem = createMenuItem("TF_MENU_EDIT_NEXT_NOTE");
        gotoPreviousNoteMenuItem = createMenuItem("TF_MENU_EDIT_PREV_NOTE");
        gotoNextUniqueMenuItem = createMenuItem("TF_MENU_GOTO_NEXT_UNIQUE");
        gotoMatchSourceSegment = createMenuItem("TF_MENU_GOTO_SELECTED_MATCH_SOURCE");
        gotoXEntrySubmenu = createMenu("TF_MENU_GOTO_X_SUBMENU");

        gotoNextXAutoMenuItem = createMenuItem("TF_MENU_GOTO_NEXT_XAUTO");
        gotoPrevXAutoMenuItem = createMenuItem("TF_MENU_GOTO_PREV_XAUTO");
        gotoNextXEnforcedMenuItem = createMenuItem("TF_MENU_GOTO_NEXT_XENFORCED");
        gotoPrevXEnforcedMenuItem = createMenuItem("TF_MENU_GOTO_PREV_XENFORCED");

        gotoHistoryBackMenuItem = createMenuItem("TF_MENU_GOTO_BACK_IN_HISTORY");
        gotoHistoryForwardMenuItem = createMenuItem("TF_MENU_GOTO_FORWARD_IN_HISTORY");
        gotoNotesPanelMenuItem = createMenuItem("TF_MENU_GOTO_NOTES_PANEL");
        gotoEditorPanelMenuItem = createMenuItem("TF_MENU_GOTO_EDITOR_PANEL");

        viewMarkTranslatedSegmentsCheckBoxMenuItem = createCheckboxMenuItem(
                "TF_MENU_DISPLAY_MARK_TRANSLATED");
        viewMarkUntranslatedSegmentsCheckBoxMenuItem = createCheckboxMenuItem(
                "TF_MENU_DISPLAY_MARK_UNTRANSLATED");
        viewMarkParagraphStartCheckBoxMenuItem = createCheckboxMenuItem("TF_MENU_DISPLAY_MARK_PARAGRAPH");
        viewDisplaySegmentSourceCheckBoxMenuItem = createCheckboxMenuItem(
                "MW_VIEW_MENU_DISPLAY_SEGMENT_SOURCES");
        viewMarkNonUniqueSegmentsCheckBoxMenuItem = createCheckboxMenuItem(
                "MW_VIEW_MENU_MARK_NON_UNIQUE_SEGMENTS");
        viewMarkNotedSegmentsCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_NOTED_SEGMENTS");
        viewMarkNBSPCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_NBSP");
        viewMarkWhitespaceCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_WHITESPACE");
        viewMarkBidiCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_BIDI");
        viewMarkAutoPopulatedCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_AUTOPOPULATED");
        viewMarkGlossaryMatchesCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_GLOSSARY_MARK");
        viewMarkLanguageCheckerCheckBoxMenuItem = createCheckboxMenuItem("LT_OPTIONS_MENU_ENABLED");
        viewMarkFontFallbackCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_FONT_FALLBACK");
        viewModificationInfoMenu = createMenu("MW_VIEW_MENU_MODIFICATION_INFO");

        ButtonGroup viewModificationInfoMenuBG = new ButtonGroup();
        viewDisplayModificationInfoNoneRadioButtonMenuItem = createRadioButtonMenuItem(
                "MW_VIEW_MENU_MODIFICATION_INFO_NONE", viewModificationInfoMenuBG);
        viewDisplayModificationInfoSelectedRadioButtonMenuItem = createRadioButtonMenuItem(
                "MW_VIEW_MENU_MODIFICATION_INFO_SELECTED", viewModificationInfoMenuBG);
        viewDisplayModificationInfoAllRadioButtonMenuItem = createRadioButtonMenuItem(
                "MW_VIEW_MENU_MODIFICATION_INFO_ALL", viewModificationInfoMenuBG);

        viewMarkUntranslatedSegmentsCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_UNTRANSLATED.getColor()));
        viewMarkParagraphStartCheckBoxMenuItem.setIcon(
                MainMenuIcons.newTextIcon(Styles.EditorColor.COLOR_PARAGRAPH_START.getColor(), '\u00b6'));
        viewDisplaySegmentSourceCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_SOURCE.getColor()));
        viewMarkNonUniqueSegmentsCheckBoxMenuItem
                .setIcon(MainMenuIcons.newTextIcon(Styles.EditorColor.COLOR_NON_UNIQUE.getColor(), 'M'));
        viewMarkNotedSegmentsCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_NOTED.getColor()));
        viewMarkNBSPCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_NBSP.getColor()));
        viewMarkWhitespaceCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_WHITESPACE.getColor()));
        viewMarkBidiCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_BIDIMARKERS.getColor()));
        viewModificationInfoMenu.setIcon(MainMenuIcons.newBlankIcon());
        viewMarkAutoPopulatedCheckBoxMenuItem.setIcon(
                MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getColor()));
        viewMarkGlossaryMatchesCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_TRANSTIPS.getColor()));
        viewMarkLanguageCheckerCheckBoxMenuItem
                .setIcon(MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor()));
        viewMarkFontFallbackCheckBoxMenuItem.setIcon(MainMenuIcons.newTextIcon(
                UIManager.getColor("Label.foreground"), new Font("Serif", Font.ITALIC, 16), 'F'));

        viewRestoreGUIMenuItem = createMenuItem("MW_OPTIONSMENU_RESTORE_GUI");

        toolsCheckIssuesMenuItem = createMenuItem("TF_MENU_TOOLS_CHECK_ISSUES");
        toolsCheckIssuesCurrentFileMenuItem = createMenuItem("TF_MENU_TOOLS_CHECK_ISSUES_CURRENT_FILE");
        toolsShowStatisticsStandardMenuItem = createMenuItem("TF_MENU_TOOLS_STATISTICS_STANDARD");
        toolsShowStatisticsMatchesMenuItem = createMenuItem("TF_MENU_TOOLS_STATISTICS_MATCHES");
        toolsShowStatisticsMatchesPerFileMenuItem = createMenuItem(
                "TF_MENU_TOOLS_STATISTICS_MATCHES_PER_FILE");
        toolsAlignFilesMenuItem = createMenuItem("TF_MENU_TOOLS_ALIGN_FILES");

        optionsPreferencesMenuItem = createMenuItem("MW_OPTIONSMENU_PREFERENCES");

        optionsMachineTranslateMenu = createMenu("TF_OPTIONSMENU_MACHINETRANSLATE");

        optionsMTAutoFetchCheckboxMenuItem = createCheckboxMenuItem("MT_AUTO_FETCH");
        optionsGlossaryMenu = createMenu("TF_OPTIONSMENU_GLOSSARY");
        optionsGlossaryFuzzyMatchingCheckBoxMenuItem = createCheckboxMenuItem(
                "TF_OPTIONSMENU_GLOSSARY_FUZZY");

        optionsDictionaryMenu = createMenu("TF_OPTIONSMENU_DICTIONARY");
        optionsDictionaryFuzzyMatchingCheckBoxMenuItem = createCheckboxMenuItem(
                "TF_OPTIONSMENU_DICTIONARY_FUZZY");

        optionsAutoCompleteMenu = createMenu("MW_OPTIONSMENU_AUTOCOMPLETE");
        // add any autocomplete view configuration menu items below
        optionsAutoCompleteShowAutomaticallyItem = createCheckboxMenuItem(
                "MW_OPTIONSMENU_AUTOCOMPLETE_SHOW_AUTOMATICALLY");
        optionsAutoCompleteHistoryCompletionMenuItem = createCheckboxMenuItem(
                "MW_OPTIONSMENU_AUTOCOMPLETE_HISTORY_COMPLETION");
        optionsAutoCompleteHistoryPredictionMenuItem = createCheckboxMenuItem(
                "MW_OPTIONSMENU_AUTOCOMPLETE_HISTORY_PREDICTION");
        optionsSetupFileFiltersMenuItem = createMenuItem("TF_MENU_DISPLAY_GLOBAL_FILTERS");
        optionsSentsegMenuItem = createMenuItem("MW_OPTIONSMENU_GLOBAL_SENTSEG");
        optionsWorkflowMenuItem = createMenuItem("MW_OPTIONSMENU_WORKFLOW");
        optionsAccessConfigDirMenuItem = createMenuItem("MW_OPTIONSMENU_ACCESS_CONFIG_DIR");

        helpContentsMenuItem = createMenuItem("TF_MENU_HELP_CONTENTS");
        helpAboutMenuItem = createMenuItem("TF_MENU_HELP_ABOUT");
        helpLastChangesMenuItem = createMenuItem("TF_MENU_HELP_LAST_CHANGES");
        helpLogMenuItem = createMenuItem("TF_MENU_HELP_LOG");
        helpUpdateCheckMenuItem = createMenuItem("TF_MENU_HELP_CHECK_FOR_UPDATES");

    }

    protected void constructMenu() {
        // Construct menu hierarchy
        projectMenu.add(projectNewMenuItem);
        projectMenu.add(projectTeamNewMenuItem);
        projectMenu.add(projectOpenMenuItem);
        projectMenu.add(projectOpenRecentMenuItem);

        projectMenu.add(projectReloadMenuItem);
        projectMenu.add(projectCloseMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(projectSaveMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(projectImportMenuItem);
        projectMenu.add(projectWikiImportMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(projectCommitSourceFiles);
        projectMenu.add(projectCommitTargetFiles);
        projectMenu.addSeparator();
        projectMenu.add(projectCompileMenuItem);
        projectMenu.add(projectSingleCompileMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(projectMedOpenMenuItem);
        projectMenu.add(projectMedCreateMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(projectEditMenuItem);
        projectMenu.add(viewFileListMenuItem);
        projectMenu.add(projectAccessProjectFilesMenu);

        projectAccessProjectFilesMenu.add(projectAccessRootMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessDictionaryMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessGlossaryMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessSourceMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessTargetMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessTMMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessExportTMMenuItem);
        projectAccessProjectFilesMenu.addSeparator();
        projectAccessProjectFilesMenu.add(projectAccessCurrentSourceDocumentMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessCurrentTargetDocumentMenuItem);
        projectAccessProjectFilesMenu.add(projectAccessWritableGlossaryMenuItem);

        projectMenu.addSeparator();
        projectMenu.add(projectRestartMenuItem);

        // all except MacOSX
        if (!Platform.isMacOSX()) {
            projectMenu.add(projectExitMenuItem);
        }

        editMenu.add(editUndoMenuItem);
        editMenu.add(editRedoMenuItem);
        editMenu.addSeparator();
        editMenu.add(editOverwriteTranslationMenuItem);
        editMenu.add(editInsertTranslationMenuItem);
        editMenu.addSeparator();
        editMenu.add(editOverwriteSourceMenuItem);
        editMenu.add(editInsertSourceMenuItem);
        editMenu.add(editSelectSourceMenuItem);
        editMenu.addSeparator();
        editMenu.add(editOverwriteMachineTranslationMenuItem);
        editMenu.addSeparator();
        editMenu.add(editTagPainterMenuItem);
        editMenu.add(editTagNextMissedMenuItem);
        editMenu.addSeparator();
        editMenu.add(editExportSelectionMenuItem);
        editMenu.add(editCreateGlossaryEntryMenuItem);
        editMenu.addSeparator();
        editMenu.add(editFindInProjectMenuItem);
        editMenu.add(editReplaceInProjectMenuItem);
        editMenu.addSeparator();
        editMenu.add(editSearchDictionaryMenuItem);
        editMenu.addSeparator();
        editMenu.add(switchCaseSubMenu);
        editMenu.add(selectFuzzySubMenu);
        selectFuzzySubMenu.add(editSelectFuzzyPrevMenuItem);
        selectFuzzySubMenu.add(editSelectFuzzyNextMenuItem);
        selectFuzzySubMenu.addSeparator();
        selectFuzzySubMenu.add(editSelectFuzzy1MenuItem);
        selectFuzzySubMenu.add(editSelectFuzzy2MenuItem);
        selectFuzzySubMenu.add(editSelectFuzzy3MenuItem);
        selectFuzzySubMenu.add(editSelectFuzzy4MenuItem);
        selectFuzzySubMenu.add(editSelectFuzzy5MenuItem);
        editMenu.add(insertCharsSubMenu);
        insertCharsSubMenu.add(insertCharsLRM);
        insertCharsSubMenu.add(insertCharsRLM);
        insertCharsSubMenu.addSeparator();
        insertCharsSubMenu.add(insertCharsLRE);
        insertCharsSubMenu.add(insertCharsRLE);
        insertCharsSubMenu.add(insertCharsPDF);
        editMenu.addSeparator();
        editMenu.add(editMultipleDefault);
        editMenu.add(editMultipleAlternate);
        editMenu.addSeparator();
        editMenu.add(editRegisterUntranslatedMenuItem);
        editMenu.add(editRegisterEmptyMenuItem);
        editMenu.add(editRegisterIdenticalMenuItem);

        switchCaseSubMenu.add(lowerCaseMenuItem);
        switchCaseSubMenu.add(upperCaseMenuItem);
        switchCaseSubMenu.add(titleCaseMenuItem);
        switchCaseSubMenu.add(sentenceCaseMenuItem);
        switchCaseSubMenu.addSeparator();
        switchCaseSubMenu.add(cycleSwitchCaseMenuItem);

        gotoMenu.add(gotoNextUntranslatedMenuItem);
        gotoMenu.add(gotoNextTranslatedMenuItem);
        gotoMenu.add(gotoNextSegmentMenuItem);
        gotoMenu.add(gotoPreviousSegmentMenuItem);
        gotoMenu.add(gotoSegmentMenuItem);
        gotoMenu.add(gotoNextNoteMenuItem);
        gotoMenu.add(gotoPreviousNoteMenuItem);
        gotoMenu.add(gotoNextUniqueMenuItem);
        gotoMenu.add(gotoMatchSourceSegment);
        gotoMenu.addSeparator();
        gotoMenu.add(gotoXEntrySubmenu);
        gotoXEntrySubmenu.add(gotoNextXAutoMenuItem);
        gotoXEntrySubmenu.add(gotoPrevXAutoMenuItem);
        gotoXEntrySubmenu.add(gotoNextXEnforcedMenuItem);
        gotoXEntrySubmenu.add(gotoPrevXEnforcedMenuItem);
        gotoMenu.addSeparator();
        gotoMenu.add(gotoHistoryBackMenuItem);
        gotoMenu.add(gotoHistoryForwardMenuItem);
        gotoMenu.addSeparator();
        gotoMenu.add(gotoNotesPanelMenuItem);
        gotoMenu.add(gotoEditorPanelMenuItem);

        viewMenu.add(viewMarkTranslatedSegmentsCheckBoxMenuItem);
        viewMenu.add(viewMarkUntranslatedSegmentsCheckBoxMenuItem);
        viewMenu.add(viewMarkParagraphStartCheckBoxMenuItem);
        viewMenu.add(viewDisplaySegmentSourceCheckBoxMenuItem);
        viewMenu.add(viewMarkNonUniqueSegmentsCheckBoxMenuItem);
        viewMenu.add(viewMarkNotedSegmentsCheckBoxMenuItem);
        viewMenu.add(viewMarkNBSPCheckBoxMenuItem);
        viewMenu.add(viewMarkWhitespaceCheckBoxMenuItem);
        viewMenu.add(viewMarkBidiCheckBoxMenuItem);
        viewMenu.add(viewMarkAutoPopulatedCheckBoxMenuItem);
        viewMenu.add(viewMarkGlossaryMatchesCheckBoxMenuItem);
        viewMenu.add(viewMarkLanguageCheckerCheckBoxMenuItem);
        viewMenu.add(viewMarkFontFallbackCheckBoxMenuItem);
        viewMenu.add(viewModificationInfoMenu);

        viewModificationInfoMenu.add(viewDisplayModificationInfoNoneRadioButtonMenuItem);
        viewModificationInfoMenu.add(viewDisplayModificationInfoSelectedRadioButtonMenuItem);
        viewModificationInfoMenu.add(viewDisplayModificationInfoAllRadioButtonMenuItem);
        viewMenu.addSeparator();
        viewMenu.add(viewRestoreGUIMenuItem);

        toolsMenu.add(toolsCheckIssuesMenuItem);
        toolsMenu.add(toolsCheckIssuesCurrentFileMenuItem);
        toolsMenu.add(toolsShowStatisticsStandardMenuItem);
        toolsMenu.add(toolsShowStatisticsMatchesMenuItem);
        toolsMenu.add(toolsShowStatisticsMatchesPerFileMenuItem);
        toolsMenu.addSeparator();
        toolsMenu.add(toolsAlignFilesMenuItem);

        if (!Platform.isMacOSX()) {
            optionsMenu.add(optionsPreferencesMenuItem);
            optionsMenu.addSeparator();
        }

        optionsMenu.add(optionsMachineTranslateMenu);

        optionsMachineTranslateMenu.add(optionsMTAutoFetchCheckboxMenuItem);
        optionsMachineTranslateMenu.addSeparator();

        optionsMenu.add(optionsGlossaryMenu);
        optionsGlossaryMenu.add(optionsGlossaryFuzzyMatchingCheckBoxMenuItem);

        optionsGlossaryMenu.addSeparator();

        optionsMenu.add(optionsDictionaryMenu);
        optionsDictionaryMenu.add(optionsDictionaryFuzzyMatchingCheckBoxMenuItem);

        optionsMenu.add(optionsAutoCompleteMenu);
        // add any autocomplete view configuration menu items below
        optionsAutoCompleteMenu.add(optionsAutoCompleteShowAutomaticallyItem);
        optionsAutoCompleteMenu.add(optionsAutoCompleteHistoryCompletionMenuItem);
        optionsAutoCompleteMenu.add(optionsAutoCompleteHistoryPredictionMenuItem);
        optionsMenu.addSeparator();
        optionsMenu.add(optionsSetupFileFiltersMenuItem);
        optionsMenu.add(optionsSentsegMenuItem);
        optionsMenu.add(optionsWorkflowMenuItem);
        optionsMenu.addSeparator();
        optionsMenu.add(optionsAccessConfigDirMenuItem);
        optionsMenu.addSeparator();

        helpMenu.add(helpContentsMenuItem);
        helpMenu.add(helpAboutMenuItem);
        helpMenu.add(helpLastChangesMenuItem);
        helpMenu.add(helpLogMenuItem);
        helpMenu.add(helpUpdateCheckMenuItem);
    }

    protected void configureActions() {
        projectOpenRecentMenuItem.addMenuListener(new MenuListener() {

            @Override
            public void menuSelected(MenuEvent e) {
                populateRecentProjects();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        projectAccessProjectFilesMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (Core.getProject().isProjectLoaded()) {
                    String sourcePath = Core.getEditor().getCurrentFile();
                    projectAccessCurrentSourceDocumentMenuItem.setEnabled(!StringUtil.isEmpty(sourcePath)
                            && new File(Core.getProject().getProjectProperties().getSourceRoot(), sourcePath)
                                    .isFile());
                    String targetPath = Core.getEditor().getCurrentTargetFile();
                    projectAccessCurrentTargetDocumentMenuItem.setEnabled(!StringUtil.isEmpty(targetPath)
                            && new File(Core.getProject().getProjectProperties().getTargetRoot(), targetPath)
                                    .isFile());
                    String glossaryPath = Core.getProject().getProjectProperties().getWriteableGlossary();
                    projectAccessWritableGlossaryMenuItem
                            .setEnabled(!StringUtil.isEmpty(glossaryPath) && new File(glossaryPath).isFile());
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        String key = "findInProjectReuseLastWindow";
        KeyStroke stroke = PropertiesShortcuts.getMainMenuShortcuts().getKeyStroke(key);
        mainWindow.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, key);
        mainWindow.getRootPane().getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Log.logInfoRB("LOG_MENU_CLICK", key);
                mainWindowMenuHandler.findInProjectReuseLastWindow();
            }
        });

        if (Platform.isMacOSX()) {
            initMacSpecific();
        }

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                updateCheckboxesOnStart();
                onProjectStatusChanged(false);
            }

            public void onApplicationShutdown() {
            }
        });

        CoreEvents.registerProjectChangeListener(
                e -> onProjectStatusChanged(Core.getProject().isProjectLoaded()));

        Preferences.addPropertyChangeListener(e -> {
            if (e.getNewValue() instanceof Boolean) {
                JMenuItem item = getItemForPreference(e.getPropertyName());
                if (item != null) {
                    item.setSelected((Boolean) e.getNewValue());
                }
            }
        });
    }

    /**
     * Code for dispatching events from components to event handlers.
     *
     * @param action
     *            ActionCommand of triggering menu item
     * @param modifiers
     *            Modifier key flags (can be zero)
     */
    @Override
    public void invokeAction(String action, int modifiers) {
        // Find method by item name.
        String methodName = action + "ActionPerformed";
        Method method = null;
        try {
            method = mainWindowMenuHandler.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignore) {
            try {
                method = mainWindowMenuHandler.getClass().getMethod(methodName, Integer.TYPE);
            } catch (NoSuchMethodException ex) {
                throw new IncompatibleClassChangeError(
                        "Error invoke method handler for main menu: there is no method " + methodName);
            }
        }

        // Call ...MenuItemActionPerformed method.
        Object[] args = method.getParameterTypes().length == 0 ? null : new Object[] { modifiers };
        try {
            method.invoke(mainWindowMenuHandler, args);
        } catch (IllegalAccessException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error execute method", ex);
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }
    }

    /**
     * Create menu instance and set title.
     *
     * @param titleKey
     *            title name key in resource bundle
     * @return menu instance
     */
    protected JMenu createMenu(String titleKey) {
        return createMenu(titleKey, null);
    }

    protected JMenu createMenu(String titleKey, MenuExtender.MenuKey menuKey) {
        JMenu result = new JMenu();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        result.addMenuListener(this);
        if (menuKey != null) {
            menus.put(menuKey, result);
        }
        return result;
    }

    /**
     * Create menu item instance and set title.
     *
     * @param titleKey
     *            title name key in resource bundle
     * @return menu item instance
     */
    protected JMenuItem createMenuItem(String titleKey) {
        JMenuItem result = new JMenuItem();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        result.addActionListener(this);
        return result;
    }

    /**
     * Create menu item instance and set title.
     *
     * @param titleKey
     *            title name key in resource bundle
     * @return menu item instance
     */
    protected JCheckBoxMenuItem createCheckboxMenuItem(final String titleKey) {
        JCheckBoxMenuItem result = new JCheckBoxMenuItem();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        result.addActionListener(this);
        return result;
    }

    /**
     * Create menu item instance and set title.
     *
     * @param titleKey
     *            title name key in resource bundle
     * @return menu item instance
     */
    protected JRadioButtonMenuItem createRadioButtonMenuItem(final String titleKey, ButtonGroup buttonGroup) {
        JRadioButtonMenuItem result = new JRadioButtonMenuItem();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        result.addActionListener(this);
        buttonGroup.add(result);
        return result;
    }

    /**
     * Set 'actionCommand' for all menu items. TODO: change to key from resource
     * bundle values
     */
    protected void setActionCommands() {
        try {
            for (Field f : StaticUtils.getAllModelFields(this.getClass())) {
                if (JMenuItem.class.isAssignableFrom(f.getType())) {
                    JMenuItem menuItem = (JMenuItem) f.get(this);
                    if (menuItem != null) {
                        menuItem.setActionCommand(f.getName());
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Enable or disable items depend of project open or close.
     *
     * @param isProjectOpened
     *            project open status: true if opened, false if closed
     */
    protected void onProjectStatusChanged(final boolean isProjectOpened) {

        JMenuItem[] itemsToSwitchOff = new JMenuItem[] { projectNewMenuItem, projectTeamNewMenuItem,
                projectOpenMenuItem, projectMedOpenMenuItem };

        JMenuItem[] itemsToSwitchOn = new JMenuItem[] { projectImportMenuItem, projectWikiImportMenuItem,
                projectReloadMenuItem, projectCloseMenuItem, projectSaveMenuItem, projectEditMenuItem,
                projectCompileMenuItem, projectSingleCompileMenuItem, projectAccessProjectFilesMenu,
                projectMedCreateMenuItem,

                editMenu, editFindInProjectMenuItem, editReplaceInProjectMenuItem, editInsertSourceMenuItem,
                editInsertTranslationMenuItem, editTagPainterMenuItem, editOverwriteSourceMenuItem,
                editOverwriteTranslationMenuItem, editRedoMenuItem, editSelectFuzzy1MenuItem,
                editSelectFuzzy2MenuItem, editSelectFuzzy3MenuItem, editSelectFuzzy4MenuItem,
                editSelectFuzzy5MenuItem, editUndoMenuItem, switchCaseSubMenu,
                editOverwriteMachineTranslationMenuItem, editRegisterUntranslatedMenuItem,
                editRegisterEmptyMenuItem, editRegisterIdenticalMenuItem,

                gotoMenu, gotoNextSegmentMenuItem, gotoNextUntranslatedMenuItem, gotoPreviousSegmentMenuItem,
                gotoSegmentMenuItem, gotoNextNoteMenuItem, gotoPreviousNoteMenuItem, gotoMatchSourceSegment,

                viewFileListMenuItem, toolsCheckIssuesMenuItem, toolsCheckIssuesCurrentFileMenuItem,
                toolsShowStatisticsStandardMenuItem, toolsShowStatisticsMatchesMenuItem,
                toolsShowStatisticsMatchesPerFileMenuItem };

        for (JMenuItem item : itemsToSwitchOff) {
            item.setEnabled(!isProjectOpened);
        }
        for (JMenuItem item : itemsToSwitchOn) {
            item.setEnabled(isProjectOpened);
        }
        if (Core.getParams().containsKey(CLIParameters.NO_TEAM)) {
            projectTeamNewMenuItem.setEnabled(false);
        }
        projectCommitSourceFiles.setEnabled(isProjectOpened && Core.getProject().isRemoteProject()
                && Core.getProject().getProjectProperties().getSourceDir().isUnderRoot());
        projectCommitTargetFiles.setEnabled(isProjectOpened && Core.getProject().isRemoteProject()
                && Core.getProject().getProjectProperties().getTargetDir().isUnderRoot());
    }

    protected JMenuItem getItemForPreference(String preference) {
        switch (preference) {
        case Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY:
            return optionsAutoCompleteShowAutomaticallyItem;
        case Preferences.AC_HISTORY_COMPLETION_ENABLED:
            return optionsAutoCompleteHistoryCompletionMenuItem;
        case Preferences.AC_HISTORY_PREDICTION_ENABLED:
            return optionsAutoCompleteHistoryPredictionMenuItem;
        case Preferences.MT_AUTO_FETCH:
            return optionsMTAutoFetchCheckboxMenuItem;
        case Preferences.DICTIONARY_FUZZY_MATCHING:
            return optionsDictionaryFuzzyMatchingCheckBoxMenuItem;
        case Preferences.GLOSSARY_STEMMING:
            return optionsGlossaryFuzzyMatchingCheckBoxMenuItem;
        default:
            return null;
        }
    }

    /** Updates menu checkboxes from preferences on start */
    protected void updateCheckboxesOnStart() {
        viewMarkTranslatedSegmentsCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkTranslated());
        viewMarkUntranslatedSegmentsCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkUntranslated());
        viewMarkParagraphStartCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkParagraphDelimitations());
        viewDisplaySegmentSourceCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isDisplaySegmentSources());
        viewMarkNonUniqueSegmentsCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkNonUniqueSegments());
        viewMarkNotedSegmentsCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkNotedSegments());
        viewMarkNBSPCheckBoxMenuItem.setSelected(Core.getEditor().getSettings().isMarkNBSP());
        viewMarkWhitespaceCheckBoxMenuItem.setSelected(Core.getEditor().getSettings().isMarkWhitespace());
        viewMarkBidiCheckBoxMenuItem.setSelected(Core.getEditor().getSettings().isMarkBidi());
        viewMarkAutoPopulatedCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkAutoPopulated());
        viewMarkGlossaryMatchesCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkGlossaryMatches());
        viewMarkLanguageCheckerCheckBoxMenuItem
                .setSelected(Core.getEditor().getSettings().isMarkLanguageChecker());
        viewMarkFontFallbackCheckBoxMenuItem.setSelected(Core.getEditor().getSettings().isDoFontFallback());

        viewDisplayModificationInfoNoneRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE
                        .equals(Core.getEditor().getSettings().getDisplayModificationInfo()));
        viewDisplayModificationInfoSelectedRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED
                        .equals(Core.getEditor().getSettings().getDisplayModificationInfo()));
        viewDisplayModificationInfoAllRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL
                        .equals(Core.getEditor().getSettings().getDisplayModificationInfo()));

        optionsAutoCompleteShowAutomaticallyItem.setSelected(
                Preferences.isPreferenceDefault(Preferences.AC_SHOW_SUGGESTIONS_AUTOMATICALLY, true));
        optionsAutoCompleteHistoryCompletionMenuItem.setSelected(
                Preferences.isPreferenceDefault(Preferences.AC_HISTORY_COMPLETION_ENABLED, true));
        optionsAutoCompleteHistoryPredictionMenuItem.setSelected(
                Preferences.isPreferenceDefault(Preferences.AC_HISTORY_PREDICTION_ENABLED, true));
        optionsMTAutoFetchCheckboxMenuItem
                .setSelected(Preferences.isPreferenceDefault(Preferences.MT_AUTO_FETCH, false));
        optionsDictionaryFuzzyMatchingCheckBoxMenuItem
                .setSelected(Preferences.isPreferenceDefault(Preferences.DICTIONARY_FUZZY_MATCHING, false));
        optionsGlossaryFuzzyMatchingCheckBoxMenuItem
                .setSelected(Preferences.isPreferenceDefault(Preferences.GLOSSARY_STEMMING, true));
    }

    /**
     * Initialize Mac-specific features.
     */
    protected void initMacSpecific() {
        try {
            // MacOSX-specific
            OSXIntegration.setQuitHandler(e -> mainWindowMenuHandler.projectExitMenuItemActionPerformed());
            OSXIntegration.setAboutHandler(e -> mainWindowMenuHandler.helpAboutMenuItemActionPerformed());
            OSXIntegration.setPreferencesHandler(
                    e -> mainWindowMenuHandler.optionsPreferencesMenuItemActionPerformed());
        } catch (NoClassDefFoundError e) {
            Log.log(e);
        }
    }

    protected void populateRecentProjects() {
        projectOpenRecentMenuItem.removeAll();
        List<String> items = RecentProjects.getRecentProjects();
        for (String project : items) {
            JMenuItem recentProjectMenuItem = new JMenuItem(project);
            File projectFile = new File(project);
            recentProjectMenuItem
                    .addActionListener(event -> ProjectUICommands.projectOpen(projectFile, true));
            recentProjectMenuItem.setEnabled(projectFile.isDirectory() && projectFile.canRead());
            projectOpenRecentMenuItem.add(recentProjectMenuItem);
        }
        projectOpenRecentMenuItem.addSeparator();
        projectOpenRecentMenuItem.add(projectClearRecentMenuItem);
        projectClearRecentMenuItem.setEnabled(!items.isEmpty());
    }

    public JMenu getMachineTranslationMenu() {
        return optionsMachineTranslateMenu;
    }

    public JMenu getOptionsMenu() {
        return optionsMenu;
    }

    public JMenu getToolsMenu() {
        return toolsMenu;
    }

    public JMenu getGlossaryMenu() {
        return optionsGlossaryMenu;
    }

    public JMenu getProjectMenu() {
        return projectMenu;
    }

    public JMenu getAutoCompletionMenu() {
        return optionsAutoCompleteMenu;
    }

    public JMenu getHelpMenu() {
        return helpMenu;
    }

    public JMenu getMenu(MenuExtender.MenuKey marker) {
        return menus.get(marker);
    }

    JMenuItem cycleSwitchCaseMenuItem;
    JMenuItem editFindInProjectMenuItem;
    JMenuItem editReplaceInProjectMenuItem;
    JMenuItem editInsertSourceMenuItem;
    JMenuItem editInsertTranslationMenuItem;
    JMenu editMenu;
    JMenuItem editOverwriteSourceMenuItem;
    JMenuItem editOverwriteTranslationMenuItem;
    JMenuItem editOverwriteMachineTranslationMenuItem;
    JMenuItem editRedoMenuItem;
    JMenu selectFuzzySubMenu;
    JMenuItem editSelectFuzzyPrevMenuItem;
    JMenuItem editSelectFuzzyNextMenuItem;
    JMenuItem editSelectFuzzy1MenuItem;
    JMenuItem editSelectFuzzy2MenuItem;
    JMenuItem editSelectFuzzy3MenuItem;
    JMenuItem editSelectFuzzy4MenuItem;
    JMenuItem editSelectFuzzy5MenuItem;
    JMenu insertCharsSubMenu;
    JMenuItem insertCharsLRM;
    JMenuItem insertCharsRLM;
    JMenuItem insertCharsLRE;
    JMenuItem insertCharsRLE;
    JMenuItem insertCharsPDF;
    public JMenuItem editMultipleDefault;
    public JMenuItem editMultipleAlternate;
    JMenuItem editUndoMenuItem;
    JMenuItem editTagPainterMenuItem;
    JMenuItem editTagNextMissedMenuItem;
    JMenuItem editExportSelectionMenuItem;
    JMenuItem editSelectSourceMenuItem;
    JMenuItem editCreateGlossaryEntryMenuItem;
    JMenuItem editSearchDictionaryMenuItem;
    JMenuItem editRegisterUntranslatedMenuItem;
    JMenuItem editRegisterEmptyMenuItem;
    JMenuItem editRegisterIdenticalMenuItem;
    public JMenuItem gotoHistoryBackMenuItem;
    public JMenuItem gotoHistoryForwardMenuItem;
    JMenuItem gotoNotesPanelMenuItem;
    JMenuItem gotoEditorPanelMenuItem;
    JMenu gotoMenu;
    JMenuItem gotoNextSegmentMenuItem;
    JMenuItem gotoNextUntranslatedMenuItem;
    JMenuItem gotoNextTranslatedMenuItem;
    JMenuItem gotoPreviousSegmentMenuItem;
    JMenuItem gotoSegmentMenuItem;
    JMenu gotoXEntrySubmenu;
    JMenuItem gotoNextXAutoMenuItem;
    JMenuItem gotoPrevXAutoMenuItem;
    JMenuItem gotoNextXEnforcedMenuItem;
    JMenuItem gotoPrevXEnforcedMenuItem;
    JMenuItem gotoNextNoteMenuItem;
    JMenuItem gotoPreviousNoteMenuItem;
    JMenuItem gotoMatchSourceSegment;
    JMenuItem gotoNextUniqueMenuItem;
    JMenuItem helpAboutMenuItem;
    JMenuItem helpContentsMenuItem;
    JMenuItem helpLastChangesMenuItem;
    JMenuItem helpLogMenuItem;
    JMenuItem helpUpdateCheckMenuItem;
    JMenu helpMenu;
    JMenuItem lowerCaseMenuItem;
    JMenu optionsMenu;
    JMenuItem viewRestoreGUIMenuItem;
    JMenuItem optionsAccessConfigDirMenuItem;
    JMenuItem optionsSentsegMenuItem;
    JMenuItem optionsSetupFileFiltersMenuItem;
    JMenu optionsMachineTranslateMenu;
    JMenu optionsGlossaryMenu;
    JMenuItem optionsGlossaryFuzzyMatchingCheckBoxMenuItem;
    JMenu optionsDictionaryMenu;
    JMenuItem optionsDictionaryFuzzyMatchingCheckBoxMenuItem;
    JMenu optionsAutoCompleteMenu;
    JMenuItem optionsAutoCompleteShowAutomaticallyItem;
    JMenuItem optionsAutoCompleteHistoryCompletionMenuItem;
    JMenuItem optionsAutoCompleteHistoryPredictionMenuItem;
    JMenuItem optionsWorkflowMenuItem;
    JCheckBoxMenuItem optionsMTAutoFetchCheckboxMenuItem;
    JMenuItem optionsPreferencesMenuItem;
    JMenuItem projectCloseMenuItem;
    JMenuItem projectCommitSourceFiles;
    JMenuItem projectCommitTargetFiles;
    JMenuItem projectCompileMenuItem;
    JMenuItem projectSingleCompileMenuItem;
    JMenuItem projectMedOpenMenuItem;
    JMenuItem projectMedCreateMenuItem;
    JMenuItem projectEditMenuItem;
    JMenuItem projectExitMenuItem;
    JMenuItem projectRestartMenuItem;
    JMenuItem projectImportMenuItem;
    JMenu projectMenu;
    JMenuItem projectNewMenuItem;
    JMenuItem projectTeamNewMenuItem;
    JMenuItem projectOpenMenuItem;
    JMenu projectOpenRecentMenuItem;
    JMenuItem projectClearRecentMenuItem;
    JMenuItem projectReloadMenuItem;
    JMenuItem projectSaveMenuItem;
    JMenuItem projectWikiImportMenuItem;
    JMenu projectAccessProjectFilesMenu;
    JMenuItem projectAccessRootMenuItem;
    JMenuItem projectAccessDictionaryMenuItem;
    JMenuItem projectAccessGlossaryMenuItem;
    JMenuItem projectAccessSourceMenuItem;
    JMenuItem projectAccessTargetMenuItem;
    JMenuItem projectAccessTMMenuItem;
    JMenuItem projectAccessExportTMMenuItem;
    JMenuItem projectAccessCurrentSourceDocumentMenuItem;
    JMenuItem projectAccessCurrentTargetDocumentMenuItem;
    JMenuItem projectAccessWritableGlossaryMenuItem;
    JMenuItem sentenceCaseMenuItem;
    JMenu switchCaseSubMenu;
    JMenuItem titleCaseMenuItem;
    JMenu toolsMenu;
    JMenuItem toolsCheckIssuesMenuItem;
    JMenuItem toolsCheckIssuesCurrentFileMenuItem;
    JMenuItem toolsShowStatisticsStandardMenuItem;
    JMenuItem toolsShowStatisticsMatchesMenuItem;
    JMenuItem toolsShowStatisticsMatchesPerFileMenuItem;
    JMenuItem toolsAlignFilesMenuItem;
    JMenuItem upperCaseMenuItem;
    JCheckBoxMenuItem viewDisplaySegmentSourceCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkNonUniqueSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkNotedSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkNBSPCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkWhitespaceCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkBidiCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkAutoPopulatedCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkGlossaryMatchesCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkLanguageCheckerCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkFontFallbackCheckBoxMenuItem;
    JMenu viewModificationInfoMenu;
    JRadioButtonMenuItem viewDisplayModificationInfoNoneRadioButtonMenuItem;
    JRadioButtonMenuItem viewDisplayModificationInfoSelectedRadioButtonMenuItem;
    JRadioButtonMenuItem viewDisplayModificationInfoAllRadioButtonMenuItem;
    JMenuItem viewFileListMenuItem;
    JCheckBoxMenuItem viewMarkTranslatedSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkUntranslatedSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkParagraphStartCheckBoxMenuItem;
    JMenu viewMenu;
}
