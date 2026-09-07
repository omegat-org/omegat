/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.util.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import org.jspecify.annotations.Nullable;

/**
 * Query language of the generic table search field: space-separated terms
 * must all match a row (in any column, case-insensitive, literal), the token
 * {@code $OR} separates alternative groups, {@code $AND} may spell out the
 * default conjunction. {@code $OR} binds weaker: "a b $OR c d" matches rows
 * containing (a and b) or (c and d).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class TableSearchQuery {

    private TableSearchQuery() {
    }

    /** Filter for the query, or null (= show everything) for a blank one. */
    public static @Nullable RowFilter<TableModel, Integer> parse(String query) {
        List<RowFilter<TableModel, Integer>> orGroups = new ArrayList<>();
        List<RowFilter<TableModel, Integer>> terms = new ArrayList<>();
        for (String token : query.trim().split("\\s+")) {
            if (token.isEmpty() || "$AND".equals(token)) {
                continue;
            }
            if ("$OR".equals(token)) {
                addGroup(orGroups, terms);
                terms = new ArrayList<>();
                continue;
            }
            terms.add(RowFilter.regexFilter("(?i)" + Pattern.quote(token)));
        }
        addGroup(orGroups, terms);
        if (orGroups.isEmpty()) {
            return null;
        }
        return orGroups.size() == 1 ? orGroups.get(0) : RowFilter.orFilter(orGroups);
    }

    private static void addGroup(List<RowFilter<TableModel, Integer>> orGroups,
            List<RowFilter<TableModel, Integer>> terms) {
        if (terms.size() == 1) {
            orGroups.add(terms.get(0));
        } else if (!terms.isEmpty()) {
            orGroups.add(RowFilter.andFilter(terms));
        }
    }
}
