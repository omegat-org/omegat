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
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.CommandVarExpansion;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

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
    private final JTextArea externalCommandTextArea;

    public ExportAndCommandStep(ProjectConfigMode mode) {
        this.mode = mode;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        exportTmRoot = new JTextField(40);
        panel.add(buildRow(OStrings.getString("PP_EXPORT_TM_ROOT"), exportTmRoot));
        panel.add(Box.createVerticalStrut(6));
        JPanel exLevels = new JPanel();
        exLevels.setLayout(new BoxLayout(exLevels, BoxLayout.X_AXIS));
        JLabel exLevelsLabel = new JLabel();
        Mnemonics.setLocalizedText(exLevelsLabel, OStrings.getString("PP_EXPORT_TM_LEVELS"));
        exLevels.add(exLevelsLabel);
        exLevels.add(Box.createHorizontalStrut(8));
        exportOmegaT = new JCheckBox();
        Mnemonics.setLocalizedText(exportOmegaT, OStrings.getString("PP_EXPORT_TM_OMEGAT"));
        exportL1 = new JCheckBox();
        Mnemonics.setLocalizedText(exportL1, OStrings.getString("PP_EXPORT_TM_LEVEL1"));
        exportL2 = new JCheckBox();
        Mnemonics.setLocalizedText(exportL2, OStrings.getString("PP_EXPORT_TM_LEVEL2"));
        exLevels.add(exportOmegaT);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL1);
        exLevels.add(Box.createHorizontalStrut(8));
        exLevels.add(exportL2);
        panel.add(exLevels);
        panel.add(Box.createVerticalStrut(12));
        JLabel externalCmdLabel = new JLabel();
        Mnemonics.setLocalizedText(externalCmdLabel, OStrings.getString("PP_EXTERNAL_COMMAND"));
        panel.add(externalCmdLabel);
        externalCommandTextArea = new JTextArea(3, 40);
        Box externalCommandBox = Box.createVerticalBox();
        externalCommandBox.setBorder(new EtchedBorder());
        externalCommandTextArea.setRows(3);
        externalCommandTextArea.setLineWrap(true);
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            externalCommandBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    OStrings.getString("PP_EXTERNAL_COMMAND")));
        } else {
            externalCommandTextArea.setEditable(false);
            externalCommandTextArea.setToolTipText(OStrings.getString("PP_EXTERN_CMD_DISABLED_TOOLTIP"));
            externalCommandBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    OStrings.getString("PP_EXTERN_CMD_DISABLED")));
        }
        externalCommandTextArea.setName(EXTERNAL_COMMAND_TEXTAREA_NAME);
        final JScrollPane externalCommandScrollPane = new JScrollPane();
        externalCommandScrollPane.setViewportView(externalCommandTextArea);
        externalCommandBox.add(externalCommandScrollPane);
        variablesList = new JComboBox<>(new Vector<>(CommandVarExpansion.getCommandVariables()));
        variablesList.setName(VARIABLE_LIST_NAME);

        // Add variable insertion controls only if project external commands are
        // enabled.
        if (Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            Border emptyBorder = new EmptyBorder(2, 0, 2, 0);
            Box bIC = Box.createHorizontalBox();
            bIC.setBorder(emptyBorder);
            Mnemonics.setLocalizedText(variablesLabel,
                    OStrings.getString("EXT_TMX_MATCHES_TEMPLATE_VARIABLES"));
            bIC.add(variablesLabel);
            bIC.add(Box.createRigidArea(new Dimension(5, 0)));
            bIC.add(variablesList);
            Mnemonics.setLocalizedText(insertButton, OStrings.getString("BUTTON_INSERT"));
            insertButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    externalCommandTextArea.replaceSelection(variablesList.getSelectedItem().toString());
                }
            });
            bIC.add(Box.createRigidArea(new Dimension(5, 0)));
            bIC.add(insertButton);
            externalCommandBox.add(bIC);
        }

        panel.add(externalCommandBox);
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
        externalCommandTextArea.setText(p.getExternalCommand());
        if (!Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            externalCommandTextArea.setEnabled(false);
        }
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            exportOmegaT.setEnabled(false);
            exportL1.setEnabled(false);
            exportL2.setEnabled(false);
            externalCommandTextArea.setEnabled(false);
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
        p.setExternalCommand(externalCommandTextArea.getText());
    }

    private String ensureSep(String s) {
        if (s == null)
            return "";
        return s.endsWith(File.separator) ? s : s + File.separator;
    }

    JButton insertButton = new JButton();

    // extern command
    JLabel variablesLabel = new javax.swing.JLabel();
    JComboBox<String> variablesList;

    public static final String VARIABLE_LIST_NAME = "project_properties_variable_list";
    public static final String EXTERNAL_COMMAND_TEXTAREA_NAME =
            "project_properties_external_command_textarea";
}
