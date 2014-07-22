/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FilenamePatternsEditorController {
    static List<String> data, result;

    public static List<String> show(List<String> excludes) {
        result = null;
        data = new ArrayList<String>(excludes);
        final FilenamePatternsEditor dialog = new FilenamePatternsEditor(Core.getMainWindow()
                .getApplicationFrame(), true);

        final AbstractTableModel model = new AbstractTableModel() {
            public int getColumnCount() {
                return 1;
            }

            public String getColumnName(int column) {
                return OStrings.getString("FILENAMEPATTERNS_MASK");
            }

            public int getRowCount() {
                return data.size();
            }

            public Object getValueAt(int row, int col) {
                return data.get(row);
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                data.set(rowIndex, aValue.toString());
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }
        };
        dialog.table.setModel(model);

        dialog.btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                result = data;
                dialog.dispose();
            }
        });
        dialog.btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                data.add("");
                model.fireTableDataChanged();
            }
        });
        dialog.btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dialog.table.getSelectedRow() >= 0 && dialog.table.getSelectedRow() < data.size()) {
                    data.remove(dialog.table.getSelectedRow());
                }
                model.fireTableDataChanged();
            }
        });

        dialog.setVisible(true);

        return result;
    }
}
