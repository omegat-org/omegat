/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
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

package org.omegat.core.data;

import java.util.List;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;

/**
 * Tests for RealProject classs.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RealProjectTest extends TestCase {
    ProjectTMX tmx;
    RealProjectWithTMXAccess project;
    IProject.FileInfo fi;

    @Before
    public void setUp() throws Exception {
        Core.initializeConsole(new TreeMap<String, String>());
    }

    /**
     * As long as the subsequent segments have the same translation, they should
     * be using the same default translation.
     */
    @Test
    public void testImportSameTranslations() {
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
    public void testImportFuzzy() {
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
    public void testImportOverwrite() {
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
        assertEquals("Translation imported, but shouldn't", tmx.getMultipleTranslation(entryKey).translation, "exist");
    }

    private void createProject(boolean supportDefaultTranslations) {
        ProjectProperties props = new ProjectProperties();
        props.setSupportDefaultTranslations(supportDefaultTranslations);
        project = new RealProjectWithTMXAccess(props);

        fi = new IProject.FileInfo();
        project.getProjectFilesList().add(fi);

        tmx = project.getTMX();
    }

    private void addSTE(IProject.FileInfo fi, String id, String source, String translation, boolean translationFuzzy) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, fi.entries.size() + 1, null, translation, null);
        ste.setSourceTranslationFuzzy(translationFuzzy);
        fi.entries.add(ste);
    }

    private void setDefault(String source, String translation) {
        EntryKey key = new EntryKey(null, source, null, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, 0, null, translation, null);
        TMXEntry tr = new TMXEntry(source, translation, true);
        tmx.setTranslation(ste, tr, true);
    }

    private void setAlternative(String id, String source, String translation) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, 0, null, translation, null);
        TMXEntry tr = new TMXEntry(source, translation, false);
        tmx.setTranslation(ste, tr, false);
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
        assertEquals("Alternative translation of '" + source + "' (id='" + id + "') imported wrong", tr.translation,
                translation);
    }

    private void checkNoAlternative(String id, String source) {
        EntryKey key = new EntryKey("test", source, id, null, null, null);
        TMXEntry tr = tmx.getMultipleTranslation(key);
        assertNull("Alternative translation of '" + source + "' (id='" + id + "') imported, but shouldn't", tr);
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
}
