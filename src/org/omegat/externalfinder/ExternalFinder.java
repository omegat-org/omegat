/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio, Aaron Madlon-Kay
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

package org.omegat.externalfinder;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.externalfinder.item.ExternalFinderConfiguration;
import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.externalfinder.item.ExternalFinderItem.SCOPE;
import org.omegat.externalfinder.item.ExternalFinderItemMenuGenerator;
import org.omegat.externalfinder.item.ExternalFinderItemPopupMenuConstructor;
import org.omegat.externalfinder.item.ExternalFinderXMLLoader;
import org.omegat.externalfinder.item.ExternalFinderXMLWriter;
import org.omegat.externalfinder.item.IExternalFinderItemLoader;
import org.omegat.externalfinder.item.IExternalFinderItemMenuGenerator;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.MenuExtender;
import org.omegat.util.gui.MenuItemPager;

/**
 * Entry point for ExternalFinder functionality.
 * <p>
 * ExternalFinder was originally a plugin developed by Chihiro Hio, and
 * generously donated for inclusion in OmegaT itself.
 * <p>
 * See {@link #getProjectFile(IProject)} and {@link ExternalFinderItem} for
 * details about how this implementation's behavior differs from the original
 * plugin.
 * <p>
 * See the plugin page or <code>package.html</code> for information about the
 * XML format.
 *
 * @see <a href=
 *      "https://github.com/hiohiohio/omegat-plugin-externalfinder">omegat-plugin-externalfinder
 *      on GitHub</a>
 */
public final class ExternalFinder {

    private ExternalFinder() {
    }

    public static final String FINDER_FILE = "finder.xml";

    private static final Logger LOGGER = Logger.getLogger(ExternalFinder.class.getName());

    /**
     * OmegaT will call this method when loading.
     */
    public static void loadPlugins() {
        // register listeners
        CoreEvents.registerApplicationEventListener(generateIApplicationEventListener());
        CoreEvents.registerProjectChangeListener(generateIProjectEventListener());
    }

    private static IProjectEventListener generateIProjectEventListener() {
        return new IProjectEventListener() {
            private final List<Component> menuItems = new ArrayList<>();

            @Override
            public void onProjectChanged(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
                switch (eventType) {
                case LOAD:
                    onLoad();
                    break;
                case CLOSE:
                    onClose();
                    break;
                default:
                    // ignore
                }
            }

            private void onLoad() {
                // clear old items
                menuItems.clear();

                // add finder items to menuItems
                IExternalFinderItemMenuGenerator generator = new ExternalFinderItemMenuGenerator(
                        ExternalFinderItem.TARGET.BOTH, false);
                List<JMenuItem> newMenuItems = generator.generate();
                // Separator
                Component separator = new JPopupMenu.Separator();
                MenuExtender.addMenuItem(MenuExtender.MenuKey.TOOLS, separator);
                menuItems.add(separator);

                // add menuItems to menu
                MenuItemPager pager = new MenuItemPager(MenuExtender.MenuKey.TOOLS);
                newMenuItems.forEach(pager::add);
                menuItems.addAll(pager.getFirstPage());
            }

            private void onClose() {
                // remove menu items
                MenuExtender.removeMenuItems(MenuExtender.MenuKey.TOOLS, menuItems);
                menuItems.clear();
                projectConfig = null;
            }
        };
    }

    private static IApplicationEventListener generateIApplicationEventListener() {
        return new IApplicationEventListener() {

            @Override
            public void onApplicationStartup() {
                Core.getEditor().registerPopupMenuConstructors(getGlobalConfig().getPriority(),
                        new ExternalFinderItemPopupMenuConstructor());
            }

            @Override
            public void onApplicationShutdown() {
            }
        };
    }

    public static void unloadPlugins() {
    }

    private static ExternalFinderConfiguration globalConfig;

    /**
     * Get the global configuration. This is stored in the user's OmegaT
     * configuration directory. If the file does not exist, an empty
     * configuration will be created and returned.
     *
     * @return The configuration (will never be null)
     */
    public static ExternalFinderConfiguration getGlobalConfig() {
        if (globalConfig == null) {
            try {
                File globalFile = getGlobalConfigFile();
                IExternalFinderItemLoader userItemLoader = new ExternalFinderXMLLoader(globalFile,
                        SCOPE.GLOBAL);
                globalConfig = userItemLoader.load();
            } catch (FileNotFoundException e) {
                // Ignore
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            if (globalConfig == null) {
                globalConfig = ExternalFinderConfiguration.empty();
            }
        }
        return globalConfig;
    }

    /**
     * Set the global configuration. Any existing configuration file will be
     * overwritten with the new one. Pass null to delete the config file.
     */
    public static void setGlobalConfig(ExternalFinderConfiguration newConfig) {
        ExternalFinderConfiguration oldConfig = globalConfig;
        globalConfig = newConfig;
        if (!Objects.equals(newConfig, oldConfig)) {
            writeConfig(newConfig, getGlobalConfigFile());
        }
    }

    private static File getGlobalConfigFile() {
        String configDir = StaticUtils.getConfigDir();
        return new File(configDir, FINDER_FILE);
    }

    private static ExternalFinderConfiguration projectConfig;

    /**
     * Get the project-specific configuration.
     *
     * @return The configuration, or null if no project is loaded or the project
     *         has no config file
     */
    public static ExternalFinderConfiguration getProjectConfig() {
        IProject currentProject = Core.getProject();
        if (!currentProject.isProjectLoaded()) {
            return null;
        }
        if (projectConfig == null) {
            // load project's xml file
            File projectFile = getProjectFile(currentProject);
            IExternalFinderItemLoader projectItemLoader = new ExternalFinderXMLLoader(projectFile,
                    SCOPE.PROJECT);
            try {
                projectConfig = projectItemLoader.load();
            } catch (FileNotFoundException e) {
                // Ignore
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return projectConfig;
    }

    /**
     * Set the project-specific configuration. Has no effect if no project is
     * loaded. Any existing configuration file will be overwritten with the new
     * one. Pass null to delete the config file.
     */
    public static void setProjectConfig(ExternalFinderConfiguration newConfig) {
        IProject currentProject = Core.getProject();
        if (!currentProject.isProjectLoaded()) {
            return;
        }
        ExternalFinderConfiguration oldConfig = projectConfig;
        projectConfig = newConfig;
        if (!Objects.equals(newConfig, oldConfig)) {
            File projectFile = getProjectFile(currentProject);
            writeConfig(newConfig, projectFile);
        }
    }

    private static void writeConfig(ExternalFinderConfiguration config, File toFile) {
        if (config == null) {
            boolean deleted = toFile.delete();
            if (!deleted) {
                LOGGER.log(Level.SEVERE, "Unable to delete ExternalFinder config file: {0}", toFile);
            }
        } else {
            try {
                File tmpFile = File.createTempFile("omt", "externalfinder");
                ExternalFinderXMLWriter writer = new ExternalFinderXMLWriter(tmpFile);
                writer.write(config);
                Files.move(tmpFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Get the project-specific config file. In the original plugin this was
     * stored in the project root ({@link ProjectProperties#getProjectRoot()});
     * now it's in the <code>omegat</code> directory for consistency with other
     * project-specific config files.
     */
    private static File getProjectFile(IProject project) {
        ProjectProperties projectProperties = project.getProjectProperties();
        File projectRoot = projectProperties.getProjectInternalDir();
        return new File(projectRoot, FINDER_FILE);
    }

    public static List<ExternalFinderItem> getItems() {
        // replace duplicated items based on name
        List<ExternalFinderItem> result = new ArrayList<>(getGlobalConfig().getItems());
        ExternalFinderConfiguration projectConfig = getProjectConfig();
        if (projectConfig != null) {
            projectConfig.getItems().forEach(item -> addOrReplaceByName(result, item));
        }
        return Collections.unmodifiableList(result);
    }

    static void addOrReplaceByName(List<ExternalFinderItem> items, ExternalFinderItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(item.getName())) {
                items.set(i, item);
                return;
            }
        }
        items.add(item);
    }
}
