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

import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Step to configure Export TM root/levels and external command.
 */
public class ExportAndCommandStep implements Step {
    private final ProjectConfigMode mode;
    private final JPanel panel = new JPanel();
    private final JTextField exportTmRoot;
    private final JCheckBox exportOmegaT;
    private final JCheckBox exportL1;
    private final JCheckBox exportL2;
    private final JTextArea externalCmd;

    public ExportAndCommandStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        exportTmRoot = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_EXPORT_TM_ROOT"), exportTmRoot));
        panel.add(Box.createVerticalStrut(6));
        JPanel exLevels = new JPanel();
        exLevels.setLayout(new BoxLayout(exLevels, BoxLayout.X_AXIS));
        exLevels.add(new JLabel(OStrings.getString("PP_EXPORT_TM_LEVELS")));
        exLevels.add(Box.createHorizontalStrut(8));
        exportOmegaT = new JCheckBox(OStrings.getString("PP_EXPORT_TM_OMEGAT"));
        exportL1 = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL1"));
        exportL2 = new JCheckBox(OStrings.getString("PP_EXPORT_TM_LEVEL2"));
        exLevels.add(exportOmegaT);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL1);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL2);
        panel.add(exLevels);
        panel.add(Box.createVerticalStrut(12));
        panel.add(new JLabel(OStrings.getString("PP_EXTERNAL_COMMAND")));
        externalCmd = new JTextArea(4, 40);
        panel.add(new JScrollPane(externalCmd));
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
        return OStrings.getString("PP_EXPORT_TM");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        exportTmRoot.setText(p.getExportTMRoot());
        List<String> lvls = p.getExportTmLevels();
        exportOmegaT.setSelected(lvls.contains("omegat"));
        exportL1.setSelected(lvls.contains("level1"));
        exportL2.setSelected(lvls.contains("level2"));
        externalCmd.setText(p.getExternalCommand());
        if (!Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            externalCmd.setEnabled(false);
        }
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            exportOmegaT.setEnabled(false);
            exportL1.setEnabled(false);
            exportL2.setEnabled(false);
            externalCmd.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        if (mode != ProjectConfigMode.NEW_PROJECT) {
            if (!new File(exportTmRoot.getText()).isDirectory())
                return OStrings.getString("NP_EXPORT_TMDIR_DOESNT_EXIST");
        }
        return null;
    }

    @Override
    public void onSave(ProjectProperties p) {
        p.setExportTMRoot(ensureSep(exportTmRoot.getText()));
        p.setExportTmLevels(exportOmegaT.isSelected(), exportL1.isSelected(), exportL2.isSelected());
        p.setExternalCommand(externalCmd.getText());
    }

    private String ensureSep(String s) {
        if (s == null)
            return "";
        return s.endsWith(File.separator) ? s : s + File.separator;
    }
}
