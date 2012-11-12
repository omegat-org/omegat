/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.core.team;

import java.io.File;
import java.net.SocketException;
import java.util.Collection;

import org.omegat.util.Log;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.ISVNAuthStoreHandler;
import org.tmatesoft.svn.core.internal.wc.ISVNAuthenticationStorageOptions;
import org.tmatesoft.svn.core.internal.wc.ISVNGnomeKeyringPasswordProvider;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * SVN repository connection implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SVNRemoteRepository implements IRemoteRepository {
    File baseDirectory;
    SVNClientManager ourClientManager;
    boolean readOnly;

    public static boolean isSVNDirectory(File localDirectory) {
        File svnDir = new File(localDirectory, ".svn");
        return svnDir.exists() && svnDir.isDirectory();
    }

    public boolean isFilesLockingAllowed() {
        return true;
    }

    /**
     * Open working copy.
     */
    public SVNRemoteRepository(File localDirectory) throws Exception {
        this.baseDirectory = localDirectory;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
        ourClientManager = SVNClientManager.newInstance(options, authManager);
    }

    public boolean isChanged(File file) throws Exception {
        SVNStatus status = ourClientManager.getStatusClient().doStatus(file, false);
        //if file not under version control, then return false.
        if (status == null) return false;
        return status.getContentsStatus() != SVNStatusType.STATUS_NORMAL;
    }

    public void setCredentials(String username, String password, boolean forceSacePlainPassword) {
        ourClientManager.dispose();

        DefaultSVNAuthenticationManager authManager = new DefaultSVNAuthenticationManager(null, true, username,
                password);
        if (forceSacePlainPassword) {
            authManager.setAuthenticationStorageOptions(FORCE_SAVE_PLAIN_PASSWORD);
        }
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager = SVNClientManager.newInstance(options, authManager);
    }

    public void setReadOnly(boolean value) {
        readOnly = value;
    }

    public void updateFullProject() throws SocketException, Exception {
        Log.logInfoRB("SVN_START", "update");
        try {
            ourClientManager.getUpdateClient().doUpdate(baseDirectory, SVNRevision.HEAD, SVNDepth.INFINITY,
                    false, false);
            Log.logInfoRB("SVN_FINISH", "update");
        } catch (SVNAuthenticationException ex) {
            // authentication failed - need to ask username/password
            Log.logWarningRB("SVN_ERROR", "update", ex.getMessage());
            throw new AuthenticationException(ex);
        } catch (SVNException ex) {
            Log.logErrorRB("SVN_ERROR", "update", ex.getMessage());
            checkNetworkException(ex);
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "update", ex.getMessage());
            throw ex;
        }
    }

    public void checkoutFullProject(String repositoryURL) throws Exception {
        Log.logInfoRB("SVN_START", "checkout");

        SVNURL url = SVNURL.parseURIDecoded(repositoryURL);
        try {
            ourClientManager.getUpdateClient().doCheckout(url, baseDirectory, SVNRevision.HEAD,
                    SVNRevision.HEAD, SVNDepth.INFINITY, false);
            Log.logInfoRB("SVN_FINISH", "checkout");
        } catch (SVNAuthenticationException ex) {
            // authentication failed - need to ask username/password
            Log.logWarningRB("TEAM_WRONG_AUTHENTICATION");
            throw new AuthenticationException(ex);
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "checkout", ex.getMessage());
            throw ex;
        }
    }

    public String getBaseRevisionId(File file) throws Exception {
        SVNInfo info = ourClientManager.getWCClient().doInfo(file, SVNRevision.BASE);

        return Long.toString(info.getCommittedRevision().getNumber());
    }

    public void restoreBase(File[] files) throws Exception {
        ourClientManager.getWCClient().doRevert(files, SVNDepth.EMPTY, null);
    }

    public void download(File[] files) throws SocketException, Exception {
        Log.logInfoRB("SVN_START", "download");
        try {
            ourClientManager.getUpdateClient().doUpdate(files, SVNRevision.HEAD, SVNDepth.INFINITY, false,
                    false);
            Log.logInfoRB("SVN_FINISH", "download");
        } catch (SVNException ex) {
            Log.logErrorRB("SVN_ERROR", "download", ex.getMessage());
            checkNetworkException(ex);
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "download", ex.getMessage());
            throw ex;
        }
    }

    public void reset() throws Exception {
        //not tested. Can anyone confirm this code?
        ourClientManager.getWCClient().doRevert(new File[] {baseDirectory}, SVNDepth.INFINITY, (Collection<String>) null);
    }

    public void upload(File file, String commitMessage) throws SocketException, Exception {
        if (readOnly) {
            // read-only - upload disabled
            Log.logInfoRB("SVN_READONLY");
            return;
        }

        Log.logInfoRB("SVN_START", "upload");
        try {
            ourClientManager.getCommitClient().doCommit(new File[] { file }, false, commitMessage, null,
                    null, false, false, SVNDepth.INFINITY);
            Log.logInfoRB("SVN_FINISH", "upload");
        } catch (SVNAuthenticationException ex) {
            // authentication failed - need to ask username/password
            Log.logWarningRB("SVN_ERROR", "update", ex.getMessage());
            throw new AuthenticationException(ex);
        } catch (SVNException ex) {
            if (ex.getErrorMessage().getErrorCode() == SVNErrorCode.FS_CONFLICT) {
                // Somebody else committed changes - it's normal. Will upload on next save.
                Log.logWarningRB("SVN_CONFLICT");
                return;
            } else {
                Log.logErrorRB("SVN_ERROR", "upload", ex.getMessage());
                checkNetworkException(ex);
            }
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "upload", ex.getMessage());
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
        }
    }

    ISVNAuthenticationStorageOptions FORCE_SAVE_PLAIN_PASSWORD = new ISVNAuthenticationStorageOptions() {
        public boolean isNonInteractive() throws SVNException {
            return false;
        }

        public ISVNAuthStoreHandler getAuthStoreHandler() throws SVNException {
            return FORCE_SAVE_PLAIN_PASSWORD_HANDLER;
        }

        public boolean isSSLPassphrasePromptSupported() {
            return false;
        }

        public ISVNGnomeKeyringPasswordProvider getGnomeKeyringPasswordProvider() {
            return null;
        }
    };

    ISVNAuthStoreHandler FORCE_SAVE_PLAIN_PASSWORD_HANDLER = new ISVNAuthStoreHandler() {
        public boolean canStorePlainTextPassphrases(String realm, SVNAuthentication auth) throws SVNException {
            return false;
        }

        public boolean canStorePlainTextPasswords(String realm, SVNAuthentication auth) throws SVNException {
            return true;
        }
    };
}
