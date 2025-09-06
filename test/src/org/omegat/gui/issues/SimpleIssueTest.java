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

import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ITMXEntry;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.SourceTextEntry;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SimpleIssueTest {

    private static final String SOURCE_TEXT = "Hello world!";
    private static final String TRANSLATION_TEXT = "Hallo Welt!";
    private static final String ENTRY_FILE = "source.txt";
    private static final String ENTRY_ID = "ID";
    private static final int ENTRY_NUMBER = 1;
    private static SourceTextEntry sourceEntry;
    private static ITMXEntry targetEntry;

    @BeforeClass
    public static void setUpClass() {
        sourceEntry = new SourceTextEntry(new EntryKey(ENTRY_FILE, SOURCE_TEXT, ENTRY_ID, "", "", null),
                ENTRY_NUMBER, null, null, Collections.emptyList());
        PrepareTMXEntry prep = new PrepareTMXEntry();
        prep.source = SOURCE_TEXT;
        prep.translation = TRANSLATION_TEXT;
        prep.creator = "Test";
        targetEntry = prep;
    }

    @Test
    public void testGetIconReturnsNonNullIcon() {
        TestingIssue issue = new TestingIssue(sourceEntry, targetEntry);
        Icon icon = issue.getIcon();
        assertNotNull("Icon should not be null", icon);
    }

    @Test
    public void testGetDetailComponentReturnsCorrectComponent() {
        TestingIssue issue = new TestingIssue(sourceEntry, targetEntry);
        Component component = issue.getDetailComponent();
        assertNotNull("Component should not be null", component);
        assertEquals("Component should be an instance of IssueDetailSplitPanel",
                IssueDetailSplitPanel.class, component.getClass());
    }

    @Test
    public void testGetDetailComponentPopulatesTextFields() {
        TestingIssue issue = new TestingIssue(sourceEntry, targetEntry);
        IssueDetailSplitPanel panel = (IssueDetailSplitPanel) issue.getDetailComponent();
        assertNotNull("First text pane should not be null", panel.firstTextPane);
        assertNotNull("Last text pane should not be null", panel.lastTextPane);
        assertEquals("First text pane should contain source entry text",
                SOURCE_TEXT, panel.firstTextPane.getText());
        assertEquals("Last text pane should contain target entry translation text",
                targetEntry.getTranslationText(), panel.lastTextPane.getText());
    }

    @Test
    public void testGetIconUsesExpectedColor() {
        TestingIssue issue = new TestingIssue(sourceEntry, targetEntry);
        SimpleColorIcon icon = (SimpleColorIcon) issue.getIcon();
        Color color = icon.getColor();
        assertEquals(TestingIssue.ICON_COLOR, color);
    }

    @Test
    public void testGetEntryNum() {
        TestingIssue issue = new TestingIssue(sourceEntry, targetEntry);
        int entryNum = issue.getSegmentNumber();
        assertEquals(ENTRY_NUMBER, entryNum);
    }
}
