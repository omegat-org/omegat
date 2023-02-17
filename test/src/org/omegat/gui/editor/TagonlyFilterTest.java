/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2023 Thomas Cordonnier
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

package org.omegat.gui.editor;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

public class TagonlyFilterTest extends org.omegat.core.TestCore {

    @Test
    public void testEditorFilter() {
        EditorSettings settings  = new EditorSettings(null);
        // False => null
        settings.setHideTagonlySegments(false);
        assertNull(settings.getMenusFilter());
        // True : return a filter and test it
        settings.setHideTagonlySegments(true);
        IEditorFilter filter1 = settings.getMenusFilter();
        EntryKey ek1 = new EntryKey(null, "Sample with <t1>tag</t1> and text", null, null, null, null);
        SourceTextEntry ste1 = new SourceTextEntry(ek1, 1, null, null, Collections.emptyList(), false);
        assertTrue(filter1.allowed(ste1));
        EntryKey ek2 = new EntryKey(null, " <t1> </t1> ", null, null, null, null);
        SourceTextEntry ste2 = new SourceTextEntry(ek2, 1, null, null, Collections.emptyList(), false);
        assertFalse(filter1.allowed(ste2));
    }

}
