/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.repositoriesmapping;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * The mapping table must refuse two mappings onto the same local folder:
 * every file under it would match more than one mapping and the project
 * would no longer load ("Multiple mappings for file").
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RepositoriesMappingControllerTest {

    private static List<RepositoryDefinition> reposWithLocals(String... locals) {
        RepositoryDefinition repo = new RepositoryDefinition();
        repo.setType("git");
        repo.setUrl("https://example.com/team-project.git");
        for (String local : locals) {
            RepositoryMapping mapping = new RepositoryMapping();
            mapping.setLocal(local);
            mapping.setRepository(local);
            repo.getMapping().add(mapping);
        }
        List<RepositoryDefinition> repos = new ArrayList<>();
        repos.add(repo);
        return repos;
    }

    private static String validate(List<RepositoryDefinition> input) {
        RepositoriesMappingController controller = new RepositoriesMappingController(
                new RepositoriesMappingPanel(), input);
        return controller.validateInput();
    }

    @Test
    public void testDistinctLocalFoldersAreValid() {
        assertNull(validate(reposWithLocals("/", "/glossary")));
    }

    @Test
    public void testDuplicateLocalFolderIsRejected() {
        assertNotNull("duplicate local folders must be rejected",
                validate(reposWithLocals("/", "/")));
    }

    @Test
    public void testSlashVariantsOfSameFolderAreRejected() {
        assertNotNull("the empty local path and the root slash address the same folder",
                validate(reposWithLocals("", "/")));
    }
}
