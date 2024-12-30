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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;

public class NameAndPathComboBoxRenderer implements ListCellRenderer<URI> {

    private final String defaultMessage;
    private final Color projectFilesCurrentFileBackground;
    private final Color projectFilesCurrentFileForeground;

    public NameAndPathComboBoxRenderer() {
        defaultMessage = Mnemonics.removeMnemonics(OStrings.getString("TF_MENU_NEWUI_PROJECT_SELECTOR"));
        projectFilesCurrentFileBackground = UIManager.getColor("OmegaT.projectFilesCurrentFileBackground");
        projectFilesCurrentFileForeground = UIManager.getColor("OmegaT.projectFilesCurrentFileForeground");
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends URI> list, URI uri, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        if (index < 0) {
            if (!uri.getScheme().equals("omegat")) {
                panel.setLayout(new FlowLayout(FlowLayout.LEADING));
                JLabel label = new JLabel(getProjectName(Path.of(uri)));
                panel.add(label);
            } else {
                panel.add(new JLabel(defaultMessage));
            }
        } else {
            if (uri != null) {
                if (uri.getScheme().equals("omegat")) {
                    switch (uri.getSchemeSpecificPart()) {
                        case "new":
                            setComponent(panel, Mnemonics.removeMnemonics(OStrings.getString(
                                    "TF_MENU_FILE_CREATE")), isSelected);
                            break;
                        case "open":
                            setComponent(panel, Mnemonics.removeMnemonics(OStrings.getString(
                                    "TF_MENU_FILE_OPEN")), isSelected);
                            break;
                        case "team":
                            setComponent(panel, Mnemonics.removeMnemonics(OStrings.getString(
                                    "TF_MENU_FILE_TEAM_CREATE")), isSelected);
                            break;
                        default:
                            panel.add(new JLabel(""));
                    }
                } else {
                    setComponent(panel, Path.of(uri), isSelected);
                }
            }
        }
        return panel;
    }

    private void setComponent(JPanel panel, String command, boolean isSelected) {
        panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 10, 1));
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        JLabel commandName = new JLabel(command);
        panel.add(commandName, BorderLayout.CENTER);
        if (isSelected) {
            panel.setBackground(projectFilesCurrentFileBackground);
            panel.setForeground(projectFilesCurrentFileForeground);
        }
    }

    private void setComponent(JPanel panel, Path path, boolean isSelected) {
        panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 10, 1));
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        JLabel projectName = new JLabel(getProjectName(path));
        JLabel projectPathLabel = new JLabel(getShortenPath(path));
        Font panelFont = panel.getFont();
        projectPathLabel.setFont(panelFont.deriveFont(panelFont.getSize() * .8f));
        projectPathLabel.setForeground(Color.GRAY);
        panel.add(projectName, BorderLayout.CENTER);
        panel.add(projectPathLabel, BorderLayout.SOUTH);
        if (isSelected) {
            panel.setBackground(projectFilesCurrentFileBackground);
            panel.setForeground(projectFilesCurrentFileForeground);
        }
    }

    private String getProjectName(Path path) {
        return path.getFileName().toString();
    }

    private static final int MAX_PATH_LEN = 50;

    private String getShortenPath(Path path) {
        Path home = Paths.get(System.getProperty("user.home"));
        String result;
        if (path.startsWith(home)) {
            result = "~/" + home.relativize(path);
        } else {
            result = path.toString();
        }
        if (result.length() > MAX_PATH_LEN) {
            result = result.substring(0, MAX_PATH_LEN) + "...";
        }
        return result;
    }
}
