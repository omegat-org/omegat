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

import javax.swing.JLabel;

import org.junit.Test;

/**
 * Unit tests for {@link CollapsibleBar} - the shared collapse/expand behavior
 * and summary composition used by the sort and filter bars.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class CollapsibleBarTest {

    /** Minimal concrete bar whose summary reflects a mutable field. */
    private static final class TestBar extends CollapsibleBar {
        String summaryText = "empty";

        TestBar() {
            getBody().add(new JLabel("row"));
            refreshSummary();
        }

        @Override
        protected String buildSummary() {
            return summaryText;
        }
    }

    @Test
    public void startsCollapsedByDefault() {
        TestBar bar = new TestBar();
        assertFalse("bar must start collapsed", bar.isExpanded());
    }

    @Test
    public void toggleExpandsAndCollapses() {
        TestBar bar = new TestBar();
        bar.toggle();
        assertTrue(bar.isExpanded());
        bar.toggle();
        assertFalse(bar.isExpanded());
    }

    @Test
    public void setExpandedControlsState() {
        TestBar bar = new TestBar();
        bar.setExpanded(true);
        assertTrue(bar.isExpanded());
        bar.setExpanded(false);
        assertFalse(bar.isExpanded());
    }

    @Test
    public void summaryReflectsModelAfterRefresh() {
        TestBar bar = new TestBar();
        assertEquals("empty", bar.getSummaryText());
        bar.summaryText = "src:foo AND tgt:bar";
        bar.refreshSummary();
        assertEquals("src:foo AND tgt:bar", bar.getSummaryText());
    }

    @Test
    public void constructorDoesNotCallBuildSummaryBeforeSubclassInit() {
        // Regression guard: if the base called buildSummary() during its own
        // constructor, the subclass field would not be set yet and this would
        // observe null instead of the initialized value.
        TestBar bar = new TestBar();
        assertEquals("empty", bar.getSummaryText());
    }
}
