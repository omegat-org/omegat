/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.namespace.QName;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig.AutoCRLF;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.util.Log;

import gen.core.project.RepositoryDefinition;

/**
 * GIT repository connection implementation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class GITRemoteRepository2 implements IRemoteRepository2 {
    private static final Logger LOGGER = Logger.getLogger(GITRemoteRepository2.class.getName());

    protected static final String LOCAL_BRANCH = "master";
    protected static final String REMOTE_BRANCH = "origin/master";
    protected static final String REMOTE = "origin";

    protected static final int TIMEOUT = 30; // seconds

    String repositoryURL;
    File localDirectory;

    protected Repository repository;

    static {
        CredentialsProvider.setDefault(new GITCredentialsProvider());
    }

    @Override
    public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) throws Exception {
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
            configRepo();
            try (Git git = new Git(repository)) {
                git.submoduleInit().call();
                git.submoduleUpdate().setTimeout(TIMEOUT).call();
            }
        } else {
            Log.logInfoRB("GIT_START", "clone");
            CloneCommand c = Git.cloneRepository();
            c.setURI(repositoryURL);
            c.setDirectory(localDirectory);
            c.setTimeout(TIMEOUT);
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
            try (Git git = new Git(repository)) {
                git.submoduleInit().call();
                git.submoduleUpdate().setTimeout(TIMEOUT).call();
            }
            configRepo();
            Log.logInfoRB("GIT_FINISH", "clone");
        }

        // cleanup repository
        try (Git git = new Git(repository)) {
            git.reset().setMode(ResetType.HARD).call();
        }
    }

    private void configRepo() throws IOException {
        StoredConfig config = repository.getConfig();

        // Deal with line endings. A normalized repo has LF line endings.
        // OmegaT uses line endings of OS for storing tmx files.
        // To do auto converting, we need to change a setting:
        if ("\r\n".equals(System.lineSeparator())) {
            // on windows machines, convert text files to CRLF
            config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, true);
        } else {
            // on Linux/Mac machines (using LF), don't convert text files
            // but use input format, unchanged.
            // NB: I don't know correct setting for OS'es like MacOS <= 9,
            // which uses CR. Git manual only speaks about converting from/to
            // CRLF, so for CR, you probably don't want conversion either.
            config.setEnum(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF,
                    AutoCRLF.INPUT);
        }

        // Perform GC synchronously to avoid locking issues
        config.setBoolean(ConfigConstants.CONFIG_GC_SECTION, null, ConfigConstants.CONFIG_KEY_AUTODETACH, false);

        config.save();
    }

    @Override
    public String getFileVersion(String file) throws IOException {
        File f = new File(localDirectory, file);
        if (!f.exists()) {
            return null;
        }
        return getCurrentVersion();
    }

    protected String getCurrentVersion() throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            Ref localBranch = repository.findRef("HEAD");
            RevCommit headCommit = walk.lookupCommit(localBranch.getObjectId());
            return headCommit.getName();
        }
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        try (Git git = new Git(repository)) {
            if (version == null) {
                version = REMOTE_BRANCH;
                // TODO fetch
                git.fetch().setRemote(REMOTE).setTimeout(TIMEOUT).call();
            }
            Log.logDebug(LOGGER, "GIT switchToVersion {0} ", version);
            git.reset().setMode(ResetType.HARD).call();
            git.checkout().setName(version).call();
            git.branchDelete().setForce(true).setBranchNames(LOCAL_BRANCH).call();
            git.checkout().setCreateBranch(true).setName(LOCAL_BRANCH).setStartPoint(version).call();
        } catch (TransportException e) {
            throw new NetworkException(e);
        }
    }

    @Override
    public void addForCommit(String path) throws Exception {
        Log.logInfoRB("GIT_START", "addForCommit");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(path).call();
            Log.logInfoRB("GIT_FINISH", "addForCommit");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "addForCommit", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public File getLocalDirectory() {
        return localDirectory;
    }

    @Override
    public String commit(String[] onVersions, String comment) throws Exception {
        if (onVersions != null) {
            // check versions
            String currentVersion = getCurrentVersion();
            boolean hasVersion = false;
            for (String v : onVersions) {
                if (v != null) {
                    hasVersion = true;
                    break;
                }
            }
            if (hasVersion) {
                boolean found = false;
                for (String v : onVersions) {
                    if (v != null) {
                        if (v.equals(currentVersion)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    throw new RuntimeException(
                            "Version changed from " + Arrays.toString(onVersions) + " to " + currentVersion);
                }
            }
        }
        if (indexIsEmpty(DirCache.read(repository))) {
            // Nothing was actually added to the index so we can just return.
            Log.logInfoRB("GIT_NO_CHANGES", "upload");
            return null;
        }
        Log.logInfoRB("GIT_START", "upload");
        try (Git git = new Git(repository)) {
            RevCommit commit = git.commit().setMessage(comment).call();
            Iterable<PushResult> results = git.push().setTimeout(TIMEOUT).setRemote(REMOTE).add(LOCAL_BRANCH)
                    .call();
            List<Status> statuses = StreamSupport.stream(results.spliterator(), false)
                    .flatMap(r -> r.getRemoteUpdates().stream()).map(RemoteRefUpdate::getStatus)
                    .collect(Collectors.toList());
            String result;
            if (statuses.isEmpty() || statuses.stream().anyMatch(s -> s != RemoteRefUpdate.Status.OK)) {
                Log.logWarningRB("GIT_CONFLICT");
                result = null;
            } else {
                result = commit.getName();
            }
            Log.logDebug(LOGGER, "GIT committed into new version {0} ", result);
            Log.logInfoRB("GIT_FINISH", "upload");
            return result;
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "upload", ex.getMessage());
            if (ex instanceof TransportException) {
                throw new NetworkException(ex);
            } else {
                throw ex;
            }
        }
    }

    private boolean indexIsEmpty(DirCache dc) throws Exception {
        DirCacheIterator dci = new DirCacheIterator(dc);
        AbstractTreeIterator old = prepareTreeParser(repository, repository.resolve(Constants.HEAD));
        try (Git git = new Git(repository)) {
            List<DiffEntry> diffs = git.diff().setOldTree(old).setNewTree(dci).call();
            return diffs.isEmpty();
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objId) throws Exception {
        // from the commit we can build the tree which allows us to construct
        // the TreeParser
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objId);
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            ObjectReader reader = repository.newObjectReader();
            treeParser.reset(reader, tree.getId());
            return treeParser;
        }
    }

    public static boolean deleteDirectory(File path) {
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
            Collection<Ref> result = new LsRemoteCommand(null).setRemote(url).setTimeout(TIMEOUT).call();
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
