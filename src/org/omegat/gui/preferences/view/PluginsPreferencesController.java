/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2022,2023 Hiroshi Miura
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

import java.awt.Dimension;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.gui.dialogs.ChoosePluginFile;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.PluginInstaller;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.DesktopWrapper;
import org.omegat.util.gui.TableColumnSizer;

/**
 * Controller for the Plugins preferences panel.
 *
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class PluginsPreferencesController extends BasePreferencesController {

    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";
    private PluginsPreferencesPanel panel;
    private PluginDetailsPane pluginDetailsPane;
    private PluginInfoTableModel model;

    /**
     * Format plugin information for details pane of UI.
     * @param info PluginInformation to show
     * @return HTML text
     */
    private static String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(info.getName()).append("</h1>");
        sb.append("<h4>Author: ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>");
        } else {
            sb.append("Unknown<br/>");
        }
        if (info.getVersion() != null) {
            sb.append("Version: ").append(info.getVersion()).append("<br/>");
        }
        sb.append(info.getCategory());
        sb.append("</h4>");
        if (info.getDescription() != null) {
            sb.append("<p>").append(info.getDescription()).append("</p>");
        }
        if (info.getLink() != null) {
            sb.append("<br/><div><a href=\"").append(info.getLink()).append("\">Plugin homepage</a></div>");
        }
        return sb.toString();
    }

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    final void selectRowAction(ListSelectionEvent evt) {
        int rowIndex = panel.tablePluginsInfo.getSelectedRow();
        if (rowIndex == -1) {
            pluginDetailsPane.setText("");
        } else {
            int index = panel.tablePluginsInfo.convertRowIndexToModel(rowIndex);
            PluginInfoTableModel model = (PluginInfoTableModel) panel.tablePluginsInfo.getModel();
            pluginDetailsPane.setText(formatDetailText(model.getItemAt(index)));
        }
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_PLUGINS");
    }

    private void initGui() {
        panel = new PluginsPreferencesPanel();
        model = new PluginInfoTableModel();
        panel.tablePluginsInfo.setModel(model);
        TableRowSorter<PluginInfoTableModel> sorter = new TableRowSorter<>(model);
        panel.tablePluginsInfo.setRowSorter(sorter);
        panel.tablePluginsInfo.getColumnModel().getColumn(PluginInfoTableModel.COLUMN_NAME).setPreferredWidth(100);
        panel.tablePluginsInfo.getColumnModel().getColumn(PluginInfoTableModel.COLUMN_VERSION).setPreferredWidth(50);

        panel.scrollTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.scrollTable.getViewport().setViewSize(new Dimension(250, 350));

        panel.tablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowAction);
        panel.tablePluginsInfo.setPreferredScrollableViewportSize(panel.tablePluginsInfo.getPreferredSize());
        DataTableStyling.applyFont(panel.tablePluginsInfo, Core.getMainWindow().getApplicationFont());
        TableColumnSizer.autoSize(panel.tablePluginsInfo, 0, true);

        pluginDetailsPane = new PluginDetailsPane();
        panel.panelPluginDetails.add(pluginDetailsPane);

        panel.browsePluginsButton.addActionListener(e -> {
            try {
                DesktopWrapper.browse(URI.create(PLUGINS_WIKI_URL));
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(panel, ex.getLocalizedMessage(), OStrings.getString("ERROR_TITLE"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.installFromDiskButton.addActionListener(e -> {
            ChoosePluginFile choosePluginFile = new ChoosePluginFile();
            if (JFileChooser.APPROVE_OPTION == choosePluginFile.showOpenDialog(
                    Core.getMainWindow().getApplicationFrame())) {
                if (PluginInstaller.getInstance().install(choosePluginFile.getSelectedFile())) {
                    setRestartRequired(true);
                }
            }
        });

        panel.showBundledPluginsCB.addActionListener(e -> {
            boolean showBundledPlugins = panel.showBundledPluginsCB.isSelected();
            model.updateModel(showBundledPlugins);
            panel.tablePluginsInfo.setRowSorter(new TableRowSorter<>(model));
        });
    }

    @Override
    protected void initFromPrefs() {
        panel.tablePluginsInfo.changeSelection(0, 0, false, false);
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    public void persist() {
    }

    @Override
    public boolean canRestoreDefaults() {
        return false;
    }
}
