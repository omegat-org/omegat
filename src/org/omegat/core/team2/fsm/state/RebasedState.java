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
