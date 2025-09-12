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

/**
 * MockEditorSettings is an implementation of the IEditorSettings interface,
 * providing customizable editor settings for marking and displaying various
 * types of content and behavior within the editor.
 * <p>
 * This class allows enabling or disabling specific editor features such as
 * marking translated segments, untranslated segments, automatically populated
 * content, glossary matches, and others. It also includes options for managing
 * the visual display of segment sources, whitespace, paragraph delimitations,
 * and bidi markers. Additionally, it supports auto-spell checking, font fallback,
 * and language checker features.
 * <p>
 * The class maintains a set of boolean properties corresponding to each available
 * setting and provides getter and setter methods for interaction with these properties.
 * Non-applicable methods in the interface are implemented with no operations.
 */
public class EditorSettingsStub implements IEditorSettings {

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
    private boolean autoSpellChecking;
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
        // do nothing
    }

    @Override
    public void updateTagValidationPreferences() {
        // do nothing
    }

    @Override
    public void updateViewPreferences() {
        // do nothing
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
}
