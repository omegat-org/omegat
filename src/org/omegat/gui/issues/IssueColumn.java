/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.issues;

import org.omegat.util.OStrings;

import javax.swing.Icon;

enum IssueColumn {
    SEG_NUM(0, OStrings.getString("ISSUES_TABLE_COLUMN_ENTRY_NUM"), Integer.class),
    ICON(1, "", Icon.class),
    TYPE(2, OStrings.getString("ISSUES_TABLE_COLUMN_TYPE"), String.class),
    DESCRIPTION(3, OStrings.getString("ISSUES_TABLE_COLUMN_DESCRIPTION"), String.class),
    ACTION_BUTTON(4, "", Icon.class);

    private final int index;
    private final String label;
    private final Class<?> clazz;

    IssueColumn(int index, String label, Class<?> clazz) {
        this.index = index;
        this.label = label;
        this.clazz = clazz;
    }

    int getIndex() {
        return index;
    }

    String getLabel() {
        return label;
    }

    Class<?> getClazz() {
        return clazz;
    }

    static IssueColumn get(int index) {
        return values()[index];
    }

}
