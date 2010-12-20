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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import net.roydesign.mac.MRJAdapter;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.openide.awt.Mnemonics;

/**
 * Class for create main menu and handle main menu events.
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
public class MainWindowMenu implements ActionListener, IMainMenu {
    private static final Logger LOGGER = Logger.getLogger(MainWindowMenu.class.getName());

    /** MainWindow instance. */
    protected final MainWindow mainWindow;

    /** MainWindow menu handler instance. */
    protected final MainWindowMenuHandler mainWindowMenuHandler;

    public MainWindowMenu(final MainWindow mainWindow, final MainWindowMenuHandler mainWindowMenuHandler) {
        this.mainWindow = mainWindow;
        this.mainWindowMenuHandler = mainWindowMenuHandler;
    }

    /**
     * Code for dispatching events from components to event handlers.
     * 
     * @param evt
     *            event info
     */
    public void actionPerformed(ActionEvent evt) {
        // Item what perform event.
        JMenuItem menuItem = (JMenuItem) evt.getSource();

        // Get item name from actionCommand.
        String action = menuItem.getActionCommand();

        Log.logInfoRB("LOG_MENU_CLICK", action);

        // Find method by item name.
        String methodName = action + "ActionPerformed";
        Method method = null;
        try {
            method = mainWindowMenuHandler.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }

        // Call ...MenuItemActionPerformed method.
        try {
            method.invoke(mainWindowMenuHandler);
        } catch (IllegalAccessException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error execute method", ex);
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }
    }

    /**
     * Initialize menu items.
     */
    JMenuBar initComponents() {
        mainMenu = new JMenuBar();
        mainMenu.add(projectMenu = createMenu("TF_MENU_FILE"));
        mainMenu.add(editMenu = createMenu("TF_MENU_EDIT"));
        mainMenu.add(gotoMenu = createMenu("MW_GOTOMENU"));
        mainMenu.add(viewMenu = createMenu("MW_VIEW_MENU"));
        mainMenu.add(toolsMenu = createMenu("TF_MENU_TOOLS"));
        mainMenu.add(optionsMenu = createMenu("MW_OPTIONSMENU"));
        mainMenu.add(helpMenu = createMenu("TF_MENU_HELP"));

        projectMenu.add(projectNewMenuItem = createMenuItem("TF_MENU_FILE_CREATE"));
        projectMenu.add(projectOpenMenuItem = createMenuItem("TF_MENU_FILE_OPEN"));
        projectMenu.add(projectImportMenuItem = createMenuItem("TF_MENU_FILE_IMPORT"));
        projectMenu.add(projectWikiImportMenuItem = createMenuItem("TF_MENU_WIKI_IMPORT"));
        projectMenu.add(projectReloadMenuItem = createMenuItem("TF_MENU_PROJECT_RELOAD"));
        projectMenu.add(projectCloseMenuItem = createMenuItem("TF_MENU_FILE_CLOSE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectSaveMenuItem = createMenuItem("TF_MENU_FILE_SAVE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectCompileMenuItem = createMenuItem("TF_MENU_FILE_COMPILE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectEditMenuItem = createMenuItem("MW_PROJECTMENU_EDIT"));
        projectMenu.add(viewFileListMenuItem = createMenuItem("TF_MENU_FILE_PROJWIN"));
        projectExitMenuItem = createMenuItem("TF_MENU_FILE_QUIT");

        // all except MacOSX
        if (!StaticUtils.onMacOSX()) {
            projectMenu.add(new JSeparator());
            projectMenu.add(projectExitMenuItem);
        }

        editMenu.add(editUndoMenuItem = createMenuItem("TF_MENU_EDIT_UNDO"));
        editMenu.add(editRedoMenuItem = createMenuItem("TF_MENU_EDIT_REDO"));
        editMenu.add(new JSeparator());
        editMenu.add(editOverwriteTranslationMenuItem = createMenuItem("TF_MENU_EDIT_RECYCLE"));
        editMenu.add(editInsertTranslationMenuItem = createMenuItem("TF_MENU_EDIT_INSERT"));
        editMenu.add(new JSeparator());
        editMenu.add(editOverwriteMachineTranslationMenuItem = createMenuItem("TF_MENU_EDIT_OVERWRITE_MACHITE_TRANSLATION"));
        editMenu.add(new JSeparator());
        editMenu.add(editOverwriteSourceMenuItem = createMenuItem("TF_MENU_EDIT_SOURCE_OVERWRITE"));
        editMenu.add(editInsertSourceMenuItem = createMenuItem("TF_MENU_EDIT_SOURCE_INSERT"));
        editMenu.add(new JSeparator());
        editMenu.add(editTagPainterMenuItem = createMenuItem("TF_MENU_EDIT_TAGPAINT"));
        editMenu.add(new JSeparator());
        editMenu.add(editExportSelectionMenuItem = createMenuItem("TF_MENU_EDIT_EXPORT_SELECTION"));
        editMenu.add(editCreateGlossaryEntryMenuItem = createMenuItem("TF_MENU_EDIT_CREATE_GLOSSARY_ENTRY"));
        editMenu.add(new JSeparator());
        editMenu.add(editFindInProjectMenuItem = createMenuItem("TF_MENU_EDIT_FIND"));
        editMenu.add(new JSeparator());
        editMenu.add(switchCaseSubMenu = createMenu("TF_EDIT_MENU_SWITCH_CASE"));
        editMenu.add(new JSeparator());
        editMenu.add(editSelectFuzzy1MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_1"));
        editMenu.add(editSelectFuzzy2MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_2"));
        editMenu.add(editSelectFuzzy3MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_3"));
        editMenu.add(editSelectFuzzy4MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_4"));
        editMenu.add(editSelectFuzzy5MenuItem = createMenuItem("TF_MENU_EDIT_COMPARE_5"));

        switchCaseSubMenu.add(lowerCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_LOWER"));
        switchCaseSubMenu.add(upperCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_UPPER"));
        switchCaseSubMenu.add(titleCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_TO_TITLE"));
        switchCaseSubMenu.add(new JSeparator());
        switchCaseSubMenu.add(cycleSwitchCaseMenuItem = createMenuItem("TF_EDIT_MENU_SWITCH_CASE_CYCLE"));

        gotoMenu.add(gotoNextUntranslatedMenuItem = createMenuItem("TF_MENU_EDIT_UNTRANS"));
        gotoMenu.add(gotoNextSegmentMenuItem = createMenuItem("TF_MENU_EDIT_NEXT"));
        gotoMenu.add(gotoPreviousSegmentMenuItem = createMenuItem("TF_MENU_EDIT_PREV"));
        gotoMenu.add(gotoSegmentMenuItem = createMenuItem("TF_MENU_EDIT_GOTO"));
        gotoMenu.add(new JSeparator());
        gotoMenu.add(gotoHistoryForwardMenuItem = createMenuItem("TF_MENU_GOTO_FORWARD_IN_HISTORY"));
        gotoMenu.add(gotoHistoryBackMenuItem = createMenuItem("TF_MENU_GOTO_BACK_IN_HISTORY"));

        viewMenu.add(viewMarkTranslatedSegmentsCheckBoxMenuItem = createCheckboxMenuItem("TF_MENU_DISPLAY_MARK_TRANSLATED"));
        viewMenu.add(viewMarkUntranslatedSegmentsCheckBoxMenuItem = createCheckboxMenuItem("TF_MENU_DISPLAY_MARK_UNTRANSLATED"));
        viewMenu.add(viewDisplaySegmentSourceCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_DISPLAY_SEGMENT_SOURCES"));
        viewMenu.add(viewMarkNonUniqueSegmentsCheckBoxMenuItem = createCheckboxMenuItem("MW_VIEW_MENU_MARK_NON_UNIQUE_SEGMENTS"));
        viewMenu.add(viewModificationInfoMenu = createMenu("MW_VIEW_MENU_MODIFICATION_INFO"));
        ButtonGroup viewModificationInfoMenuBG = new ButtonGroup();
        viewModificationInfoMenu
                .add(viewDisplayModificationInfoNoneRadioButtonMenuItem = createRadioButtonMenuItem(
                        "MW_VIEW_MENU_MODIFICATION_INFO_NONE", viewModificationInfoMenuBG));
        viewModificationInfoMenu
                .add(viewDisplayModificationInfoSelectedRadioButtonMenuItem = createRadioButtonMenuItem(
                        "MW_VIEW_MENU_MODIFICATION_INFO_SELECTED", viewModificationInfoMenuBG));
        viewModificationInfoMenu
                .add(viewDisplayModificationInfoAllRadioButtonMenuItem = createRadioButtonMenuItem(
                        "MW_VIEW_MENU_MODIFICATION_INFO_ALL", viewModificationInfoMenuBG));

        toolsMenu.add(toolsValidateTagsMenuItem = createMenuItem("TF_MENU_TOOLS_VALIDATE"));
        toolsMenu
                .add(toolsShowStatisticsStandardMenuItem = createMenuItem("TF_MENU_TOOLS_STATISTICS_STANDARD"));
        toolsMenu
                .add(toolsShowStatisticsMatchesMenuItem = createMenuItem("TF_MENU_TOOLS_STATISTICS_MATCHES"));

        optionsMenu
                .add(optionsTabAdvanceCheckBoxMenuItem = createCheckboxMenuItem("TF_MENU_DISPLAY_ADVANCE"));
        optionsMenu
                .add(optionsAlwaysConfirmQuitCheckBoxMenuItem = createCheckboxMenuItem("MW_OPTIONSMENU_ALWAYS_CONFIRM_QUIT"));
        optionsMenu.add(optionsMachineTranslateMenu = createMenu("TF_OPTIONSMENU_MACHINETRANSLATE"));

        optionsMenu.add(optionsTransTipsMenu = createMenu("TF_OPTIONSMENU_TRANSTIPS"));
        optionsTransTipsMenu
                .add(optionsTransTipsEnableMenuItem = createCheckboxMenuItem("TF_OPTIONSMENU_TRANSTIPS_ENABLE"));
        optionsTransTipsMenu
                .add(optionsTransTipsExactMatchMenuItem = createCheckboxMenuItem("TF_OPTIONSMENU_TRANSTIPS_EXACTMATCH"));
        optionsMenu.add(new JSeparator());
        optionsMenu.add(optionsFontSelectionMenuItem = createMenuItem("TF_MENU_DISPLAY_FONT"));
        optionsMenu.add(optionsSetupFileFiltersMenuItem = createMenuItem("TF_MENU_DISPLAY_FILTERS"));
        optionsMenu.add(optionsSentsegMenuItem = createMenuItem("MW_OPTIONSMENU_SENTSEG"));
        optionsMenu.add(optionsSpellCheckMenuItem = createMenuItem("MW_OPTIONSMENU_SPELLCHECK"));
        optionsMenu.add(optionsWorkflowMenuItem = createMenuItem("MW_OPTIONSMENU_WORKFLOW"));
        optionsMenu.add(optionsTagValidationMenuItem = createMenuItem("MW_OPTIONSMENU_TAGVALIDATION"));
        optionsMenu.add(optionsTeamMenuItem = createMenuItem("MW_OPTIONSMENU_TEAM"));
        optionsMenu.add(optionsExtTMXMenuItem = createMenuItem("MW_OPTIONSMENU_EXT_TMX"));
        optionsMenu.add(optionsViewOptionsMenuItem = createMenuItem("MW_OPTIONSMENU_VIEW"));
        optionsMenu.add(optionsViewOptionsMenuLoginItem = createMenuItem("MW_OPTIONSMENU_LOGIN"));
        optionsMenu.add(optionsRestoreGUIMenuItem = createMenuItem("MW_OPTIONSMENU_RESTORE_GUI"));

        optionsMenu.add(new JSeparator());

        helpMenu.add(helpContentsMenuItem = createMenuItem("TF_MENU_HELP_CONTENTS"));
        helpMenu.add(helpAboutMenuItem = createMenuItem("TF_MENU_HELP_ABOUT"));

        setActionCommands();
        MainWindowMenuShortcuts.setShortcuts(mainMenu);

        initMacSpecific();

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                updateCheckboxesOnStart();
                onProjectStatusChanged(false);
            }

            public void onApplicationShutdown() {
            }
        });

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                if (Core.getProject().isProjectLoaded()) {
                    onProjectStatusChanged(true);
                } else {
                    onProjectStatusChanged(false);
                }
            }
        });

        return mainMenu;
    }

    /** Updates menu checkboxes from preferences on start */
    private void updateCheckboxesOnStart() {
        optionsTabAdvanceCheckBoxMenuItem.setSelected(Core.getEditor().getSettings().isUseTabForAdvance());
        optionsAlwaysConfirmQuitCheckBoxMenuItem.setSelected(Preferences
                .isPreference(Preferences.ALWAYS_CONFIRM_QUIT));
        optionsTransTipsEnableMenuItem.setSelected(Preferences.isPreference(Preferences.TRANSTIPS));
        optionsTransTipsExactMatchMenuItem.setSelected(Preferences
                .isPreference(Preferences.TRANSTIPS_EXACT_SEARCH));

        viewMarkTranslatedSegmentsCheckBoxMenuItem.setSelected(Core.getEditor().getSettings()
                .isMarkTranslated());
        viewMarkUntranslatedSegmentsCheckBoxMenuItem.setSelected(Core.getEditor().getSettings()
                .isMarkUntranslated());

        viewDisplaySegmentSourceCheckBoxMenuItem.setSelected(Core.getEditor().getSettings()
                .isDisplaySegmentSources());
        viewMarkNonUniqueSegmentsCheckBoxMenuItem.setSelected(Core.getEditor().getSettings()
                .isMarkNonUniqueSegments());

        viewDisplayModificationInfoNoneRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_NONE.equals(Core.getEditor()
                        .getSettings().getDisplayModificationInfo()));
        viewDisplayModificationInfoSelectedRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(Core.getEditor()
                        .getSettings().getDisplayModificationInfo()));
        viewDisplayModificationInfoAllRadioButtonMenuItem
                .setSelected(EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(Core.getEditor()
                        .getSettings().getDisplayModificationInfo()));
    }

    /**
     * Initialize Mac-specific features.
     */
    private void initMacSpecific() {
        try {
            // MacOSX-specific
            MRJAdapter.addQuitApplicationListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainWindowMenuHandler.projectExitMenuItemActionPerformed();
                }
            });
            MRJAdapter.addAboutListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainWindowMenuHandler.helpAboutMenuItemActionPerformed();
                }
            });
        } catch (NoClassDefFoundError e) {
            Log.log(e);
        }
    }

    /**
     * Create menu instance and set title.
     * 
     * @param titleKey
     *            title name key in resource bundle
     * @return menu instance
     */
    private JMenu createMenu(final String titleKey) {
        JMenu result = new JMenu();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        return result;
    }

    /**
     * Create menu item instance and set title.
     * 
     * @param titleKey
     *            title name key in resource bundle
     * @return menu item instance
     */
    private JMenuItem createMenuItem(final String titleKey) {
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
    private JCheckBoxMenuItem createCheckboxMenuItem(final String titleKey) {
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
    private JRadioButtonMenuItem createRadioButtonMenuItem(final String titleKey, ButtonGroup buttonGroup) {
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
            for (Field f : this.getClass().getDeclaredFields()) {
                if (JMenuItem.class.isAssignableFrom(f.getType()) && f.getType() != JMenu.class) {
                    JMenuItem menuItem = (JMenuItem) f.get(this);
                    menuItem.setActionCommand(f.getName());
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
    private void onProjectStatusChanged(final boolean isProjectOpened) {
        JMenuItem[] itemsToSwitchOff = new JMenuItem[] { projectNewMenuItem, projectOpenMenuItem };

        JMenuItem[] itemsToSwitchOn = new JMenuItem[] { projectImportMenuItem, projectWikiImportMenuItem,
                projectReloadMenuItem, projectCloseMenuItem, projectSaveMenuItem, projectEditMenuItem,
                projectCompileMenuItem,

                editMenu, editFindInProjectMenuItem, editInsertSourceMenuItem, editInsertTranslationMenuItem,
                editTagPainterMenuItem, editOverwriteSourceMenuItem, editOverwriteTranslationMenuItem,
                editRedoMenuItem, editSelectFuzzy1MenuItem, editSelectFuzzy2MenuItem,
                editSelectFuzzy3MenuItem, editSelectFuzzy4MenuItem, editSelectFuzzy5MenuItem,
                editUndoMenuItem, switchCaseSubMenu, editOverwriteMachineTranslationMenuItem,

                gotoMenu, gotoNextSegmentMenuItem, gotoNextUntranslatedMenuItem, gotoPreviousSegmentMenuItem,
                gotoSegmentMenuItem,

                viewFileListMenuItem, toolsValidateTagsMenuItem, toolsShowStatisticsStandardMenuItem,
                toolsShowStatisticsMatchesMenuItem };

        for (JMenuItem item : itemsToSwitchOff) {
            item.setEnabled(!isProjectOpened);
        }
        for (JMenuItem item : itemsToSwitchOn) {
            item.setEnabled(isProjectOpened);
        }
    }

    public JMenu getMachineTranslationMenu() {
        return optionsMachineTranslateMenu;
    }

    public JMenu getOptionsMenu() {
        return optionsMenu;
    }

    JMenuItem cycleSwitchCaseMenuItem;
    JMenuItem editFindInProjectMenuItem;
    JMenuItem editInsertSourceMenuItem;
    JMenuItem editInsertTranslationMenuItem;
    JMenu editMenu;
    JMenuItem editOverwriteSourceMenuItem;
    JMenuItem editOverwriteTranslationMenuItem;
    JMenuItem editOverwriteMachineTranslationMenuItem;
    JMenuItem editRedoMenuItem;
    JMenuItem editSelectFuzzy1MenuItem;
    JMenuItem editSelectFuzzy2MenuItem;
    JMenuItem editSelectFuzzy3MenuItem;
    JMenuItem editSelectFuzzy4MenuItem;
    JMenuItem editSelectFuzzy5MenuItem;
    JMenuItem editUndoMenuItem;
    JMenuItem editTagPainterMenuItem;
    JMenuItem editExportSelectionMenuItem;
    JMenuItem editCreateGlossaryEntryMenuItem;
    public JMenuItem gotoHistoryBackMenuItem;
    public JMenuItem gotoHistoryForwardMenuItem;
    JMenu gotoMenu;
    JMenuItem gotoNextSegmentMenuItem;
    JMenuItem gotoNextUntranslatedMenuItem;
    JMenuItem gotoPreviousSegmentMenuItem;
    JMenuItem gotoSegmentMenuItem;
    JMenuItem helpAboutMenuItem;
    JMenuItem helpContentsMenuItem;
    JMenu helpMenu;
    JMenuItem lowerCaseMenuItem;
    JMenuBar mainMenu;
    JCheckBoxMenuItem optionsAlwaysConfirmQuitCheckBoxMenuItem;
    JMenuItem optionsFontSelectionMenuItem;
    JMenu optionsMenu;
    JMenuItem optionsRestoreGUIMenuItem;
    JMenuItem optionsSentsegMenuItem;
    JMenuItem optionsSetupFileFiltersMenuItem;
    JMenuItem optionsSpellCheckMenuItem;
    JCheckBoxMenuItem optionsTabAdvanceCheckBoxMenuItem;
    JMenu optionsMachineTranslateMenu;
    JMenu optionsTransTipsMenu;
    JCheckBoxMenuItem optionsTransTipsEnableMenuItem;
    JCheckBoxMenuItem optionsTransTipsExactMatchMenuItem;
    JMenuItem optionsWorkflowMenuItem;
    JMenuItem optionsTagValidationMenuItem;
    JMenuItem optionsTeamMenuItem;
    JMenuItem optionsExtTMXMenuItem;
    JMenuItem optionsViewOptionsMenuItem;
    JMenuItem optionsViewOptionsMenuLoginItem;
    JMenuItem projectCloseMenuItem;
    JMenuItem projectCompileMenuItem;
    JMenuItem projectEditMenuItem;
    JMenuItem projectExitMenuItem;
    JMenuItem projectImportMenuItem;
    JMenu projectMenu;
    JMenuItem projectNewMenuItem;
    JMenuItem projectOpenMenuItem;
    JMenuItem projectReloadMenuItem;
    JMenuItem projectSaveMenuItem;
    JMenuItem projectWikiImportMenuItem;
    JMenu switchCaseSubMenu;
    JMenuItem titleCaseMenuItem;
    JMenu toolsMenu;
    JMenuItem toolsValidateTagsMenuItem;
    JMenuItem toolsShowStatisticsStandardMenuItem;
    JMenuItem toolsShowStatisticsMatchesMenuItem;
    JMenuItem upperCaseMenuItem;
    JCheckBoxMenuItem viewDisplaySegmentSourceCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkNonUniqueSegmentsCheckBoxMenuItem;
    JMenu viewModificationInfoMenu;
    JRadioButtonMenuItem viewDisplayModificationInfoNoneRadioButtonMenuItem;
    JRadioButtonMenuItem viewDisplayModificationInfoSelectedRadioButtonMenuItem;
    JRadioButtonMenuItem viewDisplayModificationInfoAllRadioButtonMenuItem;
    JMenuItem viewFileListMenuItem;
    JCheckBoxMenuItem viewMarkTranslatedSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkUntranslatedSegmentsCheckBoxMenuItem;
    JMenu viewMenu;
}
