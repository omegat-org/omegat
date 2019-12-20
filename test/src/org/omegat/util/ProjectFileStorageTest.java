/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.data.ProjectException;
import org.omegat.core.data.ProjectProperties;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;

import gen.core.project.Omegat;

public class ProjectFileStorageTest {

    private static final File PROJECT_DIR = new File("test/data/project");

    private File tempDir;

    @Before
    public final void setUp() throws Exception {
        tempDir = Files.createTempDirectory("omegat").toFile().getAbsoluteFile();
        assertTrue(tempDir.isDirectory());
        TestPreferencesInitializer.init(tempDir.getPath());
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
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

    @Test
    public void testLoadCustomGlossaryDir() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossarydir.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("foo/glossary.txt"));
    }

    @Test
    public void testLoadCustomGlossaryFile() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossaryfile.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("glossary/bar.txt"));
    }

    @Test
    public void testLoadCustomGlossaryDirAndFile() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "customglossarydirfile.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith("foo/bar.txt"));
    }

    @Test
    public void testNearAbsolutePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        for (int i = 0; i < OConsts.MAX_PARENT_DIRECTORIES_ABS2REL; i++) {

            String prefix = repeat(i, "a/");
            File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
            projRoot.mkdirs();

            // Set project folders to absolute paths
            File srcDir = new File(tempDir, "source").getAbsoluteFile();
            File trgDir = new File(tempDir, "target").getAbsoluteFile();
            File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
            File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
            File tmDir = new File(tempDir, "tm").getAbsoluteFile();
            omt.getProject().setSourceDir(srcDir.getPath());
            omt.getProject().setTargetDir(trgDir.getPath());
            omt.getProject().setDictionaryDir(dictDir.getPath());
            omt.getProject().setGlossaryDir(glosDir.getPath());
            omt.getProject().setTmDir(tmDir.getPath());

            // Make all the actual folders
            Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir).forEach(File::mkdirs);

            // Load the ProjectProperties and verify that the project folders
            // are resolved correctly
            ProjectProperties props = ProjectFileStorage.loadPropertiesFile(projRoot, omt);
            props.verifyProject();

            // Write the project file out and read it again to make sure the
            // paths are correctly round-tripped. Since these are "near" paths
            // they should become relative and not be absolute.
            ProjectFileStorage.writeProjectFile(props);
            File outProjFile = new File(projRoot, OConsts.FILE_PROJECT);
            assertTrue(outProjFile.isFile());
            Omegat outOmt = ProjectFileStorage.parseProjectFile(outProjFile);
            String relPrefix = repeat(i + 1, "../");
            assertEquals(relPrefix + srcDir.getName(), outOmt.getProject().getSourceDir());
            assertEquals(relPrefix + trgDir.getName(), outOmt.getProject().getTargetDir());
            assertEquals(relPrefix + dictDir.getName(), outOmt.getProject().getDictionaryDir());
            assertEquals(relPrefix + glosDir.getName(), outOmt.getProject().getGlossaryDir());
            assertEquals(relPrefix + tmDir.getName(), outOmt.getProject().getTmDir());
        }
    }

    @Test
    public void testNearRelativePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        for (int i = 0; i < OConsts.MAX_PARENT_DIRECTORIES_ABS2REL; i++) {
            File projRoot = Paths.get(tempDir.getAbsolutePath(), repeat(i, "a/"), "root").toFile();
            projRoot.mkdirs();

            // Set project folders to relative paths
            File srcDir = new File(tempDir, "source").getAbsoluteFile();
            File trgDir = new File(tempDir, "target").getAbsoluteFile();
            File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
            File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
            File tmDir = new File(tempDir, "tm").getAbsoluteFile();
            String prefix = repeat(i + 1, "../");
            omt.getProject().setSourceDir(prefix + srcDir.getName());
            omt.getProject().setTargetDir(prefix + trgDir.getName());
            omt.getProject().setDictionaryDir(prefix + dictDir.getName());
            omt.getProject().setGlossaryDir(prefix + glosDir.getName());
            omt.getProject().setTmDir(prefix + tmDir.getName());

            // Make all the actual folders
            Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir).forEach(File::mkdirs);

            // Load the ProjectProperties and verify that the project folders
            // are resolved correctly
            ProjectProperties props = ProjectFileStorage.loadPropertiesFile(projRoot, omt);
            props.verifyProject();

            // Indirections should be resolved.
            assertFalse(props.getSourceRoot().contains("../"));
            assertFalse(props.getTargetRoot().contains("../"));
            assertFalse(props.getDictRoot().contains("../"));
            assertFalse(props.getGlossaryRoot().contains("../"));
            assertFalse(props.getTMRoot().contains("../"));

            // Write the project file out and read it again to make sure the
            // paths are correctly round-tripped. Since these are "near" paths
            // they should remain relative and not absolute.
            ProjectFileStorage.writeProjectFile(props);
            File outProjFile = new File(projRoot, OConsts.FILE_PROJECT);
            assertTrue(outProjFile.isFile());
            Omegat outOmt = ProjectFileStorage.parseProjectFile(outProjFile);
            assertEquals(prefix + srcDir.getName(), outOmt.getProject().getSourceDir());
            assertEquals(prefix + trgDir.getName(), outOmt.getProject().getTargetDir());
            assertEquals(prefix + dictDir.getName(), outOmt.getProject().getDictionaryDir());
            assertEquals(prefix + glosDir.getName(), outOmt.getProject().getGlossaryDir());
            assertEquals(prefix + tmDir.getName(), outOmt.getProject().getTmDir());
        }
    }

    @Test
    public void testFarAbsolutePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        String prefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL, "a/");
        File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
        projRoot.mkdirs();

        // Set project folders to absolute paths
        File srcDir = new File(tempDir, "source").getAbsoluteFile();
        File trgDir = new File(tempDir, "target").getAbsoluteFile();
        File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
        File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
        File tmDir = new File(tempDir, "tm").getAbsoluteFile();
        omt.getProject().setSourceDir(srcDir.getPath());
        omt.getProject().setTargetDir(trgDir.getPath());
        omt.getProject().setDictionaryDir(dictDir.getPath());
        omt.getProject().setGlossaryDir(glosDir.getPath());
        omt.getProject().setTmDir(tmDir.getPath());

        // Make all the actual folders
        Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir).forEach(File::mkdirs);

        // Load the ProjectProperties and verify that the project folders
        // are resolved correctly
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(projRoot, omt);
        props.verifyProject();

        // Write the project file out and read it again to make sure the
        // paths are correctly round-tripped. Since these are "far" paths
        // they should remain absolute.
        ProjectFileStorage.writeProjectFile(props);
        File outProjFile = new File(projRoot, OConsts.FILE_PROJECT);
        assertTrue(outProjFile.isFile());
        Omegat outOmt = ProjectFileStorage.parseProjectFile(outProjFile);
        assertEquals(ProjectFileStorage.normalizeSlashes(srcDir.getPath()), outOmt.getProject().getSourceDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(trgDir.getPath()), outOmt.getProject().getTargetDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(dictDir.getPath()), outOmt.getProject().getDictionaryDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(glosDir.getPath()), outOmt.getProject().getGlossaryDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(tmDir.getPath()), outOmt.getProject().getTmDir());
    }

    @Test
    public void testFarRelativePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        String prefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL, "a/");
        File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
        projRoot.mkdirs();

        // Set project folders to absolute paths
        File srcDir = new File(tempDir, "source").getAbsoluteFile();
        File trgDir = new File(tempDir, "target").getAbsoluteFile();
        File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
        File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
        File tmDir = new File(tempDir, "tm").getAbsoluteFile();
        String relPrefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL + 1, "../");
        omt.getProject().setSourceDir(relPrefix + srcDir.getName());
        omt.getProject().setTargetDir(relPrefix + trgDir.getName());
        omt.getProject().setDictionaryDir(relPrefix + dictDir.getName());
        omt.getProject().setGlossaryDir(relPrefix + glosDir.getName());
        omt.getProject().setTmDir(relPrefix + tmDir.getName());

        // Make all the actual folders
        Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir).forEach(File::mkdirs);

        // Load the ProjectProperties and verify that the project folders
        // are resolved correctly
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(projRoot, omt);
        props.verifyProject();

        // Indirections should be resolved.
        assertFalse(props.getSourceRoot().contains("../"));
        assertFalse(props.getTargetRoot().contains("../"));
        assertFalse(props.getDictRoot().contains("../"));
        assertFalse(props.getGlossaryRoot().contains("../"));
        assertFalse(props.getTMRoot().contains("../"));

        // Write the project file out and read it again to make sure the
        // paths are correctly round-tripped. Since these are "far" paths
        // they should become absolute and not remain relative.
        ProjectFileStorage.writeProjectFile(props);
        File outProjFile = new File(projRoot, OConsts.FILE_PROJECT);
        assertTrue(outProjFile.isFile());
        Omegat outOmt = ProjectFileStorage.parseProjectFile(outProjFile);
        assertEquals(ProjectFileStorage.normalizeSlashes(srcDir.getPath()), outOmt.getProject().getSourceDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(trgDir.getPath()), outOmt.getProject().getTargetDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(dictDir.getPath()), outOmt.getProject().getDictionaryDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(glosDir.getPath()), outOmt.getProject().getGlossaryDir());
        assertEquals(ProjectFileStorage.normalizeSlashes(tmDir.getPath()), outOmt.getProject().getTmDir());
    }

    @Test
    public void testMissingDirs() throws Exception {
        // Older project files can be missing path definitions, in which case
        // we should fall back to the default values.
        File projFile = new File(PROJECT_DIR, "missingdirs.project");
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir, projFile);

        assertTrue(props.getSourceRoot().endsWith(OConsts.DEFAULT_SOURCE + '/'));
        assertTrue(props.getTargetRoot().endsWith(OConsts.DEFAULT_TARGET + '/'));
        assertTrue(props.getDictRoot().endsWith(OConsts.DEFAULT_DICT + '/'));
        assertTrue(props.getGlossaryRoot().endsWith(OConsts.DEFAULT_GLOSSARY + '/'));
        assertTrue(props.getTMRoot().endsWith(OConsts.DEFAULT_TM + '/'));
    }

    private static String repeat(int n, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
