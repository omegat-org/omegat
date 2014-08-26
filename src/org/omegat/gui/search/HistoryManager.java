/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Aaron Madlon-Kay
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

package org.omegat.gui.search;

import java.util.ArrayList;
import java.util.List;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * A class for managing search/replace history
 * 
 * @author Aaron Madlon-Kay
 */
public class HistoryManager {
    
    private static final List<String> searchItems; 
    private static final List<String> replaceItems;
    private static final int maxItems;
    
    static {
        maxItems = Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_HISTORY_SIZE, 10);
        searchItems = new ArrayList<String>(maxItems);
        replaceItems = new ArrayList<String>(maxItems);
        for (int i = 0; i < maxItems; i++) {
            String searchItem = Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_SEARCH_HISTORY_ITEM_PREFIX + i, null);
            if (searchItem != null) {
                searchItems.add(searchItem);
            }
            String replaceItem = Preferences.getPreferenceDefault(Preferences.SEARCHWINDOW_REPLACE_HISTORY_ITEM_PREFIX + i, null);
            if (replaceItem != null) {
                replaceItems.add(replaceItem);
            }
        }
    }
    
    private HistoryManager() {}
    
    public static void addSearchItem(String item) {
        synchronized (searchItems) {
            searchItems.remove(item);
            searchItems.add(0, item);
            while (searchItems.size() > maxItems) {
                searchItems.remove(maxItems);
            }
        }
    }
    
    public static void addReplaceItem(String item) {
        synchronized (replaceItems) {
            replaceItems.remove(item);
            replaceItems.add(0, item);
            while (replaceItems.size() > maxItems) {
                replaceItems.remove(maxItems);
            }
        }
    }
    
    public static String[] getSearchItems() {
        synchronized (searchItems) {
            return searchItems.toArray(new String[0]);
        }
    }
    
    public static String[] getReplaceItems() {
        synchronized (replaceItems) {
            return replaceItems.toArray(new String[0]);
        }
    }
    
    public static void save() {
        synchronized (searchItems) {
            for (int i = 0; i < searchItems.size(); i++) {
                Preferences.setPreference(Preferences.SEARCHWINDOW_SEARCH_HISTORY_ITEM_PREFIX + i,
                        searchItems.get(i));
            }
        }
        synchronized (replaceItems) {
            for (int i = 0; i < replaceItems.size(); i++) {
                Preferences.setPreference(Preferences.SEARCHWINDOW_REPLACE_HISTORY_ITEM_PREFIX + i,
                        replaceItems.get(i));
            }
        }
    }
}
