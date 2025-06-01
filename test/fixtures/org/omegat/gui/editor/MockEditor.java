/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.gui.editor;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;

import java.util.List;

/**
 * MockEditor is a mock implementation of the IEditor interface. It provides
 * a minimal implementation where methods do not perform any specific
 * functionality but are present to fulfill the interface contract.
 */
public class MockEditor implements IEditor {

    private final IEditorSettings editorSettings;

    public MockEditor(IEditorSettings settings) {
        this.editorSettings = settings;
    }

    @Override
    public void windowDeactivated() {
        // do nothing
    }

    @Override
    public void undo() {
        // do nothing
    }

    @Override
    public void setFilter(IEditorFilter filter) {
        // do nothing
    }

    @Override
    public void setAlternateTranslationForCurrentEntry(boolean alternate) {
        // do nothing
    }

    @Override
    public void requestFocus() {
        // do nothing
    }

    @Override
    public void replaceEditTextAndMark(String text) {
        // do nothing
    }

    @Override
    public void replaceEditText(String text) {
        // do nothing
    }

    @Override
    public void replaceEditTextAndMark(final String text, final String origin) {
        // do nothing
    }

    @Override
    public void removeFilter() {
        // do nothing
    }

    @Override
    public void remarkOneMarker(String markerClassName) {
        // do nothing
    }

    @Override
    public void registerUntranslated() {
        // do nothing
    }

    @Override
    public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        // do nothing
    }

    @Override
    public void registerIdenticalTranslation() {
        // do nothing
    }

    @Override
    public void registerEmptyTranslation() {
        // do nothing
    }

    @Override
    public void refreshViewAfterFix(List<Integer> fixedEntries) {
        // do nothing
    }

    @Override
    public void refreshView(boolean doCommit) {
        // do nothing
    }

    @Override
    public void redo() {
        // do nothing
    }

    @Override
    public void prevEntryWithNote() {
        // do nothing
    }

    @Override
    public void prevEntry() {
        // do nothing
    }

    @Override
    public void nextXAutoEntry() {
        // do nothing
    }

    @Override
    public void prevXAutoEntry() {
        // do nothing
    }

    @Override
    public void nextXEnforcedEntry() {
        // do nothing
    }

    @Override
    public void prevXEnforcedEntry() {
        // do nothing
    }

    @Override
    public void nextUntranslatedEntry() {
        // do nothing
    }

    @Override
    public void nextUniqueEntry() {
        // do nothing
    }

    @Override
    public void nextTranslatedEntry() {
        // do nothing
    }

    @Override
    public void nextEntryWithNote() {
        // do nothing
    }

    @Override
    public void nextEntry() {
        // do nothing
    }

    @Override
    public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
                                      String markerClassName) {
        // do nothing
    }

    @Override
    public void insertText(String text) {
        // do nothing
    }

    @Override
    public void insertTextAndMark(String text) {
        // do nothing
    }

    @Override
    public void insertTag(String tag) {
        // do nothing
    }

    @Override
    public void gotoHistoryForward() {
        // do nothing
    }

    @Override
    public void gotoHistoryBack() {
        // do nothing
    }

    @Override
    public void gotoFile(int fileIndex) {
        // do nothing
    }

    @Override
    public void gotoEntryAfterFix(int fixedEntry, String fixedSource) {
        // do nothing
    }

    @Override
    public void gotoEntry(String srcString, EntryKey key) {
        // do nothing
    }

    @Override
    public void gotoEntry(int entryNum) {
        // do nothing
    }

    @Override
    public void gotoEntry(int entryNum, CaretPosition pos) {
        // do nothing
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
    public void selectSourceText() {
        // do nothing
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
        // do nothing
    }

    @Override
    public void commitAndDeactivate() {
        // do nothing
    }

    @Override
    public void changeCase(CHANGE_CASE_TO newCase) {
        // do nothing
    }

    @Override
    public void replaceEditText(final String text, final String origin) {
        // do nothing
    }

    @Override
    public void activateEntry() {
        // do nothing
    }
}
