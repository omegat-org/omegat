/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichick, Aaron Madlon-Kay
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

package org.omegat.core.team2.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;

import org.omegat.core.team2.TeamSettings;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;

/**
 * Controller for forget credentials.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class RepositoriesCredentialsController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    private RepositoriesCredentialsPanel dialog;

    @Override
    public JComponent getGui() {
        if (dialog == null) {
            initGui();
            initFromPrefs();
        }
        return dialog;
    }

    @Override
    public String toString() {
        return OStrings.getString("TEAM_REPOSITORIES_DIALOG");
    }

    private void initGui() {
        dialog = new RepositoriesCredentialsPanel();
        dialog.list.getSelectionModel()
                .addListSelectionListener(e -> dialog.btnRemove.setEnabled(dialog.list.getSelectedRow() != -1));
        dialog.btnRemove.addActionListener(e -> removeSelected());
        Dimension tableSize = dialog.list.getPreferredSize();
        dialog.list.setPreferredScrollableViewportSize(
                new Dimension(tableSize.width, dialog.list.getRowHeight() * MAX_ROW_COUNT));
    }

    @Override
    public void initFromPrefs() {
        Set<String> urls = new TreeSet<>();
        for (Object o : TeamSettings.listKeys()) {
            String key = o.toString();
            int p = key.lastIndexOf('!');
            if (p > 0) {
                urls.add(key.substring(0, p));
            }
        }
        dialog.list.setModel(new Model(urls));
    }

    @Override
    public void restoreDefaults() {
    }

    private void removeSelected() {
        int selectedIndex = dialog.list.getSelectedRow();
        if (selectedIndex < 0) {
            return;
        }
        Model model = (Model) dialog.list.getModel();
        String selected = model.lines.get(selectedIndex);
        for (Object o : TeamSettings.listKeys()) {
            String key = o.toString();
            if (key.startsWith(selected + "!")) {
                TeamSettings.set(key, null);
            }
        }
        model.lines.remove(selected);
        model.fireTableDataChanged();
    }

    @Override
    public void persist() {
    }

    @SuppressWarnings("serial")
    static class Model extends AbstractTableModel {
        List<String> lines;

        Model(Set<String> urls) {
            lines = new ArrayList<String>(urls);
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return lines.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return lines.get(row);
        }
    }
}
