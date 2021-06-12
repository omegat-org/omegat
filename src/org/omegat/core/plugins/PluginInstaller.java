/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;


/**
 * Plugin installer utility class.
 *
 * @author Hiroshi Miura
 */
public final class PluginInstaller {

    public static Boolean install(final File pluginFile) {
        Path pluginJarFile;
        PluginInformation info;
        try {
            // unpack or copy jar to temporary directory
            Path tmporaryDir = Files.createTempDirectory("omegat");
            pluginJarFile = unpackPlugin(pluginFile, tmporaryDir);
            pluginJarFile.toFile().deleteOnExit();
            tmporaryDir.toFile().deleteOnExit();
        } catch (IOException ex) {
            // wrong file specified
            Log.logErrorRB("PREFS_PLUGINS_INSTALATION_FAILED");
            JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("PREFS_PLUGINS_INSTALLATION_FAILED"),
                    OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                    JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // check manifest
        PluginsManager pluginsManager = new PluginsManager();
        Set<PluginInformation> pluginInfo = pluginsManager.parsePluginJarFileManifest(pluginJarFile.toFile());
        info = pluginInfo.stream().findFirst().orElse(null);
        if (info == null) {
            // it is not a plugin jar file.
            Log.logErrorRB("PREFS_PLUGINS_INSTALATION_FAILED");
            JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("PREFS_PLUGINS_INSTALLATION_FAILED"),
                    OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                    JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Get plugin name and version to be installed.
        String pluginName = info.getName();
        String version = info.getVersion();
        // detect current installation
        PluginInformation currentInfo = pluginsManager.getInstalledPluginInformation(info);
        String message;
        if (currentInfo != null) {
            message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_UPGRADE"), pluginName,
                    currentInfo.getVersion(), version);
        } else {
            message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_INSTALL"), pluginName,
                    version);
        }

        // confirm installation
        int result = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                message,
                OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            try {
                if (currentInfo != null) {
                    FileUtils.forceDeleteOnExit(currentInfo.getJarFile());
                }
                File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
                FileUtils.copyFileToDirectory(pluginJarFile.toFile(), homePluginsDir, true);
                return true;
            } catch (IOException ex) {
                Log.logErrorRB("PREFS_PLUGINS_INSTALLATION_FAILED");
                Log.log(ex);
                JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                        OStrings.getString("PREFS_PLUGINS_INSTALLATION_FAILED"),
                        OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                        JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Unpack plugin file when necessary and copy it.
     *
     * @param sourceFile plugin soure file to be installed (jar or zip)
     * @param targetPath target path to be installed.
     * @return installed plugin jar file path.
     * @throws IOException when source file is corrupted.
     */
    static Path unpackPlugin(File sourceFile, Path targetPath) throws IOException {
        Path target;
        if (sourceFile.getName().endsWith(".jar")) {
            target = targetPath.resolve(sourceFile.getName());
            FileUtils.copyFile(sourceFile, target.toFile());
        } else if (sourceFile.getName().endsWith(".zip")) {
            try (InputStream inputStream = new FileInputStream(sourceFile)) {
                Predicate<String> expected = f -> f.endsWith(OConsts.JAR_EXTENSION);
                List<String> extracted = StaticUtils.extractFromZip(inputStream, targetPath.toFile(), expected);
                if (extracted.size() == 0) {
                    throw new FileNotFoundException("Could not extract a jar file from zip");
                }
                target = targetPath.resolve(extracted.get(0));
                FileUtils.forceDeleteOnExit(target.toFile());
            }
        } else {
            throw new IOException("Unknown archive type: " + sourceFile.getName());
        }
        return target;
    }

}
