/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022 miurahr.
 *                Home page: http://www.omegat.org/
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.core.search;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.TestPreferencesInitializer;

public class SearcherTest {

    private File tempDir;
    private RealProjectWithTMX proj;
    private IProject.FileInfo fi;

    @Before
    public void preUp() throws Exception {
        tempDir = Files.createTempDirectory("omegat").toFile();
        TestPreferencesInitializer.init();
        ProjectProperties props = new ProjectProperties(tempDir);
        props.setSupportDefaultTranslations(true);
        props.setTargetTokenizer(DefaultTokenizer.class);
        proj = new RealProjectWithTMX(props);
        Core.setProject(proj);
        fi = new IProject.FileInfo();
        proj.getProjectFilesList().add(fi);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testSearch() throws Exception {
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", false);
        SearchExpression s = new SearchExpression();
        s.text = "list";
        s.searchExpressionType = SearchExpression.SearchExpressionType.KEYWORD;
        s.mode = SearchMode.SEARCH;
        s.glossary = false;
        s.memory = true;
        s.tm = true;
        s.allResults = true;
        s.fileNames = false;
        s.caseSensitive = false;
        s.spaceMatchNbsp = false;
        s.searchSource = true;
        s.searchTarget = true;
        s.searchTranslated = true;
        s.searchUntranslated = true;
        s.widthInsensitive = true;
        s.excludeOrphans = false;
        s.replacement = null;
        Searcher searcher = new Searcher(Core.getProject(), s);
        searcher.setThread(new LongProcessThread());
        searcher.search();
        List<SearchResultEntry> result = searcher.getSearchResults();
        assertEquals(1, result.size());
    }

    private void addSTE(IProject.FileInfo fi, String id, String source, String translation,
                          boolean translationFuzzy) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size() + 1, null, translation,
                new ArrayList<>());
        ste.setSourceTranslationFuzzy(translationFuzzy);
        fi.entries.add(ste);
        proj.getAllEntries().add(ste);
    }

    protected static class RealProjectWithTMX extends RealProject {
        public RealProjectWithTMX(ProjectProperties props) {
            super(props);
            projectTMX = new ProjectTMX();
        }

        public ProjectTMX getTMX() {
            return projectTMX;
        }

        public List<FileInfo> getProjectFilesList() {
            return projectFilesList;
        }
    }
}
