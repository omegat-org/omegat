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

package org.omegat.gui.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import org.omegat.gui.editor.SegmentMetadataConfigDialog.ColumnTableModel;
import org.omegat.gui.editor.SegmentMetadataGutter.Column;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for the column configuration of the segment metadata gutter.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SegmentMetadataConfigDialogTest {

    private AtomicInteger refreshes;
    private ColumnTableModel model;

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        refreshes = new AtomicInteger();
        model = new ColumnTableModel(refreshes::incrementAndGet);
    }

    @Test
    public void testOffersOneRowPerColumnInDisplayOrder() {
        assertEquals(Column.values().length, model.getRowCount());
        assertEquals(Column.NUMBER.getLabel(), model.getValueAt(0, 2));
        assertEquals(Boolean.TRUE, model.getValueAt(0, 0));
        assertTrue(model.isCellEditable(0, 0));
        assertFalse(model.isCellEditable(0, 1));
        assertFalse(model.isCellEditable(0, 2));
        // The text pseudo rows close the table: always on, not toggleable.
        int last = model.getRowCount() - 1;
        assertEquals(Column.TARGET_TEXT, model.columnAt(last));
        assertEquals(Column.SOURCE_TEXT, model.columnAt(last - 1));
        assertEquals(Boolean.TRUE, model.getValueAt(last, 0));
        assertFalse(model.isCellEditable(last, 0));
    }

    @Test
    public void testTheToggleWritesThePreferenceAndRefreshes() {
        model.setValueAt(Boolean.FALSE, 0, 0);
        assertFalse("The default-on column must persist its deselection",
                Column.NUMBER.isEnabled());
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testMovingARowPersistsTheDisplayOrder() {
        model.moveRow(0, Column.values().length);
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.STATUS, order.get(0));
        // The metadata row stops at the text pair, which closes the table.
        assertEquals(Column.NUMBER, order.get(order.size() - 3));
        assertEquals(Column.TARGET_TEXT, order.get(order.size() - 1));
        assertEquals(1, refreshes.get());
        assertEquals(Column.STATUS.getLabel(), model.getValueAt(0, 2));
    }

    @Test
    public void testTheDisplayOrderSurvivesUnknownNames() {
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ORDER, "DATE,UNSINN,DATE");
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.values().length, order.size());
        assertEquals(Column.DATE, order.get(0));
        assertEquals(Column.NUMBER, order.get(1));
    }

    @Test
    public void testTheTextRowsSwapButStayAdjacent() {
        int sourceRow = model.getRowCount() - 2;
        assertEquals(sourceRow + 1, model.move(sourceRow, 1));
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.TARGET_TEXT, order.get(order.size() - 2));
        assertEquals(Column.SOURCE_TEXT, order.get(order.size() - 1));
    }

    @Test
    public void testAFartherTextMoveCarriesThePairToTheOtherEnd() {
        assertFalse(Column.metadataAfterText());
        // The upper text row moves up into the metadata block: the whole
        // pair jumps to the start, the metadata columns go after the text.
        int moved = model.move(model.getRowCount() - 2, -1);
        assertEquals(0, moved);
        assertTrue(Column.metadataAfterText());
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.SOURCE_TEXT, order.get(0));
        assertEquals(Column.TARGET_TEXT, order.get(1));
        // And metadata rows never move into or beyond the leading pair.
        assertEquals(-1, model.move(2, -1));
    }

    @Test
    public void testTheNormalizationKeepsNothingBetweenTheTextRows() {
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ORDER,
                "SOURCE_TEXT,NUMBER,TARGET_TEXT");
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.SOURCE_TEXT, order.get(0));
        assertEquals(Column.TARGET_TEXT, order.get(1));
        assertEquals(Column.NUMBER, order.get(2));
    }

    @Test
    public void testTheTargetRowIsMostlyInertWhileStacked() {
        int targetRow = model.getRowCount() - 1;
        assertTrue(ColumnTableModel.stacked());
        // The alignment stays configurable even while the texts are stacked.
        assertTrue(model.isCellEditable(targetRow, 4));
        assertFalse(model.isCellEditable(targetRow, 5));
        model.cycleAlignment(targetRow);
        assertEquals(1, refreshes.get());
        Preferences.setPreference(Preferences.EDITOR_LAYOUT_STACKED, false);
        assertTrue(model.isCellEditable(targetRow, 4));
        assertTrue(model.isCellEditable(targetRow, 5));
    }
}
