/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2.impl;

import gen.core.project.RepositoryDefinition;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.TeamSettings;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;

/**
 * GIT repository connection implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class GITRemoteRepository2 implements IRemoteRepository2 {
    private static final Logger LOGGER = Logger.getLogger(GITRemoteRepository2.class.getName());

    protected static String LOCAL_BRANCH = "master";
    protected static String REMOTE_BRANCH = "origin/master";
    protected static String REMOTE = "origin";

    String repositoryURL;
    File localDirectory;

    protected Repository repository;

    static {
        CredentialsProvider.setDefault(new GITCredentialsProvider());
    }

    @Override
    public void init(RepositoryDefinition repo, File dir, TeamSettings teamSettings) throws Exception {
        repositoryURL = repo.getUrl();
        localDirectory = dir;

        String predefinedUser = repo.getOtherAttributes().get(new QName("gitUsername"));
        String predefinedPass = repo.getOtherAttributes().get(new QName("gitPassword"));
        String predefinedFingerprint = repo.getOtherAttributes().get(new QName("gitFingerprint"));
        ((GITCredentialsProvider) CredentialsProvider.getDefault()).setPredefinedCredentials(repositoryURL,
                predefinedUser, predefinedPass, predefinedFingerprint);
        ((GITCredentialsProvider) CredentialsProvider.getDefault()).setTeamSettings(teamSettings);

        File gitDir = new File(localDirectory, ".git");
        if (gitDir.exists() && gitDir.isDirectory()) {
            // already cloned
            repository = Git.open(localDirectory).getRepository();
        } else {
            Log.logInfoRB("GIT_START", "clone");
            CloneCommand c = Git.cloneRepository();
            c.setURI(repositoryURL);
            c.setDirectory(localDirectory);
            try {
                c.call();
            } catch (InvalidRemoteException e) {
                if (localDirectory.exists()) {
                    deleteDirectory(localDirectory);
                }
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof org.eclipse.jgit.errors.NoRemoteRepositoryException) {
                    BadRepositoryException bre = new BadRepositoryException(
                            ((org.eclipse.jgit.errors.NoRemoteRepositoryException) cause)
                                    .getLocalizedMessage());
                    bre.initCause(e);
                    throw bre;
                }
                throw e;
            }
            repository = Git.open(localDirectory).getRepository();
            new Git(repository).submoduleInit().call();
            new Git(repository).submoduleUpdate().call();

            // Deal with line endings. A normalized repo has LF line endings.
            // OmegaT uses line endings of OS for storing tmx files.
            // To do auto converting, we need to change a setting:
            StoredConfig config = repository.getConfig();
            if ("\r\n".equals(FileUtil.LINE_SEPARATOR)) {
                // on windows machines, convert text files to CRLF
                config.setBoolean("core", null, "autocrlf", true);
            } else {
                // on Linux/Mac machines (using LF), don't convert text files
                // but use input format, unchanged.
                // NB: I don't know correct setting for OS'es like MacOS <= 9,
                // which uses CR. Git manual only speaks about converting from/to
                // CRLF, so for CR, you probably don't want conversion either.
                config.setString("core", null, "autocrlf", "input");
            }
            config.save();
            Log.logInfoRB("GIT_FINISH", "clone");
        }
    }

    @Override
    public String getFileVersion(String file) throws Exception {
        File f = new File(localDirectory, file);
        if (!f.exists()) {
            return null;
        }
        return getCurrentVersion();
    }

    protected String getCurrentVersion() throws Exception {
        RevWalk walk = new RevWalk(repository);
        Ref localBranch = repository.getRef("HEAD");
        RevCommit headCommit = walk.lookupCommit(localBranch.getObjectId());
        return headCommit.getName();
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        if (version == null) {
            version = REMOTE_BRANCH;
            // TODO fetch
            new Git(repository).fetch().setRemote(REMOTE).call();
        }
        Log.logDebug(LOGGER, "GIT switchToVersion {0} ", version);
        new Git(repository).reset().setMode(ResetType.HARD).call();
        new Git(repository).checkout().setName(version).call();
        new Git(repository).branchDelete().setForce(true).setBranchNames(LOCAL_BRANCH).call();
        new Git(repository).checkout().setCreateBranch(true).setName(LOCAL_BRANCH).setStartPoint(version)
                .call();
    }

    @Override
    public void addForCommit(String path) throws Exception {
        Log.logInfoRB("GIT_START", "addForCommit");
        try {
            new Git(repository).add().addFilepattern(path).call();
            Log.logInfoRB("GIT_FINISH", "addForCommit");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "addForCommit", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String commit(String version, String comment) throws Exception {
        if (version != null) {
            if (!version.equals(getCurrentVersion())) {
                throw new RuntimeException("Version changed");
            }
        }
        Log.logInfoRB("GIT_START", "upload");
        try {
            RevCommit commit = new Git(repository).commit().setMessage(comment).call();
            Iterable<PushResult> results = new Git(repository).push().setRemote(REMOTE).add(LOCAL_BRANCH)
                    .call();
            int count = 0;
            for (PushResult r : results) {
                for (RemoteRefUpdate update : r.getRemoteUpdates()) {
                    count++;
                    if (update.getStatus() != RemoteRefUpdate.Status.OK) {
                        Log.logWarningRB("GIT_CONFLICT");
                    }
                }
            }
            if (count < 1) {
                Log.logWarningRB("GIT_CONFLICT");
            }
            Log.logInfoRB("GIT_FINISH", "upload");
            return commit.getName();
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "upload", ex.getMessage());
            if (ex instanceof TransportException) {
                throw new NetworkException(ex);
            } else {
                throw ex;
            }
        }
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Determines whether or not the supplied URL represents a valid Git repository.
     * 
     * <p>
     * Does the equivalent of <code>git ls-remote <i>url</i></code>.
     * 
     * @param url
     *            URL of supposed remote repository
     * @return true if repository appears to be valid, false otherwise
     */
    public static boolean isGitRepository(String url) {
        // Heuristics to save some waiting time
        try {
            Collection<Ref> result = new LsRemoteCommand(null).setRemote(url).call();
            return !result.isEmpty();
        } catch (TransportException ex) {
            String message = ex.getMessage();
            if (message.endsWith("not authorized") || message.endsWith("Auth fail")
                    || message.contains("Too many authentication failures")
                    || message.contains("Authentication is required")) {
                return true;
            }
            return false;
        } catch (GitAPIException ex) {
            return false;
        } catch (JGitInternalException ex) {
            // Happens if the URL is a Subversion URL like svn://...
            return false;
        }
    }
}
