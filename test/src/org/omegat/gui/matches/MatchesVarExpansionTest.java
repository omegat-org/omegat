/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier, Aaron Madlon-Kay
               2013-2014 Aaron Madlon-Kay
               2014 Alex Buloichik
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

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.NearString;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IEditorSettings;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Language;
import org.omegat.util.TMXProp;

public class MatchesVarExpansionTest {

    final Language LTR_LANGUAGE = new Language("pl");
    final Language RTL_LANGUAGE = new Language("ar");
    final Locale LTR_LOCALE = LTR_LANGUAGE.getLocale();
    final Locale RTL_LOCALE = RTL_LANGUAGE.getLocale();
    final Locale INITIAL_LOCALE = Locale.getDefault();

    final String VAR_CREATION_ID = "${creationId}";
    final String VAR_CREATION_DATE = "${creationDate}";
    final String VAR_INITIAL_CREATION_ID = "${initialCreationId}";
    final String VAR_INITIAL_CREATION_DATE = "${initialCreationDate}";
    final String VAR_CHANGED_ID = "${changedId}";
    final String VAR_CHANGED_DATE = "${changedDate}";
    final String VAR_FUZZY_FLAG = "${fuzzyFlag}";
    final String VAR_DIFF = "${diff}";
    final String VAR_DIFF_REVERSED = "${diffReversed}";
    final String VAR_SOURCE_LANGUAGE = "${sourceLanguage}";
    final String VAR_TARGET_LANGUAGE = "${targetLanguage}";
    final String VAR_SOURCE_TEXT = "${sourceText}";
    final String VAR_TARGET_TEXT = "${targetText}";
    final String VAR_PROJECT_SOURCE_LANG = "${projectSourceLang}";
    final String VAR_PROJECT_SOURCE_LANG_CODE = "${projectSourceLangCode}";
    final String VAR_PROJECT_TARGET_LANG = "${projectTargetLang}";
    final String VAR_PROJECT_TARGET_LANG_CODE = "${projectTargetLangCode}";
    final String VAR_FILE_NAME = "${fileName}";
    final String VAR_FILE_NAME_ONLY = "${fileNameOnly}";
    final String VAR_FILE_EXTENSION = "${fileExtension}";
    final String VAR_FILE_PATH = "${filePath}";
    final String VAR_FILE_SHORT_PATH = "${fileShortPath}";
    final String VAR_ID = "${id}";
    final String VAR_SCORE_BASE = "${score}";
    final String VAR_SCORE_NOSTEM = "${noStemScore}";
    final String VAR_SCORE_ADJUSTED = "${adjustedScore}";

    final String DEFAULT_TEMPLATE = VAR_ID + ". " + VAR_FUZZY_FLAG + VAR_SOURCE_TEXT + "\n" + VAR_TARGET_TEXT + "\n"
            + "<" + VAR_SCORE_BASE + "/" + VAR_SCORE_NOSTEM + "/" + VAR_SCORE_ADJUSTED + "% " + VAR_FILE_PATH + ">";

    final String TEST_TEMPLATE = VAR_ID + ". ... " + VAR_SOURCE_TEXT + "\n" + VAR_TARGET_TEXT
            + "\n" + "<" + VAR_SCORE_BASE + "/" + VAR_SCORE_NOSTEM + "/" + VAR_SCORE_ADJUSTED + "% "
            + VAR_FILE_PATH + ">";

    final String BIG_TEMPLATE = VAR_ID + ". " + VAR_PROJECT_SOURCE_LANG + " - " + VAR_FILE_NAME + " "
            + VAR_SOURCE_LANGUAGE + ": "
            + VAR_SOURCE_TEXT + "\n" + VAR_TARGET_LANGUAGE + " " + VAR_TARGET_TEXT + "\n"
            + "<" + VAR_SCORE_BASE + "/" + VAR_SCORE_NOSTEM + "/" + VAR_SCORE_ADJUSTED + "% " + VAR_FILE_PATH
            + " by " + VAR_INITIAL_CREATION_ID + " >\n" + VAR_PROJECT_TARGET_LANG;

    final String TEST_BIDI_TEMPLATE = VAR_SOURCE_TEXT + " and " + VAR_TARGET_TEXT;

    @Before
    public void setUp() {
        TestCoreInitializer.initEditor(editor);
    }

    @After
    public void resetInitialLocale() {
        Locale.setDefault(INITIAL_LOCALE);
    }

    @Test
    public void testApplyBiDiReplacers_allRtl() {
        Locale.setDefault(RTL_LOCALE);
        setupAllRtlProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(TEST_BIDI_TEMPLATE);
        String expected = "mock source text and mock target text";
        String actual = expander.apply(getMockNearString(), 2).text;
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyBiDiReplacers_allLtr() {
        Locale.setDefault(LTR_LOCALE);
        setupAllLtrProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(TEST_BIDI_TEMPLATE);
        String expected = "mock source text and mock target text";
        String actual = expander.apply(getMockNearString(), 2).text;
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyBiDiReplacers_rtlToLtr() {
        Locale.setDefault(RTL_LOCALE);
        setupRtlToLtrProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(TEST_BIDI_TEMPLATE);
        String expected = BiDiUtils.BIDI_RLE + "mock source text" + BiDiUtils.BIDI_PDF + " and "
                + BiDiUtils.BIDI_LRE + "mock target text" + BiDiUtils.BIDI_PDF;
        String actual = expander.apply(getMockNearString(), 2).text;
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyBiDiReplacers_ltrToRtl() {
        Locale.setDefault(LTR_LOCALE);
        setupLtrToRtlProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(TEST_BIDI_TEMPLATE);
        String expected = BiDiUtils.BIDI_LRE + "mock source text" + BiDiUtils.BIDI_PDF + " and "
                + BiDiUtils.BIDI_RLE + "mock target text" + BiDiUtils.BIDI_PDF;
        String actual = expander.apply(getMockNearString(), 2).text;
        assertEquals(expected, actual);
    }

    @Test
    public void testApply_allLtr() {
        Locale.setDefault(LTR_LOCALE);
        setupAllLtrProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(BIG_TEMPLATE);
        String expected = "2. pl - mock testing project mock source language: mock source text\nmock target language mock target text\n<20/40/60% mock testing project by mock creator >\npl";
        String actual = expander.apply(getMockNearString(), 2).text;
        assertEquals(expected, actual);
    }

    @Test
    public void testExpandVariables() {
        Locale.setDefault(LTR_LOCALE);
        setupAllLtrProject();
        MatchesVarExpansion expander = new MatchesVarExpansion(TEST_TEMPLATE);
        String expected = "${id}. ... ${sourceText}\nmock target text\n<20/40/60% mock testing project>";
        String actual = expander.expandVariables(getMockNearString());
        assertEquals(expected, actual);
    }

    // Helper methods

    private void setupAllLtrProject() {
        setupProject(LTR_LANGUAGE, LTR_LANGUAGE);
    }

    private void setupAllRtlProject() {
        setupProject(RTL_LANGUAGE, RTL_LANGUAGE);
    }

    private void setupRtlToLtrProject() {
        setupProject(RTL_LANGUAGE, LTR_LANGUAGE);
    }

    private void setupLtrToRtlProject() {
        setupProject(LTR_LANGUAGE, RTL_LANGUAGE);
    }

    public NearString getMockNearString() {
        List<TMXProp> testProps = new ArrayList<>();
        testProps.add(new TMXProp("sourceLanguage", "mock source language"));
        testProps.add(new TMXProp("targetLanguage", "mock target language"));

        return new NearString(null, "mock source text", "mock target text", null, false, 20, 40, 60, null,
                "mock testing project", "mock creator", 20020523, "mock modifier", 20020523, testProps);
    };

    private void setupProject(Language sourceLanguage, Language targetLanguage) {
        Core.setProject(new NotLoadedProject() {
            @Override
            public boolean isProjectLoaded() {
                return true;
            }

            @Override
            public ProjectProperties getProjectProperties() {
                return new ProjectProperties() {
                    @Override
                    public Language getSourceLanguage() {
                        return sourceLanguage;
                    }

                    @Override
                    public String getTMRoot() {
                        return "/mock/testing/path";
                    }

                    @Override
                    public Language getTargetLanguage() {
                        return targetLanguage;
                    }
                };
            }
        });
    }

    final IEditor editor = new IEditor() {

        @Override
        public SourceTextEntry getCurrentEntry() {
            return null;
        }

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
            return null;
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
    };

}
