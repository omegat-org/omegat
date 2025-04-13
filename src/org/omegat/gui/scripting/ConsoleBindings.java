/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Briac Pilpre
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
package org.omegat.gui.scripting;

import java.awt.Frame;
import java.util.List;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.IGlossaries;

/** Class mocking the GUI elements when scripts are executed in console mode. */
public class ConsoleBindings implements IGlossaries, IEditor, IScriptLogger {

    @Override
    public final void print(Object o) {
        System.out.print(o.toString());
    }

    @Override
    public final void println(Object o) {
        System.out.println(o.toString());
    }

    @Override
    public void clear() {
    }

    @Override
    public final String getCurrentFile() {
        return null;
    }

    @Override
    public final String getCurrentTargetFile() {
        return null;
    }

    @Override
    public SourceTextEntry getCurrentEntry() {
        return null;
    }

    @Override
    public final int getCurrentEntryNumber() {
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
    public void nextXAutoEntry() {

    }

    @Override
    public void prevXAutoEntry() {

    }

    @Override
    public void nextXEnforcedEntry() {

    }

    @Override
    public void prevXEnforcedEntry() {

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
    public void gotoFile(int fileIndex) throws IndexOutOfBoundsException {

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
    public void replaceEditText(final String text, final String origin) {

    }

    @Override
    public void replaceEditText(String text) {

    }

    @Override
    public void replaceEditTextAndMark(final String text, final String origin) {

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
    public final IEditorSettings getSettings() {
        return null;
    }

    @Override
    public void undo() {

    }

    @Override
    public void redo() {

    }

    @Override
    public final String getSelectedText() {
        return null;
    }

    @Override
    public void selectSourceText() {

    }

    @Override
    public void setAlternateTranslationForCurrentEntry(boolean alternate) {

    }

    @Override
    public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks, String markerClassName) {

    }

    @Override
    public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {

    }

    @Override
    public void remarkOneMarker(String markerClassName) {

    }

    @Override
    public final IEditorFilter getFilter() {
        return null;
    }

    @Override
    public void setFilter(IEditorFilter filter) {

    }

    @Override
    public void removeFilter() {

    }

    @Override
    public final String getCurrentTranslation() {
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
    public final IAutoCompleter getAutoCompleter() {
        return null;
    }

    @Override
    public final void unlockSegment() {
    
    }

    @Override
    public final List<GlossaryEntry> getDisplayedEntries() {
        return null;
    }

    @Override
    public void showCreateGlossaryEntryDialog(Frame parent) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public boolean isOrientationAllLtr() {
        return true;
    }
}
