/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Maxym Mykhalchuk
               2008-2014 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.util.Preferences;

/**
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class GlossaryTextAreaTest extends TestCore {
    /**
     * Testing setGlossaryEntries of org.omegat.gui.main.GlossaryTextArea.
     */
    @Test
    public void testSetGlossaryEntries() throws Exception {
        Preferences.setPreference(org.omegat.util.Preferences.MARK_GLOSSARY_MATCHES, false);

        final List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", "", false, null));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2", false, null));
        final GlossaryTextArea gta = new GlossaryTextArea(Core.getMainWindow());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                gta.setFoundResult(null, entries);
            }
        });
        // Make sure representations of both entries are rendered
        DefaultGlossaryRenderer renderer = new DefaultGlossaryRenderer();
        StyledDocument doc = new DefaultStyledDocument();
        renderer.render(entries.get(0), doc);
        renderer.render(entries.get(1), doc);
        String expected = doc.getText(0, doc.getLength());
        assertEquals(expected, gta.getText());
    }

    /**
     * Testing clear in org.omegat.gui.main.GlossaryTextArea.
     */
    @Test
    public void testClear() throws Exception {
        Preferences.setPreference(org.omegat.util.Preferences.MARK_GLOSSARY_MATCHES, false);

        final List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", "", false, null));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2", false, null));
        final GlossaryTextArea gta = new GlossaryTextArea(Core.getMainWindow());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                gta.setFoundResult(null, entries);
            }
        });
        assertFalse(gta.getText().isEmpty());
        SwingUtilities.invokeAndWait(gta::clear);
        assertTrue(gta.getText().isEmpty());
    }
}
