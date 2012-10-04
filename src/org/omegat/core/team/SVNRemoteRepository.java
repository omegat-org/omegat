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

import org.omegat.util.Log;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
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

        return status.getContentsStatus() != SVNStatusType.STATUS_NORMAL;
    }

    public void setCredentials(String username, String password) {
        ourClientManager.dispose();

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager = SVNClientManager.newInstance(options, authManager);
    }

    public void setReadOnly(boolean value) {
        readOnly = value;
    }

    public void updateFullProject() throws Exception {
        Log.logInfoRB("SVN_START", "update");
        try {
            ourClientManager.getUpdateClient().doUpdate(baseDirectory, SVNRevision.HEAD, SVNDepth.INFINITY,
                    false, false);
            Log.logInfoRB("SVN_FINISH", "update");
        } catch (SVNAuthenticationException ex) {
            // authentication failed - need to ask username/password
            Log.logWarningRB("SVN_ERROR", "update", ex.getMessage());
            throw new AuthenticationException(ex);
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

    public void restoreBase(File file) throws Exception {
        ourClientManager.getWCClient().doRevert(new File[] { file }, SVNDepth.EMPTY, null);
    }

    public void download(File file) throws Exception {
        Log.logInfoRB("SVN_START", "download");
        try {
            ourClientManager.getUpdateClient().doUpdate(file, SVNRevision.HEAD, SVNDepth.INFINITY, false,
                    false);
            Log.logInfoRB("SVN_FINISH", "download");
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "download", ex.getMessage());
        }
    }

    public void upload(File file, String commitMessage) throws Exception {
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
            } else {
                Log.logErrorRB("SVN_ERROR", "upload", ex.getMessage());
            }
            throw ex;
        } catch (Exception ex) {
            Log.logErrorRB("SVN_ERROR", "upload", ex.getMessage());
            throw ex;
        }
    }
}
