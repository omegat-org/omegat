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
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.util.OStrings;
import org.omegat.util.RecentProjects;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.ResourcesUtil;

/**
 * @author Hiroshi Miura
 */
public class MainWindowAccessTools {

    JComboBox<String> recentProjectCB;
    JComboBox<String> sourceFilesCB;
    JButton searchButton;
    JButton settingsButton;

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
        recentProjectCB.setModel(new DefaultComboBoxModel<>(getRecentProjectList("").toArray(String[]::new)));
        recentProjectCB.setEnabled(true);
        recentProjectCB.setPreferredSize(new Dimension(300, 20));
        recentProjectCB.setMaximumSize(new Dimension(400, 20));
        container.add(recentProjectCB);

        JLabel sourceTitle = new JLabel(OStrings.getString("TF_MENU_NEWUI_FILE_SELECTOR"));
        container.add(sourceTitle);
        sourceFilesCB = new JComboBox<>();
        sourceFilesCB.setModel(new DefaultComboBoxModel<>(new String[0]));
        sourceFilesCB.setEnabled(true);
        sourceFilesCB.setPreferredSize(new Dimension(300, 20));
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
            // when select project from the list, we open it.
            String item = recentProjectCB.getSelectedItem().toString();
            if (StringUtil.isEmpty(item)) {
                return;
            }
            recentProjectCB
                    .setModel(new DefaultComboBoxModel<>(getRecentProjectList(item).toArray(String[]::new)));
            Optional<String> targetProject = RecentProjects.getRecentProjects().stream()
                    .filter(f -> Paths.get(f).getFileName().toString().equals(item)).findFirst();
            targetProject.ifPresent(s -> ProjectUICommands.projectOpen(new File(s)));
        });

        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            @Override
            public void onNewFile(final String activeFileName) {
                List<IProject.FileInfo> projectFiles = Core.getProject().getProjectFiles();
                List<String> listFiles = new ArrayList<>();
                listFiles.add(activeFileName);
                listFiles.addAll(projectFiles.stream().map(info -> info.filePath)
                        .filter(n -> !n.equals(activeFileName)).collect(Collectors.toList()));
                sourceFilesCB.setModel(new DefaultComboBoxModel<>(listFiles.toArray(new String[0])));
            }

            @Override
            public void onEntryActivated(final SourceTextEntry newEntry) {
            }
        });

        sourceFilesCB.addActionListener(actionEvent -> {
            String item = sourceFilesCB.getModel().getSelectedItem().toString();
            int modelRow = -1;
            List<IProject.FileInfo> projectFiles = Core.getProject().getProjectFiles();
            for (int i = 0; i < projectFiles.size(); i++) {
                if (Paths.get(projectFiles.get(i).filePath).getFileName().toString().equals(item)) {
                    modelRow = i;
                    break;
                }
            }
            if (modelRow >= 0) {
                Core.getEditor().gotoFile(modelRow);
                Core.getEditor().requestFocus();
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                onProjectStatusChanged(false);
            }

            public void onApplicationShutdown() {
            }
        });
    }

    private List<String> getRecentProjectList(String current) {
        List<String> recent = new ArrayList<>();
        recent.add(current);
        recent.addAll(
                RecentProjects.getRecentProjects().stream().map(f -> Paths.get(f).getFileName().toString())
                        .filter(f -> !f.equals(current)).collect(Collectors.toList()));
        return recent;
    }

    private void onProjectStatusChanged(final boolean isProjectOpened) {
        if (isProjectOpened) {
            SwingUtilities.invokeLater(() -> {
                List<IProject.FileInfo> projectFiles = Core.getProject().getProjectFiles();
                sourceFilesCB.setModel(new DefaultComboBoxModel<>(
                        projectFiles.stream().map(i -> i.filePath).toArray(String[]::new)));
                sourceFilesCB.revalidate();

                recentProjectCB.setModel(new DefaultComboBoxModel<>(RecentProjects.getRecentProjects().stream()
                        .map(f -> Paths.get(f).getFileName().toString()).toArray(String[]::new)));
            });
        } else {
            sourceFilesCB.setModel(new DefaultComboBoxModel<>(new String[0]));
            sourceFilesCB.revalidate();
        }

    }

}
