/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay, Alex Buloichik
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import org.omegat.core.team2.impl.SVNRemoteRepository2;

import gen.core.project.RepositoryDefinition;

@RunWith(Parameterized.class)
public class SVNRemoteRepository2IT extends AbstractRemoteRepository2IT {

    String svnSubPath;
    SVNURL tgtURL;

    @Parameterized.Parameters
    public static Collection<String> subPath() {
        return Arrays.asList("", "/asubrepo");
    }

    public SVNRemoteRepository2IT(String subPath) {
        this.svnSubPath = subPath;
    }

    @Override
    void prepareLocalRepo() throws Exception {
        SVNRepositoryFactoryImpl.setup();
        tgtURL = SVNRepositoryFactory.createLocalRepository( tempRepoDir.toFile(), true , false );
        prepareFilesInLocalRepository(tgtURL.toString());
    }

    private void prepareFilesInLocalRepository(String url) throws Exception {
        Path tempSVNClientDir = Files.createTempDirectory("omegat-team-svnc");

        SVNRemoteRepository2 rr2 = new SVNRemoteRepository2();

        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        repositoryDefinition.setType("SVN");
        repositoryDefinition.setUrl(url);

        ProjectTeamSettings projectTeamSettings = new ProjectTeamSettings(tempSVNClientDir.toFile());

        File svnCheckoutDir = new File(tempSVNClientDir.toFile(), "mysvnrepo");
        rr2.init(repositoryDefinition, svnCheckoutDir, projectTeamSettings);
        rr2.switchToVersion(null);

        String newFile = createFileInSubdir(svnCheckoutDir, "asubrepo");
        rr2.addForCommit(newFile);
        rr2.commit(null, "init");

        FileUtils.deleteDirectory(tempSVNClientDir.toFile());
    }

    @Override
    IRemoteRepository2 getRr2() {
        return new SVNRemoteRepository2();
    }

    @Override
    void configureRepositoryDefinition() {
        repositoryDefinition.setType("SVN");
        repositoryDefinition.setUrl(tgtURL.toString() + svnSubPath);
    }

    void assertFileOrDirDeleted(String dir, String fileInDir, String actual) {
        assertEquals("the directory itself is deleted", dir, actual);
    }

    String toRr2Notation(String file) {
        return file;
    }
}
