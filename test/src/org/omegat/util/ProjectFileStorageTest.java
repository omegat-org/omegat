/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.ProjectException;
import org.omegat.core.data.ProjectProperties;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;

import gen.core.project.Omegat;
import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
        } catch (ProjectException ignored) {
            // expected.
        }
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getSourceRoot().endsWith("source/"));
        assertTrue(props.getTargetRoot().endsWith("target/"));
        assertTrue(props.getGlossaryRoot().endsWith("glossary/"));
        assertTrue(props.getWriteableGlossary().endsWith("glossary/glossary.txt"));
        assertTrue(props.getTMRoot().endsWith("tm/"));
        assertTrue(props.getDictRoot().endsWith("dictionary/"));
        assertEquals(props.getExportTMRoot(), props.getProjectRoot());
        assertEquals(tempDir.getName(), props.getProjectName());
        assertEquals(new Language("en-us"), props.getSourceLanguage());
        assertEquals(new Language("fr-fr"), props.getTargetLanguage());
        assertEquals(LuceneEnglishTokenizer.class, props.getSourceTokenizer());
        assertEquals(LuceneFrenchTokenizer.class, props.getTargetTokenizer());
        assertTrue(props.isSentenceSegmentingEnabled());
        assertTrue(props.isSupportDefaultTranslations());
        assertFalse(props.isRemoveTags());
        assertTrue(props.isExportTm("level1"));
        assertTrue(props.isExportTm("level2"));
        assertTrue(props.isExportTm("omegat"));
        assertTrue(props.getExternalCommand().isEmpty());
        List<String> excludes = props.getSourceRootExcludes();
        assertEquals(6, excludes.size());
        assertEquals("**/.svn/**", excludes.get(0));
    }

    @Test
    public void testSaveTeamProject() throws Exception {
        // create & write a project
        ProjectProperties p = getProjectProperties();
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("");
        repositoryMapping.setLocal("");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("git");
        repositoryDefinition.setBranch("main");
        repositoryDefinition.setUrl("https://example.com/example.git");
        p.setRepositories(Collections.singletonList(repositoryDefinition));
        ProjectFileStorage.writeProjectFile(p);
        // check file
        compareXML(new File(PROJECT_DIR, "team.project"), new File(tempDir, "omegat.project"));
    }

    @Test
    public void testSaveTeamProjectWithExclude() throws Exception {
        // create & write a project
        ProjectProperties p = getProjectProperties();
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("");
        repositoryMapping.setLocal("");
        repositoryMapping.getExcludes().add("exclude1");
        repositoryMapping.getExcludes().add("exclude2");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("git");
        repositoryDefinition.setBranch("main");
        repositoryDefinition.setUrl("https://example.com/example.git");
        p.setRepositories(Collections.singletonList(repositoryDefinition));
        ProjectFileStorage.writeProjectFile(p);
        // check file
        compareXML(new File(PROJECT_DIR, "teamMapWithExclude.project"), new File(tempDir, "omegat.project"));
    }

    @Test
    public void testSaveTeamProjectWithMapping() throws Exception {
        // create & write a project
        ProjectProperties p = getProjectProperties();
        //
        List<RepositoryDefinition> repositories = new ArrayList<>();
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("");
        repositoryMapping.setLocal("");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("git");
        repositoryDefinition.setBranch("main");
        repositoryDefinition.setUrl("https://example.com/example.git");
        repositories.add(repositoryDefinition);
        //
        RepositoryDefinition repositoryDefinition1 = new RepositoryDefinition();
        repositoryDefinition1.setType("git");
        repositoryDefinition1.setBranch("main");
        repositoryDefinition1.setUrl("git@example.com:example.git");
        RepositoryMapping repositoryMapping1 = new RepositoryMapping();
        repositoryMapping1.setRepository("/docs");
        repositoryMapping1.setLocal("source");
        repositoryDefinition1.getMapping().add(repositoryMapping1);
        RepositoryMapping repositoryMapping2 = new RepositoryMapping();
        repositoryMapping2.setRepository("/manual");
        repositoryMapping2.setLocal("source/manual");
        repositoryDefinition1.getMapping().add(repositoryMapping2);
        repositories.add(repositoryDefinition1);
        //
        p.setRepositories(repositories);
        ProjectFileStorage.writeProjectFile(p);
        // check file
        compareXML(new File(PROJECT_DIR, "teamWithMap.project"), new File(tempDir, "omegat.project"));
    }

    private @NotNull ProjectProperties getProjectProperties() throws Exception {
        ProjectProperties p = new ProjectProperties(tempDir);
        p.setSourceLanguage("en-US");
        p.setTargetLanguage("fr-FR");
        p.setSourceTokenizer(LuceneEnglishTokenizer.class);
        p.setTargetTokenizer(LuceneFrenchTokenizer.class);
        p.setSentenceSegmentingEnabled(true);
        return p;
    }

    @Test
    public void testLoadCustomGlossaryDir() throws Exception {
        loadPropertiesAndAssertGlossary("customglossarydir.project", "foo/glossary.txt");
    }

    @Test
    public void testLoadCustomGlossaryFile() throws Exception {
        loadPropertiesAndAssertGlossary("customglossaryfile.project", "glossary/bar.txt");
    }

    @Test
    public void testLoadCustomGlossaryDirAndFile() throws Exception {
        loadPropertiesAndAssertGlossary("customglossarydirfile.project", "foo/bar.txt");
    }

    private void loadPropertiesAndAssertGlossary(String projectFileName, String expectedGlossaryPath) throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir, new File(PROJECT_DIR, projectFileName));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.getWriteableGlossary().endsWith(expectedGlossaryPath));
    }

    @Test
    public void testLoadProjectWithNonDefaultExportTMLevels() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "nondefaultexporttmoptions.project"));
        props.autocreateDirectories();
        props.verifyProject();
        assertTrue(props.isExportTm("level1"));
        assertFalse(props.isExportTm("level2"));
        assertFalse(props.isExportTm("omegat"));
    }

    @Test
    public void testWriteProjectWithExportTMLevelsChanged() throws Exception {
        ProjectProperties props = ProjectFileStorage.loadPropertiesFile(tempDir,
                new File(PROJECT_DIR, "defaultdirs.project"));
        props.autocreateDirectories();
        props.verifyProject();
        props.setExportTmLevels(List.of("level1"));

        // Write the project file and read it again to verify that export TM
        // levels were set correctly
        ProjectFileStorage.writeProjectFile(props);
        props = ProjectFileStorage.loadPropertiesFile(tempDir, new File(props.getProjectRoot(), OConsts.FILE_PROJECT));
        assertTrue(props.isExportTm("level1"));
        assertFalse(props.isExportTm("level2"));
        assertFalse(props.isExportTm("omegat"));
    }

    @Test
    public void testNearAbsolutePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        for (int i = 0; i < OConsts.MAX_PARENT_DIRECTORIES_ABS2REL; i++) {
            String prefix = repeat(i, "a/");
            File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
            assertTrue("Failed to create directory: " + projRoot.getAbsolutePath(), projRoot.mkdirs());

            // Set project folders to absolute paths
            File srcDir = new File(tempDir, "source").getAbsoluteFile();
            File trgDir = new File(tempDir, "target").getAbsoluteFile();
            File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
            File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
            File tmDir = new File(tempDir, "tm").getAbsoluteFile();
            File exportTmDir = new File(tempDir, "export_tm").getAbsoluteFile();
            omt.getProject().setSourceDir(srcDir.getPath());
            omt.getProject().setTargetDir(trgDir.getPath());
            omt.getProject().setDictionaryDir(dictDir.getPath());
            omt.getProject().setGlossaryDir(glosDir.getPath());
            omt.getProject().setTmDir(tmDir.getPath());
            omt.getProject().setExportTmDir(exportTmDir.getPath());

            // Make all the actual folders
            for (File file : Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir, exportTmDir)) {
                if (!file.exists()) {
                    assertTrue("Failed to create project directory: " + file.getAbsolutePath(), file.mkdir());
                }
            }

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
            assertEquals(getDir(relPrefix, srcDir), outOmt.getProject().getSourceDir());
            assertEquals(getDir(relPrefix, trgDir), outOmt.getProject().getTargetDir());
            assertEquals(getDir(relPrefix, dictDir), outOmt.getProject().getDictionaryDir());
            assertEquals(getDir(relPrefix, glosDir), outOmt.getProject().getGlossaryDir());
            assertEquals(getDir(relPrefix, tmDir), outOmt.getProject().getTmDir());
            assertEquals(getDir(relPrefix, exportTmDir), outOmt.getProject().getExportTmDir());
        }
    }

    @Test
    public void testNearRelativePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        for (int i = 0; i < OConsts.MAX_PARENT_DIRECTORIES_ABS2REL; i++) {
            File projRoot = Paths.get(tempDir.getAbsolutePath(), repeat(i, "a/"), "root").toFile();
            assertTrue("Failed to create project directory: " + projRoot.getAbsolutePath(), projRoot.mkdirs());

            // Set project folders to relative paths
            File srcDir = new File(tempDir, "source").getAbsoluteFile();
            File trgDir = new File(tempDir, "target").getAbsoluteFile();
            File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
            File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
            File tmDir = new File(tempDir, "tm").getAbsoluteFile();
            File exportTmDir = new File(tempDir, "export_tm").getAbsoluteFile();
            String prefix = repeat(i + 1, "../");
            omt.getProject().setSourceDir(getDir(prefix, srcDir));
            omt.getProject().setTargetDir(getDir(prefix, trgDir));
            omt.getProject().setDictionaryDir(getDir(prefix, dictDir));
            omt.getProject().setGlossaryDir(getDir(prefix, glosDir));
            omt.getProject().setTmDir(getDir(prefix, tmDir));
            omt.getProject().setExportTmDir(getDir(prefix, exportTmDir));

            // Make all the actual folders
            for (File file : Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir, exportTmDir)) {
                if (!file.exists()) {
                    assertTrue("Failed to create project directory: " + file.getAbsolutePath(), file.mkdir());
                }
            }

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
            assertFalse(props.getExportTMRoot().contains("../"));

            // Write the project file out and read it again to make sure the
            // paths are correctly round-tripped. Since these are "near" paths
            // they should remain relative and not absolute.
            ProjectFileStorage.writeProjectFile(props);
            File outProjFile = new File(projRoot, OConsts.FILE_PROJECT);
            assertTrue(outProjFile.isFile());
            Omegat outOmt = ProjectFileStorage.parseProjectFile(outProjFile);
            assertEquals(getDir(prefix, srcDir), outOmt.getProject().getSourceDir());
            assertEquals(getDir(prefix, trgDir), outOmt.getProject().getTargetDir());
            assertEquals(getDir(prefix, dictDir), outOmt.getProject().getDictionaryDir());
            assertEquals(getDir(prefix, glosDir), outOmt.getProject().getGlossaryDir());
            assertEquals(getDir(prefix, tmDir), outOmt.getProject().getTmDir());
            assertEquals(getDir(prefix, exportTmDir), outOmt.getProject().getExportTmDir());
        }
    }

    private static @NotNull String getDir(String prefix, File srcDir) {
        return prefix + srcDir.getName();
    }

    @Test
    public void testFarAbsolutePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        String prefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL, "a/");
        File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
        assertTrue("Failed to create project directory: " + projRoot.getAbsolutePath(), projRoot.mkdirs());

        // Set project folders to absolute paths
        File srcDir = new File(tempDir, "source").getAbsoluteFile();
        File trgDir = new File(tempDir, "target").getAbsoluteFile();
        File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
        File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
        File tmDir = new File(tempDir, "tm").getAbsoluteFile();
        File exportTmDir = new File(tempDir, "export_tm").getAbsoluteFile();
        omt.getProject().setSourceDir(srcDir.getPath());
        omt.getProject().setTargetDir(trgDir.getPath());
        omt.getProject().setDictionaryDir(dictDir.getPath());
        omt.getProject().setGlossaryDir(glosDir.getPath());
        omt.getProject().setTmDir(tmDir.getPath());
        omt.getProject().setExportTmDir(exportTmDir.getPath());

        // Make all the actual folders
        for (File file : Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir, exportTmDir)) {
            if (!file.exists()) {
                assertTrue("Failed to create project directory: " + file.getAbsolutePath(), file.mkdir());
            }
        }

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
        assertEquals(ProjectFileStorage.normalizeSlashes(exportTmDir.getPath()), outOmt.getProject().getExportTmDir());
    }

    @Test
    public void testFarRelativePaths() throws Exception {
        File projFile = new File(PROJECT_DIR, "defaultdirs.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);

        String prefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL, "a/");
        File projRoot = Paths.get(tempDir.getAbsolutePath(), prefix, "root").toFile();
        assertTrue("Failed to create project directory: " + projRoot.getAbsolutePath(), projRoot.mkdirs());

        // Set project folders to absolute paths
        File srcDir = new File(tempDir, "source").getAbsoluteFile();
        File trgDir = new File(tempDir, "target").getAbsoluteFile();
        File dictDir = new File(tempDir, "dictionary").getAbsoluteFile();
        File glosDir = new File(tempDir, "glossary").getAbsoluteFile();
        File tmDir = new File(tempDir, "tm").getAbsoluteFile();
        File exportTmDir = new File(tempDir, "export_tm").getAbsoluteFile();
        String relPrefix = repeat(OConsts.MAX_PARENT_DIRECTORIES_ABS2REL + 1, "../");
        omt.getProject().setSourceDir(getDir(relPrefix, srcDir));
        omt.getProject().setTargetDir(getDir(relPrefix, trgDir));
        omt.getProject().setDictionaryDir(getDir(relPrefix, dictDir));
        omt.getProject().setGlossaryDir(getDir(relPrefix, glosDir));
        omt.getProject().setTmDir(getDir(relPrefix, tmDir));
        omt.getProject().setExportTmDir(getDir(relPrefix, exportTmDir));

        // Make all the actual folders
        for (File file : Arrays.asList(srcDir, trgDir, dictDir, glosDir, tmDir, exportTmDir)) {
            if (!file.exists()) {
                assertTrue("Failed to create project directory: " + file.getAbsolutePath(), file.mkdir());
            }
        }

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
        assertFalse(props.getExportTMRoot().contains("../"));

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
        assertEquals(ProjectFileStorage.normalizeSlashes(exportTmDir.getPath()), outOmt.getProject().getExportTmDir());
    }

    @Test
    public void testProjectFileWithEntities() throws Exception {
        File projFile = new File(PROJECT_DIR, "entities.project");
        Omegat omt = ProjectFileStorage.parseProjectFile(projFile);
        assertEquals("translation_fr-ZZ_check", omt.getProject().getTargetDir());
        assertEquals("fr-ZZ", omt.getProject().getTargetLang());
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
        assertTrue(props.getExportTMRoot().endsWith(OConsts.DEFAULT_EXPORT_TM + '/'));
    }

    private static String repeat(int n, String s) {
        return String.valueOf(s).repeat(Math.max(0, n));
    }

    protected void compareXML(File f1, File f2) throws Exception {
        compareXML(f1.toURI().toURL(), f2.toURI().toURL());
    }
    protected void compareXML(URL f1, URL f2) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        var doc1 = builder.parse(f1.toExternalForm());
        var doc2 = builder.parse(f2.toExternalForm());
        assertThat(doc1).and(doc2)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .ignoreWhitespace()
                .ignoreComments()
                .ignoreChildNodesOrder()
                .areIdentical();
    }
}
