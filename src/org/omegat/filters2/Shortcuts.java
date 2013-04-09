/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.filters2;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for store shortcuts info in parsing time.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class Shortcuts {
    public final List<String> shortcuts = new ArrayList<String>();
    public final List<String> shortcutDetails = new ArrayList<String>();

    public boolean isEmpty() {
        return shortcuts.isEmpty();
    }

    public void put(String shortcut, String details) {
        shortcuts.add(shortcut);
        shortcutDetails.add(details);
    }

    public void clear() {
        shortcuts.clear();
        shortcutDetails.clear();
    }

    public Shortcuts extractFor(String text) {
        Shortcuts r = new Shortcuts();
        for (int i = 0; i < shortcuts.size(); i++) {
            if (text.contains(shortcuts.get(i))) {
                r.shortcuts.add(shortcuts.get(i));
                r.shortcutDetails.add(shortcutDetails.get(i));
            }
        }
        return r;
    }
}
