/*******************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
 ******************************************************************************/

package org.omegat.gui.search;

import org.omegat.core.Core;
import org.omegat.core.search.SearchMode;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

public final class SearchWindowManager {

    private SearchWindowManager() {
    }

    /**
     * Set of all open search windows.
     */
    private static final List<SearchWindowController> searches = new ArrayList<>();

    public static void createSearchWindow(SearchMode mode) {
        String text = Core.getMainWindow().getSelectedText();
        createSearchWindow(mode, text);
    }

    public static void createSearchWindow(SearchMode mode, String query) {
        SearchWindowController search = new SearchWindowController(mode);
        addSearchWindow(search);
        search.makeVisible(query);
    }

    public static void closeSearchWindows() {
        synchronized (searches) {
            // dispose other windows
            for (SearchWindowController sw : searches) {
                sw.dispose();
            }
            searches.clear();
        }
    }

    /**
     * Run a search for the given source text in the most recent open SEARCH
     * window, or in a new one when none is open. Used by the source
     * concordance search menu action when its immediate search option is on.
     *
     * @param query
     *            source text to search for
     */
    public static void searchSource(String query) {
        obtainSearchWindow().searchImmediately(query);
    }

    /**
     * Insert the given source text into the most recent open SEARCH window,
     * or into a new one when none is open. The search field gets the focus
     * and the inserted text is selected, so typing replaces it right away.
     * Used by the source concordance search menu action when its immediate
     * search option is off.
     *
     * @param query
     *            source text to insert
     */
    public static void insertSource(String query) {
        SearchWindowController swc = obtainSearchWindow();
        swc.makeVisible(query);
        swc.focusSearchField();
    }

    private static SearchWindowController obtainSearchWindow() {
        for (int i = searches.size() - 1; i >= 0; i--) {
            SearchWindowController swc = searches.get(i);
            if (swc.getMode() == SearchMode.SEARCH) {
                return swc;
            }
        }
        SearchWindowController search = new SearchWindowController(SearchMode.SEARCH);
        addSearchWindow(search);
        return search;
    }

    /**
     * Label of the search behaviour option of the Search for Source Segment
     * command, with the currently effective menu shortcut baked in. Used by
     * the editor preferences panel and the gear menu of the Search window.
     */
    public static String searchSourceSegmentOptionLabel() {
        KeyStroke ks = PropertiesShortcuts.getMainMenuShortcuts()
                .getKeyStroke("editSearchSourceSegmentMenuItem");
        String shortcut = ks == null ? "" : StaticUIUtils.getKeyStrokeText(ks);
        return StringUtil.format(OStrings.getString("WF_OPTION_SEARCH_SOURCE_SEGMENT"), shortcut);
    }

    public static boolean reuseSearchWindow(String text) {
        for (int i = searches.size() - 1; i >= 0; i--) {
            SearchWindowController swc = searches.get(i);
            if (swc.getMode() == SearchMode.SEARCH) {
                swc.makeVisible(text);
                return true;
            }
        }
        return false;
    }

    private static void addSearchWindow(final SearchWindowController newSearchWindow) {
        newSearchWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                removeSearchWindow(newSearchWindow);
            }
        });
        synchronized (searches) {
            searches.add(newSearchWindow);
        }
    }

    private static void removeSearchWindow(SearchWindowController searchWindow) {
        synchronized (searches) {
            searches.remove(searchWindow);
        }
    }
}
