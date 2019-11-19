/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Management of recent projects
 *
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 */
public final class RecentProjects {

    private static final List<String> RECENT_PROJECTS;
    private static final int MOST_RECENT_PROJECT_SIZE;

    static {
        MOST_RECENT_PROJECT_SIZE = Preferences.getPreferenceDefault(Preferences.MOST_RECENT_PROJECTS_SIZE,
                OConsts.MAX_RECENT_PROJECTS);
        RECENT_PROJECTS = new ArrayList<>(MOST_RECENT_PROJECT_SIZE);
        for (int i = 0; i < MOST_RECENT_PROJECT_SIZE; i++) {
            String project = Preferences.getPreferenceDefault(Preferences.MOST_RECENT_PROJECTS_PREFIX + i, null);
            if (project != null) {
                RECENT_PROJECTS.add(project);
            }
        }
    }

    private RecentProjects() {
    }

    private static void saveToPrefs() {
        synchronized (RECENT_PROJECTS) {
            for (int i = 0; i < MOST_RECENT_PROJECT_SIZE; i++) {
                String project = i < RECENT_PROJECTS.size() ? RECENT_PROJECTS.get(i) : "";
                Preferences.setPreference(Preferences.MOST_RECENT_PROJECTS_PREFIX + i, project);
            }
        }
    }

    public static List<String> getRecentProjects() {
        synchronized (RECENT_PROJECTS) {
            return new ArrayList<>(RECENT_PROJECTS);
        }
    }

    public static void add(String element) {
        if (StringUtil.isEmpty(element)) {
            return;
        }

        synchronized (RECENT_PROJECTS) {
            RECENT_PROJECTS.remove(element);
            RECENT_PROJECTS.add(0, element);

            // Shrink the list to match the desired size.
            while (RECENT_PROJECTS.size() > MOST_RECENT_PROJECT_SIZE) {
                RECENT_PROJECTS.remove(MOST_RECENT_PROJECT_SIZE);
            }
        }
        saveToPrefs();
    }

    public static void clear() {
        synchronized (RECENT_PROJECTS) {
            RECENT_PROJECTS.clear();
        }
        saveToPrefs();
    }

}
