/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2025 Hiroshi Miura
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

import org.omegat.util.Platform;
import org.omegat.util.gui.MenuExtender;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class TestingMainMenu implements IMainMenu {

    private final JMenu projectMenu = new JMenu("Project");
    private final JMenu toolsMenu = new JMenu("Tools");
    private final JMenu gotoMenu = new JMenu("Goto");
    private final JMenu optionsMenu = new JMenu("Options");
    private final JMenu helpMenu = new JMenu("Help");
    private final JMenu machineTranslationMenu = new JMenu("MachineTranslate");
    private final JMenu glossaryMenu = new JMenu("Glossary");
    private final JMenu autoCompleteMenu = new JMenu("AutoComplete");

    @Override
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

    @Override
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

    @Override
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

    @Override
    public JMenu getMachineTranslationMenu() {
        return machineTranslationMenu;
    }
    @Override
    public JMenu getGlossaryMenu() {
        return glossaryMenu;
    }
    @Override
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
    @Override
    public void invokeAction(String action, int modifiers) {
    }
}
