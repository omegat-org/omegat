/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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
import java.util.Map;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.Language;

/**
 * @author Hiroshi Miura
 */
public class GlossarySearcherTest extends TestCore {
    @Test
    public void testGlossarySearcherEnglish() {
        String sourceText = "source";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneEnglishTokenizer();
        Language language = new Language("en");
        setupProject(language);
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        glossarySearcherCommon(sourceText, tok, language, entries);
    }

    @Test
    public void testIsCjkMatchJapanese() {
        String sourceText = "\u5834\u6240";
        String targetText = "\u5857\u5E03";
        Language language = new Language("ja");
        setupProject(language);
        assertTrue(GlossarySearcher.isCjkMatch(sourceText, sourceText));
        assertFalse(GlossarySearcher.isCjkMatch(sourceText, targetText));
    }

    @Test
    public void testGlossarySearcherJapanese1() {
        String sourceText = "\u5834\u6240";
        String translationText = "translation";
        String commentText = "comment";
        ITokenizer tok = new LuceneJapaneseTokenizer();
        Language language = new Language("ja");
        setupProject(language);
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry(sourceText, translationText, commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(1, result.size());
        assertEquals(sourceText, result.get(0).getSrcText());
        assertEquals(commentText, result.get(0).getCommentText());
        assertEquals(translationText, result.get(0).getLocText());
    }

    @Test
    public void testGlossarySearcherJapanese2() {
        String sourceText = "\u5834\u6240";
        String translationText = "translation";
        String commentText = "comment";
        Language language = new Language("ja");
        setupProject(language);
        ITokenizer tok = new LuceneJapaneseTokenizer();
        List<GlossaryEntry> entries = new ArrayList<>();
        entries.add(new GlossaryEntry("\u5857\u5E03", "wrong", commentText, true, "origin"));
        List<GlossaryEntry> result = glossarySearcherCommon(sourceText, tok, language, entries);
        assertEquals(0, result.size());
    }

    private void setupProject(Language language) {
        // setup project
        final ProjectProperties props = new ProjectProperties() {
            public Language getSourceLanguage() {
                return language;
            }

            public Language getTargetLanguage() {
                return new Language("pl");
            }
        };

            Core.setProject(new

        IProject() {
            public void setTranslation (SourceTextEntry entry, PrepareTMXEntry trans,
            boolean defaultTranslation, TMXEntry.ExternalLinked externalLinked){
            }

            public void setTranslation (SourceTextEntry entry, PrepareTMXEntry trans,
            boolean defaultTranslation, TMXEntry.ExternalLinked externalLinked,
            AllTranslations previousTranslations){
            }

            public void setNote (SourceTextEntry entry, TMXEntry oldTrans, String note){
            }

            public void saveProjectProperties () {
            }

            public void saveProject ( boolean doTeamSync){
            }

            public void iterateByMultipleTranslations (MultipleTranslationsIterator it){
            }

            public void iterateByDefaultTranslations (DefaultTranslationsIterator it){
            }

            public boolean isProjectModified () {
                return false;
            }

            public boolean isProjectLoaded () {
                return true;
            }

            public boolean isOrphaned (EntryKey entry){
                return false;
            }

            public boolean isOrphaned (String source){
                return false;
            }

            public TMXEntry getTranslationInfo (SourceTextEntry ste){
                return null;
            }

            public AllTranslations getAllTranslations (SourceTextEntry ste){
                return null;
            }

            public Map<String, ExternalTMX> getTransMemories () {
                return null;
            }

            public ITokenizer getTargetTokenizer () {
                return null;
            }

            public StatisticsInfo getStatistics () {
                return null;
            }

            public ITokenizer getSourceTokenizer () {
                return null;
            }

            public ProjectProperties getProjectProperties () {
                return props;
            }

            public List<IProject.FileInfo> getProjectFiles () {
                return null;
            }

            public Map<Language, ProjectTMX> getOtherTargetLanguageTMs () {
                return null;
            }

            public List<SourceTextEntry> getAllEntries () {
                return null;
            }

            public void compileProject (String sourcePattern){
            }

            public void closeProject () {
            }

            public List<String> getSourceFilesOrder () {
                return null;
            }

            public void setSourceFilesOrder (List < String > filesList) {
            }

            @Override
            public String getTargetPathForSourceFile (String sourceFile){
                return null;
            }

            @Override
            public boolean isTeamSyncPrepared () {
                return false;
            }

            @Override
            public void teamSync () {
            }

            @Override
            public void teamSyncPrepare () {
            }

            @Override
            public boolean isRemoteProject () {
                return false;
            }

            @Override
            public void commitSourceFiles () {
            }

            @Override
            public void compileProjectAndCommit (String sourcePattern,boolean doPostProcessing, boolean commitTargetFiles){
            }
        });
    }
    private List<GlossaryEntry> glossarySearcherCommon(String sourceText, ITokenizer tok, Language language,
                                                       List<GlossaryEntry> entries) {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        String[] prop = new String[]{};
        SourceTextEntry ste = new SourceTextEntry(key, 1, prop, sourceText, new ArrayList<>());
        GlossarySearcher searcher = new GlossarySearcher(tok, language, false);
        return searcher.searchSourceMatches(ste, entries);
    }

}
