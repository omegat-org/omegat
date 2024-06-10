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
package org.omegat.gui.editor.mark;

import java.util.List;

import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;

public class MarkerTestBase extends TestCore {


    IEditorSettings editorSettings = new MockEditorSettings();

    IEditor editor = new MockEditor();

    public static class MockEditorSettings implements IEditorSettings {

        private boolean useTabForAdvance;
        private boolean markTranslated;
        private boolean markUntranslated;
        private boolean markAutoPopulated;
        private boolean displaySegmentSources;
        private boolean markNonUniqueSegments;
        private boolean markNoted;
        private boolean markNBSP;
        private boolean markWhitespace;
        private boolean markParagraphDelimitations;
        private boolean markBidi;
        private String displayModificationInfo;
        private boolean autoSpellChecking;
        private boolean viewSourceBold;
        private boolean viewActiveSourceBold;
        private boolean markFirstNonUnique;
        private boolean markGlossaryMatches;
        private boolean markLanguageChecker;
        private boolean doFontFallback;
        private boolean markAlt;

        @Override
        public boolean isUseTabForAdvance() {
            return useTabForAdvance;
        }

        @Override
        public void setUseTabForAdvance(boolean useTabForAdvance) {
            this.useTabForAdvance = useTabForAdvance;
        }

        @Override
        public boolean isMarkTranslated() {
            return markTranslated;
        }

        @Override
        public void setMarkTranslated(boolean markTranslated) {
            this.markTranslated = markTranslated;
        }

        @Override
        public boolean isMarkUntranslated() {
            return markUntranslated;
        }

        @Override
        public void setMarkUntranslated(boolean markUntranslated) {
            this.markUntranslated = markUntranslated;
        }

        @Override
        public boolean isMarkAutoPopulated() {
            return markAutoPopulated;
        }

        @Override
        public void setMarkAutoPopulated(boolean markAutoPopulated) {
            this.markAutoPopulated = markAutoPopulated;
        }

        @Override
        public boolean isDisplaySegmentSources() {
            return displaySegmentSources;
        }

        @Override
        public void setDisplaySegmentSources(boolean displaySegmentSources) {
            this.displaySegmentSources = displaySegmentSources;
        }

        @Override
        public boolean isMarkNonUniqueSegments() {
            return markNonUniqueSegments;
        }

        @Override
        public void setMarkNonUniqueSegments(boolean markNonUniqueSegments) {
            this.markNonUniqueSegments = markNonUniqueSegments;
        }

        @Override
        public boolean isMarkNotedSegments() {
            return markNoted;
        }

        @Override
        public void setMarkNotedSegments(boolean markNotedSegments) {
            markNoted = markNotedSegments;
        }

        @Override
        public boolean isMarkNBSP() {
            return markNBSP;
        }

        @Override
        public void setMarkNBSP(boolean markNBSP) {
            this.markNBSP = markNBSP;
        }

        @Override
        public boolean isMarkWhitespace() {
            return markWhitespace;
        }

        @Override
        public void setMarkWhitespace(boolean markWhitespace) {
            this.markWhitespace = markWhitespace;
        }

        @Override
        public boolean isMarkBidi() {
            return markBidi;
        }

        @Override
        public void setMarkBidi(boolean markBidi) {
            this.markBidi = markBidi;
        }

        @Override
        public boolean isMarkAltTranslations() {
            return markAlt;
        }

        @Override
        public void setMarkAltTranslations(final boolean markAltTranslations) {
            markAlt = markAltTranslations;
        }

        @Override
        public boolean isAutoSpellChecking() {
            return autoSpellChecking;
        }

        @Override
        public void setAutoSpellChecking(boolean isNeedToSpell) {
            this.autoSpellChecking = isNeedToSpell;
        }

        @Override
        public boolean isDoFontFallback() {
            return doFontFallback;
        }

        @Override
        public void setDoFontFallback(boolean doFallback) {
            this.doFontFallback = doFallback;
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

        @Override
        public boolean isMarkLanguageChecker() {
            return markLanguageChecker;
        }

        @Override
        public void setMarkLanguageChecker(boolean markLanguageChecker) {
            this.markLanguageChecker = markLanguageChecker;
        }

        @Override
        public boolean isMarkGlossaryMatches() {
            return markGlossaryMatches;
        }

        @Override
        public void setMarkGlossaryMatches(boolean markGlossaryMatches) {
            this.markGlossaryMatches = markGlossaryMatches;
        }

        @Override
        public void setMarkParagraphDelimitations(boolean mark) {
            markParagraphDelimitations = mark;
        }

        @Override
        public boolean isMarkParagraphDelimitations() {
            return markParagraphDelimitations;
        }
    };

    public class MockEditor implements IEditor {

        @Override
        public void windowDeactivated() {
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
        public void replaceEditTextAndMark(final String text, final String origin) {
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
        public void gotoEntry(int entryNum, CaretPosition pos) {
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
        public void changeCase(IEditor.CHANGE_CASE_TO newCase) {
        }

        @Override
        public void replaceEditText(final String text, final String origin) {

        }

        @Override
        public void activateEntry() {
        }

        @Override
        public boolean isOrientationAllLtr() {
            return true;
        }
            
        @Override
        public void unlockSegment() {
        }
    }
}
