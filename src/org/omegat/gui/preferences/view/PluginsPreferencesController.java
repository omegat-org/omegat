/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2021 Hiroshi Miura
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

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.core.plugins.PluginInstaller;
import org.omegat.gui.dialogs.ChoosePluginFile;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.gui.TableColumnSizer;


/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class PluginsPreferencesController extends BasePreferencesController {

    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";
    private PluginsPreferencesPanel panel;
    private PluginDetailsPane pluginDetailsPane;
    private PluginDetailHeader pluginDetailHeader;
    private PluginInstaller pluginInstaller;

    /**
     * Format plugin information for details pane of UI.
     * @param info PluginInformation to show
     * @return HTML text
     */
    private static String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h4>Author: ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>\n");
        } else {
            sb.append("Unknown<br/>\n");
        }
        if (info.getVersion() != null) {
            sb.append("Version: ").append(info.getVersion()).append("<br/>\n");
        }
        sb.append("</h4>\n");
        if (info.getDescription() != null) {
            sb.append("<p>").append(info.getDescription()).append("</p>\n");
        }
        if (info.getLink() != null) {
            sb.append("<br/><div><a href=\"").append(info.getLink()).append("\">Plugin homepage</a></div>");
        }
        return sb.toString();
    }

    @Override
    public JComponent getGui() {
        if (panel == null) {
            pluginInstaller = new PluginInstaller();
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    final void selectRowAction(ListSelectionEvent evt) {
        int rowIndex = panel.tablePluginsInfo.convertRowIndexToModel(
                panel.tablePluginsInfo.getSelectedRow());
        if (rowIndex == -1) {
            pluginDetailsPane.setText("");
        } else {
            PluginInfoTableModel model = (PluginInfoTableModel) panel.tablePluginsInfo.getModel();
            PluginInformation info = model.getValueAt(rowIndex);
            pluginDetailHeader.labelPluginName.setText(info.getName());
            pluginDetailHeader.labelCategory.setText(info.getCategory());
            for(ActionListener act : pluginDetailHeader.installButton.getActionListeners()) {
                pluginDetailHeader.installButton.removeActionListener(act);
            }
            pluginDetailHeader.installButton.setEnabled(true);
            if (info.getStatus().equals(PluginInformation.Status.UNINSTALLED)) {
                pluginDetailHeader.installButton.setText(OStrings.getString("PREFS_PLUGINS_INSTALL"));
            } else if (info.getStatus().equals(PluginInformation.Status.UPGRADABLE)) {
                pluginDetailHeader.installButton.setText(OStrings.getString("PREFS_PLUGINS_UPGRADE"));
            } else if (info.getStatus().equals(PluginInformation.Status.BUNDLED)) {
                pluginDetailHeader.installButton.setText(OStrings.getString("PREFS_PLUGINS_BUNDLED"));
                pluginDetailHeader.installButton.setEnabled(false);
            } else {
                pluginDetailHeader.installButton.setText(OStrings.getString("PREFS_PLUGINS_UPTODATE"));
                pluginDetailHeader.installButton.setEnabled(false);
            }
            pluginDetailHeader.installButton.addActionListener(e -> {
                pluginDetailHeader.installButton.setEnabled(false);
                pluginInstaller.installFromRemote(this, info);
            });
            StringBuilder detailTextBuilder = new StringBuilder(formatDetailText(model.getValueAt(rowIndex)));
            pluginDetailsPane.setText(detailTextBuilder.toString());
        }
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_PLUGINS");
    }

    private void initGui() {
        panel = new PluginsPreferencesPanel();
        panel.tablePluginsInfo.setAutoCreateRowSorter(true);
        panel.tablePluginsInfo.getColumnModel().getColumn(PluginInfoTableModel.COLUMN_NAME).setPreferredWidth(100);
        panel.tablePluginsInfo.getColumnModel().getColumn(PluginInfoTableModel.COLUMN_VERSION).setPreferredWidth(50);
        pluginDetailHeader = new PluginDetailHeader();
        pluginDetailsPane = new PluginDetailsPane();
        panel.panelPluginDetails.add(pluginDetailHeader);
        panel.panelPluginDetails.add(pluginDetailsPane);
        panel.scrollTable.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        panel.scrollTable.getViewport().setViewSize(new Dimension(250, 350));

        PluginInfoTableModel model = (PluginInfoTableModel) panel.tablePluginsInfo.getModel();
        TableRowSorter<PluginInfoTableModel> sorter = new TableRowSorter<>(model);
        panel.tablePluginsInfo.setRowSorter(sorter);
        panel.tablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowAction);
        panel.tablePluginsInfo.setPreferredScrollableViewportSize(panel.tablePluginsInfo.getPreferredSize());
        TableColumnSizer.autoSize(panel.tablePluginsInfo, 0, true);

        panel.installPluginsButton.setText(OStrings.getString("PREFS_PLUGINS_INSTALL_FROM_DISK"));
        panel.installPluginsButton.addActionListener(e -> {
            ChoosePluginFile choosePluginFile = new ChoosePluginFile();
            int choosePluginFileResult = choosePluginFile.showOpenDialog(Core.getMainWindow().getApplicationFrame());
            if (choosePluginFileResult == JFileChooser.APPROVE_OPTION) {
                if (pluginInstaller.install(choosePluginFile.getSelectedFile(), false)) {
                    setRestartRequired(true);
                } else {
                    JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                            OStrings.getString("PREFS_PLUGINS_INSTALLATION_FAILED"),
                            OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                            JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    @Override
    protected void initFromPrefs() {
    }

    @Override
    public void restoreDefaults() {
    }

    @Override
    public void persist() {
    }
}
