/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Damien Rembert
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

public final class RemoteRepositoryFactoryTest {

    @Test
    public void testDetectRepositoryType_svnPrefix() {
        String url = "svn://example.com/repo";
        assertEquals("svn", RemoteRepositoryFactory.detectRepositoryType(url));
    }

    @Test
    public void testDetectRepositoryType_gitPrefix() {
        String url = "git://example.com/repo";
        assertEquals("git", RemoteRepositoryFactory.detectRepositoryType(url));
    }

    @Test
    public void testDetectRepositoryType_httpsGitPrefix() {
        String url = "https://git.example.com/repo";
        assertEquals("git", RemoteRepositoryFactory.detectRepositoryType(url));
    }

    @Test
    public void testDetectRepositoryType_gitSuffix() {
        String url = "https://example.com/repo.git";
        assertEquals("git", RemoteRepositoryFactory.detectRepositoryType(url));
    }

}
