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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.gui.dialogs.PluginInstallerDialogController;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;


/**
 * Plugin installer utility class.
 *
 * @author Hiroshi Miura
 */
public final class PluginInstaller {

    public static void install(final PluginsManager pluginsManager, final File pluginFile) {
        try {
            // unpack or copy jar to temporary directory
            Path tmporaryDir = Files.createTempDirectory("omegat");
            Path pluginJarFile = unpackPlugin(pluginFile, tmporaryDir);
            pluginJarFile.toFile().deleteOnExit();
            tmporaryDir.toFile().deleteOnExit();
            // check manifest
            Set<PluginInformation> pluginInfo = pluginsManager.parsePluginJarFileManifest(pluginJarFile.toFile());
            Optional<PluginInformation> info = pluginInfo.stream().findFirst();
            PluginInformation currentInfo = null;
            final Map<String, String> installConfig = new HashMap<>();
            // detect current installation
            String currentVersion = null;
            if (info.isPresent()) {
                currentInfo = pluginsManager.getInstalledPluginInformation(info.get());
                if (currentInfo != null) {
                    installConfig.put(PluginInstallerDialogController.CURRENT_VERSION, currentInfo.getVersion());
                    installConfig.put(PluginInstallerDialogController.ACTION_NAME, "Update");
                }
            } else {
                installConfig.put(PluginInstallerDialogController.CURRENT_VERSION, null);
                installConfig.put(PluginInstallerDialogController.ACTION_NAME, "Install");
            }
            // confirm installation
            new PluginInstallerDialogController(pluginInfo, pluginsManager,
                    installConfig).show(Core.getMainWindow().getApplicationFrame());
            boolean result = Boolean.parseBoolean(installConfig.get(PluginInstallerDialogController.DO_INSTALL_KEY));
            if (result) {
                try {
                    if (currentInfo != null) {
                        FileUtils.forceDelete(currentInfo.getJarFile());
                    }
                    File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
                    FileUtils.copyFileToDirectory(pluginJarFile.toFile(), homePluginsDir, true);
                } catch (IOException ex) {
                    Log.log(ex);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
