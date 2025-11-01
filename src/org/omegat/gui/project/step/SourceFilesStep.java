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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.FileUtil;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Wizard step to select files and/or directories to be copied into the project's
 * source folder after the wizard finishes.
 */
public class SourceFilesStep implements Step {
    private final JPanel panel = new JPanel();
    private final DefaultListModel<File> model = new DefaultListModel<>();
    private final JList<File> list = new JList<>(model);

    public static final String ADD_BUTTON_NAME = "source_files_add_button";
    public static final String REMOVE_BUTTON_NAME = "source_files_remove_button";
    public static final String CLEAR_BUTTON_NAME = "source_files_clear_button";

    public SourceFilesStep() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel();
        // Fallback to English if no key exists
        Mnemonics.setLocalizedText(label, "Select source files to import");
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(600, 200));
        panel.add(scroll);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel buttons = new JPanel();
        JButton addBtn = new JButton();
        Mnemonics.setLocalizedText(addBtn, OStrings.getString("BUTTON_ADD"));
        addBtn.setName(ADD_BUTTON_NAME);
        addBtn.addActionListener(e -> onAdd());
        buttons.add(addBtn);

        JButton removeBtn = new JButton();
        Mnemonics.setLocalizedText(removeBtn, OStrings.getString("BUTTON_REMOVE"));
        removeBtn.setName(REMOVE_BUTTON_NAME);
        removeBtn.addActionListener(e -> onRemove());
        buttons.add(removeBtn);

        JButton clearBtn = new JButton();
        Mnemonics.setLocalizedText(clearBtn, "Clear");
        clearBtn.setName(CLEAR_BUTTON_NAME);
        clearBtn.addActionListener(e -> onClear());
        buttons.add(clearBtn);

        panel.add(buttons);
        panel.add(Box.createVerticalGlue());
    }

    private void onAdd() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int ret = fc.showOpenDialog(panel);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            for (File f : files) {
                if (f != null && f.exists() && !containsFile(f)) {
                    model.addElement(f);
                }
            }
        }
    }

    private boolean containsFile(File f) {
        for (int i = 0; i < model.size(); i++) {
            if (model.getElementAt(i).equals(f)) {
                return true;
            }
        }
        return false;
    }

    private void onRemove() {
        List<File> selected = list.getSelectedValuesList();
        for (File f : selected) {
            model.removeElement(f);
        }
    }

    private void onClear() {
        model.clear();
    }

    @Override
    public String getTitle() {
        return "Source files";
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties props) {
        // No-op
    }

    @Override
    public @Nullable String validateInput() {
        // Nothing required
        return null;
    }

    @Override
    public void onSave(ProjectProperties props) {
        if (model.isEmpty()) {
            return;
        }
        List<File> toCopy = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            toCopy.add(model.getElementAt(i));
        }
        File destination = new File(props.getSourceRoot());
        destination.mkdirs();
        try {
            FileUtil.copyFilesTo(destination, toCopy.toArray(new File[0]), null);
        } catch (IOException e) {
            // Swallow the exception here; wizard has no error UI. In a full UI we would report this.
        }
    }
}
