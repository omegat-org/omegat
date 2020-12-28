/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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
import static org.junit.Assert.assertTrue;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

public class GlossaryAutoCompleterViewTest extends TestCore {

    private final List<GlossaryEntry> currentEntries = new ArrayList<>();

    @Test
    public void testSuggestions() {
        GlossaryAutoCompleterView view = new GlossaryAutoCompleterView();
        List<AutoCompleterItem> result = view.computeListData("blah", false);

        assertTrue(result.isEmpty());

        Stream.of("foo", "bar", "BAZ").map(t -> new GlossaryEntry("", t, "", true, null))
                .forEach(currentEntries::add);

        // No context -> suggest everything
        result = view.computeListData("", false);
        assertEquals(3, result.size());

        // Context with match -> suggest only match(es)
        result = view.computeListData("f", false);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0).payload);

        result = view.computeListData("b", false);
        assertEquals(3, result.size());
        assertEquals("bar", result.get(0).payload);
        assertEquals("baz", result.get(1).payload);
        assertEquals("BAZ", result.get(2).payload);

        // Context match with differing case -> suggest matched case and original
        result = view.computeListData("F", false);
        assertEquals(2, result.size());
        assertEquals("Foo", result.get(0).payload);
        assertEquals("foo", result.get(1).payload);

        result = view.computeListData("FO", false);
        assertEquals(2, result.size());
        assertEquals("FOO", result.get(0).payload);
        assertEquals("foo", result.get(1).payload);

        result = view.computeListData("B", false);
        assertEquals(3, result.size());
        // BAZ comes first because it is a verbatim glossary term
        assertEquals("BAZ", result.get(0).payload);
        assertEquals("Bar", result.get(1).payload);

        result = view.computeListData("Ba", false);
        assertEquals(3, result.size());
        assertEquals("Bar", result.get(0).payload);
        assertEquals("bar", result.get(1).payload);
        assertEquals("BAZ", result.get(2).payload);

        // Context match that matches entire option -> counts as no match,
        // so fall back to suggesting everything
        result = view.computeListData("foo", false);
        assertEquals(3, result.size());

        // In-context only
        result = view.computeListData("", true);
        assertTrue(result.isEmpty());

        result = view.computeListData("f", true);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0).payload);
    }

    @Before
    public final void setUp() {
        Core.setProject(new NotLoadedProject() {
            @Override
            public ITokenizer getTargetTokenizer() {
                return new DefaultTokenizer();
            }
            @Override
            public ProjectProperties getProjectProperties() {
                try {
                    return new ProjectProperties() {
                        @Override
                        public Language getTargetLanguage() {
                            return new Language(Locale.ENGLISH);
                        }
                    };
                } catch (Exception e) {
                    return null;
                }
            }
        });
        TestCoreInitializer.initEditor(new IEditor() {

            @Override
            public String getCurrentFile() {
                return null;
            }

            @Override
            public String getCurrentTargetFile() {
                return null;
            }

            @Override
            public SourceTextEntry getCurrentEntry() {
                return null;
            }

            @Override
            public int getCurrentEntryNumber() {
                return 0;
            }

            @Override
            public void activateEntry() {
            }

            @Override
            public void commitAndDeactivate() {
            }

            @Override
            public void commitAndLeave() {
            }

            @Override
            public void nextEntry() {
            }

            @Override
            public void prevEntry() {
            }

            @Override
            public void nextEntryWithNote() {
            }

            @Override
            public void prevEntryWithNote() {
            }

            @Override
            public void nextUntranslatedEntry() {
            }

            @Override
            public void nextTranslatedEntry() {
            }

            @Override
            public void nextUniqueEntry() {
            }

            @Override
            public void gotoFile(int fileIndex) {
            }

            @Override
            public void gotoEntry(int entryNum) {
            }

            @Override
            public void gotoEntry(int entryNum, CaretPosition pos) {
            }

            @Override
            public void gotoEntry(String srcString, EntryKey key) {
            }

            @Override
            public void gotoEntryAfterFix(int fixedEntry, String fixedSource) {
            }

            @Override
            public void refreshViewAfterFix(List<Integer> fixedEntries) {
            }

            @Override
            public void refreshView(boolean doCommit) {
            }

            @Override
            public void requestFocus() {
            }

            @Override
            public void changeCase(CHANGE_CASE_TO newCase) {
            }

            @Override
            public void replaceEditText(String text) {
            }

            @Override
            public void replaceEditTextAndMark(String text) {
            }

            @Override
            public void insertText(String text) {
            }

            @Override
            public void insertTextAndMark(String text) {
            }

            @Override
            public void insertTag(String tag) {
            }

            @Override
            public void gotoHistoryForward() {
            }

            @Override
            public void gotoHistoryBack() {
            }

            @Override
            public IEditorSettings getSettings() {
                return null;
            }

            @Override
            public void undo() {
            }

            @Override
            public void redo() {
            }

            @Override
            public String getSelectedText() {
                return null;
            }

            @Override
            public void setAlternateTranslationForCurrentEntry(boolean alternate) {
            }

            @Override
            public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
                    String markerClassName) {
            }

            @Override
            public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
            }

            @Override
            public void remarkOneMarker(String markerClassName) {
            }

            @Override
            public IEditorFilter getFilter() {
                return null;
            }

            @Override
            public void setFilter(IEditorFilter filter) {
            }

            @Override
            public void removeFilter() {
            }

            @Override
            public String getCurrentTranslation() {
                return null;
            }

            @Override
            public void windowDeactivated() {
            }

            @Override
            public void registerUntranslated() {
            }

            @Override
            public void registerEmptyTranslation() {
            }

            @Override
            public void registerIdenticalTranslation() {
            }

            @Override
            public IAutoCompleter getAutoCompleter() {

                return null;
            }

        });
        TestCoreInitializer.initGlossary(new IGlossaries() {

            @Override
            public List<GlossaryEntry> getDisplayedEntries() {
                return currentEntries;
            }

            @Override
            public void showCreateGlossaryEntryDialog(Frame parent) {
            }
        });
        currentEntries.clear();
    }
}
