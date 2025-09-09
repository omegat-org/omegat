package org.omegat.core.team2.fsm.state;

import org.omegat.core.data.ProjectTMX;
import org.omegat.core.team2.RebaseAndCommit;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.fsm.PreparedFileInfo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * No synchronization in progress - ready to start new sync operation.
 */
public final class IdleState extends TeamSyncState {

    public IdleState(RemoteRepositoryProvider provider, boolean isOnlineMode) {
        super(Collections.emptyMap(), provider, isOnlineMode);
    }

    @Override
    public TeamSyncState prepare(File projectDir, String tmxPath, String glossaryPath) throws Exception {
        if (!canPrepare()) {
            throw new IllegalStateException("Cannot prepare from idle state: " + getStatusDescription());
        }

        Map<String, PreparedFileInfo> prepared = new HashMap<>();

        // Prepare TMX if applicable
        if (tmxPath != null && provider.isUnderMapping(tmxPath)) {
            PreparedFileInfo tmxPrepared = prepareFile(projectDir, tmxPath);
            if (tmxPrepared != null) {
                prepared.put(tmxPath, tmxPrepared);
            }
        }

        // Prepare glossary if applicable
        if (glossaryPath != null && provider.isUnderMapping(glossaryPath)) {
            PreparedFileInfo glossaryPrepared = prepareFile(projectDir, glossaryPath);
            if (glossaryPrepared != null) {
                prepared.put(glossaryPath, glossaryPrepared);
            }
        }

        return new PreparedState(prepared, provider, isOnlineMode);
    }

    private PreparedFileInfo prepareFile(File projectDir, String path) throws Exception {
        String savedVersion = provider.getTeamSettings().get(RebaseAndCommit.VERSION_PREFIX + path);
        if (savedVersion == null) {
            return null;
        }

        File baseFile = provider.switchToVersion(path, savedVersion);
        File headFile = provider.switchToVersion(path, null);
        String headVersion = provider.getVersion(path);

        return new PreparedFileInfo.Builder(path)
                .withVersions(savedVersion, headVersion)
                .withFiles(provider.toPrepared(baseFile), provider.toPrepared(headFile))
                .build();
    }

    @Override
    public TeamSyncState rebase(ProjectTMX projectTMX) throws Exception {
        throw new IllegalStateException("Cannot rebase from idle state - must prepare first");
    }

    @Override
    public TeamSyncState commit() throws Exception {
        throw new IllegalStateException("Cannot commit from idle state - nothing to commit");
    }

    @Override
    public TeamSyncState reset() {
        return this; // Already idle
    }

    @Override
    public boolean canPrepare() {
        return provider != null && isOnlineMode;
    }

    @Override
    public boolean canRebase() {
        return false;
    }

    @Override
    public boolean canCommit() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        if (provider == null) {
            return "No remote repository configured";
        }
        if (!isOnlineMode) {
            return "Offline mode - cannot sync";
        }
        return "Ready to prepare synchronization";
    }
}
