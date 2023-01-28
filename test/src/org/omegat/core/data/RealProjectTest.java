/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
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

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for RealProject classs.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RealProjectTest {
    ProjectTMX tmx;
    RealProjectWithTMXAccess project;
    IProject.FileInfo fi;
    Path tempDir;

    @Before
    public final void setUp() throws Exception {
        tempDir = Files.createTempDirectory("omegat-core-ut");
        Core.initializeConsole(new TreeMap<>());
        TestPreferencesInitializer.init();
    }

    @After
    public final void tearDown() throws Exception {
        if (project != null) {
            project.unlockProject();
        }
        FileUtils.deleteDirectory(tempDir.toFile());
    }

    /**
     * As long as the subsequent segments have the same translation, they should
     * be using the same default translation.
     */
    @Test
    public void testImportSameTranslations() throws Exception {
        createProject(true);
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", false);
        addSTE(fi, "id2", "List of sections in %s", "Liste des sections de %s", false);
        addSTE(fi, "id3", "List of sections in %s", "Ceci est la liste des sections de %s", false);

        project.importTranslationsFromSources();

        // The first two translations should be the same default translation,
        // the third one should be loaded as alternative.
        checkDefault("List of sections in %s", "Liste des sections de %s");
        checkNoAlternative("id1", "List of sections in %s");
        checkNoAlternative("id2", "List of sections in %s");
        checkAlternative("id3", "List of sections in %s", "Ceci est la liste des sections de %s");

        /* The same for alternative-only project. */
        createProject(false);
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", false);
        addSTE(fi, "id2", "List of sections in %s", "Liste des sections de %s", false);
        addSTE(fi, "id3", "List of sections in %s", "Ceci est la liste des sections de %s", false);

        project.importTranslationsFromSources();
        checkNoDefault("List of sections in %s");
        checkAlternative("id1", "List of sections in %s", "Liste des sections de %s");
        checkAlternative("id2", "List of sections in %s", "Liste des sections de %s");
        checkAlternative("id3", "List of sections in %s", "Ceci est la liste des sections de %s");
    }

    /**
     * Fuzzy shouldn't be loaded.
     */
    @Test
    public void testImportFuzzy() throws Exception {
        createProject(true);
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", true);

        project.importTranslationsFromSources();
        checkNoDefault("List of sections in %s");
        checkNoAlternative("id1", "List of sections in %s");

        /* The same for alternative-only project. */
        createProject(false);
        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", true);

        project.importTranslationsFromSources();
        checkNoDefault("List of sections in %s");
        checkNoAlternative("id1", "List of sections in %s");
    }

    /**
     * Exist translation should be overwritten.
     */
    @Test
    public void testImportOverwrite() throws Exception {
        createProject(true);
        setDefault("List of sections in %s", "exist");

        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", false);
        addSTE(fi, "id2", "List of sections in %s", "Ceci est la liste des sections de %s", false);

        project.importTranslationsFromSources();
        assertEquals("Translation imported, but shouldn't",
                tmx.getDefaultTranslation("List of sections in %s").translation, "exist");
        checkNoAlternative("id1", "List of sections in %s");
        checkNoAlternative("id2", "List of sections in %s");

        /* The same for alternative-only project. */
        createProject(false);
        setAlternative("id1", "List of sections in %s", "exist");

        addSTE(fi, "id1", "List of sections in %s", "Liste des sections de %s", false);

        project.importTranslationsFromSources();
        checkNoDefault("List of sections in %s");
        EntryKey entryKey = new EntryKey("test", "List of sections in %s", "id1", null, null, null);
        assertEquals("Translation imported, but shouldn't", tmx.getMultipleTranslation(entryKey).translation,
                "exist");
    }

    private void createProject(boolean supportDefaultTranslations) throws Exception {
        ProjectProperties props = new ProjectProperties(tempDir.toFile());
        props.setSupportDefaultTranslations(supportDefaultTranslations);
        props.setTargetTokenizer(DefaultTokenizer.class);
        project = new RealProjectWithTMXAccess(props);

        fi = new IProject.FileInfo();
        project.getProjectFilesList().add(fi);

        tmx = project.getTMX();
    }

    private void addSTE(IProject.FileInfo fi, String id, String source, String translation,
            boolean translationFuzzy) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size() + 1, null, translation,
                new ArrayList<ProtectedPart>());
        ste.setSourceTranslationFuzzy(translationFuzzy);
        fi.entries.add(ste);
    }

    private void setDefault(String source, String translation) {
        EntryKey key = new EntryKey(null, source, null, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, 0, null, translation, new ArrayList<ProtectedPart>());
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.source = source;
        tr.translation = translation;
        tmx.setTranslation(ste, new TMXEntry(tr, true, null), true);
    }

    private void setAlternative(String id, String source, String translation) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, 0, null, translation, new ArrayList<ProtectedPart>());
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.source = source;
        tr.translation = translation;
        tmx.setTranslation(ste, new TMXEntry(tr, false, null), false);
    }

    private void checkDefault(String source, String translation) {
        TMXEntry tr = tmx.getDefaultTranslation(source);
        assertNotNull("Default translation of '" + source + "' not imported", tr);
        assertEquals("Default translation of '" + source + "' imported wrong", tr.translation, translation);
    }

    private void checkNoDefault(String source) {
        TMXEntry tr = tmx.getDefaultTranslation(source);
        assertNull("Default translation of '" + source + "' imported, but shouldn't", tr);
    }

    private void checkAlternative(String id, String source, String translation) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        TMXEntry tr = tmx.getMultipleTranslation(key);
        assertNotNull("Alternative translation of '" + source + "' (id='" + id + "') not imported", tr);
        assertEquals("Alternative translation of '" + source + "' (id='" + id + "') imported wrong",
                tr.translation, translation);
    }

    private void checkNoAlternative(String id, String source) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        TMXEntry tr = tmx.getMultipleTranslation(key);
        assertNull("Alternative translation of '" + source + "' (id='" + id + "') imported, but shouldn't",
                tr);
    }

    @Test
    public void saveProjectProperties() throws Exception {
        createProject(true);
        project.saveProjectProperties();
        assertTrue(Files.exists(tempDir.resolve("omegat.project")));
    }

    @Test
    public void getTranslationInfo() throws Exception {
        createProject(true);
        String source = "List of sections in %s";
        String translation = "Liste des sections de %s";
        addSTE(fi, "id1", source, translation, false);
        project.importTranslationsFromSources();
        EntryKey key = new EntryKey("test", source, "id1", null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size(), null, translation,
                new ArrayList<>());
        TMXEntry entry = project.getTranslationInfo(ste);
        assertEquals(source, entry.source);
        assertEquals(translation, entry.translation);
    }

    @Test
    public void getProjectProperties() throws Exception {
        createProject(true);
        ProjectProperties prop = project.getProjectProperties();
        assertEquals(tempDir.toFile(), prop.projectRootDir.getAbsoluteFile());
        assertEquals(tempDir.getFileName().toString(), prop.getProjectName());
        assertEquals(tempDir.resolve("omegat"), Paths.get(prop.getProjectInternal()));
    }

    @Test
    public void setTranslation() throws Exception {
        createProject(true);
        assertFalse(project.isProjectModified());
        String source = "List of sections in %s";
        String translation = "Liste des sections de %s";
        SourceTextEntry ste = setDefault2(source, translation);
        assertTrue(project.isProjectModified());
        TMXEntry entry = project.getTranslationInfo(ste);
        assertEquals(source, entry.source);
        assertEquals(translation, entry.translation);
    }

    @Test
    public void setNote() throws Exception {
        createProject(true);
        String source = "List of sections in %s";
        String translation = "Liste des sections de %s";
        SourceTextEntry ste = setDefault2(source, translation);
        project.setNote(ste, getTMXEntry(source, translation), "Note");
        TMXEntry entry = project.getTranslationInfo(ste);
        assertTrue(entry.hasNote());
        assertEquals("Note", entry.note);
    }

    private SourceTextEntry setDefault2(final String source, final String translation) {
        EntryKey key = new EntryKey("test", source, null, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size(), null, translation,
                new ArrayList<>());
        project.setTranslation(ste, getPrepareTMXEntry(source, translation), true, null);
        return ste;
    }

    private TMXEntry getTMXEntry(final String source, final String translation) {
        return new TMXEntry(getPrepareTMXEntry(source, translation), true, null);
    }

    private PrepareTMXEntry getPrepareTMXEntry(final String source, final String translation) {
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.source = source;
        tr.translation = translation;
        return tr;
    }

    /**
     * Class for access to some internal variables.
     */
    protected static class RealProjectWithTMXAccess extends RealProject {
        public RealProjectWithTMXAccess(ProjectProperties props) {
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

    public static TMXEntry createEmptyTMXEntry() {
        return new TMXEntry(new PrepareTMXEntry(), true, null);
    }
}
