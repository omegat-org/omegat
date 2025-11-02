/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               2025 Hiroshi Miura
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

package org.omegat.gui.dialogs;

import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import static javax.swing.BoxLayout.*;

/**
 * UI for repository mapping.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
@SuppressWarnings("serial")
public class RepositoriesMappingPanel extends JPanel {

    /**
     * Creates new form RepositoriesMappingDialog
     */
    public RepositoriesMappingPanel() {
        initComponents();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, PAGE_AXIS));

        GridBagConstraints gridBagConstraints;

        JPanel repositoriesTablePanel = new JPanel();
        JLabel repositoriesTableTitle = new JLabel();
        repositoriesScrollPane = new JScrollPane();
        tableRepositories = new JTable();
        JPanel jPanel5 = new JPanel();
        JPanel jPanel6 = new JPanel();
        btnRepoAdd = new JButton();
        btnRepoRemove = new JButton();
        JPanel jPanel4 = new JPanel();
        JLabel jLabel2 = new JLabel();
        mappingScrollPane = new JScrollPane();
        tableMapping = new JTable();
        JPanel jPanel7 = new JPanel();
        JPanel jPanel8 = new JPanel();
        btnMappingRemove = new JButton();
        btnMappingAdd = new JButton();

        repositoriesTablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        repositoriesTablePanel.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(repositoriesTableTitle, OStrings.getString("RMD_TABLE_REPOSITORIES"));
        repositoriesTablePanel.add(repositoriesTableTitle, BorderLayout.NORTH);

        tableRepositories.setAutoCreateRowSorter(true);
        tableRepositories.setFillsViewportHeight(true);
        tableRepositories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        repositoriesScrollPane.setViewportView(tableRepositories);
        repositoriesScrollPane.setPreferredSize(new Dimension(400, 100));

        repositoriesTablePanel.add(repositoriesScrollPane, BorderLayout.CENTER);

        jPanel5.setLayout(new BorderLayout());

        jPanel6.setLayout(new java.awt.GridBagLayout());

        Mnemonics.setLocalizedText(btnRepoAdd, OStrings.getString("RMD_BTN_ADD"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel6.add(btnRepoAdd, gridBagConstraints);

        Mnemonics.setLocalizedText(btnRepoRemove, OStrings.getString("RMD_BTN_REMOVE"));
        btnRepoRemove.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel6.add(btnRepoRemove, gridBagConstraints);

        jPanel5.add(jPanel6, BorderLayout.CENTER);

        repositoriesTablePanel.add(jPanel5, BorderLayout.EAST);

        add(repositoriesTablePanel);

        jPanel4.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(jLabel2, OStrings.getString("RMD_TABLE_MAPPING"));
        jPanel4.add(jLabel2, BorderLayout.NORTH);

        tableMapping.setAutoCreateRowSorter(true);
        tableMapping.setFillsViewportHeight(true);
        tableMapping.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mappingScrollPane.setViewportView(tableMapping);
        mappingScrollPane.setPreferredSize(new Dimension(400, 100));

        jPanel4.add(mappingScrollPane, BorderLayout.CENTER);

        jPanel7.setLayout(new BorderLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        Mnemonics.setLocalizedText(btnMappingRemove, OStrings.getString("RMD_BTN_REMOVE"));
        btnMappingRemove.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(btnMappingRemove, gridBagConstraints);

        Mnemonics.setLocalizedText(btnMappingAdd, OStrings.getString("RMD_BTN_ADD"));
        btnMappingAdd.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(btnMappingAdd, gridBagConstraints);

        jPanel7.add(jPanel8, BorderLayout.CENTER);

        jPanel4.add(jPanel7, BorderLayout.EAST);

        add(jPanel4);
   }

    JButton btnMappingAdd;
    JButton btnMappingRemove;
    JButton btnRepoAdd;
    JButton btnRepoRemove;
    JScrollPane repositoriesScrollPane;
    JScrollPane mappingScrollPane;
    JTable tableMapping;
    JTable tableRepositories;
}
