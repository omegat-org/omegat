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
