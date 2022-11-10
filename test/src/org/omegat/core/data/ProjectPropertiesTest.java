/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.junit.Test;

import org.omegat.util.Platform;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Tests for ProjectProperties class.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectPropertiesTest {

    @Test
    public void test1() {
        ProjectProperties p = new ProjectProperties();
        p.setProjectRoot("/tmp");
        switch (Platform.getOsType()) {
        case WIN32:
        case WIN64:
            p.getSourceDir().setRelativeOrAbsolute("C:\\dir");
            assertEquals("C:/dir/", p.getSourceDir().getAsString());
            assertFalse(p.getSourceDir().isUnderRoot());
            assertNull(p.getSourceDir().getUnderRoot());

            p = new ProjectProperties();
            p.projectRootDir = new File("C:\\some");
            p.getSourceDir().setRelativeOrAbsolute("dir\\1");
            assertEquals("C:/some/dir/1/", p.getSourceDir().getAsString());
            assertTrue(p.getSourceDir().isUnderRoot());
            assertEquals("dir/1/", p.getSourceDir().getUnderRoot());

            break;
        default:
            p.getSourceDir().setRelativeOrAbsolute("/dir");
            assertEquals("/dir/", p.getSourceDir().getAsString());
            assertFalse(p.getSourceDir().isUnderRoot());
            assertNull(p.getSourceDir().getUnderRoot());

            p = new ProjectProperties();
            p.projectRootDir = new File("/some");
            p.getSourceDir().setRelativeOrAbsolute("dir/1");
            assertEquals("/some/dir/1/", p.getSourceDir().getAsString());
            assertTrue(p.getSourceDir().isUnderRoot());
            assertEquals("dir/1/", p.getSourceDir().getUnderRoot());

            break;
        }
    }

    @Test
    public void test2() {
        ProjectProperties p = new ProjectProperties();

        p.getSourceDir().setRelativeOrAbsolute("dir\\next");
        assertEquals("dir/next/", p.getSourceDir().getAsString());
        assertTrue(p.getSourceDir().isUnderRoot());

        p.getWritableGlossaryFile().setRelativeOrAbsolute("dir\\file");
        assertEquals("dir/file", p.getWritableGlossaryFile().getAsString());
        assertTrue(p.getWritableGlossaryFile().isUnderRoot());
    }

    @Test
    public void test3() {
        ProjectProperties p = new ProjectProperties();
        p.setProjectRoot("/tmp");

        p.getSourceDir().setRelativeOrAbsolute("/tmp/source");
        assertEquals("/tmp/source/", p.getSourceDir().getAsString());
        assertEquals("source/", p.getSourceDir().getUnderRoot());
        assertTrue(p.getSourceDir().isUnderRoot());
        assertFalse(p.isTeamProject());
    }

    @Test
    public void testIsTeamProjectOnGitTeam() {
        ProjectProperties p = new ProjectProperties();
        p.setProjectRoot("/tmp");
        p.getSourceDir().setRelativeOrAbsolute("/tmp/source");
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("");
        repositoryMapping.setLocal("");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("git");
        repositoryDefinition.setBranch("main");
        repositoryDefinition.setUrl("https://example.com/example.git");
        p.setRepositories(Collections.singletonList(repositoryDefinition));
        assertTrue(p.isTeamProject());
    }

    @Test
    public void testIsTeamProjectOnSVNTeam() {
        ProjectProperties p = new ProjectProperties();
        p.setProjectRoot("/tmp");
        p.getSourceDir().setRelativeOrAbsolute("/tmp/source");
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("");
        repositoryMapping.setLocal("");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("svn");
        repositoryDefinition.setBranch("trunk");
        repositoryDefinition.setUrl("https://example.com/example");
        p.setRepositories(Collections.singletonList(repositoryDefinition));
        assertTrue(p.isTeamProject());
    }

    @Test
    public void testIsTeamProjectOnGitButNoRemoteProject() {
        ProjectProperties p = new ProjectProperties();
        p.setProjectRoot("/tmp");
        p.getSourceDir().setRelativeOrAbsolute("/tmp/source");
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        RepositoryMapping repositoryMapping = new RepositoryMapping();
        repositoryMapping.setRepository("doc_src/en");
        repositoryMapping.setLocal("source/foo");
        repositoryDefinition.getMapping().add(repositoryMapping);
        repositoryDefinition.setType("git");
        repositoryDefinition.setBranch("master");
        repositoryDefinition.setUrl("https://example.com/example.git");
        p.setRepositories(Collections.singletonList(repositoryDefinition));
        assertFalse(p.isTeamProject());
    }
}
