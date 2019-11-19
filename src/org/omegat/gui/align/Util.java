/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.align;

import java.util.List;

import org.omegat.util.Language;

/**
 * @author Aaron Madlon-Kay
 */
public final class Util {

    private Util() {
    }

    /**
     * Get the index of an item in a list, not based on equality but on object identity (<code>==</code>).
     *
     * @param items
     * @param item
     * @return
     */
    static <T> int indexByIdentity(List<T> items, T item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Remove an item from a list, not based on equality but on object identity (<code>==</code>).
     *
     * @param items
     * @param item
     * @return
     */
    static <T> boolean removeByIdentity(List<T> items, T item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Join a list of objects with a delimiter.
     *
     * @param delimiter
     *            String to insert between each item
     * @param items
     *            List of items to join
     * @return String of items joined by delimiter
     */
    static String join(String delimiter, List<?> items) {
        if (items.isEmpty()) {
            return "";
        }
        if (items.size() == 1) {
            return String.valueOf(items.get(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            sb.append(delimiter);
        }
        sb.delete(sb.length() - delimiter.length(), sb.length());
        return sb.toString();
    }

    /**
     * Join a list of objects with a delimiter appropriate for the given language (empty delimiter, or U+0020
     * SPACE).
     *
     * @param lang
     *            Language of items
     * @param items
     *            List of items to join
     * @return String of items joined by language-appropriate delimiter
     */
    static String join(Language lang, List<?> items) {
        return Util.join(lang.isSpaceDelimited() ? " " : "", items);
    }
}
