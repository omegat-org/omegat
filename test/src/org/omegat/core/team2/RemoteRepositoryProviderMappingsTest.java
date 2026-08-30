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

package org.omegat.core.team2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * A repository definition with duplicated mapping entries used to make the
 * project unloadable: every file under the duplicated folder matched more
 * than one mapping and oneMapping() threw. Duplicates carry no information,
 * so loading drops them instead.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RemoteRepositoryProviderMappingsTest {

    private static RepositoryDefinition repoWithMappings(String... localRepositoryPairs) {
        RepositoryDefinition repo = new RepositoryDefinition();
        repo.setType("git");
        repo.setUrl("https://example.com/team-project.git");
        for (int i = 0; i < localRepositoryPairs.length; i += 2) {
            RepositoryMapping mapping = new RepositoryMapping();
            mapping.setLocal(localRepositoryPairs[i]);
            mapping.setRepository(localRepositoryPairs[i + 1]);
            repo.getMapping().add(mapping);
        }
        return repo;
    }

    @Test
    public void testExactDuplicateIsDropped() {
        RepositoryDefinition repo = repoWithMappings("/", "/", "/", "/");
        RemoteRepositoryProvider.dropDuplicateMappings(repo);
        assertEquals(1, repo.getMapping().size());
    }

    @Test
    public void testSlashVariantsCountAsDuplicates() {
        // the root mapping is written as ""/"" by team checkouts and as
        // "/"/"/" by the mapping dialog
        RepositoryDefinition repo = repoWithMappings("", "", "/", "/");
        RemoteRepositoryProvider.dropDuplicateMappings(repo);
        assertEquals(1, repo.getMapping().size());
        assertEquals("the first entry must survive", "", repo.getMapping().get(0).getLocal());
    }

    @Test
    public void testDistinctMappingsAreKept() {
        RepositoryDefinition repo = repoWithMappings("/", "/", "/glossary", "/shared/glossary");
        RemoteRepositoryProvider.dropDuplicateMappings(repo);
        assertEquals(2, repo.getMapping().size());
    }
}
