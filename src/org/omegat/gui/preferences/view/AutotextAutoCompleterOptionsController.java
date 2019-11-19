/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences.view;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.omegat.gui.editor.autotext.Autotext;
import org.omegat.gui.editor.autotext.Autotext.AutotextItem;
import org.omegat.gui.editor.autotext.AutotextTableModel;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public class AutotextAutoCompleterOptionsController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    private final JFileChooser fc = new JFileChooser();
    private AutotextAutoCompleterOptionsPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_AUTOCOMPLETER_AUTOTEXT");
    }

    private void initGui() {
        panel = new AutotextAutoCompleterOptionsPanel();

        fc.setDialogType(JFileChooser.FILES_ONLY);
        fc.addChoosableFileFilter(new FileNameExtensionFilter(OStrings.getString("AC_AUTOTEXT_FILE"), "autotext"));
        panel.entryTable.setModel(new AutotextTableModel(Autotext.getItems()));
        panel.saveButton.addActionListener(e -> saveFile());
        panel.loadButton.addActionListener(e -> loadFile());

        TableColumnModel cModel = panel.entryTable.getColumnModel();
        cModel.getColumn(0).setHeaderValue(OStrings.getString("AC_AUTOTEXT_ABBREVIATION")); // NOI18N
        cModel.getColumn(1).setHeaderValue(OStrings.getString("AC_AUTOTEXT_TEXT")); // NOI18N
        cModel.getColumn(2).setHeaderValue(OStrings.getString("AC_AUTOTEXT_COMMENT")); // NOI18N

        panel.sortAlphabeticallyCheckBox.addActionListener(
                e -> panel.sortFullTextCheckBox.setEnabled(panel.sortAlphabeticallyCheckBox.isSelected()));
        panel.addNewRowButton.addActionListener(e -> {
            int newRow = ((AutotextTableModel) panel.entryTable.getModel()).addRow(new AutotextItem(),
                    panel.entryTable.getSelectedRow());
            panel.entryTable.changeSelection(newRow, 0, false, false);
            panel.entryTable.changeSelection(newRow, panel.entryTable.getColumnCount() - 1, false, true);
            panel.entryTable.editCellAt(newRow, 0);
            panel.entryTable.transferFocus();
        });
        panel.removeEntryButton.addActionListener(e -> {
            if (panel.entryTable.getSelectedRow() != -1) {
                ((AutotextTableModel) panel.entryTable.getModel()).removeRow(panel.entryTable.getSelectedRow());
            }
        });
        panel.enabledCheckBox.addActionListener(e -> updateEnabledness());
        Dimension tableSize = panel.entryTable.getPreferredSize();
        panel.entryTable.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, panel.entryTable.getRowHeight() * MAX_ROW_COUNT));

    }

    private void updateEnabledness() {
        panel.sortFullTextCheckBox.setEnabled(panel.sortAlphabeticallyCheckBox.isSelected());
        StaticUIUtils.setHierarchyEnabled(panel.displayPanel, panel.enabledCheckBox.isSelected());
        StaticUIUtils.setHierarchyEnabled(panel.entriesPanel, panel.enabledCheckBox.isSelected());
    }

    private void loadFile() {
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(panel)) {
            try {
                List<AutotextItem> data = Autotext.load(fc.getSelectedFile());
                panel.entryTable.setModel(new AutotextTableModel(data));
            } catch (IOException ex) {
                Logger.getLogger(AutotextAutoCompleterOptionsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveFile() {
        if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(panel)) {
            try {
                List<AutotextItem> data = ((AutotextTableModel) panel.entryTable.getModel()).getData();
                Autotext.save(data, fc.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(panel, OStrings.getString("AC_AUTOTEXT_UNABLE_TO_SAVE"));
            }
        }

    }

    @Override
    protected void initFromPrefs() {
        panel.sortByLengthCheckBox.setSelected(Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_BY_LENGTH));
        panel.sortAlphabeticallyCheckBox
                .setSelected(Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_ALPHABETICALLY));
        panel.sortFullTextCheckBox.setSelected(Preferences.isPreference(Preferences.AC_AUTOTEXT_SORT_FULL_TEXT));
        panel.enabledCheckBox.setSelected(Preferences.isPreferenceDefault(Preferences.AC_AUTOTEXT_ENABLED,
                Preferences.AC_AUTOTEXT_ENABLED_DEFAULT));

        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        panel.sortByLengthCheckBox.setSelected(false);
        panel.sortAlphabeticallyCheckBox.setSelected(false);
        panel.sortFullTextCheckBox.setSelected(false);
        panel.enabledCheckBox.setSelected(Preferences.AC_AUTOTEXT_ENABLED_DEFAULT);

        updateEnabledness();
    }

    @Override
    public void persist() {
        TableCellEditor editor = panel.entryTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }

        Autotext.setList(((AutotextTableModel) panel.entryTable.getModel()).getData());

        try {
            Autotext.save();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(panel, OStrings.getString("AC_AUTOTEXT_UNABLE_TO_SAVE"));
        }

        Preferences.setPreference(Preferences.AC_AUTOTEXT_SORT_BY_LENGTH, panel.sortByLengthCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_AUTOTEXT_SORT_ALPHABETICALLY,
                panel.sortAlphabeticallyCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_AUTOTEXT_SORT_FULL_TEXT, panel.sortFullTextCheckBox.isSelected());
        Preferences.setPreference(Preferences.AC_AUTOTEXT_ENABLED, panel.enabledCheckBox.isSelected());
    }
}
