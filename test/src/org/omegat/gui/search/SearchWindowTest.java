/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.search;

import java.awt.HeadlessException;
import java.util.List;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchMode;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;

public class SearchWindowTest extends TestCore {

    @Test
    public void testLoadSearchWindow() {
        try {
            new SearchWindowController(SearchMode.SEARCH);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testLoadSearchAndReplaceWindow() {
        try {
            new SearchWindowController(SearchMode.REPLACE);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IEditorSettings editorSettings = new IEditorSettings() {

            @Override
            public boolean isUseTabForAdvance() {
                return false;
            }

            @Override
            public void setUseTabForAdvance(boolean useTabForAdvance) {
            }

            @Override
            public boolean isMarkTranslated() {
                return false;
            }

            @Override
            public void setMarkTranslated(boolean markTranslated) {
            }

            @Override
            public boolean isMarkUntranslated() {
                return false;
            }

            @Override
            public void setMarkUntranslated(boolean markUntranslated) {
            }

            @Override
            public boolean isMarkAutoPopulated() {
                return false;
            }

            @Override
            public void setMarkAutoPopulated(boolean markAutoPopulated) {
            }

            @Override
            public boolean isDisplaySegmentSources() {
                return false;
            }

            @Override
            public void setDisplaySegmentSources(boolean displaySegmentSources) {
            }

            @Override
            public boolean isMarkNonUniqueSegments() {
                return false;
            }

            @Override
            public void setMarkNonUniqueSegments(boolean markNonUniqueSegments) {
            }

            @Override
            public boolean isMarkNotedSegments() {
                return false;
            }

            @Override
            public void setMarkNotedSegments(boolean markNotedSegments) {
            }

            @Override
            public boolean isMarkNBSP() {
                return false;
            }

            @Override
            public void setMarkNBSP(boolean markNBSP) {
            }

            @Override
            public boolean isMarkWhitespace() {
                return false;
            }

            @Override
            public void setMarkWhitespace(boolean markWhitespace) {
            }

            @Override
            public boolean isMarkBidi() {
                return false;
            }

            @Override
            public void setMarkBidi(boolean markBidi) {
            }

            @Override
            public boolean isAutoSpellChecking() {
                return false;
            }

            @Override
            public void setAutoSpellChecking(boolean isNeedToSpell) {
            }

            @Override
            public boolean isDoFontFallback() {
                return false;
            }

            @Override
            public void setDoFontFallback(boolean doFallback) {
            }

            @Override
            public String getDisplayModificationInfo() {
                return null;
            }

            @Override
            public void setDisplayModificationInfo(String displayModificationInfo) {
            }

            @Override
            public void updateTagValidationPreferences() {
            }

            @Override
            public void updateViewPreferences() {
            }
        };
        TestCoreInitializer.initEditor(new IEditor() {

            @Override
            public void windowDeactivated() {
            }

            @Override
            public void waitForCommit(int timeoutSeconds) {
            }

            @Override
            public void undo() {
            }

            @Override
            public void setFilter(IEditorFilter filter) {
            }

            @Override
            public void setAlternateTranslationForCurrentEntry(boolean alternate) {
            }

            @Override
            public void requestFocus() {
            }

            @Override
            public void replaceEditTextAndMark(String text) {
            }

            @Override
            public void replaceEditText(String text) {
            }

            @Override
            public void removeFilter() {
            }

            @Override
            public void remarkOneMarker(String markerClassName) {
            }

            @Override
            public void registerUntranslated() {
            }

            @Override
            public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
            }

            @Override
            public void registerIdenticalTranslation() {
            }

            @Override
            public void registerEmptyTranslation() {
            }

            @Override
            public void refreshViewAfterFix(List<Integer> fixedEntries) {
            }

            @Override
            public void refreshView(boolean doCommit) {
            }

            @Override
            public void redo() {
            }

            @Override
            public void prevEntryWithNote() {
            }

            @Override
            public void prevEntry() {
            }

            @Override
            public void nextUntranslatedEntry() {
            }

            @Override
            public void nextUniqueEntry() {
            }

            @Override
            public void nextTranslatedEntry() {
            }

            @Override
            public void nextEntryWithNote() {
            }

            @Override
            public void nextEntry() {
            }

            @Override
            public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
                    String markerClassName) {
            }

            @Override
            public void insertText(String text) {
            }

            @Override
            public void gotoHistoryForward() {
            }

            @Override
            public void gotoHistoryBack() {
            }

            @Override
            public void gotoFile(int fileIndex) {
            }

            @Override
            public void gotoEntryAfterFix(int fixedEntry, String fixedSource) {
            }

            @Override
            public void gotoEntry(String srcString, EntryKey key) {
            }

            @Override
            public void gotoEntry(int entryNum) {
            }

            @Override
            public IEditorSettings getSettings() {
                return editorSettings;
            }

            @Override
            public String getSelectedText() {
                return null;
            }

            @Override
            public IEditorFilter getFilter() {
                return null;
            }

            @Override
            public String getCurrentTranslation() {
                return null;
            }

            @Override
            public String getCurrentTargetFile() {
                return null;
            }

            @Override
            public String getCurrentFile() {
                return null;
            }

            @Override
            public int getCurrentEntryNumber() {
                return 0;
            }

            @Override
            public SourceTextEntry getCurrentEntry() {
                return null;
            }

            @Override
            public IAutoCompleter getAutoCompleter() {
                return null;
            }

            @Override
            public void commitAndLeave() {
            }

            @Override
            public void commitAndDeactivate() {
            }

            @Override
            public void changeCase(CHANGE_CASE_TO newCase) {
            }

            @Override
            public void activateEntry() {
            }
        });
    }
}
