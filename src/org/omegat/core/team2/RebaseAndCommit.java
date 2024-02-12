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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.util.OStrings;

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

    private static final ILogger LOGGER = LoggerFactory.getLogger(RebaseAndCommit.class, OStrings.getResourceBundle());

    public static final String VERSION_PREFIX = "version-based-on.";

    /**
     * Load BASE and HEAD from remote repository into temp storage for future
     * rebase.
     */
    public static Prepared prepare(RemoteRepositoryProvider provider, File projectDir, String path)
            throws Exception {
        if (!provider.isUnderMapping(path)) {
            throw new RuntimeException("Path is not under mapping: " + path);
        }

        final String currentBaseVersion;
        String savedVersion = provider.getTeamSettings().get(VERSION_PREFIX + path);
        if (savedVersion == null) {
            return null;
        }

        Prepared r = new Prepared();
        r.path = path;
        currentBaseVersion = savedVersion;
        LOGGER.atDebug().setMessage("Retrieve BASE({0}) version of '{1}'")
                .addArgument(currentBaseVersion).addArgument(path).log();
        // retrieve BASE version
        File baseFile = provider.switchToVersion(path, currentBaseVersion);
        // save it to prepared dir
        r.versionBase = currentBaseVersion;
        r.fileBase = provider.toPrepared(baseFile);

        LOGGER.atDebug().setMessage("Retrieve HEAD version of '{0}'").addArgument(path).log();
        // retrieve HEAD version
        File headFile = provider.switchToVersion(path, null);
        // get version id
        // save it to prepared dir
        r.versionHead = provider.getVersion(path);
        r.fileHead = provider.toPrepared(headFile);
        return r;
    }

    public static void rebaseAndCommit(Prepared prep, RemoteRepositoryProvider provider, File projectDir,
            String path, IRebase rebaser) throws Exception {

        if (!provider.isUnderMapping(path)) {
            throw new RuntimeException("Path is not under mapping: " + path);
        }

        LOGGER.atDebug().setMessage("Rebase and commit '{0}'").addArgument(path).log();

        final String currentBaseVersion;
        String savedVersion = provider.getTeamSettings().get(VERSION_PREFIX + path);
        if (savedVersion != null) {
            currentBaseVersion = savedVersion;
        } else {
            // version wasn't stored - assume latest. TODO Probably need to ask
            // ?
            provider.switchToVersion(path, null);
            currentBaseVersion = provider.getVersion(path);
        }
        final File localFile = new File(projectDir, path);
        final boolean fileChangedLocally;
        {
            File baseRepoFile = null;
            if (prep != null && prep.versionBase.equals(currentBaseVersion)) {
                baseRepoFile = prep.fileBase;
            }
            if (baseRepoFile == null) {
                baseRepoFile = provider.switchToVersion(path, currentBaseVersion);
            }
            if (!localFile.exists()) {
                // there is no local file - just use remote
                LOGGER.atDebug().setMessage("local file '{0}' doesn't exist").addArgument(path).log();
                fileChangedLocally = false;
            } else if (FileUtils.contentEquals(baseRepoFile, localFile)) {
                // versioned file was not changed - no need to commit
                LOGGER.atDebug().setMessage("local file '{0}' wasn't changed").addArgument(path).log();
                fileChangedLocally = false;
            } else {
                LOGGER.atDebug().setMessage("local file '{0}' was changed").addArgument(path).log();
                fileChangedLocally = true;
                rebaser.parseBaseFile(baseRepoFile);
            }
            // baseRepoFile is not valid anymore because we will switch to other
            // version
        }

        File headRepoFile = null;
        String headVersion = null;
        if (prep != null) {
            headVersion = prep.versionHead;
            headRepoFile = prep.fileHead;
        }
        if (headVersion == null) {
            headRepoFile = provider.switchToVersion(path, null);
            headVersion = provider.getVersion(path);
        }
        final boolean fileChangedRemotely;
        {
            if (!localFile.exists()) {
                // there is no local file - just use remote
                if (headRepoFile.exists()) {
                    fileChangedRemotely = true;
                    rebaser.parseHeadFile(headRepoFile);
                } else {
                    // there is no remote file also
                    fileChangedRemotely = false;
                }
            } else if (StringUtils.equals(currentBaseVersion, headVersion)) {
                LOGGER.atDebug().setMessage("remote file '{0}' wasn't changed").addArgument(path).log();
                fileChangedRemotely = false;
            } else {
                // base and head versions are differ - somebody else committed
                // changes
                LOGGER.atDebug().setMessage("remote file '{0}' was changed").addArgument(path).log();
                fileChangedRemotely = true;
                rebaser.parseHeadFile(headRepoFile);
            }
        }

        final File tempOut = new File(projectDir, path + "#based_on_" + headVersion);
        if (tempOut.exists() && !tempOut.delete()) {
            throw new Exception("Unable to delete previous temp file");
        }
        boolean needBackup = false;
        if (fileChangedLocally && fileChangedRemotely) {
            // rebase need only in case file was changed locally AND remotely
            LOGGER.atDebug().setMessage("rebase and save '{0}'").addArgument(path).log();
            needBackup = true;
            rebaser.rebaseAndSave(tempOut);
        } else if (fileChangedLocally && !fileChangedRemotely) {
            // only local changes - just use local file
            LOGGER.atDebug().setMessage("only local changes - just use local file '{0}'").addArgument(path).log();
        } else if (!fileChangedLocally && fileChangedRemotely) {
            // only remote changes - get remote
            LOGGER.atDebug().setMessage("only remote changes - get remote '{0}'").addArgument(path).log();
            needBackup = true;
            if (headRepoFile.exists()) { // otherwise file was removed remotely
                FileUtils.copyFile(headRepoFile, tempOut);
            }
        } else {
            LOGGER.atDebug().setMessage("there are no changes '{0}'").addArgument(path).log();
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
                LOGGER.atDebug().setMessage("create local file {0}").addArgument(localFile).log();
            }
        }

        if (prep != null) {
            prep.needToCommit = fileChangedLocally;
            prep.commitComment = rebaser.getCommentForCommit();
            if (fileChangedLocally) {
                prep.charset = rebaser.getFileCharset(localFile);
            }
            // no need to commit yet - it will make other thread after
            return;
        } else if (fileChangedLocally) {
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
    }

    /**
     * Commit later.
     */
    public static String commitPrepared(Prepared prep, RemoteRepositoryProvider provider,
            String possibleHeadVersion) throws Exception {
        if (!prep.needToCommit) {
            // there was no changes
            return null;
        }
        provider.copyFilesFromProjectToRepos(prep.path, prep.charset);
        String newVersion = provider.commitFileAfterVersion(prep.path, prep.commitComment, prep.versionHead,
                possibleHeadVersion);
        if (newVersion != null) {
            // file was committed good
            provider.getTeamSettings().set(VERSION_PREFIX + prep.path, newVersion);
        }
        return newVersion;
    }

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
     * Info about prepared file.
     */
    public static class Prepared {
        public String path;
        public File fileBase, fileHead;
        public String versionBase, versionHead;
        public boolean needToCommit;
        public String commitComment;
        public String charset;
    }
}
