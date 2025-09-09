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

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.fsm.PreparedFileInfo;
import org.omegat.core.team2.fsm.context.RebaseContext;
import org.omegat.core.team2.fsm.operation.GlossaryRebaseOperation;
import org.omegat.core.team2.fsm.operation.IRebaseOperation;
import org.omegat.core.team2.fsm.operation.TMXRebaseOperation;
import org.omegat.util.OConsts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Files have been prepared for synchronization - ready for rebase.
 */
public final class PreparedState extends TeamSyncState {

    public PreparedState(Map<String, PreparedFileInfo> preparedFiles, RemoteRepositoryProvider provider,
                         boolean isOnlineMode) {
        super(preparedFiles, provider, isOnlineMode);
    }

    @Override
    public TeamSyncState prepare(File projectDir, String tmxPath, String glossaryPath) throws Exception {
        throw new IllegalStateException("Already prepared - use rebase() or reset() first");
    }

    @Override
    public TeamSyncState rebase(ProjectTMX projectTMX) throws Exception {
        if (!canRebase()) {
            throw new IllegalStateException("Cannot rebase: " + getStatusDescription());
        }

        Map<String, PreparedFileInfo> rebasedFiles = new HashMap<>();

        for (Map.Entry<String, PreparedFileInfo> entry : preparedFiles.entrySet()) {
            String path = entry.getKey();
            PreparedFileInfo prepared = entry.getValue();

            // Perform rebase operation and update file info
            PreparedFileInfo rebased = performRebase(prepared, path, projectTMX);
            rebasedFiles.put(path, rebased);
        }

        return new RebasedState(rebasedFiles, provider, isOnlineMode);
    }

    private PreparedFileInfo performRebase(PreparedFileInfo prepared, String path, ProjectTMX projectTMX)
            throws Exception {
        // Create a context for the rebase operation
        RebaseContext context = new RebaseContext(prepared, provider, path);

        // Determine if file changed locally and remotely
        boolean fileChangedLocally = context.isFileChangedLocally();
        boolean fileChangedRemotely = context.isFileChangedRemotely();

        String commitComment = null;
        String charset = null;

        if (fileChangedLocally) {
            // Create rebaser based on file type
            IRebaseOperation rebaser = createRebaser(path, projectTMX);
            rebaser.parseBaseFile(context.getBaseFile());

            if (fileChangedRemotely) {
                rebaser.parseHeadFile(context.getHeadFile());
            }

            // Perform the actual rebase
            context.performRebase(fileChangedLocally, fileChangedRemotely, rebaser);

            commitComment = rebaser.getCommentForCommit();
            charset = rebaser.getFileCharset(context.getLocalFile());
        }

        return prepared.withCommitInfo(fileChangedLocally, commitComment, charset);
    }

    private IRebaseOperation createRebaser(String path, ProjectTMX projectTMX) {
        ProjectProperties prop = Core.getProject().getProjectProperties(); // FIXME
        if (path.endsWith(OConsts.STATUS_EXTENSION)) {
            return new TMXRebaseOperation(projectTMX, prop);
        } else if (path.contains("glossary")) {
            return new GlossaryRebaseOperation(prop);
        }
        throw new IllegalArgumentException("Unknown file type for rebase: " + path);
    }

    @Override
    public TeamSyncState commit() throws Exception {
        throw new IllegalStateException("Cannot commit from prepared state - must rebase first");
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
        return !preparedFiles.isEmpty();
    }

    @Override
    public boolean canCommit() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        return String.format("Prepared %d files for rebase", preparedFiles.size());
    }
}
