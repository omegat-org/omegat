/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2023 Hiroshi Miura
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

package org.omegat.gui.preferences.view;

import javax.swing.JPanel;

import org.omegat.util.OStrings;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class AppearancePreferencesPanel extends JPanel {

    /** Creates new form AppearancePanel */
    public AppearancePreferencesPanel() {
        initComponents();
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbThemeSelect = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cbMenustyleSelect = new javax.swing.JComboBox<>();
        LightDefaultThemeSelectionLabel = new javax.swing.JLabel();
        cbLightThemeSelect = new javax.swing.JComboBox<>();
        DarkThemeSelectionLabel = new javax.swing.JLabel();
        cbDarkThemeSelect = new javax.swing.JComboBox<>();
        useLightDefaultThemeRB = new javax.swing.JRadioButton();
        useDarkThemeRB = new javax.swing.JRadioButton();
        syncWithOSColorRB = new javax.swing.JRadioButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        restoreWindowButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setMinimumSize(new java.awt.Dimension(250, 200));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.setAlignmentX(0.0F);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/omegat/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("MW_OPTIONMENU_APPEARANCE_THEME_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel1, gridBagConstraints);

        cbThemeSelect.setMinimumSize(new java.awt.Dimension(280, 80));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(cbThemeSelect, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, OStrings.getString("MW_OPTIONMENU_APPEARANCE_MENUSTYLE_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel2, gridBagConstraints);

        cbMenustyleSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMenustyleSelect.setMinimumSize(new java.awt.Dimension(280, 80));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(cbMenustyleSelect, gridBagConstraints);
        org.openide.awt.Mnemonics.setLocalizedText(LightDefaultThemeSelectionLabel, "Light/Default Theme:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(LightDefaultThemeSelectionLabel, gridBagConstraints);

        cbLightThemeSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel1.add(cbLightThemeSelect, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(DarkThemeSelectionLabel, "Dark Theme:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(DarkThemeSelectionLabel, gridBagConstraints);

        cbDarkThemeSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(cbDarkThemeSelect, gridBagConstraints);

        add(jPanel1);

        buttonGroup1.add(useLightDefaultThemeRB);
        org.openide.awt.Mnemonics.setLocalizedText(useLightDefaultThemeRB, "Use Light/Default theme");
        add(useLightDefaultThemeRB);

        buttonGroup1.add(useDarkThemeRB);
        org.openide.awt.Mnemonics.setLocalizedText(useDarkThemeRB, "Use dark theme");
        add(useDarkThemeRB);

        buttonGroup1.add(syncWithOSColorRB);
        org.openide.awt.Mnemonics.setLocalizedText(syncWithOSColorRB, "Sync with OS color preference");
        add(syncWithOSColorRB);
        add(filler1);

        org.openide.awt.Mnemonics.setLocalizedText(restoreWindowButton, OStrings.getString("MW_OPTIONSMENU_RESTORE_GUI")); // NOI18N
        add(restoreWindowButton);
    }

    javax.swing.JComboBox<String> cbMenustyleSelect;
    javax.swing.JComboBox<String> cbThemeSelect;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    javax.swing.JLabel DarkThemeSelectionLabel;
    javax.swing.JLabel LightDefaultThemeSelectionLabel;
    javax.swing.ButtonGroup buttonGroup1;
    javax.swing.JComboBox<String> cbDarkThemeSelect;
    javax.swing.JComboBox<String> cbLightThemeSelect;
    private javax.swing.JPanel jPanel1;
    javax.swing.JButton restoreWindowButton;
    javax.swing.JRadioButton syncWithOSColorRB;
    javax.swing.JRadioButton useDarkThemeRB;
    javax.swing.JRadioButton useLightDefaultThemeRB;
}
