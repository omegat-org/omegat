/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre, Aaron Madlon-Kay
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

package org.omegat.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.ProjectUICommands;

/**
 * Management of recent projects
 * 
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public class RecentProjects {
    
    private static final List<String> recentProjects;
    private static final int mostRecentProjectSize;
    
    static {
        mostRecentProjectSize = Preferences.getPreferenceDefault(Preferences.MOST_RECENT_PROJECTS_SIZE, OConsts.MAX_RECENT_PROJECTS);
        recentProjects = new ArrayList<String>(mostRecentProjectSize);
        for (int i = 0; i < mostRecentProjectSize; i++) {
            String project = Preferences.getPreferenceDefault(Preferences.MOST_RECENT_PROJECTS_PREFIX + i, null);
            if (project != null) {
                recentProjects.add(project);
            }
        }
    }
    
    private RecentProjects() {}

    private static void saveToPrefs() {
        for (int i = 0; i < recentProjects.size(); i++) {
            String project = recentProjects.get(i);
            if (!StringUtil.isEmpty(project)) {
                Preferences.setPreference(Preferences.MOST_RECENT_PROJECTS_PREFIX + i,
                        recentProjects.get(i));
            }
        }
    }

    public static void updateMenu() {
        
        IMainWindow window = Core.getMainWindow();
        if (window == null) {
            return;
        }
        
        JMenuItem recentMenu = window.getMainMenu().getProjectRecentMenuItem();
        if (recentMenu == null) {
            return;
        }

        recentMenu.removeAll();

        synchronized(recentProjects) {
            for (final String project : recentProjects) {
                JMenuItem recentProjectMenuItem = new JMenuItem(project);
                recentProjectMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (Core.getProject().isProjectLoaded()) {
                            CoreEvents.registerProjectChangeListener(new CloseThenOpen(project));
                            ProjectUICommands.projectClose();
                        } else {
                            ProjectUICommands.projectOpen(new File(project));
                        }
                    }
                });
                recentMenu.add(recentProjectMenuItem);
            }
        }
    }

    public static void add(String element) {
        if (StringUtil.isEmpty(element)) {
            return;
        }
        recentProjects.remove(element);
        recentProjects.add(0, element);

        // Shrink the list to match the desired size.
        while (recentProjects.size() > mostRecentProjectSize) {
            recentProjects.remove(mostRecentProjectSize);
        }
        updateMenu();
        saveToPrefs();
    }

    private static class CloseThenOpen implements IProjectEventListener {
        
        private final String project;
        
        public CloseThenOpen(String project) {
            this.project = project;
        }
        
        @Override
        public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
            if (eventType == PROJECT_CHANGE_TYPE.CLOSE) {
                ProjectUICommands.projectOpen(new File(project));
                CoreEvents.unregisterProjectChangeListener(this);
            }
        }
    }
    
}
