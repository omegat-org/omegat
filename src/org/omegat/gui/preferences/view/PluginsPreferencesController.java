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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int TABLE_WIDTH = 450;
    private static final int TABLE_HEIGHT = 300;

    private static final String LABEL_AUTHOR = "PREFS_PLUGINS_LABEL_AUTHOR";
    private static final String LABEL_AUTHOR_UNKNOWN = "PREFS_PLUGINS_LABEL_AUTHOR_UNKNOWN";
    private static final String LABEL_CATEGORY = "PREFS_PLUGINS_LABEL_CATEGORY";
    private static final String LABEL_VERSION = "PREFS_PLUGINS_LABEL_VERSION";
    private static final String LABEL_HOMEPAGE = "PREFS_PLUGINS_LABEL_HOMEPAGE";
    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";

    private PluginsPreferencesPanel panel;
    private PluginDetailsPane pluginDetailsPane;
    private PluginInfoTableModel installedPluginsModel;
    private PluginInfoTableModel bundledPluginsModel;
    private PluginInfoTableModel currentModel;
    // Tab index mapping to PluginInformation.Status
    private static final PluginInformation.Status[] TAB_STATUS_MAPPING = {
            PluginInformation.Status.INSTALLED,  // Tab 0
            PluginInformation.Status.BUNDLED     // Tab 1
    };

    /**
     * Format plugin information for details pane of UI.
     * @param info PluginInformation to show
     * @return HTML text
     */
    private static String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(info.getName()).append("</h1>");
        sb.append("<h4>").append(OStrings.getString(LABEL_AUTHOR)).append(": ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>");
        } else {
            sb.append(OStrings.getString(LABEL_AUTHOR_UNKNOWN)).append("<br/>");
        }
        if (info.getVersion() != null) {
            sb.append(OStrings.getString(LABEL_VERSION)).append(": ");
            sb.append(info.getVersion()).append("<br/>");
        }
        sb.append(OStrings.getString(LABEL_CATEGORY)).append(": ");
        sb.append(info.getCategory().getLocalizedValue());
        sb.append("</h4>");
        if (info.getDescription() != null) {
            sb.append("<p>").append(info.getDescription()).append("</p>");
        }
        if (info.getLink() != null) {
            sb.append("<br/><div>");
            sb.append("<a href=\"").append(info.getLink()).append("\">");
            sb.append(OStrings.getString(LABEL_HOMEPAGE)).append("</a></div>");
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
        } else if (rowIndex < currentModel.getRowCount()) {
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
        installedPluginsModel = new PluginInfoTableModel();
        bundledPluginsModel = new PluginInfoTableModel();
        loadPluginsByStatus(PluginInformation.Status.INSTALLED);
        loadPluginsByStatus(PluginInformation.Status.BUNDLED);
        currentModel = installedPluginsModel;
        panel.tablePluginsInfo.setModel(currentModel);

        setupTable();
        pluginDetailsPane = new PluginDetailsPane();
        panel.panelPluginDetails.add(pluginDetailsPane);

        setupTabs();

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
    }

    private void setupTabs() {
        if (panel.tabbedPanePlugins == null) {
            return;
        }
        panel.tabbedPanePlugins.removeAll();
        panel.tabbedPanePlugins.addTab(
                OStrings.getString("PREFS_PLUGINS_TAB_INSTALLED"),
                null  // No content component
        );
        panel.tabbedPanePlugins.addTab(
                OStrings.getString("PREFS_PLUGINS_TAB_BUNDLED"),
                null  // No content component
        );
        panel.tabbedPanePlugins.setSelectedIndex(0);
        panel.tabbedPanePlugins.addChangeListener(e -> onTabChanged());
    }

    private void setupTable() {
        TableRowSorter<PluginInfoTableModel> sorter = new TableRowSorter<>(currentModel);
        panel.tablePluginsInfo.setRowSorter(sorter);

        panel.scrollTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.scrollTable.getViewport().setViewSize(new Dimension(TABLE_WIDTH, TABLE_HEIGHT));

        panel.tablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowAction);
        panel.tablePluginsInfo.setPreferredScrollableViewportSize(panel.tablePluginsInfo.getPreferredSize());
        DataTableStyling.applyFont(panel.tablePluginsInfo, Core.getMainWindow().getApplicationFont());
        TableColumnSizer.autoSize(panel.tablePluginsInfo, 0, true);
    }

    /**
     * Handle tab change events
     */
    public void onTabChanged() {
        if (panel.tabbedPanePlugins == null) {
            return;
        }
        int selectedIndex = panel.tabbedPanePlugins.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= TAB_STATUS_MAPPING.length) {
            return;
        }

        PluginInformation.Status status = TAB_STATUS_MAPPING[selectedIndex];

        // Switch table model based on selected tab
        switch (status) {
        case INSTALLED:
            currentModel = installedPluginsModel;
            break;
        case BUNDLED:
            currentModel = bundledPluginsModel;
            break;
        default:
            currentModel = installedPluginsModel;
            break;
        }

        // Update table with new model
        panel.tablePluginsInfo.setModel(currentModel);
        TableRowSorter<PluginInfoTableModel> sorter = new TableRowSorter<>(currentModel);
        panel.tablePluginsInfo.setRowSorter(sorter);

        // Clear selection and details
        panel.tablePluginsInfo.clearSelection();
        pluginDetailsPane.setText("");

        // Refresh data if needed
        refreshCurrentCategoryData(status);
    }

    private void refreshCurrentCategoryData(PluginInformation.Status status) {
        // Reload data for the current category if needed
        loadPluginsByStatus(status);
    }

    private void loadPluginsForModel(PluginInfoTableModel model, PluginInformation.Status status) {
        List<PluginInformation> allPlugins = PluginInstaller.getInstance().getPluginList();
        List<PluginInformation> filteredPlugins;

        switch (status) {
        case INSTALLED:
            // Load non-bundled installed plugins
            filteredPlugins = allPlugins.stream()
                    .filter(p -> !p.isBundled() && p.getStatus() == PluginInformation.Status.INSTALLED)
                    .collect(Collectors.toList());
            break;
        case BUNDLED:
            // Load bundled plugins that come with OmegaT
            filteredPlugins = allPlugins.stream()
                    .filter(PluginInformation::isBundled)
                    .collect(Collectors.toList());
            break;
        default:
            filteredPlugins = new ArrayList<>();
            break;
        }

        // Update the model with filtered plugins
        model.setPlugins(filteredPlugins);
    }

    private void loadPluginsByStatus(PluginInformation.Status status) {
        PluginInfoTableModel targetModel;

        switch (status) {
        case INSTALLED:
            targetModel = installedPluginsModel;
            break;
        case BUNDLED:
            targetModel = bundledPluginsModel;
            break;
        default:
            return;
        }

        // Clear existing data
        targetModel.clear();

        // Load plugins for the target model
        loadPluginsForModel(targetModel, status);
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
