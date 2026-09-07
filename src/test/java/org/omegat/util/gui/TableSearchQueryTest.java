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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.junit.Test;

/**
 * Proves the query language of the generic table search: space-separated
 * terms combine with AND, $AND spells that out, $OR separates alternative
 * groups and binds weaker, terms are literal and case-insensitive.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class TableSearchQueryTest {

    private static final Object[][] ROWS = {
            { "Editor", "Go to next segment", "ctrl N" },
            { "Editor", "Go to previous segment", "ctrl P" },
            { "Menu", "Save project", "ctrl S" },
            { "Menu", "Open a.b project", "" }, };

    private static int hits(String query) {
        DefaultTableModel model = new DefaultTableModel(ROWS,
                new Object[] { "Scope", "Function", "Shortcut" });
        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(TableSearchQuery.parse(query));
        return table.getRowCount();
    }

    @Test
    public void testBlankShowsEverything() {
        assertNull(TableSearchQuery.parse(""));
        assertNull(TableSearchQuery.parse("   "));
        assertNull("bare operators are no terms", TableSearchQuery.parse(" $AND $OR "));
    }

    @Test
    public void testTermsCombineWithAnd() {
        assertEquals(2, hits("editor segment"));
        assertEquals(1, hits("editor NEXT"));
        assertEquals("term order must not matter", 1, hits("next editor"));
        assertEquals(0, hits("editor save"));
        assertEquals("$AND equals the implicit conjunction", 1, hits("editor $AND next"));
    }

    @Test
    public void testOrSeparatesGroupsAndBindsWeaker() {
        assertEquals(3, hits("editor $OR save"));
        // (editor AND next) OR (menu AND save) - not editor AND (next OR menu) ...
        assertEquals(2, hits("editor next $OR menu save"));
        assertEquals("trailing operator is harmless", 1, hits("save $OR"));
    }

    @Test
    public void testTermsAreLiteral() {
        assertEquals("the dot must not act as a regex wildcard", 1, hits("a.b"));
        assertEquals(0, hits("a,b"));
    }
}
