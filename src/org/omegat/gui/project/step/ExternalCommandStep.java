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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.CommandVarExpansion;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * Optional step to configure an external command executed on Save/Compile.
 * This step is only included when Preferences.ALLOW_PROJECT_EXTERN_CMD is true.
 */
public class ExternalCommandStep implements ProjectWizardStep {
    private final JPanel panel = new JPanel();

    private final JTextArea externalCommandTextArea = new JTextArea(3, 40);
    private final JButton insertButton = new JButton();
    private final JLabel variablesLabel = new JLabel();
    private JComboBox<String> variablesList = new JComboBox<>();

    public ExternalCommandStep() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createExternalCommandBox());
        panel.add(Box.createVerticalGlue());
    }

    private Box createExternalCommandBox() {
        Box externalCommandBox = Box.createVerticalBox();
        externalCommandBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        externalCommandTextArea.setRows(3);
        externalCommandTextArea.setLineWrap(true);

        boolean allowed = Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD);
        if (allowed) {
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

        if (allowed) {
            Box bIC = Box.createHorizontalBox();
            bIC.setBorder(new EmptyBorder(2, 0, 2, 0));
            Mnemonics.setLocalizedText(variablesLabel,
                    OStrings.getString("EXT_TMX_MATCHES_TEMPLATE_VARIABLES"));
            bIC.add(variablesLabel);
            bIC.add(Box.createRigidArea(new Dimension(5, 0)));
            bIC.add(variablesList);
            Mnemonics.setLocalizedText(insertButton, OStrings.getString("BUTTON_INSERT"));
            insertButton.addActionListener(e ->
                    externalCommandTextArea.replaceSelection(variablesList.getSelectedItem().toString()));
            bIC.add(Box.createRigidArea(new Dimension(5, 0)));
            bIC.add(insertButton);
            externalCommandBox.add(bIC);
        }
        return externalCommandBox;
    }

    @Override
    public String getTitle() {
        return OStrings.getString("PP_EXTERNAL_COMMAND");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        externalCommandTextArea.setText(p.getExternalCommand());
        if (!Preferences.isPreference(Preferences.ALLOW_PROJECT_EXTERN_CMD)) {
            externalCommandTextArea.setEnabled(false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        return null; // No validation required for command text
    }

    @Override
    public void onSave(ProjectProperties p) {
        p.setExternalCommand(externalCommandTextArea.getText());
    }

    public static final String VARIABLE_LIST_NAME = "project_properties_variable_list";
    public static final String EXTERNAL_COMMAND_TEXTAREA_NAME = "project_properties_external_command_textarea";
}
