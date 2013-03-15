/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Preferences;

/**
 * 
 * @author Maxym Mykhalchuk
 */
public class GlossaryTextAreaTest extends TestCore {
    /**
     * Testing setGlossaryEntries of org.omegat.gui.main.GlossaryTextArea.
     */
    public void testSetGlossaryEntries() throws Exception {
        Preferences.setPreference(org.omegat.util.Preferences.TRANSTIPS, false);

        final List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        final GlossaryTextArea gta = new GlossaryTextArea();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                gta.setFoundResult(null, entries);
            }
        });
        String GTATEXT = "source1 = translation1\n\nsource2 = translation2\ncomment2\n\n";
        if (!gta.getText().equals(GTATEXT))
            fail("Glossary pane doesn't show what it should.");
    }

    /**
     * Testing clear in org.omegat.gui.main.GlossaryTextArea.
     */
    public void testClear() throws Exception {
        Preferences.setPreference(org.omegat.util.Preferences.TRANSTIPS, false);

        final List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        final GlossaryTextArea gta = new GlossaryTextArea();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                gta.setFoundResult(null, entries);
            }
        });
        gta.clear();
        if (gta.getText().length() > 0)
            fail("Glossary pane isn't empty.");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestCoreInitializer.initEditor(new IEditor() {
            public void activateEntry() {
            }

            public void changeCase(CHANGE_CASE_TO newCase) {
            }

            public void commitAndDeactivate() {
            }

            public void commitAndLeave() {
            }

            public SourceTextEntry getCurrentEntry() {
                return null;
            }

            public int getCurrentEntryNumber() {
                return 0;
            }

            public String getCurrentFile() {
                return null;
            }

            public String getSelectedText() {
                return null;
            }

            public EditorSettings getSettings() {
                return null;
            }

            public void gotoEntry(int entryNum) {
            }

            public void gotoFile(int fileIndex) {
            }

            public void gotoHistoryBack() {
            }

            public void gotoHistoryForward() {
            }

            public void insertText(String text) {
            }

            public void setAlternateTranslationForCurrentEntry(boolean alternate) {
            }

            public void markActiveEntrySource(
                    SourceTextEntry requiredActiveEntry, List<Mark> marks,
                    String markerClassName) {
            }

            public void nextEntry() {
            }

            public void nextUntranslatedEntry() {
            }

            public void prevEntry() {
            }

            public void undo() {
            }

            public void redo() {
            }

            public void registerPopupMenuConstructors(int priority,
                    IPopupMenuConstructor constructor) {
            }

            public void replaceEditText(String text) {
            }

            public void requestFocus() {
            }
            public void remarkOneMarker(String markerClassName) {
            }

            public void addFilter(List<Integer> entryList) {
            }

            public void removeFilter() {
            }

            public void setEmptyTranslation(boolean flag) {
            }

            public void activateEntry(int preferredPosition) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void nextEntryWithNote() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void prevEntryWithNote() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public String getCurrentTranslation() {
                return null;
            }
        });
    }
}
