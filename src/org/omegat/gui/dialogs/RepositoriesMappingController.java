/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Controller for repositories mapping UI.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RepositoriesMappingController {

    /**
     * Enum of supported repository types corresponding to
     * org.omegat.core.team2.impl.*RemoteRepository*
     */
    enum RepoType {
        GIT, SVN, HTTP;

        public String getLocalizedString() {
            return OStrings.getString("RMD_TABLE_REPO_TYPE_" + name());
        }
    }

    private List<RepositoryDefinition> result;

    private RepositoriesMappingDialog dialog;
    private AbstractTableModel modelRepo;
    private List<RowRepo> listRepo;
    private AbstractTableModel modelMapping;
    private List<RowMapping> listMapping;

    private TableColumnSizer repoSizer;
    private TableColumnSizer mappingSizer;

    public List<RepositoryDefinition> show(Frame parent, List<RepositoryDefinition> input) {
        dialog = new RepositoriesMappingDialog(parent, true);
        dialog.setLocationRelativeTo(parent);

        dialog.getRootPane().setDefaultButton(dialog.okButton);

        StaticUIUtils.setEscapeClosable(dialog);

        listRepo = new ArrayList<RowRepo>();
        listMapping = new ArrayList<RowMapping>();
        putData(input);

        initTableModels();
        initButtons();
        reinitRepoUrlDropdown();

        dialog.tableRepositories.getSelectionModel().addListSelectionListener(e -> updateState());
        dialog.tableMapping.getSelectionModel().addListSelectionListener(e -> updateState());
        dialog.tableRepositories.getModel().addTableModelListener(e -> updateState());

        repoSizer = TableColumnSizer.autoSize(dialog.tableRepositories, 1, true);
        mappingSizer = TableColumnSizer.autoSize(dialog.tableMapping, 0, true);

        updateState();

        dialog.setVisible(true);
        return result;
    }

    private void updateState() {
        dialog.btnRepoRemove.setEnabled(dialog.tableRepositories.getSelectedRow() != -1);
        dialog.btnMappingRemove.setEnabled(dialog.tableMapping.getSelectedRow() != -1);
        dialog.btnMappingAdd.setEnabled(dialog.tableRepositories.getRowCount() > 0);
    }

    @SuppressWarnings("serial")
    void initTableModels() {

        modelRepo = new AbstractTableModel() {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
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
                    r.type = (RepoType) aValue;
                    break;
                case 1:
                    String old = r.url;
                    r.url = (String) aValue;
                    changeRepoUrl(old, r.url);
                    reinitRepoUrlDropdown();
                    break;
                }
                repoSizer.reset();
                repoSizer.adjustTableColumns();
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
        dialog.tableRepositories.setModel(modelRepo);

        JComboBox<RepoType> comboBox = new JComboBox<>(RepoType.values());
        comboBox.setRenderer(new DelegatingComboBoxRenderer<RepoType, String>() {
            @Override
            protected String getDisplayText(RepoType value) {
                return value == null ? "" : value.getLocalizedString();
            }
        });
        dialog.tableRepositories.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBox));

        modelMapping = new AbstractTableModel() {

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
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
                mappingSizer.reset();
                mappingSizer.adjustTableColumns();
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
        dialog.tableMapping.setModel(modelMapping);
    }

    void initButtons() {
        dialog.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean doneEditing = Stream.of(dialog.tableMapping, dialog.tableRepositories)
                        .map(JTable::getCellEditor).allMatch(editor -> editor == null || editor.stopCellEditing());
                if (!doneEditing) {
                    return;
                }
                String r = isValid();
                if (r != null) {
                    JOptionPane.showMessageDialog(dialog, r, OStrings.getString("TF_ERROR"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                result = getData();
                dialog.dispose();
            }
        });
        dialog.btnRepoAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listRepo.add(new RowRepo());
                int row = listRepo.size() - 1;
                modelRepo.fireTableRowsInserted(row, row);
                dialog.tableRepositories.setRowSelectionInterval(row, row);
            }
        });
        dialog.btnRepoRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = dialog.tableRepositories.getSelectedRow();
                if (r >= 0) {
                    listRepo.remove(r);
                    modelRepo.fireTableRowsDeleted(r, r);
                }
            }
        });
        dialog.btnMappingAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RowMapping mapping = new RowMapping();
                if (dialog.tableRepositories.getRowCount() == 1) {
                    mapping.repoUrl = (String) dialog.tableRepositories.getValueAt(0, 1);
                }
                listMapping.add(mapping);
                int row = listMapping.size() - 1;
                modelMapping.fireTableRowsInserted(row, row);
                dialog.tableMapping.setRowSelectionInterval(row, row);
            }
        });
        dialog.btnMappingRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = dialog.tableMapping.getSelectedRow();
                if (r >= 0) {
                    listMapping.remove(r);
                    modelMapping.fireTableRowsDeleted(r, r);
                }
            }
        });
    }

    /**
     * User changed repo URL - need to change in all mappings.
     */
    void changeRepoUrl(String oldUrl, String newUrl) {
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

    void reinitRepoUrlDropdown() {
        JComboBox<String> comboBox = new JComboBox<>();
        for (RowRepo r : listRepo) {
            comboBox.addItem(r.url);
        }
        dialog.tableMapping.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBox));
    }

    /**
     * Check if data is valid. Returns null if all valid.
     */
    String isValid() {
        Set<String> urls = new TreeSet<String>();
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

    void putData(List<RepositoryDefinition> data) {
        if (data == null) {
            return;
        }
        for (RepositoryDefinition rd : data) {
            RowRepo r = new RowRepo();
            r.type = RepoType.valueOf(rd.getType().toUpperCase(Locale.ENGLISH));
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

    List<RepositoryDefinition> getData() {
        List<RepositoryDefinition> result = new ArrayList<RepositoryDefinition>();
        for (RowRepo r : listRepo) {
            RepositoryDefinition rd = new RepositoryDefinition();
            rd.setType(r.type.name().toLowerCase(Locale.ENGLISH));
            rd.setUrl(r.url);
            result.add(rd);
            for (RowMapping m : listMapping) {
                if (r.url.equals(m.repoUrl)) {
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
        }
        return result;
    }

    String normalizeMapping(String mapping) {
        return StringUtil.isEmpty(mapping) ? "/" : mapping;
    }

    static class RowRepo {
        public RepoType type;
        public String url;
    }

    static class RowMapping {
        public String repoUrl;
        public String local = "/";
        public String remote = "/";
        public String excludes;
        public String includes;
    }
}
