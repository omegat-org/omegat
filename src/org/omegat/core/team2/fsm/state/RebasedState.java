/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2016 Alex Buloichik
               2009-2010 Didier Briel
               2012 Guido Leenders, Didier Briel, Martin Fleurke
               2013 Aaron Madlon-Kay, Didier Briel
               2014 Aaron Madlon-Kay, Didier Briel
               2015 Aaron Madlon-Kay
               2017-2018 Didier Briel
               2018 Enrique Estevez Fernandez
               2019 Thomas Cordonnier
               2020 Briac Pilpre
               2025 Hiroshi Miura
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

package org.omegat.core.team2.fsm.state;

import org.omegat.core.data.ProjectTMX;
import org.omegat.core.team2.RebaseAndCommit;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.fsm.PreparedFileInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rebase completed - ready for commit.
 */
public final class RebasedState extends TeamSyncState {

    public RebasedState(Map<String, PreparedFileInfo> preparedFiles,
                        RemoteRepositoryProvider provider,
                        boolean isOnlineMode) {
        super(preparedFiles, provider, isOnlineMode);
    }

    @Override
    public TeamSyncState prepare(File projectDir, String tmxPath, String glossaryPath) throws Exception {
        throw new IllegalStateException("Already in rebased state - use commit() or reset() first");
    }

    @Override
    public TeamSyncState rebase(ProjectTMX projectTMX) throws Exception {
        throw new IllegalStateException("Already rebased - use commit() or reset() first");
    }

    @Override
    public TeamSyncState commit() throws Exception {
        if (!canCommit()) {
            throw new IllegalStateException("Cannot commit: " + getStatusDescription());
        }

        List<String> committedVersions = new ArrayList<>();

        for (PreparedFileInfo fileInfo : preparedFiles.values()) {
            if (fileInfo.needToCommit()) {
                String newVersion = commitFile(fileInfo);
                if (newVersion != null) {
                    committedVersions.add(newVersion);
                    updateVersionSettings(fileInfo.getPath(), newVersion);
                }
            }
        }
        /*
            String newVersion = RebaseAndCommit.commitPrepared(tmxPrepared, remoteRepositoryProvider, null);
            RebaseAndCommit.commitPrepared(glossaryPrepared, remoteRepositoryProvider, newVersion);
        */
        provider.cleanPrepared();
        return new IdleState(provider, isOnlineMode);
    }

    private String commitFile(PreparedFileInfo fileInfo) throws Exception {
        provider.copyFilesFromProjectToRepos(fileInfo.getPath(), fileInfo.getCharset());
        String newVersion = provider.commitFileAfterVersion(
                fileInfo.getPath(),
                fileInfo.getCommitComment(),
                fileInfo.getVersionHead(),
                null
        );

        if (newVersion != null) {
            provider.getTeamSettings().set(RebaseAndCommit.VERSION_PREFIX + fileInfo.getPath(), newVersion);
        }

        return newVersion;
    }

    private void updateVersionSettings(String path, String newVersion) {
        provider.getTeamSettings().set(RebaseAndCommit.VERSION_PREFIX + path, newVersion);
    }

    @Override
    public TeamSyncState reset() throws IOException {
        provider.cleanPrepared();
        return new IdleState(provider, isOnlineMode);
    }

    @Override
    public boolean canPrepare() {
        return false;
    }

    @Override
    public boolean canRebase() {
        return false;
    }

    @Override
    public boolean canCommit() {
        return preparedFiles.values().stream().anyMatch(PreparedFileInfo::needToCommit);
    }

    @Override
    public String getStatusDescription() {
        long commitCount = preparedFiles.values().stream()
                .mapToLong(f -> f.needToCommit() ? 1 : 0)
                .sum();
        return String.format("Rebased - %d files ready to commit", commitCount);
    }
}
