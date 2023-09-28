/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
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

package org.omegat.languagetools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
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
import org.omegat.core.data.TMXEntry.ExternalLinked;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.languagetools.LanguageToolWrapper.LanguageToolMarker;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FalseFriendsTest extends TestCore {
    @Before
    public final void setUp() {
        final ProjectProperties props = new ProjectProperties() {
            public Language getSourceLanguage() {
                return new Language("en");
            }

            public Language getTargetLanguage() {
                return new Language("pl");
            }
        };

        Core.setProject(new IProject() {
            public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans,
                    boolean defaultTranslation, TMXEntry.ExternalLinked externalLinked) {
            }

            public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans,
                    boolean defaultTranslation, ExternalLinked externalLinked,
                    AllTranslations previousTranslations) throws OptimisticLockingFail {
            }

            public void setNote(SourceTextEntry entry, TMXEntry oldTrans, String note) {
            }

            public void saveProjectProperties() throws Exception {
            }

            public void saveProject(boolean doTeamSync) {
            }

            public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
            }

            public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
            }

            public boolean isProjectModified() {
                return false;
            }

            public boolean isProjectLoaded() {
                return true;
            }

            public boolean isOrphaned(EntryKey entry) {
                return false;
            }

            public boolean isOrphaned(String source) {
                return false;
            }

            public TMXEntry getTranslationInfo(SourceTextEntry ste) {
                return null;
            }

            public AllTranslations getAllTranslations(SourceTextEntry ste) {
                return null;
            }

            public Map<String, ExternalTMX> getTransMemories() {
                return null;
            }

            public ITokenizer getTargetTokenizer() {
                return null;
            }

            public StatisticsInfo getStatistics() {
                return null;
            }

            public ITokenizer getSourceTokenizer() {
                return null;
            }

            public ProjectProperties getProjectProperties() {
                return props;
            }

            public List<FileInfo> getProjectFiles() {
                return null;
            }

            public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
                return null;
            }

            public List<SourceTextEntry> getAllEntries() {
                return null;
            }

            public void compileProject(String sourcePattern) throws Exception {
            }

            public void closeProject() {
            }

            public List<String> getSourceFilesOrder() {
                return null;
            }

            public void setSourceFilesOrder(List<String> filesList) {
            }

            @Override
            public String getTargetPathForSourceFile(String sourceFile) {
                return null;
            }

            @Override
            public boolean isTeamSyncPrepared() {
                return false;
            }

            @Override
            public void teamSync() {
            }

            @Override
            public void teamSyncPrepare() throws Exception {
            }

            @Override
            public boolean isRemoteProject() {
                return false;
            }

            @Override
            public void commitSourceFiles() throws Exception {
            }

            @Override
            public void compileProjectAndCommit(String sourcePattern, boolean doPostProcessing, boolean commitTargetFiles)
            throws Exception {
            }
        });
        LanguageToolWrapper.setBridgeFromCurrentProject();
    }

    @Test
    public void testExecute() throws Exception {
        LanguageToolMarker marker = new LanguageToolMarker() {
            public boolean isEnabled() {
                return true;
            };
        };

        List<Mark> marks = marker.getMarksForEntry(null, "This is abnegation.", "To jest abnegacja.", true);
        assertEquals(1, marks.size());
        assertTrue(marks.get(0).toolTipText.contains("slovenliness"));
    }

    @Test
    public void testRemoveRules() throws Exception {
        LanguageToolMarker marker = new LanguageToolMarker() {
            public boolean isEnabled() {
                return true;
            };
        };

        List<Mark> marks = marker.getMarksForEntry(null, "This is some long text without translation.", "", true);
        assertEquals(0, marks.size());

        marks = marker.getMarksForEntry(null, "This is text with the same translation.",
                "This is text with the same translation.", true);
        assertEquals(0, marks.size());
    }
}
