/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class IssuesTableModelTest {

    @Mock
    private JTable mockTable;

    @Mock
    private IIssue mockIssue1;

    @Mock
    private IIssue mockIssue2;

    @SuppressWarnings("rawtypes")
    @Mock
    private RowSorter mockRowSorter;

    private IssuesTableModel tableModel;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock issues
        when(mockIssue1.getSegmentNumber()).thenReturn(1);
        when(mockIssue1.getTypeName()).thenReturn("Test Issue 1");
        when(mockIssue1.getDescription()).thenReturn("First test issue");
        when(mockIssue1.hasMenuComponents()).thenReturn(true);

        when(mockIssue2.getSegmentNumber()).thenReturn(2);
        when(mockIssue2.getTypeName()).thenReturn("Test Issue 2");
        when(mockIssue2.getDescription()).thenReturn("Second test issue");
        when(mockIssue2.hasMenuComponents()).thenReturn(false);

        List<IIssue> testIssues = Arrays.asList(mockIssue1, mockIssue2);

        // Create the table model first (needed for TestingIssuesTableModelRowSorter)
        tableModel = new IssuesTableModel(mockTable, testIssues);

        when(mockRowSorter.convertRowIndexToView(0)).thenReturn(0);
        when(mockRowSorter.convertRowIndexToView(1)).thenReturn(1);
        when(mockTable.getRowSorter()).thenReturn(mockRowSorter);
        when(mockTable.getSelectedRow()).thenReturn(-1);

        tableModel = new IssuesTableModel(mockTable, testIssues);
    }

    @Test
    public void testGetRowCount() {
        assertEquals(2, tableModel.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(IssueColumn.values().length, tableModel.getColumnCount());
    }

    @Test
    public void testGetColumnName() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            assertEquals(IssueColumn.get(i).getLabel(), tableModel.getColumnName(i));
        }
    }

    @Test
    public void testGetValueAtSegmentNumber() {
        assertEquals(1, tableModel.getValueAt(0, IssueColumn.SEG_NUM.ordinal()));
        assertEquals(2, tableModel.getValueAt(1, IssueColumn.SEG_NUM.ordinal()));
    }

    @Test
    public void testGetValueAtTypeName() {
        assertEquals("Test Issue 1", tableModel.getValueAt(0, IssueColumn.TYPE.ordinal()));
        assertEquals("Test Issue 2", tableModel.getValueAt(1, IssueColumn.TYPE.ordinal()));
    }

    @Test
    public void testGetValueAtDescription() {
        assertEquals("First test issue", tableModel.getValueAt(0, IssueColumn.DESCRIPTION.ordinal()));
        assertEquals("Second test issue", tableModel.getValueAt(1, IssueColumn.DESCRIPTION.ordinal()));
    }

    @Test
    public void testGetIssueAt() {
        assertEquals(mockIssue1, tableModel.getIssueAt(0));
        assertEquals(mockIssue2, tableModel.getIssueAt(1));
    }

    @Test
    public void testMouseoverRowCol() {
        tableModel.setMouseoverRow(1);
        tableModel.setMouseoverCol(2);

        assertEquals(1, tableModel.getMouseoverRow());
        assertEquals(2, tableModel.getMouseoverCol());
    }

    @Test
    public void testActionMenuIconVisibility() {
        // Test invisible icon for issue without menu components
        Icon icon = tableModel.getActionMenuIcon(mockIssue2, 1, IssueColumn.ACTION_BUTTON.ordinal());
        assertTrue(icon.getIconWidth() > 0);

        // Test visible icon for issue with menu components on mouseover
        tableModel.setMouseoverRow(0);
        tableModel.setMouseoverCol(IssueColumn.ACTION_BUTTON.ordinal());
        icon = tableModel.getActionMenuIcon(mockIssue1, 0, IssueColumn.ACTION_BUTTON.ordinal());
        assertTrue(icon.getIconWidth() > 0);
    }
}