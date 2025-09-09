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

package org.omegat.core.team2.fsm.context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.omegat.core.team2.RebaseAndCommit;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.fsm.PreparedFileInfo;
import org.omegat.core.team2.fsm.operation.IRebaseOperation;
import org.omegat.util.Log;

import java.io.File;

/**
 * Context for rebase operations that encapsulates the complex logic from RebaseAndCommit.rebaseAndCommit
 */
public class RebaseContext {

    private final PreparedFileInfo prepared;
    private final RemoteRepositoryProvider provider;
    private final String path;
    private final File projectDir;
    private final File localFile;

    private File baseRepoFile;
    private File headRepoFile;
    private String currentBaseVersion;
    private String headVersion;

    public RebaseContext(PreparedFileInfo prepared, RemoteRepositoryProvider provider, String path) throws Exception {
        this.prepared = prepared;
        this.provider = provider;
        this.path = path;
        this.projectDir = provider.getProjectRoot();
        this.localFile = new File(projectDir, path);

        initializeVersions();
    }

    private void initializeVersions() throws Exception {
        // Get current base version
        String savedVersion = provider.getTeamSettings().get(RebaseAndCommit.VERSION_PREFIX + path);
        if (savedVersion != null) {
            currentBaseVersion = savedVersion;
        } else {
            provider.switchToVersion(path, null);
            currentBaseVersion = provider.getVersion(path);
        }

        // Set up base file
        if (prepared != null && prepared.getVersionBase().equals(currentBaseVersion)) {
            baseRepoFile = prepared.getFileBase();
        }
        if (baseRepoFile == null) {
            baseRepoFile = provider.switchToVersion(path, currentBaseVersion);
        }

        // Set up head file
        if (prepared != null) {
            headVersion = prepared.getVersionHead();
            headRepoFile = prepared.getFileHead();
        }
        if (headVersion == null) {
            headRepoFile = provider.switchToVersion(path, null);
            headVersion = provider.getVersion(path);
        }
    }

    public boolean isFileChangedLocally() throws Exception {
        if (!localFile.exists()) {
            Log.logDebug("local file '{0}' doesn't exist", path);
            return false;
        } else if (FileUtils.contentEquals(baseRepoFile, localFile)) {
            Log.logDebug("local file '{0}' wasn't changed", path);
            return false;
        } else {
            Log.logDebug("local file '{0}' was changed", path);
            return true;
        }
    }

    public boolean isFileChangedRemotely() {
        if (!localFile.exists()) {
            return headRepoFile.exists();
        } else if (StringUtils.equals(currentBaseVersion, headVersion)) {
            Log.logDebug("remote file '{0}' wasn't changed", path);
            return false;
        } else {
            Log.logDebug("remote file '{0}' was changed", path);
            return true;
        }
    }

    public void performRebase(boolean fileChangedLocally, boolean fileChangedRemotely, IRebaseOperation rebaser) throws Exception {
        final File tempOut = new File(projectDir, path + "#based_on_" + headVersion);
        if (tempOut.exists() && !tempOut.delete()) {
            throw new Exception("Unable to delete previous temp file");
        }

        boolean needBackup = false;

        if (fileChangedLocally && fileChangedRemotely) {
            // rebase need only in case file was changed locally AND remotely
            Log.logDebug("rebase and save '{0}'", path);
            needBackup = true;
            rebaser.rebaseAndSave(tempOut);
        } else if (fileChangedLocally) {
            // only local changes - just use local file
            Log.logDebug("only local changes - just use local file '{0}'", path);
        } else if (fileChangedRemotely) {
            // only remote changes - get remote
            Log.logDebug("only remote changes - get remote '{0}'", path);
            needBackup = true;
            if (headRepoFile.exists()) { // otherwise file was removed remotely
                FileUtils.copyFile(headRepoFile, tempOut);
            }
        } else {
            Log.logDebug("there are no changes '{0}'", path);
        }

        if (needBackup) {
            updateLocalFile(tempOut);
        }
    }

    private void updateLocalFile(File tempOut) throws Exception {
        // new file was saved, need to update version
        // code below tries to update file "in transaction" with update version
        if (localFile.exists()) {
            final File bakTemp = new File(projectDir, path + "#oldbased_on_" + currentBaseVersion);
            boolean ignored = bakTemp.delete();
            FileUtils.moveFile(localFile, bakTemp);
        }
        provider.getTeamSettings().set(RebaseAndCommit.VERSION_PREFIX + path, headVersion);
        if (tempOut.exists()) {
            boolean ignored = localFile.delete();
            FileUtils.moveFile(tempOut, localFile);
            Log.logDebug("create local file {0}", localFile);
        }
    }

    // Getters
    public File getBaseFile() {
        return baseRepoFile;
    }
    public File getHeadFile() {
        return headRepoFile;
    }
    public File getLocalFile() {
        return localFile;
    }
    public String getHeadVersion() {
        return headVersion;
    }
}
