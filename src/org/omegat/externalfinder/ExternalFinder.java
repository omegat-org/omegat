/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.externalfinder;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.externalfinder.item.ExternalFinderItemMenuGenerator;
import org.omegat.externalfinder.item.ExternalFinderItemPopupMenuConstructor;
import org.omegat.externalfinder.item.ExternalFinderXMLItemLoader;
import org.omegat.externalfinder.item.IExternalFinderItemLoader;
import org.omegat.externalfinder.item.IExternalFinderItemMenuGenerator;
import org.omegat.util.StaticUtils;

public class ExternalFinder {

    public static final String FINDER_FILE = "finder.xml";

    // the default value means the items may be placed at the top of popup menu.
    private static final int DEFAULT_POPUP_PRIORITY = 50;

    /**
     * to support v2 of OmegaT, this class will be registered as a base-plugin
     * class.
     */
    public ExternalFinder() {
        loadPlugins();
    }

    /**
     * OmegaT will call this method when loading.
     */
    public static void loadPlugins() {
        // shared list of items loaded when a project is opened and cleared when a project is closed.
        final List<ExternalFinderItem> finderItems = new ArrayList<ExternalFinderItem>();

        // register listeners
        CoreEvents.registerApplicationEventListener(ExternalFinder.generateIApplicationEventListener(finderItems));
        CoreEvents.registerProjectChangeListener(ExternalFinder.generateIProjectEventListener(finderItems));
    }

    private static IProjectEventListener generateIProjectEventListener(final List<ExternalFinderItem> finderItems) {
        return new IProjectEventListener() {
            private final List<Component> menuItems = new ArrayList<Component>();

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
                synchronized (finderItems) {
                    finderItems.clear();
                }

                // load user's xml file
                // Even though the file is independent from projects, it is
                // loaded when a project is loaded for providing a chance to reload it.
                final String configDir = StaticUtils.getConfigDir();
                final File userFile = new File(configDir, FINDER_FILE);
                if (userFile.canRead()) {
                    final IExternalFinderItemLoader userItemLoader = new ExternalFinderXMLItemLoader(userFile);
                    final List<ExternalFinderItem> loadedUserItems = userItemLoader.load();

                    synchronized (finderItems) {
                        finderItems.addAll(loadedUserItems);
                    }
                }

                // load project's xml file
                final IProject currentProject = Core.getProject();
                final ProjectProperties projectProperties = currentProject.getProjectProperties();
                final String projectRoot = projectProperties.getProjectRoot();
                final File projectFile = new File(projectRoot, FINDER_FILE);
                if (projectFile.canRead()) {
                    final IExternalFinderItemLoader projectItemLoader = new ExternalFinderXMLItemLoader(projectFile);
                    final List<ExternalFinderItem> loadedProjectItems = projectItemLoader.load();

                    synchronized (finderItems) {
                        // replace duplicated items based on name
                        for (ExternalFinderItem item : finderItems) {
                            if (loadedProjectItems.contains(item)) {
                                final int index = loadedProjectItems.indexOf(item);
                                final ExternalFinderItem newItem = loadedProjectItems.get(index);
                                item.replaceRefs(newItem);
                                loadedProjectItems.remove(index);
                            }
                        }

                        finderItems.addAll(loadedProjectItems);
                    }
                }

                // add finder items to menuItems
                final IExternalFinderItemMenuGenerator generator
                        = new ExternalFinderItemMenuGenerator(finderItems, ExternalFinderItem.TARGET.BOTH, false);
                final List<Component> newMenuItems = generator.generate();
                menuItems.addAll(newMenuItems);

                // add menuItems to menu
                final JMenu toolsMenu = Core.getMainWindow().getMainMenu().getToolsMenu();
                for (Component component : menuItems) {
                    toolsMenu.add(component);
                }
            }

            private void onClose() {
                // remove menu items
                final JMenu menu = Core.getMainWindow().getMainMenu().getToolsMenu();
                for (int i = 0, n = menuItems.size(); i < n; i++) {
                    menu.remove(menuItems.get(i));
                }
                menuItems.clear();

                synchronized (finderItems) {
                    finderItems.clear();
                }
            }
        };
    }

    private static IApplicationEventListener generateIApplicationEventListener(final List<ExternalFinderItem> finderItems) {
        return new IApplicationEventListener() {

            @Override
            public void onApplicationStartup() {
                int priority = DEFAULT_POPUP_PRIORITY;

                // load user's xml file for priority of popup items
                final String configDir = StaticUtils.getConfigDir();
                final File userFile = new File(configDir, FINDER_FILE);
                if (userFile.canRead()) {
                    final IExternalFinderItemLoader userItemLoader = new ExternalFinderXMLItemLoader(userFile);
                    priority = userItemLoader.loadPopupPriority(priority);
                }

                Core.getEditor().registerPopupMenuConstructors(priority, new ExternalFinderItemPopupMenuConstructor(finderItems));
            }

            @Override
            public void onApplicationShutdown() {
            }
        };
    }
}
