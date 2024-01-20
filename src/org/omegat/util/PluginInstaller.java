/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021-2023 Hiroshi Miura
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

package org.omegat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import org.omegat.core.Core;
import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;


/**
 * Plugin installer utility class.
 *
 * @author Hiroshi Miura
 */
public final class PluginInstaller {

    private static final String PLUGIN_TYPE = "OmegaT-Plugin";

    private PluginInstaller() {
    }

    public static boolean install(final File pluginFile) {
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
            Log.logErrorRB("PREFS_PLUGINS_INSTALLATION_FAILED", ex.getLocalizedMessage());
            return false;
        }

        // check manifest
        try {
            info = parsePluginJarFileManifest(pluginJarFile.toFile()).stream().findFirst()
                    .orElseThrow(() -> new IOException(OStrings.getString("PREFS_PLUGINS_UNKNOWN_ARCHIVE")));
        } catch (IOException e) {
            // it is not a plugin jar file.
            Log.logErrorRB("PREFS_PLUGINS_INSTALLATION_FAILED", e.getLocalizedMessage());
            Core.getMainWindow().displayWarningRB("PREFS_PLUGINS_INSTALLATION_FAILED",
                    e.getLocalizedMessage());
            return false;
        }
        // Get a plugin name and version to be installed.
        String pluginName = info.getName();
        String version = info.getVersion();
        // detect current installation
        PluginInformation currentInfo = getInstalledPlugins().getOrDefault(info.getClassName(), null);
        String title = "";
        String message;
        if (currentInfo != null) {
            if (currentInfo.getVersion().equals(version)) {
                title = OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION");
                message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_OVERWRITE"), pluginName,
                        currentInfo.getVersion(), version);
            } else {
                title = OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_UPGRADE");
                message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_UPGRADE"), pluginName,
                        currentInfo.getVersion(), version);
            }
        } else {
            title = OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION");
            message = StringUtil.format(OStrings.getString("PREFS_PLUGINS_CONFIRM_INSTALL"), pluginName,
                    version);
        }

        // confirm installation
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                Core.getMainWindow().getApplicationFrame(), message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE)) {
            if (doInstall(currentInfo, pluginJarFile.toFile())) {
                JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(), OStrings.getString(
                        "PREFS_PLUGINS_INSTALLATION_SUCCEED"));
                return true;
            }
            JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("PREFS_PLUGINS_INSTALLATION_FAILED"),
                    OStrings.getString("PREFS_PLUGINS_TITLE_CONFIRM_INSTALLATION"), JOptionPane.YES_OPTION,
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private static boolean doInstall(PluginInformation currentInfo, File file) {
        try {
            if (currentInfo != null) {
                URL url = currentInfo.getUrl();
                if (url != null) {
                    File jarFile = new File(url.getPath().substring(5, url.getPath().indexOf("!")));
                    if (!jarFileInInstallDir(jarFile)) {
                        // plugin file may be in ~/.omegat/plugins/
                        if (jarFile.getName().equals(file.getName())) {
                            // try to override?
                            File bakFile = new File(jarFile.getPath() + ".bak");
                            FileUtils.moveFile(jarFile, bakFile);
                            FileUtils.forceDeleteOnExit(bakFile);
                        } else {
                            FileUtils.forceDeleteOnExit(jarFile);
                        }
                    }
                }
            }
            File homePluginsDir = new File(StaticUtils.getConfigDir(), "plugins");
            FileUtils.copyFileToDirectory(file, homePluginsDir, true);
            return true;
        } catch (IOException ex) {
            Log.logErrorRB("PREFS_PLUGINS_INSTALLATION_FAILED");
            Log.log(ex);
        }
        return false;
    }

    /**
     * Check if jarFile is placed in OmegaT installed system directory.
     *
     * @param jarFile
     *            a file determine.
     * @return true when a file is under installed directory, otherwise return
     *         false.
     */
    private static boolean jarFileInInstallDir(File jarFile) {
        Path installDir = Paths.get(StaticUtils.installDir()).normalize();
        Path jarPath = jarFile.toPath().normalize();
        return jarPath.startsWith(installDir);
    }

    /**
     * Unpack a plugin file when necessary and copy it.
     *
     * @param sourceFile
     *            plugin source file to be installed (jar or zip)
     * @param targetPath
     *            target path to be installed.
     * @return jar file path of installed plugin.
     * @throws IOException
     *             when source file is corrupted.
     */
    static Path unpackPlugin(File sourceFile, Path targetPath) throws IOException {
        Path target;
        if (sourceFile.getName().endsWith(".jar")) {
            target = targetPath.resolve(sourceFile.getName());
            FileUtils.copyFile(sourceFile, target.toFile());
        } else if (sourceFile.getName().endsWith(".zip")) {
            try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
                Predicate<String> expected = f -> f.endsWith(OConsts.JAR_EXTENSION);
                List<String> extracted = StaticUtils.extractFromZip(inputStream, targetPath.toFile(),
                        expected);
                if (extracted.isEmpty()) {
                    throw new FileNotFoundException("Could not extract a jar file from zip");
                }
                target = targetPath.resolve(extracted.get(0));
            }
        } else {
            throw new IOException("Unknown archive type: " + sourceFile.getName());
        }
        // clean up extracted temporary target jar file.
        FileUtils.forceDeleteOnExit(target.toFile());
        return target;
    }

    /**
     * Parse Manifest from plugin jar file.
     *
     * @param pluginJarFile
     *            plugin jar file
     * @return PluginInformation
     */
    static Set<PluginInformation> parsePluginJarFileManifest(File pluginJarFile) throws IOException {
        Set<PluginInformation> pluginInfo = new HashSet<>();
        URL[] urls = new URL[1];
        urls[0] = pluginJarFile.toURI().toURL();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try (URLClassLoader pluginsClassLoader = new URLClassLoader(urls, cl)) {
            for (Enumeration<URL> mlist = pluginsClassLoader.getResources("META-INF/MANIFEST.MF"); mlist
                    .hasMoreElements();) {
                URL mu = mlist.nextElement();
                if (Files.isSameFile(pluginJarFile.toPath(),
                        Paths.get(PluginUtils.getJarFileUrlFromResourceUrl(mu).getFile()))) {
                    pluginInfo.addAll(parsePluginJarFileManifest(mu));
                }
            }
        }
        return pluginInfo;
    }

    /**
     *
     * @param manifestUrl
     *            URL of MANIFEST.MF file
     * @return plugin information
     */
    static Set<PluginInformation> parsePluginJarFileManifest(URL manifestUrl) throws IOException {
        Set<PluginInformation> pluginInfo = new HashSet<>();
        try (InputStream in = manifestUrl.openStream()) {
            Manifest m = new Manifest(in);
            String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
            if (pluginClasses != null) {
                for (String clazz : pluginClasses.split("\\s+")) {
                    if (clazz.trim().isEmpty()) {
                        continue;
                    }
                    pluginInfo.add(PluginInformation.Builder.fromManifest(clazz, m, manifestUrl,
                            PluginInformation.Status.NEW));
                }
            } else if (m.getMainAttributes().getValue(PLUGIN_TYPE) != null) {
                // parse OldManifest
                Map<String, Attributes> entries = m.getEntries();
                for (Map.Entry<String, Attributes> e : entries.entrySet()) {
                    String key = e.getKey();
                    Attributes attrs = e.getValue();
                    String sType = attrs.getValue(PLUGIN_TYPE);
                    if (sType == null) {
                        // WebStart signing section, or other section
                        continue;
                    }
                    pluginInfo.add(PluginInformation.Builder.fromManifest(key, m, manifestUrl,
                            PluginInformation.Status.NEW));
                }
            }
        }
        return pluginInfo;
    }

    /**
     * Return installed plugins.
     *
     * @return Map of PluginInformation
     */
    private static Map<String, PluginInformation> getInstalledPlugins() {
        Map<String, PluginInformation> installedPlugins = new TreeMap<>();
        PluginUtils.getPluginInformations().stream()
                .sorted(Comparator.comparing(PluginInformation::getClassName))
                .filter(info -> !installedPlugins.containsKey(info.getClassName()))
                .forEach(info -> installedPlugins.put(info.getClassName(), info));
        return installedPlugins;
    }
}
