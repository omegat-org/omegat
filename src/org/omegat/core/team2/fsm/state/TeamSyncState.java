package org.omegat.core.team2.fsm.state;

import org.omegat.core.data.ProjectTMX;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.fsm.PreparedFileInfo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Team synchronization state machine for managing prepared files and sync workflow.
 */
public abstract class TeamSyncState {

    protected final Map<String, PreparedFileInfo> preparedFiles;
    protected final RemoteRepositoryProvider provider;
    protected final boolean isOnlineMode;

    protected TeamSyncState(Map<String, PreparedFileInfo> preparedFiles,
                            RemoteRepositoryProvider provider,
                            boolean isOnlineMode) {
        this.preparedFiles = Collections.unmodifiableMap(new HashMap<>(preparedFiles));
        this.provider = provider;
        this.isOnlineMode = isOnlineMode;
    }

    // State transition methods
    public abstract TeamSyncState prepare(File projectDir, String tmxPath, String glossaryPath) throws Exception;
    public abstract TeamSyncState rebase(ProjectTMX projectTMX) throws Exception;
    public abstract TeamSyncState commit() throws Exception;
    public abstract TeamSyncState reset() throws IOException;

    // Query methods
    public abstract boolean canPrepare();
    public abstract boolean canRebase();
    public abstract boolean canCommit();
    public abstract String getStatusDescription();

    // Helper methods
    public boolean hasPreparedFile(String path) {
        return preparedFiles.containsKey(path);
    }

    public PreparedFileInfo getPreparedFile(String path) {
        return preparedFiles.get(path);
    }

    public Set<String> getPreparedFilePaths() {
        return preparedFiles.keySet();
    }
}
