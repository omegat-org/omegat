/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
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

import java.io.Console;

import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.auth.ISVNProxyManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
import org.tmatesoft.svn.core.io.SVNRepository;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.core.team2.gui.UserPassDialog;
import org.omegat.core.team2.impl.TeamUtils.Credentials;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Authentication manager for SVN. See details about authentication at the
 * <a href="https://wiki.svnkit.com/Authentication">SVNKit document</a>.
 * Authentication manager will be created for each repository instance.
 * <p>
 * Only username+password authentication supported. Proxy isn't supported for
 * "https://" repositories.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SVNAuthenticationManager implements ISVNAuthenticationManager {
    static final int CONNECT_TIMEOUT = 30 * 1000; // 30 seconds
    static final int READ_TIMEOUT = 60 * 1000; // 60 seconds

    private static final Logger LOGGER = LoggerFactory.getLogger(SVNAuthenticationManager.class);

    private final String predefinedUser;
    private final String predefinedPass;

    @SuppressWarnings("unused")
    public SVNAuthenticationManager(String repoUrl, String predefinedUser, String predefinedPass,
            ProjectTeamSettings teamSettings) {
        this(predefinedUser, predefinedPass);
    }

    public SVNAuthenticationManager(String predefinedUser, String predefinedPass) {
        this.predefinedUser = predefinedUser;
        this.predefinedPass = predefinedPass;
    }

    @Override
    public void acknowledgeAuthentication(boolean accepted, String kind, String realm,
            SVNErrorMessage errorMessage, SVNAuthentication authentication) {
        if (!accepted) {
            LOGGER.atDebug().log("SVN authentication error: {}", errorMessage);
        }
    }

    @Override
    public void acknowledgeTrustManager(TrustManager manager) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConnectTimeout(SVNRepository repository) {
        return CONNECT_TIMEOUT;
    }

    @Override
    public int getReadTimeout(SVNRepository repository) {
        return READ_TIMEOUT;
    }

    protected SVNAuthentication ask(String kind, SVNURL url, String message) throws SVNException {
        UserPassDialog userPassDialog = new UserPassDialog(Core.getMainWindow().getApplicationFrame());
        userPassDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
        userPassDialog.setDescription(message);
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() != UserPassDialog.RET_OK) {
            return null;
        }
        userPassDialog.setPerHostCheckBoxText(OStrings.getString("TEAM_CREDENTIALS_PER_HOST", url.getHost()));
        Credentials credentials = new Credentials();
        credentials.username = userPassDialog.getUsername();
        credentials.password = userPassDialog.getPassword();
        credentials.perHost = userPassDialog.isPerHost();
        saveCredentials(url, credentials);
        return getAuthenticatorInstance(kind, url, credentials);
    }

    protected SVNAuthentication askCUI(String kind, SVNURL url, String message) throws SVNException {
        Console console = System.console();
        if (console == null) {
            Log.logInfoRB("SVN_NO_CONSOLE");
            if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
                // user auth shouldn't be null.
                return SVNUserNameAuthentication.newInstance("", false, url, false);
            }
            return null;
        }
        Credentials credentials = new Credentials();
        if (predefinedUser != null) {
            credentials.username = predefinedUser;
        } else {
            credentials.username = console.readLine(message);
        }
        credentials.password = new String(console.readPassword(message));
        credentials.perHost = TeamUtils.askYesNoCui(OStrings.getString("TEAM_CREDENTIALS_PER_HOST", url.getHost()),
                false);
        saveCredentials(url, credentials);
        return getAuthenticatorInstance(kind, url, credentials);
    }

    @Override
    public SVNAuthentication getFirstAuthentication(String kind, String realm, SVNURL url)
            throws SVNException {
        // Check supported kind and return when there are predefined credentials
        switch (kind) {
        case USERNAME:
            if (predefinedUser != null) {
                return SVNUserNameAuthentication.newInstance(predefinedUser, false, url, false);
            }
            break;
        case PASSWORD:
            if (predefinedUser != null && predefinedPass != null) {
                return SVNPasswordAuthentication.newInstance(predefinedUser, predefinedPass.toCharArray(),
                        false, url, false);
            }
            break;
        case SSH:
            if (predefinedUser != null && predefinedPass != null) {
                return SVNSSHAuthentication.newInstance(predefinedUser, predefinedPass.toCharArray(), -1,
                        false, url, false);
            }
            break;
        case SSL:
            // Attempting to use SSL authentication will intentionally raise an exception.
            // because it is currently unsupported in this implementation.
            //
            // In the future, when SSL authentication is supported, the following method
            // could be used to create an appropriate instance like as:
            // "SVNSSLAuthentication.newInstance(kind, null, false, url, false);"
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.RA_UNKNOWN_AUTH));
        default:
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.AUTHN_NO_PROVIDER));
        }

        Credentials credentials = loadCredentials(url);
        if (credentials.username != null) {
            return getAuthenticatorInstance(kind, url, credentials);
        }

        if (StaticUIUtils.isGUI()) {
            return ask(kind, url, OStrings.getString("TEAM_USERPASS_FIRST", url.getPath()));
        } else {
            // run on headless.
            return askCUI(kind, url, OStrings.getString("TEAM_USERPASS_FIRST", url.getPath()));
        }
    }

    public SVNAuthentication getNextAuthentication(String kind, String realm, SVNURL url)
            throws SVNException {
        if (predefinedUser != null && predefinedPass != null) {
            throw new KnownException("TEAM_PREDEFINED_CREDENTIALS_ERROR");
        }
        if (StaticUIUtils.isGUI()) {
            return ask(kind, url, OStrings.getString("TEAM_USERPASS_WRONG", url.getPath()));
        } else {
            // run on headless.
            return askCUI(kind, url, OStrings.getString("TEAM_USERPASS_WRONG", url.getPath()));
        }
    }

    // Updated field with extracted class
    private ISVNProxyManager noProxyManager;

    @Override
    public ISVNProxyManager getProxyManager(SVNURL url) {
        if (noProxyManager == null) {
            noProxyManager = new NoProxyManager();
        }
        return noProxyManager;
    }

    @Override
    public TrustManager getTrustManager(SVNURL url) {
        return null;
    }

    @Override
    public boolean isAuthenticationForced() {
        return false;
    }

    @Override
    public void setAuthenticationProvider(ISVNAuthenticationProvider provider) {
        throw new UnsupportedOperationException();
    }

    static class NoProxyManager implements ISVNProxyManager {

        @Override
        public String getProxyHost() {
            return null;
        }

        @Override
        public String getProxyPassword() {
            return null;
        }

        @Override
        public int getProxyPort() {
            return -1;
        }

        @Override
        public String getProxyUserName() {
            return null;
        }

        @Override
        public void acknowledgeProxyContext(boolean accepted, SVNErrorMessage errorMessage) {
            // do nothing
        }
    }

    private SVNAuthentication getAuthenticatorInstance(String kind, SVNURL url, Credentials credentials)
            throws SVNException {
        if (ISVNAuthenticationManager.PASSWORD.equals(kind)) {
            return SVNPasswordAuthentication.newInstance(credentials.username,
                    credentials.password.toCharArray(), false, url, false);
        } else if (ISVNAuthenticationManager.SSH.equals(kind)) {
            return SVNSSHAuthentication.newInstance(credentials.username, credentials.password.toCharArray(),
                    -1, false, url, false);
        } else if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
            return SVNUserNameAuthentication.newInstance(credentials.username, false, url, false);
        } else {
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.RA_UNKNOWN_AUTH));
        }
    }

    private Credentials loadCredentials(SVNURL url) {
        return TeamUtils.loadCredentials(url.toString(), url.getProtocol(), url.getHost(), url.getPath(),
                url.getPort());
    }

    private void saveCredentials(SVNURL url, Credentials credentials) {
        TeamUtils.saveCredentials(url.toString(), url.getProtocol(), url.getHost(), url.getPath(),
                url.getPort(), credentials);
    }

}
