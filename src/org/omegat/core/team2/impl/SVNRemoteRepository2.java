/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik, Aaron Madlon-Kay
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

package org.omegat.core.team2.impl;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.wc17.SVNWCContext;
import org.tmatesoft.svn.core.wc.DefaultSVNRepositoryPool;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.util.Log;

import gen.core.project.RepositoryDefinition;

/**
 * SVN repository connection implementation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Martin Fleurke
 */
public class SVNRemoteRepository2 implements IRemoteRepository2 {
    private static final Logger LOGGER = Logger.getLogger(SVNRemoteRepository2.class.getName());

    // System property to indicate backend.
    // {@see https://support.tmatesoft.com/t/replacing-trilead-ssh2-with-apache-sshd/2778/3}
    private static final String SVNKIT_SSH_CLIENT = "svnkit.ssh.client";
    private static final String APACHE = "apache";

    RepositoryDefinition config;
    File baseDirectory;
    SVNClientManager ourClientManager;
    ProjectTeamSettings projectTeamSettings;

    /**
     * Constructor of SVN remote repository.
     */
    public SVNRemoteRepository2() {
        // Specify MINA-SSHD for transport.
        System.setProperty(SVNKIT_SSH_CLIENT, APACHE);
    }

    @Override
    public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) throws Exception {
        config = repo;
        baseDirectory = dir;
        projectTeamSettings = teamSettings;

        String predefinedUser = repo.getOtherAttributes().get(new QName("svnUsername"));
        String predefinedPass = repo.getOtherAttributes().get(new QName("svnPassword"));
        if (predefinedUser == null) {
            predefinedUser = SVNURL.parseURIEncoded(repo.getUrl()).getUserInfo();
        }

        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ISVNAuthenticationManager authManager = new SVNAuthenticationManager(repo, predefinedUser,
                predefinedPass, teamSettings);
        SVNWCContext svnwcContext = new SVNWCContext(options, null);
        SvnOperationFactory svnOperationFactory = new SvnOperationFactory(svnwcContext);
        svnOperationFactory.setAuthenticationManager(authManager);
        svnOperationFactory.setRepositoryPool(new DefaultSVNRepositoryPool(authManager, options));
        ourClientManager = SVNClientManager.newInstance(svnOperationFactory);
        if (baseDirectory.exists()) {
            ourClientManager.getWCClient().doCleanup(baseDirectory);
            ourClientManager.getWCClient().doRevert(new File[] { baseDirectory }, SVNDepth.fromRecurse(true),
                    null);
        }
    }

    @Override
    public String getFileVersion(String file) throws Exception {
        File f = new File(baseDirectory, file);
        if (!f.exists()) {
            return null;
        }
        SVNInfo info = ourClientManager.getWCClient().doInfo(f, SVNRevision.BASE);
        Log.logDebug(LOGGER, "SVN committed revision for file {0} is {1}", file,
                info.getCommittedRevision().getNumber());

        return Long.toString(info.getCommittedRevision().getNumber());
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        Log.logInfoRB("SVN_START", "checkout to " + version);

        SVNURL url = SVNURL.parseURIEncoded(SVNEncodingUtil.autoURIEncode(config.getUrl()));
        SVNRevision toRev;
        if (version != null) {
            toRev = SVNRevision.create(Long.parseLong(version));
        } else {
            toRev = SVNRevision.HEAD;
        }

        try {
            ourClientManager.getUpdateClient().doCheckout(url, baseDirectory, SVNRevision.HEAD, toRev,
                    SVNDepth.INFINITY, false);
            Log.logInfoRB("SVN_FINISH", "checkout");
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "checkout", ex.getMessage());
            checkNetworkException(ex);
            throw ex;
        }
    }

    @Override
    public void addForCommit(String path) throws Exception {
        File f = new File(baseDirectory, path);
        ourClientManager.getWCClient().doAdd(f, true, false, true, SVNDepth.EMPTY, false, true);
    }

    @Override
    public void addForDeletion(String path) throws Exception {
        File f = new File(baseDirectory, path);
        ourClientManager.getWCClient().doDelete(f, true, false);
    }

    @Override
    public File getLocalDirectory() {
        return baseDirectory;
    }

    @Override
    public String[] getRecentlyDeletedFiles() throws Exception {
        final ArrayList<String> deleted = new ArrayList<>();

        final SVNInfo info = ourClientManager.getWCClient().doInfo(baseDirectory, SVNRevision.HEAD);
        SVNRevision currentRevision = info.getRevision();

        String settingKey = "lastDeleteCheckForName" + baseDirectory.getName();
        String sinceRevisionString = projectTeamSettings.get(settingKey);
        SVNRevision sinceRevision;
        if (sinceRevisionString == null) {
            sinceRevision = currentRevision;
        } else {
            sinceRevision = SVNRevision.parse(sinceRevisionString);
        }

        final String repoPath = info.getPath();
        try {
            ourClientManager.getLogClient().doLog(new File[] { baseDirectory }, sinceRevision,
                    currentRevision, false, true,
                    // to get the list of files changed/deleted
                    1000000, new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry en) throws SVNException {
                            if (en.getRevision() == sinceRevision.getNumber()) {
                                return;
                            }
                            Map<String, SVNLogEntryPath> changedPaths = en.getChangedPaths();
                            for (Map.Entry<String, SVNLogEntryPath> entry : changedPaths.entrySet()) {
                                SVNLogEntryPath path = entry.getValue();

                                String filePath = path.getPath();
                                // eg /remotedir/my/file;
                                // repoPath = remotedir. To strip /remotedir/,
                                // add 2 for the slashes.
                                // But if remoteDir is empty, then only
                                // 1 slash to be removed.
                                if ("".equals(repoPath)) {
                                    filePath = filePath.substring(1);
                                } else {
                                    filePath = filePath.substring(repoPath.length() + 2);
                                }
                                filePath = filePath.replace('/', File.separatorChar);
                                // filepath is always using '/', but on windows
                                // we want to return path with backslashes

                                if (path.getType() == 'D') {
                                    deleted.add(filePath);
                                } else if (path.getType() == 'A') {
                                    deleted.remove(filePath);
                                    // added after it has been deleted. Don't
                                    // delete any more!
                                }
                            }
                        }
                    });
        } catch (SVNException e) {
            e.printStackTrace();
        }

        projectTeamSettings.set(settingKey, Long.toString(currentRevision.getNumber()));

        String[] result = new String[deleted.size()];
        return deleted.toArray(result);
    }

    @Override
    public String commit(String[] onVersions, String comment) throws Exception {
        Log.logInfoRB("SVN_START", "commit");
        File[] forCommit = new File[] { baseDirectory };

        try {
            SVNCommitInfo info = ourClientManager.getCommitClient().doCommit(forCommit, false, comment, null,
                    null, false, false, SVNDepth.INFINITY);
            Log.logDebug(LOGGER, "SVN committed into new revision {0}", info.getNewRevision());
            if (info.getNewRevision() < 0) {
                // empty commit - file was not changed
                info = new SVNCommitInfo(Long.parseLong(getFileVersion("")), null, null, null);
            }
            Log.logInfoRB("SVN_FINISH", "commit");
            return Long.toString(info.getNewRevision());
        } catch (SVNException ex) {
            if (Arrays.asList(SVNErrorCode.FS_TXN_OUT_OF_DATE, SVNErrorCode.WC_NOT_UP_TO_DATE,
                    SVNErrorCode.FS_CONFLICT).contains(ex.getErrorMessage().getErrorCode())) {
                // Somebody else committed changes - it's normal. Will upload on
                // next save.
                Log.logWarningRB("SVN_CONFLICT");
                ourClientManager.getWCClient().doRevert(new File[] { baseDirectory },
                        SVNDepth.fromRecurse(true), null);
                return null;
            } else {
                Log.logErrorRB("SVN_ERROR", "commit", ex.getMessage());
                checkNetworkException(ex);
            }
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "commit", ex.getMessage());
            throw ex;
        }
    }

    void checkNetworkException(Exception ex) throws NetworkException {
        if (ex.getCause() instanceof SocketException) {
            throw new NetworkException(ex.getCause());
        }
        if (ex instanceof SVNException) {
            SVNException se = (SVNException) ex;
            if (se.getErrorMessage().getErrorCode().getCategory() == SVNErrorCode.RA_DAV_CATEGORY) {
                throw new NetworkException(se);
            }

            if (Arrays.asList(SVNErrorCode.RA_SVN_IO_ERROR, SVNErrorCode.RA_SVN_CONNECTION_CLOSED)
                    .contains(se.getErrorMessage().getErrorCode())) {
                throw new NetworkException(se);
            }
        }
    }

    /**
     * Determines whether or not the supplied URL represents a valid Subversion
     * repository.
     *
     * <p>
     * Does the equivalent of <code>svn info <i>url</i></code>.
     *
     * @param url
     *            URL of supposed remote repository
     * @return true if repository appears to be valid, false otherwise
     */
    public static boolean isSVNRepository(String url) {
        // Heuristics to save some waiting time
        try {

            ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
            SVNClientManager ourClientManager = SVNClientManager.newInstance(options,
                    (ISVNAuthenticationManager) null);

            ourClientManager.getWCClient().doInfo(SVNURL.parseURIEncoded(SVNEncodingUtil.autoURIEncode(url)),
                    SVNRevision.HEAD, SVNRevision.HEAD);
        } catch (SVNAuthenticationException ex) {
            // TODO: Non-SVN URLs such as below give a false positive with this
            // heuristic:
            // https://twitter.com/amadlonkay/status/699716236372889600
            return true;
        } catch (SVNException ex) {
            return false;
        }
        return true;
    }
}
