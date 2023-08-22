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

package org.omegat.gui.accesstool;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.MainMenuIcons;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowMenuHandler;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.RecentProjects;
import org.omegat.util.gui.ResourcesUtil;

/**
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class AccessTools extends JPanel {

    JComboBox<URI> recentProjectCB;
    JButton goButton;
    JComboBox<ProjectFileInformation> sourceFilesCB;
    JButton searchButton;
    JButton settingsButton;

    private ProjectComboBoxModel projectComboBoxModel;
    private SourceComboBoxModel sourceComboBoxModel;
    private final MainWindowMenuHandler mainWindowMenuHandler;

    private URI selectedProject = null;

    private final static int MAX_PATH_LENGTH_SHOWN = 25;
    private final static float CHECKBOX_HEIGHT_RATIO = 1.8f;

    public AccessTools(final MainWindow mainWindow,
                       final MainWindowMenuHandler mainWindowMenuHandler) {
        this.mainWindowMenuHandler = mainWindowMenuHandler;
        initComponents();
    }

    public void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        recentProjectCB = new JComboBox<>();
        int fontHeight = recentProjectCB.getFont().getSize();
        int cbHeight = (int)(CHECKBOX_HEIGHT_RATIO * fontHeight);
        int cbWidth = fontHeight * MAX_PATH_LENGTH_SHOWN;
        final NameAndPathComboBoxRenderer recentProjectRenderer = new NameAndPathComboBoxRenderer();
        recentProjectCB.setRenderer(recentProjectRenderer);
        recentProjectCB.setUI(new ToolbarComboboxUI());
        List<URI> projectList = new ArrayList<>();
        try {
            projectList.add(new URI("omegat", "new", null));
            projectList.add(new URI("omegat", "open", null));
            projectList.add(new URI("omegat", "team", null));
        } catch (URISyntaxException ignored) {
        }
        RecentProjects.getRecentProjects().stream()
                .map(f -> Paths.get(f).toAbsolutePath().toUri())
                .filter(AccessTools::checkProjectFolder)
                .distinct()
                .forEach(projectList::add);
        projectComboBoxModel = new ProjectComboBoxModel(projectList);
        recentProjectCB.setModel(projectComboBoxModel);
        recentProjectCB.setEnabled(true);
        recentProjectCB.setPreferredSize(new Dimension(cbWidth, cbHeight));
        recentProjectCB.setMaximumSize(new Dimension(cbWidth, cbHeight));
        add(recentProjectCB);
        goButton = new JButton(OStrings.getString("TF_MENU_NEWUI_PROJECT_GO"));
        add(goButton);

        JLabel sourceTitle = new JLabel(OStrings.getString("TF_MENU_NEWUI_FILE_SELECTOR"));
        add(sourceTitle);
        sourceFilesCB = new JComboBox<>();
        sourceComboBoxModel = new SourceComboBoxModel(Collections.emptyList());
        sourceFilesCB.setUI(new ToolbarComboboxUI());
        sourceFilesCB.setModel(sourceComboBoxModel);
        sourceFilesCB.setRenderer(new ProjectFileRenderer());
        sourceFilesCB.setEnabled(true);
        sourceFilesCB.setPreferredSize(new Dimension(cbWidth, cbHeight));
        sourceFilesCB.setMaximumSize(new Dimension(cbWidth, cbHeight));
        add(sourceFilesCB);

        searchButton = new JButton("",
                Objects.requireNonNullElseGet(UIManager.getIcon("OmegaT.newUI.search.icon"),
                () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.search.png"))));
        searchButton.setBorderPainted(false);
        settingsButton = new JButton("",
                Objects.requireNonNullElseGet(UIManager.getIcon("OmegaT.newUI.settings.icon"),
                () -> MainMenuIcons.newImageIcon(ResourcesUtil.getBundledImage("newUI.settings.png"))));
        settingsButton.setBorderPainted(false);

        // -- right side
        add(Box.createGlue());
        add(searchButton);
        add(settingsButton);

        searchButton.addActionListener(actionEvent -> {
            mainWindowMenuHandler.editFindInProjectMenuItemActionPerformed();
        });
        settingsButton.addActionListener(actionEvent -> {
            mainWindowMenuHandler.optionsPreferencesMenuItemActionPerformed();
        });
        recentProjectCB.addActionListener(actionEvent -> {
            // when select a project from the list, we open it.
            final Object item = recentProjectCB.getSelectedItem();
            if (item == null) {
                return;
            }
            if (item instanceof URI) {
                URI projectUri = (URI)item;
                if (projectUri.getScheme().equals("omegat")) {
                    switch (projectUri.getSchemeSpecificPart()) {
                        case "new":
                            mainWindowMenuHandler.projectNewMenuItemActionPerformed();
                            break;
                        case "open":
                            mainWindowMenuHandler.projectOpenMenuItemActionPerformed();
                            break;
                        case "team":
                            mainWindowMenuHandler.projectTeamNewMenuItemActionPerformed();
                            break;
                        default:
                            break;
                    }
                } else {
                    selectedProject = RecentProjects.getRecentProjects().stream()
                            .map(f -> Paths.get(f).toAbsolutePath().toUri())
                            .filter(uri -> uri.equals(projectUri))
                            .findFirst()
                            .orElse(null);
                }
            }
        });

        goButton.addActionListener(actionEvent -> {
            if (selectedProject != null) {
                openSelectedProject(selectedProject);
            }
        });

        sourceFilesCB.addActionListener(actionEvent -> {
            final Object selected = sourceComboBoxModel.getSelectedItem();
            if (selected == null) {
                return;
            }
            int modelRow = ((ProjectFileInformation) selected).getModelRow();
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

    private void openSelectedProject(URI uri) {
        // clear file list at first.
        sourceComboBoxModel.clear();
        sourceFilesCB.revalidate();
        ProjectUICommands.projectOpen(new File(uri), true);
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
        final List<ProjectFileInformation> files;
        if (activeFileName != null) {
            sourceComboBoxModel.addElement(new ProjectFileInformation(activeFileName));
            Core.getProject().getProjectFiles().stream()
                    .filter(f -> !f.filePath.equals(activeFileName))
                    .map(ProjectFileInformation::new)
                    .forEach(it -> sourceComboBoxModel.addElement(it));
        } else {
            files = Core.getProject().getProjectFiles().stream()
                    .map(ProjectFileInformation::new)
                    .collect(Collectors.toList());
            sourceComboBoxModel.addAll(files);
        }
    }

    private static boolean checkProjectFolder(URI project) {
        File f = Paths.get(project).toFile();
        if (!f.isDirectory()) {
            return false;
        }
        File projectFile = new File(f, OConsts.FILE_PROJECT);
        return projectFile.exists() && projectFile.canWrite();
    }

    /**
     * ComboBoxModel for project access tool.
     */
    @SuppressWarnings("serial")
    static class ProjectComboBoxModel extends DefaultComboBoxModel<URI> {

        public ProjectComboBoxModel(List<URI> list) {
            super(list.toArray(URI[]::new));
        }

        public void update() {
            final List<String> recentProjects = RecentProjects.getRecentProjects();
            if (recentProjects.size() > getSize()) {
                // when a new project is added to the list
                recentProjects.stream()
                        .map(f -> Paths.get(f).toAbsolutePath().toUri())
                        .filter(AccessTools::checkProjectFolder)
                        .distinct()
                        .filter(p -> getIndexOf(p) < 0)  // when new project
                        .forEach(this::addElement);
            }
        }
    }

    /**
     * CombBoxModel for file access tool.
     */
    @SuppressWarnings("serial")
    static class SourceComboBoxModel extends DefaultComboBoxModel<ProjectFileInformation> {

        /**
         * Constructor.
         * @param items default lists of SourceFileInfo.
         */
        public SourceComboBoxModel(final List<ProjectFileInformation> items) {
            super(items.toArray(new ProjectFileInformation[0]));
        }

        public void clear() {
            int sizeOfElements = getSize();
            for (int i = 0 ; i < sizeOfElements ; i++) {
                removeElementAt(0);
            }
        }
    }
}
