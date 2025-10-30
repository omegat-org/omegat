/**************************************************************************
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
 * **************************************************************************/
package org.omegat.gui.project.step;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.project.ProjectConfigMode;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.TableColumnSizer;
import org.openide.awt.Mnemonics;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Wizard step for configuring project repositories mapping (Team project).
 * Replicates the behavior of the legacy RepositoriesMappingController dialog
 * in an embedded panel suitable for the wizard.
 */
public class RepositoriesMappingStep implements Step {

    private final ProjectConfigMode mode;

    private final JPanel panel = new JPanel(new BorderLayout());

    private final JTable tableRepositories = new JTable();
    private final JTable tableMapping = new JTable();

    private final JButton btnRepoAdd = new JButton();
    private final JButton btnRepoRemove = new JButton();
    private final JButton btnMappingAdd = new JButton();
    private final JButton btnMappingRemove = new JButton();

    private final List<RowRepo> listRepo = new ArrayList<>();
    private final List<RowMapping> listMapping = new ArrayList<>();

    private AbstractTableModel modelRepo;
    private AbstractTableModel modelMapping;

    public RepositoriesMappingStep(ProjectConfigMode mode) {
        this.mode = mode;
        buildUI();
    }

    @Override
    public String getTitle() {
        return OStrings.getString("RMD_TITLE");
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void onLoad(ProjectProperties p) {
        // Reset data
        listRepo.clear();
        listMapping.clear();
        putData(p.getRepositories());
        initTableModels();
        updateState();
        if (mode == ProjectConfigMode.RESOLVE_DIRS) {
            setEnabledRecursive(panel, false);
        }
    }

    @Override
    public @Nullable String validateInput() {
        return isValid();
    }

    @Override
    public void onSave(ProjectProperties p) {
        String err = isValid();
        if (err == null) {
            p.setRepositories(getData());
        }
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Repositories section
        JPanel reposPanel = new JPanel(new BorderLayout());
        reposPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel lblRepos = new JLabel();
        Mnemonics.setLocalizedText(lblRepos, OStrings.getString("RMD_TABLE_REPOSITORIES"));
        reposPanel.add(lblRepos, BorderLayout.NORTH);

        tableRepositories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableRepositories.setFillsViewportHeight(true);
        reposPanel.add(new JScrollPane(tableRepositories), BorderLayout.CENTER);

        JPanel repoBtnsWrap = new JPanel(new BorderLayout());
        JPanel repoBtns = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(3, 3, 3, 3);

        Mnemonics.setLocalizedText(btnRepoAdd, OStrings.getString("RMD_BTN_ADD"));
        gbc.gridy = 1;
        repoBtns.add(btnRepoAdd, gbc);
        Mnemonics.setLocalizedText(btnRepoRemove, OStrings.getString("RMD_BTN_REMOVE"));
        btnRepoRemove.setEnabled(false);
        gbc.gridy = 2;
        repoBtns.add(btnRepoRemove, gbc);
        repoBtnsWrap.add(repoBtns, BorderLayout.CENTER);
        reposPanel.add(repoBtnsWrap, BorderLayout.EAST);

        content.add(reposPanel, BorderLayout.NORTH);

        // Bottom: Mapping section
        JPanel mappingPanel = new JPanel(new BorderLayout());
        JLabel lblMap = new JLabel();
        Mnemonics.setLocalizedText(lblMap, OStrings.getString("RMD_TABLE_MAPPING"));
        mappingPanel.add(lblMap, BorderLayout.NORTH);
        tableMapping.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMapping.setFillsViewportHeight(true);
        mappingPanel.add(new JScrollPane(tableMapping), BorderLayout.CENTER);

        JPanel mapBtnsWrap = new JPanel(new BorderLayout());
        JPanel mapBtns = new JPanel(new GridBagLayout());
        gbc.gridy = 6;
        Mnemonics.setLocalizedText(btnMappingAdd, OStrings.getString("RMD_BTN_ADD"));
        btnMappingAdd.setEnabled(false);
        mapBtns.add(btnMappingAdd, gbc);
        gbc.gridy = 7;
        Mnemonics.setLocalizedText(btnMappingRemove, OStrings.getString("RMD_BTN_REMOVE"));
        btnMappingRemove.setEnabled(false);
        mapBtns.add(btnMappingRemove, gbc);
        mapBtnsWrap.add(mapBtns, BorderLayout.CENTER);
        mappingPanel.add(mapBtnsWrap, BorderLayout.EAST);

        content.add(mappingPanel, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        // Wire buttons and selections
        btnRepoAdd.addActionListener(e -> {
            listRepo.add(new RowRepo());
            int row = listRepo.size() - 1;
            modelRepo.fireTableRowsInserted(row, row);
            tableRepositories.setRowSelectionInterval(row, row);
            reinitRepoUrlDropdown();
        });
        btnRepoRemove.addActionListener(e -> {
            int r = tableRepositories.getSelectedRow();
            if (r >= 0) {
                String old = listRepo.get(r).url;
                listRepo.remove(r);
                modelRepo.fireTableRowsDeleted(r, r);
                // Clean mappings pointing to removed repo
                listMapping.removeIf(m -> java.util.Objects.equals(old, m.repoUrl));
                modelMapping.fireTableDataChanged();
                reinitRepoUrlDropdown();
            }
        });
        btnMappingAdd.addActionListener(e -> {
            RowMapping mapping = new RowMapping();
            if (tableRepositories.getRowCount() == 1) {
                Object v = tableRepositories.getValueAt(0, 1);
                mapping.repoUrl = v == null ? null : v.toString();
            }
            listMapping.add(mapping);
            int row = listMapping.size() - 1;
            modelMapping.fireTableRowsInserted(row, row);
            tableMapping.setRowSelectionInterval(row, row);
        });
        btnMappingRemove.addActionListener(e -> {
            int r = tableMapping.getSelectedRow();
            if (r >= 0) {
                listMapping.remove(r);
                modelMapping.fireTableRowsDeleted(r, r);
            }
        });

        tableRepositories.getSelectionModel().addListSelectionListener(e -> updateState());
        tableMapping.getSelectionModel().addListSelectionListener(e -> updateState());
    }

    private void initTableModels() {
        modelRepo = new AbstractTableModel() {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (rowIndex >= listRepo.size()) {
                    return null;
                }
                RowRepo r = listRepo.get(rowIndex);
                switch (columnIndex) {
                case 0:
                    return r.type == null ? null : r.type.getLocalizedString();
                case 1:
                    return r.url;
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                RowRepo r = listRepo.get(rowIndex);
                switch (columnIndex) {
                case 0:
                    if (aValue instanceof RepoType) {
                        r.type = (RepoType) aValue;
                    } else if (aValue != null) {
                        r.type = RepoType.fromLocalized(aValue.toString());
                    }
                    break;
                case 1:
                    String old = r.url;
                    r.url = (String) aValue;
                    changeRepoUrl(old, r.url);
                    reinitRepoUrlDropdown();
                    break;
                }
            }

            @Override
            public int getRowCount() {
                return listRepo.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                case 0:
                    return OStrings.getString("RMD_TABLE_REPOSITORIES_TYPE");
                case 1:
                    return OStrings.getString("RMD_TABLE_REPOSITORIES_URL");
                }
                return null;
            }
        };
        tableRepositories.setModel(modelRepo);
        // Provide a combo renderer/editor for types
        javax.swing.JComboBox<RepoType> typeCombo = new javax.swing.JComboBox<>(RepoType.values());
        typeCombo.setRenderer(new DelegatingComboBoxRenderer<RepoType, String>() {
            @Override
            protected String getDisplayText(RepoType value) {
                return value == null ? "" : value.getLocalizedString();
            }
        });
        TableColumnModel rcm = tableRepositories.getColumnModel();
        rcm.getColumn(0).setCellEditor(new javax.swing.DefaultCellEditor(typeCombo));
        rcm.getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Object display = value;
                if (value instanceof RepoType) {
                    display = ((RepoType) value).getLocalizedString();
                }
                return super.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
            }
        });
        TableColumnSizer.autoSize(tableRepositories, 1, true);

        modelMapping = new AbstractTableModel() {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (rowIndex >= listMapping.size()) {
                    return null;
                }
                RowMapping r = listMapping.get(rowIndex);
                switch (columnIndex) {
                case 0:
                    return r.repoUrl;
                case 1:
                    return r.local;
                case 2:
                    return r.remote;
                case 3:
                    return r.excludes;
                case 4:
                    return r.includes;
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                RowMapping r = listMapping.get(rowIndex);
                switch (columnIndex) {
                case 0:
                    r.repoUrl = (String) aValue;
                    break;
                case 1:
                    r.local = (String) aValue;
                    break;
                case 2:
                    r.remote = (String) aValue;
                    break;
                case 3:
                    r.excludes = (String) aValue;
                    break;
                case 4:
                    r.includes = (String) aValue;
                    break;
                }
            }

            @Override
            public int getRowCount() {
                return listMapping.size();
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                case 0:
                    return OStrings.getString("RMD_TABLE_MAPPING_REPO");
                case 1:
                    return OStrings.getString("RMD_TABLE_MAPPING_LOCAL");
                case 2:
                    return OStrings.getString("RMD_TABLE_MAPPING_REMOTE");
                case 3:
                    return OStrings.getString("RMD_TABLE_MAPPING_EXCLUDES");
                case 4:
                    return OStrings.getString("RMD_TABLE_MAPPING_INCLUDES");
                }
                return null;
            }
        };
        tableMapping.setModel(modelMapping);
        reinitRepoUrlDropdown();
        TableColumnSizer.autoSize(tableMapping, 0, true);
    }

    private void updateState() {
        btnRepoRemove.setEnabled(tableRepositories.getSelectedRow() != -1);
        btnMappingRemove.setEnabled(tableMapping.getSelectedRow() != -1);
        btnMappingAdd.setEnabled(tableRepositories.getRowCount() > 0);
    }

    private void changeRepoUrl(String oldUrl, String newUrl) {
        if (oldUrl == null) {
            return;
        }
        for (int i = 0; i < listMapping.size(); i++) {
            if (oldUrl.equals(listMapping.get(i).repoUrl)) {
                listMapping.get(i).repoUrl = newUrl;
                modelMapping.fireTableCellUpdated(i, 0);
            }
        }
    }

    private void reinitRepoUrlDropdown() {
        javax.swing.JComboBox<String> comboBox = new javax.swing.JComboBox<>();
        for (RowRepo r : listRepo) {
            comboBox.addItem(r.url);
        }
        tableMapping.getColumnModel().getColumn(0).setCellEditor(new javax.swing.DefaultCellEditor(comboBox));
    }

    private String isValid() {
        Set<String> urls = new TreeSet<>();
        for (RowRepo r : listRepo) {
            if (StringUtil.isEmpty(r.url)) {
                return OStrings.getString("RMD_INVALID_BLANK_REPO");
            }
            if (r.type == null) {
                return MessageFormat.format(OStrings.getString("RMD_INVALID_REPO_TYPE"), r.type);
            }
            if (!urls.add(r.url)) {
                return MessageFormat.format(OStrings.getString("RMD_INVALID_DUPLICATE_REPO"), r.url);
            }
        }
        for (RowMapping r : listMapping) {
            if (StringUtil.isEmpty(r.repoUrl)) {
                return OStrings.getString("RMD_INVALID_BLANK_REPO");
            }
            if (!urls.contains(r.repoUrl)) {
                return MessageFormat.format(OStrings.getString("RMD_INVALID_UNKNOWN_REPO"), r.repoUrl);
            }
        }
        return null;
    }

    private void putData(List<RepositoryDefinition> data) {
        if (data == null) {
            return;
        }
        for (RepositoryDefinition rd : data) {
            RowRepo r = new RowRepo();
            r.type = RepoType.fromTypeId(rd.getType());
            r.url = rd.getUrl();
            listRepo.add(r);
            for (RepositoryMapping rm : rd.getMapping()) {
                RowMapping m = new RowMapping();
                m.repoUrl = rd.getUrl();
                m.local = normalizeMapping(rm.getLocal());
                m.remote = normalizeMapping(rm.getRepository());
                m.excludes = String.join(";", rm.getExcludes());
                m.includes = String.join(";", rm.getIncludes());
                listMapping.add(m);
            }
        }
    }

    private List<RepositoryDefinition> getData() {
        List<RepositoryDefinition> result = new ArrayList<>();
        for (RowRepo r : listRepo) {
            RepositoryDefinition rd = new RepositoryDefinition();
            rd.setType(r.type.toTypeId());
            rd.setUrl(r.url);
            for (RowMapping m : listMapping) {
                if (java.util.Objects.equals(r.url, m.repoUrl)) {
                    RepositoryMapping rm = new RepositoryMapping();
                    rm.setLocal(normalizeMapping(m.local));
                    rm.setRepository(normalizeMapping(m.remote));
                    if (!StringUtil.isEmpty(m.excludes)) {
                        rm.getExcludes().addAll(Arrays.asList(m.excludes.trim().split(";")));
                    }
                    if (!StringUtil.isEmpty(m.includes)) {
                        rm.getIncludes().addAll(Arrays.asList(m.includes.trim().split(";")));
                    }
                    rd.getMapping().add(rm);
                }
            }
            result.add(rd);
        }
        return result;
    }

    private String normalizeMapping(String mapping) {
        return StringUtil.isEmpty(mapping) ? "/" : mapping;
    }

    private static void setEnabledRecursive(JComponent comp, boolean enabled) {
        comp.setEnabled(enabled);
        for (java.awt.Component c : comp.getComponents()) {
            if (c instanceof JComponent) {
                setEnabledRecursive((JComponent) c, enabled);
            } else {
                c.setEnabled(enabled);
            }
        }
    }

    private enum RepoType {
        GIT, SVN, HTTP, FILE;

        String getLocalizedString() {
            return OStrings.getString("RMD_TABLE_REPO_TYPE_" + name());
        }

        String toTypeId() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        static RepoType fromTypeId(String id) {
            if (id == null) {
                return null;
            }
            return RepoType.valueOf(id.toUpperCase(Locale.ENGLISH));
        }

        static RepoType fromLocalized(String text) {
            for (RepoType t : values()) {
                if (t.getLocalizedString().equals(text)) {
                    return t;
                }
            }
            return null;
        }
    }

    private static class RowRepo {
        RepoType type;
        String url;
    }

    private static class RowMapping {
        String repoUrl;
        String local = "/";
        String remote = "/";
        String excludes;
        String includes;
    }
}
