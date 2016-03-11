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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.gui.dialogs.TeamUserPassDialog;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNCommitInfo;
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
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * SVN repository connection implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class SVNRemoteRepository2 implements IRemoteRepository2 {
    private static final Logger LOGGER = Logger.getLogger(SVNRemoteRepository2.class.getName());

    RepositoryDefinition config;
    File baseDirectory;
    SVNClientManager ourClientManager;
    List<File> filesForCommit = new ArrayList<File>();
    int credentialAskCount;

    @Override
    public void init(RepositoryDefinition repo, File dir) throws Exception {
        config = repo;
        baseDirectory = dir;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
        ourClientManager = SVNClientManager.newInstance(options, authManager);
        ourClientManager.getWCClient().doCleanup(baseDirectory);
    }

    public void setCredentials(Credentials credentials) {
        ourClientManager.dispose();

        DefaultSVNAuthenticationManager authManager = new DefaultSVNAuthenticationManager(null, true,
                credentials.username, new String(credentials.password));
        if (credentials.saveAsPlainText) {
            authManager.setAuthenticationStorageOptions(FORCE_SAVE_PLAIN_PASSWORD);
        }
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager = SVNClientManager.newInstance(options, authManager);
    }

    @Override
    public String getFileVersion(String file) throws Exception {
        SVNInfo info = ourClientManager.getWCClient().doInfo(new File(baseDirectory, file), SVNRevision.BASE);
        Log.logDebug(LOGGER, "SVN committed revision for file {0} is {1}", file, info.getCommittedRevision()
                .getNumber());

        return Long.toString(info.getCommittedRevision().getNumber());
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        Log.logInfoRB("SVN_START", "checkout");
        filesForCommit.clear();

        SVNURL url = SVNURL.parseURIDecoded(config.getUrl());
        SVNRevision toRev;
        if (version != null) {
            toRev = SVNRevision.create(Long.parseLong(version));
        } else {
            toRev = SVNRevision.HEAD;
        }

        while (true) {
            try {
                Log.logInfoRB("SVN_FINISH", "checkout");
                long rev = ourClientManager.getUpdateClient().doCheckout(url, baseDirectory,
                        SVNRevision.HEAD, toRev, SVNDepth.INFINITY, false);
                break;
            } catch (SVNAuthenticationException ex) {
                // authentication failed - need to ask username/password
                Log.logWarningRB("SVN_ERROR", "checkout", ex.getMessage());
                if (askCredentials()) {
                    // iterate again
                    continue;
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                Log.logErrorRB("SVN_ERROR", "checkout", ex.getMessage());
                throw ex;
            }
        }
    }

    @Override
    public void addForCommit(String path) throws Exception {
        filesForCommit.add(new File(baseDirectory, path));
    }

    @Override
    public String commit(String version, String comment) throws Exception {
        Log.logInfoRB("SVN_START", "commit");
        File[] forCommit = filesForCommit.toArray(new File[filesForCommit.size()]);
        filesForCommit.clear();

        while (true) {
            try {
                SVNCommitInfo info = ourClientManager.getCommitClient().doCommit(forCommit, false, comment,
                        null, null, false, false, SVNDepth.INFINITY);
                Log.logDebug(LOGGER, "SVN committed into new revision {0}", info.getNewRevision());
                if (info.getNewRevision() < 0) {
                    throw new Exception("SVN commit returns -1");
                }
                Log.logInfoRB("SVN_FINISH", "commit");
                return Long.toString(info.getNewRevision());
            } catch (SVNAuthenticationException ex) {
                // authentication failed - need to ask username/password
                Log.logWarningRB("SVN_ERROR", "commit", ex.getMessage());
                if (askCredentials()) {
                    // iterate again
                    continue;
                } else {
                    throw ex;
                }
            } catch (SVNException ex) {
                if (ex.getErrorMessage().getErrorCode() == SVNErrorCode.FS_TXN_OUT_OF_DATE) {
                    // Somebody else committed changes - it's normal. Will upload on next save.
                    Log.logWarningRB("SVN_CONFLICT");
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

    /**
     * Show dialog for ask credentials.
     * 
     * Probably, it will be better to implement own authentication manager. See details about authentication at
     * the http://wiki.svnkit.com/Authentication.
     * 
     * @return
     * @throws Exception
     */
    boolean askCredentials() throws Exception {
        credentialAskCount++;
        final Credentials result = new Credentials();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                String message = OStrings.getString(credentialAskCount < 2 ? "TEAM_USERPASS_FIRST"
                        : "TEAM_USERPASS_WRONG");

                TeamUserPassDialog userPassDialog = new TeamUserPassDialog(Core.getMainWindow()
                        .getApplicationFrame());
                DockingUI.displayCentered(userPassDialog);
                userPassDialog.descriptionTextArea.setText(message);
                userPassDialog.setVisible(true);
                if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
                    result.username = userPassDialog.userText.getText();
                    char[] arrayPassword = userPassDialog.passwordField.getPassword();
                    result.password = Arrays.copyOf(arrayPassword, arrayPassword.length);
                    Arrays.fill(arrayPassword, '0');
                    result.saveAsPlainText = userPassDialog.cbForceSavePlainPassword.isSelected();
                    result.readOnly = userPassDialog.cbReadOnly.isSelected();
                } else {
                    result.username = null;
                }
            }
        });
        if (result.username != null) {
            setCredentials(result);
            return true;
        } else {
            return false;
        }
    }

    static ISVNAuthenticationStorageOptions FORCE_SAVE_PLAIN_PASSWORD = new ISVNAuthenticationStorageOptions() {
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

    static ISVNAuthStoreHandler FORCE_SAVE_PLAIN_PASSWORD_HANDLER = new ISVNAuthStoreHandler() {
        public boolean canStorePlainTextPassphrases(String realm, SVNAuthentication auth) throws SVNException {
            return false;
        }

        public boolean canStorePlainTextPasswords(String realm, SVNAuthentication auth) throws SVNException {
            return true;
        }
    };
}
