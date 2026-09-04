/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik, Martin Fleurke, Aaron Madlon-Kay
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

package org.omegat.core.team2;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.jetbrains.annotations.Nullable;
import org.omegat.core.team2.operation.IRebaseOperation;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;

import org.omegat.util.Log;

/**
 * Core for rebase and commit files.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public final class RebaseAndCommit {

    private RebaseAndCommit() {
    }

    public static final String VERSION_PREFIX = "version-based-on.";

    /**
     * Load BASE and HEAD from remote repository into temp storage for future
     * rebase.
     */
    public static @Nullable PreparedFileInfo prepare(RemoteRepositoryProvider provider, File projectDir, String path)
            throws Exception {
        if (!provider.isUnderMapping(path)) {
            throw new RuntimeException("Path is not under mapping: " + path);
        }

        String savedVersion = provider.getTeamSettings().get(RebaseAndCommit.VERSION_PREFIX + path);
        if (savedVersion == null) {
            return null;
        }

        Prepared r = new Prepared();
        r.path = path;
        final String currentBaseVersion = savedVersion;
        // retrieve BASE version
        File baseFile;
        try {
            baseFile = provider.switchToVersion(path, currentBaseVersion);
        } catch (Exception ex) {
            if (!isMissingVersion(ex)) {
                throw ex;
            }
            // The saved base version no longer exists in the repository, e.g.
            // after the repository or the local copy was rebuilt. Skip the
            // preparation; the synchronous rebase recovers via an empty base.
            Log.logWarningRB("TEAM_BASE_VERSION_LOST", path, currentBaseVersion);
            Log.logDebug("stale base version in prepare: {0}", ex);
            return null;
        }
        // save it to prepared dir
        r.versionBase = currentBaseVersion;
        r.fileBase = provider.toPrepared(baseFile);

        // retrieve HEAD version
        File headFile = provider.switchToVersion(path, null);
        // get version id
        r.versionHead = provider.getVersion(path);
        r.fileHead = provider.toPrepared(headFile);

        return new PreparedFileInfo(r);
    }

    public static @Nullable PreparedFileInfo rebaseAndCommit(@Nullable PreparedFileInfo prep, RemoteRepositoryProvider provider,
                                                   File projectDir, String path, IRebaseOperation rebaser)
            throws Exception {
        if (!provider.isUnderMapping(path)) {
            throw new RuntimeException("Path is not under mapping: " + path);
        }

        final BaseState base = resolveBase(prep, provider, projectDir, path);
        final String currentBaseVersion = base.version();
        final File baseRepoFile = base.file();
        final File localFile = new File(projectDir, path);
        final boolean fileChangedLocally;
        if (!localFile.exists()) {
            // there is no local file - just use remote
            Log.logDebug("local file '{0}' doesn't exist", path);
            fileChangedLocally = false;
        } else if (FileUtils.contentEquals(baseRepoFile, localFile)) {
            // versioned file was not changed - no need to commit
            Log.logDebug("local file '{0}' wasn't changed", path);
            fileChangedLocally = false;
        } else {
            Log.logDebug("local file '{0}' was changed", path);
            fileChangedLocally = true;
            rebaser.parseBaseFile(baseRepoFile);
        }
        // baseRepoFile is not valid anymore because we will switch to other
        // version

        File headRepoFile = null;
        String headVersion = null;
        if (prep != null && prep.getVersionHead() != null && prep.getFileHead() != null) {
            headVersion = prep.getVersionHead();
            headRepoFile = prep.getFileHead();
        }
        if (headVersion == null) {
            headRepoFile = provider.switchToVersion(path, null);
            headVersion = provider.getVersion(path);
        }
        final boolean fileChangedRemotely;
        if (!localFile.exists()) {
            // there is no local file - just use remote
            if (headRepoFile.exists()) {
                fileChangedRemotely = true;
                rebaser.parseHeadFile(headRepoFile);
            } else {
                // there is no remote file also
                fileChangedRemotely = false;
            }
        } else if (Objects.equals(currentBaseVersion, headVersion)) {
            Log.logDebug("remote file '{0}' wasn't changed", path);
            fileChangedRemotely = false;
        } else {
            // base and head versions are differ - somebody else committed
            // changes
            Log.logDebug("remote file '{0}' was changed", path);
            fileChangedRemotely = true;
            rebaser.parseHeadFile(headRepoFile);
        }

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
        } else if (fileChangedLocally /* && !fileChangedRemotely = true */) {
            // only local changes - just use local file
            Log.logDebug("only local changes - just use local file '{0}'", path);
        } else if (fileChangedRemotely /* && !fileChangedLocally = true */) {
            // only remote changes - get remote
            Log.logDebug("only remote changes - get remote '{0}'", path);
            needBackup = true;
            if (headRepoFile.exists()) { // otherwise file was removed remotely
                FileUtils.copyFile(headRepoFile, tempOut);
            }
        } else {
            Log.logDebug("there are no changes '{0}'", path);
            // there are no changes
        }

        if (needBackup) {
            // new file was saved, need to update version
            // code below tries to update file "in transaction" with update
            // version
            if (localFile.exists()) {
                final File bakTemp = new File(projectDir, path + "#oldbased_on_" + currentBaseVersion);
                boolean ignored = bakTemp.delete();
                FileUtils.moveFile(localFile, bakTemp);
            }
            provider.getTeamSettings().set(VERSION_PREFIX + path, headVersion);
            if (tempOut.exists()) {
                boolean ignored = localFile.delete();
                FileUtils.moveFile(tempOut, localFile);
                Log.logDebug("create local file {0}", localFile);
            }
        }

        if (base.stale() && !needBackup && headVersion != null) {
            // nothing was merged or committed, so heal the lost marker directly
            provider.getTeamSettings().set(VERSION_PREFIX + path, headVersion);
        }

        if (prep != null) {
            Prepared prepared = new Prepared();
            // commitPrepared() later needs the path and the head version
            prepared.path = path;
            prepared.versionBase = currentBaseVersion;
            prepared.versionHead = headVersion;
            prepared.needToCommit = fileChangedLocally;
            prepared.commitComment = rebaser.getCommentForCommit();
            if (fileChangedLocally) {
                prepared.charset = rebaser.getFileCharset(localFile);
            }
            // no need to commit yet - it will make other thread after
            return new PreparedFileInfo(prepared);
        }

        if (fileChangedLocally) {
            // new file already saved - need to commit
            String comment = rebaser.getCommentForCommit();
            provider.copyFilesFromProjectToRepos(path, rebaser.getFileCharset(localFile));
            String newVersion = provider.commitFileAfterVersion(path, comment, headVersion, null);
            if (newVersion != null) {
                // file was committed good
                provider.getTeamSettings().set(VERSION_PREFIX + path, newVersion);
            }
            rebaser.reload(headRepoFile);
        } else {
            // no changes so just load.
            rebaser.reload(headRepoFile);
        }
        return null;
    }

    /**
     * Commit later.
     */
    public static @Nullable String commitPrepared(PreparedFileInfo prep, RemoteRepositoryProvider provider,
            @Nullable String possibleHeadVersion) throws Exception {
        if (!prep.needToCommit()) {
            // there was no changes
            return null;
        }
        provider.copyFilesFromProjectToRepos(prep.getPath(), prep.getCharset());
        String newVersion = provider.commitFileAfterVersion(prep.getPath(), prep.getCommitComment(),
                prep.getVersionHead(), possibleHeadVersion);
        if (newVersion != null) {
            // file was committed good
            provider.getTeamSettings().set(VERSION_PREFIX + prep.getPath(), newVersion);
        }
        return newVersion;
    }

    @Deprecated
    public interface IRebase {
        /**
         * Rebaser should read and parse BASE version of file. It can't just
         * remember file path because file will be removed after switch into
         * other version. Rebase can be called after that or can not be called.
         * <p>
         * Case for non-exist file: it's correct call. That means file is just
         * created in local box. But after that, remote repository can also
         * contain file, i.e. two users created file independently, then rebase
         * will be called. Implementation should interpret non-exist file as
         * empty data.
         */
        void parseBaseFile(File file) throws Exception;

        /**
         * Rebaser should read and parse HEAD version of file. It can't just
         * remember file path because file will be removed after switch into
         * other version. Rebase can be called after that or can not be called.
         * <p>
         * Case for non-exist file: it's correct call. That means file was
         * removed from repository. Implementation should interpret non-exist
         * file as empty data.
         */
        void parseHeadFile(File file) throws Exception;

        /**
         * Rebase using BASE, HEAD and non-committed version should be
         * processed. At this time parseBaseFile and parseHeadFile was already
         * called. Keep in mind that this method can display some dialogs to
         * user, i.e. can work up to some minutes.
         */
        void rebaseAndSave(File out) throws Exception;

        /**
         * Reload projectTMX from resulted TMX.
         */
        void reload(File file) throws Exception;

        /**
         * Construct commit message.
         */
        String getCommentForCommit();

        /**
         * Get charset of file for convert EOL to repository. Implementation can
         * return null if conversion not required.
         */
        String getFileCharset(File file) throws Exception;
    }

    /**
     * The base version to rebase against and its checked-out file. When the
     * stored marker is stale (the version no longer exists in the repository),
     * {@code stale} is true, {@code file} does not exist and {@code version}
     * is a sentinel: the rebase contract treats a non-existent file as empty
     * data, so local and remote survive as a three-way merge with an empty
     * base instead of one side silently overwriting the other.
     */
    private record BaseState(String version, File file, boolean stale) {
    }

    private static BaseState resolveBase(PreparedFileInfo prep, RemoteRepositoryProvider provider,
            File projectDir, String path) throws Exception {
        String savedVersion = provider.getTeamSettings().get(VERSION_PREFIX + path);
        if (savedVersion == null) {
            File file = provider.switchToVersion(path, null);
            return new BaseState(provider.getVersion(path), file, false);
        }
        if (prep != null && savedVersion.equals(prep.getVersionBase()) && prep.getFileBase() != null) {
            return new BaseState(savedVersion, prep.getFileBase(), false);
        }
        try {
            return new BaseState(savedVersion, provider.switchToVersion(path, savedVersion), false);
        } catch (Exception ex) {
            if (!isMissingVersion(ex)) {
                throw ex;
            }
            // The saved base version no longer exists in the repository, e.g.
            // after the repository or the local copy was rebuilt. Recover with
            // an empty base instead of making the project unloadable.
            Log.logWarningRB("TEAM_BASE_VERSION_LOST", path, savedVersion);
            Log.logDebug("stale base version in rebaseAndCommit: {0}", ex);
            File empty = new File(projectDir, path + "#lost_base_" + savedVersion);
            if (empty.exists() && !empty.delete()) {
                throw new IOException("Unable to delete previous temp file " + empty);
            }
            return new BaseState("lost-base-" + savedVersion, empty, true);
        }
    }

    /**
     * Only a version that provably does not exist (anymore) in the repository
     * counts as a stale marker. Transient failures - network outages, stale
     * lock files, full disks - must keep failing loudly: recovering from them
     * would destroy a still-valid marker and turn a hiccup into a merge
     * against the wrong base.
     */
    private static boolean isMissingVersion(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof MissingObjectException || t instanceof RefNotFoundException) {
                return true;
            }
            if (t instanceof SVNException
                    && ((SVNException) t).getErrorMessage().getErrorCode() == SVNErrorCode.FS_NO_SUCH_REVISION) {
                return true;
            }
        }
        return false;
    }

    /**
     * Info about prepared file.
     */
    public static class Prepared {
        public String path;
        public @Nullable File fileBase, fileHead;
        public @Nullable String versionBase;
        public @Nullable String versionHead;
        public boolean needToCommit;
        public @Nullable String commitComment;
        public @Nullable String charset;
    }
}
