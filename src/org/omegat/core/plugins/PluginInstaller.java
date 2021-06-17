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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.core.threads.PluginDownloadThread;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.util.HttpConnectionUtils;
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

    private static final String LIST_URL = "https://github.com/omegat-org/omegat-plugins/releases/download/continuous-release/plugins.MF";
    private LongProcessThread checkStop;

    public PluginInstaller() {
    }

    public void setThread(LongProcessThread thread) {
        checkStop = thread;
    }

    public void installFromRemote(final PluginsPreferencesController controller, final PluginInformation info) {
        try {
            URL downloadUrl = new URL(info.getRemoteJarFileUrl());
            String jarFilename = info.getJarFilename();
            String sha256sum = info.getSha256Sum();
            PluginDownloadThread downloadThread = new PluginDownloadThread(downloadUrl, sha256sum, jarFilename, this);
            downloadThread.start();
            checkStop.checkInterrupted();
            controller.setRestartRequired(true);
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    public Boolean install(final File pluginFile, final boolean background) {
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
            return false;
        }

        // check manifest
        Set<PluginInformation> pluginInfo = parsePluginJarFileManifest(pluginJarFile.toFile());
        info = pluginInfo.stream().findFirst().orElse(null);
        if (info == null) {
            // it is not a plugin jar file.
            Log.logErrorRB("PREFS_PLUGINS_INSTALATION_FAILED");
            return false;
        }

        // Get plugin name and version to be installed.
        String pluginName = info.getName();
        String version = info.getVersion();
        // detect current installation
        PluginInformation currentInfo = getInstalledPluginInformation(info);
        String message;
        if (currentInfo != null) {
            message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_UPGRADE"), pluginName,
                    currentInfo.getVersion(), version);
        } else {
            message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_INSTALL"), pluginName,
                    version);
        }

        // confirm installation
        if (background ||
                JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                    message,
                    OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE)) {
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

    /**
     * Parse Manifest from plugin jar file.
     * @param pluginJarFile plugin jar file
     * @return PluginInformation
     */
    public static Set<PluginInformation> parsePluginJarFileManifest(File pluginJarFile) {
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
                            pluginInfo.add(new PluginInformation(clazz, m, null, PluginInformation.Status.UNINSTALLED));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return pluginInfo;
    }

    /**
     * Return installed plugins.
     * @return Set of PluginInformation
     */
    public static Map<String, PluginInformation> getInstalledPlugins() {
        Map<String, PluginInformation> installedPlugins = new TreeMap<>();
        PluginUtils.getPluginInformations().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .filter(info -> !installedPlugins.containsKey(info.getClassName()))
                .forEach(info -> installedPlugins.put(info.getClassName(), info));
        return installedPlugins;
    }

    /**
     * Get plugin information installed to system specified by parameter.
     * @param info PluginInformation to search
     * @return PluginInformation when found, otherwise return null
     */
    public static PluginInformation getInstalledPluginInformation(PluginInformation info) {
        return getInstalledPlugins().getOrDefault(info.getClassName(), null);
    }

    /**
     * Download plugin list from github repository.
     * @return set of PluginInformation
     */
    static Set<PluginInformation> getPluginsList() {
        Set<PluginInformation> pluginInfo = new TreeSet<>();
        String raw_value;
        try {
            raw_value = HttpConnectionUtils.getURL(new URL(LIST_URL));
            Scanner scanner = new Scanner(raw_value);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    sb.append(line).append("\n");
                } else {
                    try {
                        Manifest m = new Manifest(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
                        String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
                        for (String clazz : pluginClasses.split("\\s+")) {
                            if (clazz.trim().isEmpty()) {
                                continue;
                            }
                            pluginInfo.add(new PluginInformation(clazz, m, null, PluginInformation.Status.UNINSTALLED));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb = new StringBuilder();
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pluginInfo;
    }

    /**
     * Return known available plugins.
     * It can has plugins that has already installed.
     * @return Map of PluginInformation
     */
     public static Map<String, PluginInformation> getPluginInformations() {
          Map<String, PluginInformation> plugins = getInstalledPlugins();
          getPluginsList().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .forEach(info -> {
                    String key = info.getClassName();
                    if (plugins.get(key) != null) {
                        if (info.compareTo(plugins.get(key)) > 0) {
                            info.setStatus(PluginInformation.Status.UPGRADABLE);
                        } else {
                            info.setStatus(PluginInformation.Status.INSTALLED);
                        }
                        plugins.put(key, info);
                    }
                });
          return plugins;
    }
}
