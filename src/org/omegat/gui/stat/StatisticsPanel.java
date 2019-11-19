/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.stat;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DataTableStyling;

/**
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class StatisticsPanel extends BaseStatisticsPanel {

    public StatisticsPanel(StatisticsWindow window) {
        super(window);
        setLayout(new BorderLayout());
    }

    @Override
    public void appendTable(String title, String[] headers, String[][] data) {
        // Nothing
    }

    @Override
    public void appendTextData(String result) {
        // Nothing
    }

    @Override
    public void setTable(String[] headers, String[][] data) {
        // Nothing
    }

    public void setProjectTableData(final String[] headers, final String[][] projectData) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (projectData == null || projectData.length == 0) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String title = OStrings.getString("CT_STATS_Project_Statistics");
                add(generateTableDisplay(title, headers, projectData), BorderLayout.NORTH);
            }
        });
    }

    public void setFilesTableData(final String[] headers, final String[][] filesData) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (filesData == null || filesData.length == 0) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String title = OStrings.getString("CT_STATS_FILE_Statistics");
            TitledTablePanel panel = generateTableDisplay(title, headers, filesData);

            TableModel dataModel = panel.table.getModel();

            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(dataModel);
            Comparator<String> intComparator = (s1, s2) -> {
                try {
                    return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2);
                }
            };
            for (int i = 0; i < dataModel.getColumnCount(); i++) {
                rowSorter.setComparator(i, intComparator);
            }

            panel.table.setRowSorter(rowSorter);

            panel.table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            int row = panel.table.rowAtPoint(e.getPoint());
                            int fileIndex = panel.table.convertRowIndexToModel(row);
                            gotoFile(fileIndex);
                        } catch (IndexOutOfBoundsException ex) {
                            // Ignore
                        }
                    }
                }
            });

            panel.table.getColumnModel().getColumn(0).setCellRenderer(DataTableStyling.getTextCellRenderer());
            add(panel, BorderLayout.CENTER);
        });
    }

    /** code mostly from from org.omegat.gui.filelist.ProjectFilesListController.gotoFile(int) */
    private void gotoFile(int fileIndex) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = getCursor();
        setCursor(hourglassCursor);
        try {
            Core.getEditor().gotoFile(fileIndex);
            Core.getEditor().requestFocus();
        } finally {
            setCursor(oldCursor);
        }
    }

}
