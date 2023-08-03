/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Briac Pilpre
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

package org.omegat.core.team2.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.core.team2.RemoteRepositoryProvider;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Simple file repository implementation. The "remote" files are read-only,
 * there is no "commit" to avoid clobbering remote files.
 */
public class FileRepository implements IRemoteRepository2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRepository.class);

    private RepositoryDefinition config;
    private File baseDirectory;

    @Override
    public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) throws Exception {
        LOGGER.atDebug().log("Initialize file repository");
        config = repo;
        baseDirectory = dir;
    }

    @Override
    public String getFileVersion(String file) throws Exception {
        return null;
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        if (version != null) {
            throw new RuntimeException("Not supported");
        }

        LOGGER.atDebug().log("Update to latest");
        File baseSource = new File(config.getUrl());

        // If the path is relative, it's relative to the projectDir
        if (!baseSource.isAbsolute()) {
            baseSource = new File(Core.getProject().getProjectProperties().getProjectRootDir(),
                    config.getUrl());
            LOGGER.atDebug().log("Using base directory \"" + baseSource.getCanonicalPath() + "\"");
        }

        // retrieve all mapped files
        for (RepositoryMapping m : config.getMapping()) {
            File src = new File(baseSource, m.getRepository());
            File dst = new File(baseDirectory, m.getRepository());
            LOGGER.atDebug()
                    .log("Copy \"" + src.getAbsolutePath() + "\" to \"" + dst.getAbsolutePath() + "\".");
            copyFiles(src, dst);
        }
    }

    private void copyFiles(File src, File dst) throws IOException {
        dst.getParentFile().mkdirs();
        String repoDir = new File(RemoteRepositoryProvider.REPO_SUBDIR).getName();
        if (src.exists() && src.isDirectory()) {
            for (File f : src.listFiles()) {
                // Skip the ".repositories" directory to avoid recursion problems
                if (f.getName().equals(repoDir)) {
                    continue;
                }
                copyFiles(f, new File(dst, f.getName()));
            }
        } else {
            FileUtils.copyFile(src, dst);
        }
    }

    @Override
    public void addForCommit(String path) throws Exception {
        LOGGER.atDebug().log(
                String.format("Cannot add files for commit for File repositories. Skipping \"%s\".", path));
    }

    @Override
    public void addForDeletion(String path) throws Exception {
        LOGGER.atDebug().log(
                String.format("Cannot add files for deletion for File repositories. Skipping \"%s\".", path));
    }

    @Override
    public File getLocalDirectory() {
        return baseDirectory;
    }

    @Override
    public String[] getRecentlyDeletedFiles() throws Exception {
        return new String[0];
    }

    @Override
    public String commit(String[] onVersions, String comment) throws Exception {
        LOGGER.atDebug().log("Commit not supported for File repositories.");

        return null;
    }

}
