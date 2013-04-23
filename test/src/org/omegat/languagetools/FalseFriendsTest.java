/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
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

package org.omegat.languagetools;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IProjectEventListener.PROJECT_CHANGE_TYPE;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FalseFriendsTest extends TestCore {
    protected void setUp() throws Exception {
        super.setUp();

        final ProjectProperties props = new ProjectProperties() {
            public Language getSourceLanguage() {
                return new Language("en");
            }

            public Language getTargetLanguage() {
                return new Language("pl");
            }
        };

        Core.setProject(new IProject() {
            public void setTranslation(SourceTextEntry entry, String trans, String note, boolean isDefault) {
            }

            public void saveProjectProperties() throws Exception {
            }

            public void saveProject() {
            }

            public void iterateByMultipleTranslations(MultipleTranslationsIterator it) {
            }

            public void iterateByDefaultTranslations(DefaultTranslationsIterator it) {
            }

            public boolean isProjectModified() {
                return false;
            }

            public boolean isProjectLoaded() {
                return false;
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

            public IRemoteRepository getRepository() {
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
        });
    }

    @Test
    public void testExecute() throws Exception {
        LanguageToolWrapper wrapper = new LanguageToolWrapper();

        wrapper.onProjectChanged(PROJECT_CHANGE_TYPE.LOAD);

        List<Mark> marks = wrapper.getMarksForEntry(null, "This is abnegation.", "To jest abnegacja.", true);
        assertEquals(1, marks.size());
        assertTrue(marks.get(0).toolTipText.contains("slovenliness"));
    }

    @Test
    public void testRemoveRules() throws Exception {
        LanguageToolWrapper wrapper = new LanguageToolWrapper();

        wrapper.onProjectChanged(PROJECT_CHANGE_TYPE.LOAD);

        List<Mark> marks = wrapper.getMarksForEntry(null, "This is some long text without translation.", "", true);
        assertEquals(0, marks.size());

        marks = wrapper.getMarksForEntry(null, "This is text with the same translation.",
                "This is text with the same translation.", true);
        assertEquals(0, marks.size());
    }
}
