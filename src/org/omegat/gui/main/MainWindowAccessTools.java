/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
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

import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.nio.CharBuffer;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.OStrings;
import org.omegat.util.RecentProjects;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.ResourcesUtil;

/**
 * @author Hiroshi Miura
 */
public class MainWindowAccessTools {

    JComboBox<String> recentProjectCB;
    JComboBox<SourceFileInfo> sourceFilesCB;
    JButton searchButton;
    JButton settingsButton;

    private ProjectComboBoxModel projectComboBoxModel;
    private SourceComboBoxModel sourceComboBoxModel;

    private final MainWindowMenuHandler mainWindowMenuHandler;

    static MainWindowAccessTools of(Container container, MainWindowMenuHandler mainWindowMenuHandler) {
        MainWindowAccessTools mainWindowAccessTools = new MainWindowAccessTools(mainWindowMenuHandler);
        mainWindowAccessTools.initComponents(container);
        return mainWindowAccessTools;
    }

    MainWindowAccessTools(final MainWindowMenuHandler mainWindowMenuHandler) {
        this.mainWindowMenuHandler = mainWindowMenuHandler;
    }

    void initComponents(Container container) {
        JLabel recentLabel = new JLabel(OStrings.getString("TF_MENU_NEWUI_PROJECT_SELECTOR"));
        container.add(recentLabel);
        recentProjectCB = new JComboBox<>();
        projectComboBoxModel = new ProjectComboBoxModel();
        recentProjectCB.setModel(projectComboBoxModel);
        recentProjectCB.setEnabled(true);
        recentProjectCB.setPreferredSize(new Dimension(300, 20));
        recentProjectCB.setMaximumSize(new Dimension(400, 20));
        container.add(recentProjectCB);

        JLabel sourceTitle = new JLabel(OStrings.getString("TF_MENU_NEWUI_FILE_SELECTOR"));
        container.add(sourceTitle);
        sourceFilesCB = new JComboBox<>();
        sourceComboBoxModel = new SourceComboBoxModel(Collections.emptyList());
        sourceFilesCB.setModel(sourceComboBoxModel);
        sourceFilesCB.setEnabled(true);
        sourceFilesCB.setPreferredSize(new Dimension(320, 20));
        sourceFilesCB.setMaximumSize(new Dimension(400, 20));
        container.add(sourceFilesCB);

        searchButton = new JButton("",
                Objects.requireNonNullElseGet(UIManager.getIcon("OmegaT.newUI.search.icon"),
                () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.search.png"))));
        searchButton.setBorderPainted(false);
        settingsButton = new JButton("",
                Objects.requireNonNullElseGet(UIManager.getIcon("OmegaT.newUI.settings.icon"),
                () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.settings.png"))));
        settingsButton.setBorderPainted(false);

        // -- right side
        container.add(Box.createGlue());
        container.add(searchButton);
        container.add(settingsButton);

        searchButton.addActionListener(actionEvent -> {
            mainWindowMenuHandler.editFindInProjectMenuItemActionPerformed();
        });
        settingsButton.addActionListener(actionEvent -> {
            mainWindowMenuHandler.optionsPreferencesMenuItemActionPerformed();
        });
        recentProjectCB.addActionListener(actionEvent -> {
            // when select a project from the list, we open it.
            final Object item = recentProjectCB.getSelectedItem();
            if (item == null || StringUtil.isEmpty(item.toString())) {
                return;
            }
            Optional<String> targetProject = RecentProjects.getRecentProjects().stream()
                    .filter(f -> Paths.get(f).getFileName().toString().equals(item)).findFirst();
            targetProject.ifPresent(s -> {
                // clear file list at first.
                sourceComboBoxModel.clear();
                sourceFilesCB.revalidate();
                ProjectUICommands.projectOpen(new File(s), true);
            });
        });

        sourceFilesCB.addActionListener(actionEvent -> {
            final Object selected = sourceComboBoxModel.getSelectedItem();
            if (selected == null) {
                return;
            }
            int modelRow = ((SourceFileInfo) selected).getModelRow();
            if (modelRow >= 0) {
                Core.getEditor().gotoFile(modelRow);
                Core.getEditor().requestFocus();
            }
        });

        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType.equals(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD)) {
                onProjectStatusChanged(Core.getProject().isProjectLoaded());
            }
        });
    }


    private void onProjectStatusChanged(final boolean isProjectOpened) {
        if (isProjectOpened) {
            SwingUtilities.invokeLater(() -> {
                updateProjectFiles(null);
                projectComboBoxModel.update();
                sourceFilesCB.revalidate();
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                sourceComboBoxModel.clear();
                sourceFilesCB.revalidate();
            });
        }
    }

    private synchronized void updateProjectFiles(String activeFileName) {
        sourceComboBoxModel.clear();
        final List<SourceFileInfo> files;
        if (activeFileName != null) {
            sourceComboBoxModel.addElement(new SourceFileInfo(activeFileName));
            Core.getProject().getProjectFiles().stream()
                    .filter(f -> !f.filePath.equals(activeFileName))
                    .map(SourceFileInfo::new)
                    .forEach(it -> sourceComboBoxModel.addElement(it));
        } else {
            files = Core.getProject().getProjectFiles().stream()
                    .map(SourceFileInfo::new)
                    .collect(Collectors.toList());
            sourceComboBoxModel.addAll(files);
        }
    }

    /**
     * Creates a string of NBSP that is 'spaces' spaces long.
     *
     * @param spaces The number of spaces to add to the string.
     */
    private static String spaces(int spaces) {
      return CharBuffer.allocate(spaces).toString().replace('\0', (char) 160);
    }

    static class SourceFileInfo {

        private static final int FILE_SELECTOR_WIDTH = 48;

        private final String filePath;
        private final int segments;

        public SourceFileInfo(final String activeFileName) {
            filePath = Paths.get(activeFileName).getFileName().toString();
            segments =
                    Core.getProject().getProjectFiles().stream()
                            .filter(fi -> Paths.get(fi.filePath).getFileName().toString().equals(this.filePath))
                            .map(fi -> fi.entries.size())
                            .findFirst()
                            .orElse(0);
        }

        public SourceFileInfo(final IProject.FileInfo f) {
            filePath = Paths.get(f.filePath).getFileName().toString();
            segments = f.entries.size();
        }

        @Override
        public String toString() {
            int numSpaceCh = FILE_SELECTOR_WIDTH - Integer.toString(segments).length() - filePath.length() - 1;
            if (numSpaceCh < 0) {
                return String.format("%s%s %d", filePath.substring(0, filePath.length() - numSpaceCh - 2),
                        "...", segments);
            }
            return String.format("%s%s %d", filePath, spaces(numSpaceCh), segments);

        }

        public int getModelRow() {
            int modelRow = -1;
            List<IProject.FileInfo> projectFiles = Core.getProject().getProjectFiles();
            for (int i = 0; i < projectFiles.size(); i++) {
                if (Paths.get(projectFiles.get(i).filePath).getFileName().toString().equals(this.filePath)) {
                    modelRow = i;
                    break;
                }
            }
            return modelRow;
        }
    }

    /**
     * ComboBoxModel for project access tool.
     */
    @SuppressWarnings("serial")
    static class ProjectComboBoxModel extends DefaultComboBoxModel<String> {

        public ProjectComboBoxModel() {
            super(RecentProjects.getRecentProjects()
                    .stream()
                    .map(f -> Paths.get(f).getFileName().toString())
                    .toArray(String[]::new));
        }

        public void update() {
            final List<String> recentProjects = RecentProjects.getRecentProjects();
            if (recentProjects.size() > getSize()) {
                // when a new project is added to the list
                recentProjects.stream()
                        .map(f -> Paths.get(f).getFileName().toString())
                        .filter(p -> getIndexOf(p) < 0)  // when new project
                        .forEach(this::addElement);
            }
        }
    }

    /**
     * CombBoxModel for file access tool.
     */
    @SuppressWarnings("serial")
    static class SourceComboBoxModel extends DefaultComboBoxModel<SourceFileInfo> {

        /**
         * Constructor.
         * @param items default lists of SourceFileInfo.
         */
        public SourceComboBoxModel(final List<SourceFileInfo> items) {
            super(items.toArray(new SourceFileInfo[0]));
        }

        public void clear() {
            int sizeOfElements = getSize();
            for (int i = 0 ; i < sizeOfElements ; i++) {
                removeElementAt(0);
            }
        }
    }
}
