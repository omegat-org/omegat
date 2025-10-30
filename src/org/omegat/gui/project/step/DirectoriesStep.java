/*
 * **************************************************************************
 * OmegaT - Computer Assisted Translation (CAT) tool
 *         with fuzzy matching, translation memory, keyword search,
 *         glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2025 Hiroshi Miura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * **************************************************************************
 */
package org.omegat.gui.project.step;

import java.awt.Color;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;

/**
 * Step to configure directory paths.
 */
public class DirectoriesStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();
    private final JTextField srcDir;
    private final JTextField trgDir;
    private final JTextField glosDir;
    private final JTextField writableGlos;
    private final JTextField tmDir;
    private final JTextField dictDir;

    public DirectoriesStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        srcDir = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_SRC_ROOT"), srcDir));
        panel.add(Box.createVerticalStrut(6));
        trgDir = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_LOC_ROOT"), trgDir));
        panel.add(Box.createVerticalStrut(6));
        glosDir = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_GLOS_ROOT"), glosDir));
        panel.add(Box.createVerticalStrut(6));
        writableGlos = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_WRITEABLE_GLOS"), writableGlos));
        panel.add(Box.createVerticalStrut(6));
        tmDir = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_TM_ROOT"), tmDir));
        panel.add(Box.createVerticalStrut(6));
        dictDir = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_DICT_ROOT"), dictDir));
    }

    private JPanel buildRow(String label, JComponent comp) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.add(new JLabel(label));
        row.add(Box.createHorizontalStrut(8));
        row.add(comp);
        return row;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_DIRECTORIES");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        srcDir.setText(p.getSourceRoot());
        trgDir.setText(p.getTargetRoot());
        glosDir.setText(p.getGlossaryRoot());
        writableGlos.setText(p.getWriteableGlossary());
        tmDir.setText(p.getTMRoot());
        dictDir.setText(p.getDictRoot());
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            // Highlight missing directories in red
            paintIfMissing(srcDir);
            paintIfMissing(trgDir);
            paintIfMissing(glosDir);
            // Writable glossary must be inside glossary dir
            File wg = new File(writableGlos.getText());
            File wParent = wg.getParentFile();
            if (wParent == null || !wParent.equals(new File(glosDir.getText()))) {
                writableGlos.setForeground(Color.RED);
            }
            paintIfMissing(tmDir);
            paintIfMissing(dictDir);
        }
    }

    private void paintIfMissing(JTextField field) {
        if (!new File(field.getText()).isDirectory()) {
            field.setForeground(Color.RED);
        }
    }

    @Override
    public @Nullable String validateInput() {
        if (mode != ProjectConfigMode.NEW_PROJECT) {
            if (!new File(srcDir.getText()).isDirectory())
                return OStrings.getString("NP_SOURCEDIR_DOESNT_EXIST");
            if (!new File(trgDir.getText()).isDirectory())
                return OStrings.getString("NP_TRANSDIR_DOESNT_EXIST");
            if (!new File(glosDir.getText()).isDirectory())
                return OStrings.getString("NP_GLOSSDIR_DOESNT_EXIST");
            File wg = new File(writableGlos.getText());
            if (wg.getParentFile() == null || !wg.getParentFile().equals(new File(glosDir.getText()))) {
                return OStrings.getString("NP_W_GLOSDIR_NOT_INSIDE_GLOS");
            }
            if (!new File(tmDir.getText()).isDirectory())
                return OStrings.getString("NP_TMDIR_DOESNT_EXIST");
            if (!new File(dictDir.getText()).isDirectory())
                return OStrings.getString("NP_DICTDIR_DOESNT_EXIST");
        }
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        p.setSourceRoot(ensureSep(srcDir.getText()));
        p.setTargetRoot(ensureSep(trgDir.getText()));
        p.setGlossaryRoot(ensureSep(glosDir.getText()));
        p.setWriteableGlossary(writableGlos.getText());
        p.setTMRoot(ensureSep(tmDir.getText()));
        p.setDictRoot(ensureSep(dictDir.getText()));
    }

    private String ensureSep(String s) {
        if (s == null)
            return "";
        return s.endsWith(File.separator) ? s : s + File.separator;
    }
}
