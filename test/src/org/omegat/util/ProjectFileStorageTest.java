/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.omegat.core.data.ProjectException;
import org.omegat.core.data.ProjectProperties;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;

import junit.framework.TestCase;

public class ProjectFileStorageTest extends TestCase {

    private static final File PROJECT_DIR = new File("test/data/project");

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        tempDir = Files.createTempDirectory("omegat").toFile();
        assertTrue(tempDir.isDirectory());
        TestPreferencesInitializer.init(tempDir.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(FileUtil.deleteTree(tempDir));
    }

    public void testLoadDefaults() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "defaultdirs.project"));
        try {
            props.verifyProject();
            fail("Project props should fail verification when dirs don't exist yet");
        } catch (ProjectException ex) {
        }
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getSourceRoot().endsWith("source/"));
        assertTrue(props.getTargetRoot().endsWith("target/"));
        assertTrue(props.getGlossaryRoot().endsWith("glossary/"));
        assertTrue(props.getWriteableGlossary().endsWith("glossary/glossary.txt"));
        assertTrue(props.getTMRoot().endsWith("tm/"));
        assertTrue(props.getDictRoot().endsWith("dictionary/"));
        assertEquals(tempDir.getName(), props.getProjectName());
        assertEquals(new Language("en-us"), props.getSourceLanguage());
        assertEquals(new Language("fr-fr"), props.getTargetLanguage());
        assertEquals(LuceneEnglishTokenizer.class, props.getSourceTokenizer());
        assertEquals(LuceneFrenchTokenizer.class, props.getTargetTokenizer());
        assertTrue(props.isSentenceSegmentingEnabled());
        assertTrue(props.isSupportDefaultTranslations());
        assertFalse(props.isRemoveTags());
        assertTrue(props.getExternalCommand().isEmpty());
        List<String> excludes = props.getSourceRootExcludes();
        assertEquals(6, excludes.size());
        assertEquals("**/.svn/**", excludes.get(0));
    }

    public void testLoadCustomGlossaryDir() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossarydir.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("foo/glossary.txt"));
    }

    public void testLoadCustomGlossaryFile() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossaryfile.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("glossary/bar.txt"));
    }

    public void testLoadCustomGlossaryDirAndFile() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossarydirfile.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("foo/bar.txt"));
    }
}
