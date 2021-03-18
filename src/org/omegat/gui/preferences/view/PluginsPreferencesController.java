/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import org.apache.commons.io.FileUtils;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.dialogs.ChoosePluginFile;
import org.omegat.gui.dialogs.PluginInstallerDialogController;
import org.omegat.gui.plugin.PluginDetailsPane;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.DesktopWrapper;
import org.omegat.util.gui.TableColumnSizer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

/**
 * @author Aaron Madlon-Kay
 */
public class PluginsPreferencesController extends BasePreferencesController {

    public static final String PLUGINS_WIKI_URL = "https://sourceforge.net/p/omegat/wiki/Plugins/";
    private PluginsPreferencesPanel panel;
    private TableRowSorter<LocalPluginInfoTableModel> sorter;
    private PluginDetailsPane pluginDetailsPane;
    private final Map<String, String> installConfig = new HashMap<>();

    @Override
    public final JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    class FilterDocumentListener implements DocumentListener {
        void setFilterTextAction() {
            String filterText = panel.filterTextField.getText();
            if ("".equals(filterText) || filterText == null) {
                sorter.setRowFilter(null);
                panel.tablePluginsInfo.doLayout();
                return;
            }
            RowFilter<LocalPluginInfoTableModel, Object> rf;
            try {
                rf = RowFilter.regexFilter(filterText, LocalPluginInfoTableModel.COLUMN_NAME);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
            sorter.setRowFilter(rf);
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            setFilterTextAction();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            setFilterTextAction();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            setFilterTextAction();
        }
    }

    final void selectRowAction(ListSelectionEvent evt) {
        int rowIndex = panel.tablePluginsInfo.convertRowIndexToModel(panel.tablePluginsInfo.getSelectedRow());
        if (rowIndex == -1) {
            pluginDetailsPane.setText("");
        } else {
            LocalPluginInfoTableModel model = (LocalPluginInfoTableModel) panel.tablePluginsInfo.getModel();
            String name = (String) model.getValueAt(rowIndex, LocalPluginInfoTableModel.COLUMN_NAME);
            StringBuilder sb = new StringBuilder();
            Optional<PluginInformation> pluginInformation = PluginUtils.getPluginInformations().stream()
                    .filter(info -> info.getName().equals(name))
                    .findFirst();
            pluginInformation.ifPresent(information -> sb.append(formatDetailText(information)));
            pluginDetailsPane.setText(sb.toString());
        }
    }

    private String formatDetailText(PluginInformation info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(info.getName()).append("</h2>\n");
        sb.append("<h4>Author: ");
        if (info.getAuthor() != null) {
            sb.append(info.getAuthor()).append("<br/>\n");
        } else {
            sb.append("Unknown<br/>\n");
        }
        if (info.getCategory() != null) {
            sb.append("Category: ").append(info.getCategory()).append("<br/>\n");
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
    public String toString() {
        return OStrings.getString("PREFS_TITLE_PLUGINS");
    }

    private void initGui() {
        panel = new PluginsPreferencesPanel();
        pluginDetailsPane = new PluginDetailsPane();
        panel.panelPluginDetails.add(pluginDetailsPane);
        TableColumnSizer.autoSize(panel.tablePluginsInfo, 0, true);
        panel.browsePluginsButton.addActionListener(e -> {
            try {
                DesktopWrapper.browse(URI.create(PLUGINS_WIKI_URL));
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(panel, ex.getLocalizedMessage(),
                        OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        });
        LocalPluginInfoTableModel model = (LocalPluginInfoTableModel) panel.tablePluginsInfo.getModel();
        sorter = new TableRowSorter<>(model);
        panel.tablePluginsInfo.setRowSorter(sorter);
        panel.tablePluginsInfo.getSelectionModel().addListSelectionListener(this::selectRowAction);
        panel.filterTextField.getDocument().addDocumentListener(new FilterDocumentListener());
        panel.installFromDiskButton.addActionListener(e -> {
            ChoosePluginFile choosePluginFile = new ChoosePluginFile();
            int choosePluginFileResult = choosePluginFile.showOpenDialog(Core.getMainWindow().getApplicationFrame());
            if (choosePluginFileResult != JFileChooser.APPROVE_OPTION) {
                // user press 'Cancel' in dialog
                return;
            }
            final File pluginFile = choosePluginFile.getSelectedFile();
            File pluginJarFile;
            if (pluginFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                // TODO: support zip
                // extract zip file into temporary directory and look up *.jar file
                // and set it to pluginJarFile.
                return;
            } else {
                pluginJarFile = pluginFile;
            }
            // check manifest
            Set<PluginInformation> pluginInfo = parsePluginJarFileManifest(pluginJarFile);
            new PluginInstallerDialogController(pluginInfo,
                    installConfig).show(Core.getMainWindow().getApplicationFrame());
            boolean result = Boolean.parseBoolean(installConfig.get(PluginInstallerDialogController.DO_INSTALL_KEY));
            if (result) {
                try {
                    installPlugin(pluginJarFile);
                } catch (IOException ex) {
                    Log.log(ex);
                }
            }
        });
    }

    private void installPlugin(File pluginJarFile) throws IOException {
        File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
        FileUtils.copyFileToDirectory(pluginJarFile, homePluginsDir, true);
    }

    private Set<PluginInformation> parsePluginJarFileManifest(File pluginJarFile) {
        Set<PluginInformation> pluginInfo = new HashSet<>();
        try {
            URL[] urls = new URL[1];
            urls[0] = pluginJarFile.toURI().toURL();
            URLClassLoader pluginsClassLoader = new URLClassLoader(urls,
                    PluginsPreferencesController.class.getClassLoader());
            for (Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF"); mlist
                    .hasMoreElements();) {
                URL mu = mlist.nextElement();
                try (InputStream in = mu.openStream()) {
                    Manifest m = new Manifest(in);
                    String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
                    if (pluginClasses != null) {
                        for (String clazz : pluginClasses.split("\\s+")) {
                            if (clazz.trim().isEmpty()) {
                                continue;
                            }
                            pluginInfo.add(new PluginInformation(clazz, m));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return pluginInfo;
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
