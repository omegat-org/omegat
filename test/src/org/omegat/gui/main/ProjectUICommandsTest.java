/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.omegat.core.TestCore;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.ProjectFileStorage;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * @author Hiroshi Miura
 */
public class ProjectUICommandsTest extends TestCore {

    @Test
    public void testIsIdenticalOmegatProjectProperties0() throws Exception {
        final File projectRootFolder = new File("test/data/team-project/l10n-ja");
        ProjectProperties props = ProjectFileStorage
                .loadProjectProperties(projectRootFolder.getAbsoluteFile());
        ProjectProperties another = ProjectFileStorage
                .loadProjectProperties(projectRootFolder.getAbsoluteFile());
        assertTrue(ProjectUICommands.isIdenticalOmegatProjectProperties(props, another));
        another.setExportTmLevels(false, false, false);
        assertFalse(ProjectUICommands.isIdenticalOmegatProjectProperties(props, another));
    }

    /**
     * Test normal path of getRootRepositoryMapping.
     */
    @Test
    public void testGetRootRepositoryMapping0() {
        List<RepositoryDefinition> repos = new ArrayList<>();
        repos.add(createRootDef());
        // Prepare source mapping
        final RepositoryDefinition sourceDef = new RepositoryDefinition();
        sourceDef.setType("http");
        sourceDef.setUrl("https://example.com/");
        RepositoryMapping sourceMap = new RepositoryMapping();
        sourceMap.setRepository("some_source");
        sourceMap.setLocal("source/some");
        sourceDef.getMapping().add(sourceMap);
        // check ProjectUICommands.getRootRepositoryMapping
        RepositoryDefinition rootDef = ProjectUICommands.getRootRepositoryMapping(repos);
        assertEquals("main", rootDef.getBranch());
        assertEquals("git", rootDef.getType());
        assertEquals("git@github.com:omegat-L10N/ja.git", rootDef.getUrl());
        assertEquals(1, rootDef.getMapping().size());
        assertEquals("/", rootDef.getMapping().get(0).getLocal());
        assertEquals("/", rootDef.getMapping().get(0).getRepository());
    }

    @Test
    public void testGetRootRepositoryMappingSvn() throws Exception {
        final File projectRootFolder = new File("test/data/team-project/svn");
        ProjectProperties props = ProjectFileStorage
                .loadProjectProperties(projectRootFolder.getAbsoluteFile());
        RepositoryDefinition rootRepo = ProjectUICommands.getRootRepositoryMapping(props.getRepositories());
        assertEquals("svn", rootRepo.getType());
        assertEquals(1, rootRepo.getMapping().size());
        assertEquals("/", rootRepo.getMapping().get(0).getLocal());
        assertEquals("/", rootRepo.getMapping().get(0).getRepository());
    }

    /**
     * Test normal path of setRootRepositoryMapping.
     */
    @Test
    public void testSetRootRepositoryMapping0() {
        List<RepositoryDefinition> repos = new ArrayList<>();
        repos.add(createRootDef());
        // Prepare local definition of repository argument
        RepositoryDefinition def = new RepositoryDefinition();
        def.setBranch("main");
        def.setType("git");
        def.setUrl("https://github.com/omegat-L10N/ja.git");
        ProjectUICommands.setRootRepositoryMapping(repos, def);
        assertEquals(1, repos.size());
        assertEquals("git", repos.get(0).getType());
        assertEquals("main", repos.get(0).getBranch());
        assertEquals(1, repos.get(0).getMapping().size());
        assertEquals("/", repos.get(0).getMapping().get(0).getRepository());
        assertEquals("/", repos.get(0).getMapping().get(0).getLocal());
    }

    @Test
    public void testIsRepositoryEqual() {
        RepositoryDefinition repo = createRootDef();
        RepositoryDefinition def = new RepositoryDefinition();
        def.setBranch("main");
        def.setType("git");
        def.setUrl("https://github.com/omegat-L10N/ja.git");
        assertFalse(ProjectUICommands.isRepositoryEquals(repo, def));
        // XXX: isRepositoryEquals ignores difference in mapping
        assertTrue(ProjectUICommands.isRepositoryEquals(repo, repo));
    }

    private RepositoryDefinition createRootDef() {
        // Prepare team root mapping
        RepositoryDefinition teamDef = new RepositoryDefinition();
        teamDef.setBranch("main");
        teamDef.setType("git");
        teamDef.setUrl("git@github.com:omegat-L10N/ja.git");
        RepositoryMapping map = new RepositoryMapping();
        map.setRepository("/");
        map.setLocal("/");
        teamDef.getMapping().add(map);
        return teamDef;
    }
}
