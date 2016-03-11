/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichick
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

package org.omegat.core.team2.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import org.omegat.core.Core;
import org.omegat.core.team2.TeamSettings;
import org.omegat.util.gui.DockingUI;

/**
 * Controller for forget credentials.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RepositoriesCredentialsController {
    private static final String PREFIX = "login.username.";

    public static void show() {
        Set<String> urls = new TreeSet<String>();
        for (String key : TeamSettings.listKeys()) {
            int p = key.lastIndexOf('!');
            if (p > 0) {
                urls.add(key.substring(0, p));
            }
        }

        final RepositoriesCredentialsDialog dialog = new RepositoriesCredentialsDialog(
                Core.getMainWindow().getApplicationFrame(), true);

        dialog.list.setModel(new Model(urls));
        dialog.list.getTableHeader().setVisible(false);
        dialog.btnRemove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = dialog.list.getSelectedRow();
                if (selectedIndex < 0) {
                    return;
                }
                String selected = ((Model) dialog.list.getModel()).lines.get(selectedIndex);
                for (String key : TeamSettings.listKeys()) {
                    if (key.startsWith(selected + "!")) {
                        TeamSettings.set(key, null);
                    }
                }
                ((Model) dialog.list.getModel()).lines.remove(selected);
                ((Model) dialog.list.getModel()).fireTableDataChanged();
            }
        });
        dialog.btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        DockingUI.displayCentered(dialog);
        dialog.setVisible(true);
    }

    static class Model extends AbstractTableModel {
        List<String> lines;

        public Model(Set<String> urls) {
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
