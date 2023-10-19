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
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

import gen.core.project.RepositoryDefinition;

/**
 * Authentication manager for SVN. See details about authentication at the
 * http://wiki.svnkit.com/Authentication. Authentication manager created for
 * each repository instance.
 *
 * Only username+password authentication supported. Proxy isn't supported for
 * https:// repositories.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SVNAuthenticationManager implements ISVNAuthenticationManager {
    static final int CONNECT_TIMEOUT = 30 * 1000; // 30 seconds
    static final int READ_TIMEOUT = 60 * 1000; // 60 seconds

    private static final Logger LOGGER = LoggerFactory.getLogger(SVNAuthenticationManager.class);

    private final RepositoryDefinition repoDef;
    private final String predefinedUser;
    private final String predefinedPass;
    private final ProjectTeamSettings teamSettings;

    public SVNAuthenticationManager(String repoUrl, String predefinedUser, String predefinedPass,
            ProjectTeamSettings teamSettings) {
        this(getDef(repoUrl), predefinedUser, predefinedPass, teamSettings);
    }

    public SVNAuthenticationManager(RepositoryDefinition repoDef, String predefinedUser,
            String predefinedPass, ProjectTeamSettings teamSettings) {
        this.repoDef = repoDef;
        this.predefinedUser = predefinedUser;
        this.predefinedPass = predefinedPass;
        this.teamSettings = teamSettings;
    }

    private static RepositoryDefinition getDef(String repoUrl) {
        RepositoryDefinition def = new RepositoryDefinition();
        def.setUrl(repoUrl);
        return def;
    }

    @Override
    public void acknowledgeAuthentication(boolean accepted, String kind, String realm,
            SVNErrorMessage errorMessage, SVNAuthentication authentication) throws SVNException {
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
            // raise exception because of unsupported.
            // when support SSL authentication, we will return
            // SVNSSLAuthentication.newInstance(kind, null, false, url, false);
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.RA_UNKNOWN_AUTH));
        default:
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.AUTHN_NO_PROVIDER));
        }

        Credentials credentials = loadCredentials(url);
        if (credentials.username != null) {
            return getAuthenticatorInstance(kind, url, credentials);
        }

        if (Core.getMainWindow() == null || Core.getMainWindow() instanceof ConsoleWindow) {
            // run on headless.
            return askCUI(kind, url, OStrings.getString("TEAM_USERPASS_FIRST", url.getPath()));
        } else {
            return ask(kind, url, OStrings.getString("TEAM_USERPASS_FIRST", url.getPath()));
        }
    }

    public SVNAuthentication getNextAuthentication(String kind, String realm, SVNURL url)
            throws SVNException {
        if (predefinedUser != null && predefinedPass != null) {
            throw new KnownException("TEAM_PREDEFINED_CREDENTIALS_ERROR");
        }
        if (Core.getMainWindow() == null) {
            // run on headless.
            return askCUI(kind, url, OStrings.getString("TEAM_USERPASS_WRONG", url.getPath()));
        } else {
            return ask(kind, url, OStrings.getString("TEAM_USERPASS_WRONG", url.getPath()));
        }
    }

    @Override
    public ISVNProxyManager getProxyManager(SVNURL url) throws SVNException {
        return NO_PROXY;
    }

    @Override
    public TrustManager getTrustManager(SVNURL url) throws SVNException {
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

    ISVNProxyManager NO_PROXY = new ISVNProxyManager() {
        public String getProxyHost() {
            return null;
        }

        public String getProxyPassword() {
            return null;
        }

        public int getProxyPort() {
            return -1;
        }

        public String getProxyUserName() {
            return null;
        }

        public void acknowledgeProxyContext(boolean accepted, SVNErrorMessage errorMessage) {
        }
    };

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
