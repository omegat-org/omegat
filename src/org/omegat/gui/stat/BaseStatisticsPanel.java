/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.stat;

import java.awt.Font;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.omegat.core.Core;
import org.omegat.core.statistics.IStatsConsumer;
import org.omegat.util.Preferences;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.TableColumnSizer;

/**
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public abstract class BaseStatisticsPanel extends JPanel implements IStatsConsumer {

    private final StatisticsWindow window;

    public BaseStatisticsPanel(StatisticsWindow window) {
        this.window = window;
    }

    @Override
    public void showProgress(int percent) {
        window.showProgress(percent);
    }

    @Override
    public void finishData() {
        window.finishData();
    }

    @Override
    public void setTextData(String data) {
        window.setTextData(data);
    }

    @Override
    public void setDataFile(String path) {
        File file = new File(path);
        OSXIntegration.setProxyIcon(getRootPane(), file.isFile() ? file : null);
    }

    static class StringArrayTableModel extends AbstractTableModel {

        private final String[][] data;

        StringArrayTableModel(String[][] data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.length;
        }

        @Override
        public int getColumnCount() {
            return data == null || data.length == 0 ? 0
                    : data[0] == null ? 0 : data[0].length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }
    }

    protected TitledTablePanel generateTableDisplay(String title, String[] headers, String[][] data) {
        TitledTablePanel panel = new TitledTablePanel();

        DataTableStyling.applyColors(panel.table);
        panel.table.setDefaultRenderer(Object.class, DataTableStyling.getNumberCellRenderer());
        Font font = panel.table.getFont();
        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            font = Core.getMainWindow().getApplicationFont();
        }
        DataTableStyling.applyFont(panel.table, font);

        panel.title.setText(title);
        panel.table.setModel(new StringArrayTableModel(data));
        setTableHeaders(panel.table, headers);
        panel.table.getColumnModel().getColumn(0).setCellRenderer(
                DataTableStyling.getHeaderTextCellRenderer());
        TableColumnSizer.autoSize(panel.table, 0, false);
        panel.table.setPreferredScrollableViewportSize(panel.table.getPreferredSize());
        return panel;
    }

    protected static void setTableHeaders(JTable table, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setHeaderValue(headers[i]);
        }
    }
}
