/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.project.step;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.NewProjectFileChooser;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * First step to select the new project folder. Navigation cannot proceed until a valid folder is chosen.
 */
public class ProjectFolderStep implements Step {
    private final JPanel panel = new JPanel();
    private final JTextField folderField = new JTextField(40);
    private final JButton browseBtn = new JButton();

    private File selectedDir;

    public static final String BROWSE_BUTTON_NAME = "project_folder_browse_button";
    public static final String FOLDER_FIELD_NAME = "project_folder_textfield";

    public ProjectFolderStep() {
        panel.add(createProjectFolderBox());
        panel.add(Box.createVerticalGlue());
    }

    private JPanel createProjectFolderBox() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lbl = new JLabel();
        Mnemonics.setLocalizedText(lbl, OStrings.getString("PP_SAVE_PROJECT_FILE"));
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        row.add(folderField);
        folderField.setName(FOLDER_FIELD_NAME);
        row.add(Box.createRigidArea(new Dimension(5, 0)));
        Mnemonics.setLocalizedText(browseBtn, "Browse...");
        browseBtn.setName(BROWSE_BUTTON_NAME);
        browseBtn.addActionListener(e -> onBrowse());
        row.add(browseBtn);
        return row;
    }

    private void onBrowse() {
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        int result = ndc.showSaveDialog(null);
        if (result == NewProjectFileChooser.APPROVE_OPTION) {
            File dir = ndc.getSelectedFile();
            folderField.setText(dir.getAbsolutePath());
        }
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_SAVE_PROJECT_FILE");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable String validateInput() {
        String path = folderField.getText();
        if (path == null || path.trim().isEmpty()) {
            return "Folder not selected"; // Do not show dialog; wizard blocks Next
        }
        File dir = new File(path);
        if (dir.isDirectory()) {
            Path p = dir.toPath();
            if (!Files.isReadable(p) || !Files.isWritable(p)) {
                return "Folder must be readable and writable";
            }
        } else {
            File parent = dir.getParentFile();
            if (parent == null) {
                return "Invalid folder";
            }
            Path pp = parent.toPath();
            if (!Files.isReadable(pp) || !Files.isWritable(pp)) {
                return "Parent folder must be readable and writable";
            }
        }
        // Passed validation
        selectedDir = dir;
        return null;
    }

    @Override
    public void onSave(ProjectProperties props) {
        // No-op; wizard will read selectedDir and construct properties
    }

    public JTextField getFolderField() {
        return folderField;
    }

    public File getSelectedDir() {
        return selectedDir != null ? selectedDir : new File(folderField.getText());
    }
}
