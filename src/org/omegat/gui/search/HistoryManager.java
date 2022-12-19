/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Aaron Madlon-Kay
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

package org.omegat.gui.search;

import java.util.ArrayList;
import java.util.List;

import org.omegat.util.Preferences;

/**
 * A class for managing search/replace history
 *
 * @author Aaron Madlon-Kay
 */
public final class HistoryManager {

    private static final List<String> SEARCH_ITEMS;
    private static final List<String> REPLACE_ITEMS;
    private static final int MAX_ITEMS;

    static {
        MAX_ITEMS = Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_HISTORY_SIZE, 50);
        SEARCH_ITEMS = new ArrayList<String>(MAX_ITEMS);
        REPLACE_ITEMS = new ArrayList<String>(MAX_ITEMS);
        for (int i = 0; i < MAX_ITEMS; i++) {
            String searchItem = Preferences
                    .getPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_HISTORY_ITEM_PREFIX + i, null);
            if (searchItem != null) {
                SEARCH_ITEMS.add(searchItem);
            }
            String replaceItem = Preferences
                    .getPreferenceDefault(Preferences.SEARCHWINDOW_REPLACE_HISTORY_ITEM_PREFIX + i, null);
            if (replaceItem != null) {
                REPLACE_ITEMS.add(replaceItem);
            }
        }
    }

    private HistoryManager() {
    }

    public static void addSearchItem(String item) {
        synchronized (SEARCH_ITEMS) {
            SEARCH_ITEMS.remove(item);
            SEARCH_ITEMS.add(0, item);
            while (SEARCH_ITEMS.size() > MAX_ITEMS) {
                SEARCH_ITEMS.remove(MAX_ITEMS);
            }
        }
    }

    public static void addReplaceItem(String item) {
        synchronized (REPLACE_ITEMS) {
            REPLACE_ITEMS.remove(item);
            REPLACE_ITEMS.add(0, item);
            while (REPLACE_ITEMS.size() > MAX_ITEMS) {
                REPLACE_ITEMS.remove(MAX_ITEMS);
            }
        }
    }

    public static String[] getSearchItems() {
        synchronized (SEARCH_ITEMS) {
            return SEARCH_ITEMS.toArray(new String[SEARCH_ITEMS.size()]);
        }
    }

    public static String[] getReplaceItems() {
        synchronized (REPLACE_ITEMS) {
            return REPLACE_ITEMS.toArray(new String[REPLACE_ITEMS.size()]);
        }
    }

    public static void save() {
        synchronized (SEARCH_ITEMS) {
            for (int i = 0; i < SEARCH_ITEMS.size(); i++) {
                Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_HISTORY_ITEM_PREFIX + i,
                        SEARCH_ITEMS.get(i));
            }
        }
        synchronized (REPLACE_ITEMS) {
            for (int i = 0; i < REPLACE_ITEMS.size(); i++) {
                Preferences.setPreference(Preferences.SEARCHWINDOW_REPLACE_HISTORY_ITEM_PREFIX + i,
                        REPLACE_ITEMS.get(i));
            }
        }
    }
}
