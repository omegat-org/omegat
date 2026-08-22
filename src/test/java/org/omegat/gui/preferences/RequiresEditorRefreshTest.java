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

package org.omegat.gui.preferences;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.omegat.gui.preferences.view.CustomColorSelectionController;

/**
 * The preferences window rebuilds the editor view after saving only when a
 * visited view asks for it. The default must stay conservative (true), and
 * the colours view — whose changes apply through the colors-changed event
 * and paint-time resolution — must opt out, so colour edits stay
 * instantaneous on large documents.
 *
 * @author Stephan Pakebusch
 */
public class RequiresEditorRefreshTest {

    @Test
    public void testViewsRequireTheEditorRefreshByDefault() {
        IPreferencesController plainView = new BasePreferencesController() {
            @Override
            public javax.swing.JComponent getGui() {
                return null;
            }

            @Override
            public void persist() {
            }

            @Override
            public void undoChanges() {
            }

            @Override
            public void restoreDefaults() {
            }

            @Override
            protected void initFromPrefs() {
            }

            @Override
            public String toString() {
                return "plain view";
            }
        };
        assertTrue(plainView.requiresEditorRefresh());
    }

    @Test
    public void testColorsViewAppliesItselfWithoutTheRefresh() {
        assertFalse(new CustomColorSelectionController().requiresEditorRefresh());
    }
}
