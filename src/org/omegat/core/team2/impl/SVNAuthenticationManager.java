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
import java.util.Arrays;
import java.util.logging.Logger;

import javax.net.ssl.TrustManager;

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
import org.omegat.core.team2.TeamSettings;
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
    static final String KEY_USERNAME_SUFFIX = "username";
    static final String KEY_PASSWORD_SUFFIX = "password";

    private static final Logger LOGGER = Logger.getLogger(SVNAuthenticationManager.class.getName());

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

    private static RepositoryDefinition getDef(String repoUrl){
        RepositoryDefinition def = new RepositoryDefinition();
        def.setUrl(repoUrl);
        return def;
    }

    @Override
    public void acknowledgeAuthentication(boolean accepted, String kind, String realm,
            SVNErrorMessage errorMessage, SVNAuthentication authentication) throws SVNException {
        if (!accepted) {
            Log.logDebug(LOGGER, "SVN authentication error: {0}", errorMessage);
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
        SVNUserPassDialog userPassDialog = new SVNUserPassDialog(Core.getMainWindow().getApplicationFrame());
        userPassDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
        userPassDialog.descriptionTextArea.setText(message);
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() != SVNUserPassDialog.RET_OK) {
            return null;
        }

        String user = userPassDialog.userText.getText();
        char[] pass = userPassDialog.passwordField.getPassword();
        saveCredentials(url, user, Arrays.toString(pass));
        return getAuthenticatorInstance(kind, url, user, pass);
    }

    protected SVNAuthentication askCUI(String kind, SVNURL url, String message) throws SVNException {
        Console console = System.console();
        if (console == null) {
            Log.log("No console found.");
            if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
                // user auth shouldn't be null.
                return SVNUserNameAuthentication.newInstance("", false, url, false);
            }
            return null;
        }
        String user;
        if (predefinedUser != null) {
            user = predefinedUser;
        } else {
            user = console.readLine(message);
        }
        char[] pass = console.readPassword(message);
        saveCredentials(url, user, Arrays.toString(pass));
        return getAuthenticatorInstance(kind, url, user, pass);
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
            return getAuthenticatorInstance(kind, url, credentials.username, credentials.password.toCharArray());
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
    };

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

    private SVNAuthentication getAuthenticatorInstance(String kind, SVNURL url, String user, char[] pass)
            throws SVNException {
        if (ISVNAuthenticationManager.PASSWORD.equals(kind)) {
            return SVNPasswordAuthentication.newInstance(user, pass, false, url, false);
        } else if (ISVNAuthenticationManager.SSH.equals(kind)) {
            return SVNSSHAuthentication.newInstance(user, pass, -1, false, url, false);
        } else if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
            return SVNUserNameAuthentication.newInstance(user, false, url, false);
        } else {
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.RA_UNKNOWN_AUTH));
        }
    }

    private Credentials loadCredentials(SVNURL url) {
        Credentials credentials = new Credentials();
        // check stored credential with a backward compatible key.
        credentials.username = TeamSettings.get(repoDef.getUrl() + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = TeamUtils
                .decodePassword(TeamSettings.get(repoDef.getUrl() + "!" + KEY_PASSWORD_SUFFIX));
        if (credentials.username != null) {
            return credentials;
        }

        String saveUri = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        credentials.username = TeamSettings.get(saveUri + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = TeamUtils
                .decodePassword(TeamSettings.get(saveUri + "!" + KEY_PASSWORD_SUFFIX));
        return credentials;
    }

    private void saveCredentials(SVNURL url, String user, String password) {
        String saveUri = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        TeamSettings.set(saveUri + "!" + KEY_USERNAME_SUFFIX, user);
        TeamSettings.set(saveUri + "!" + KEY_PASSWORD_SUFFIX, TeamUtils.encodePassword(password));
    }

    /**
     * POJO to hold credentials.
     */
    public static class Credentials {
        public String username = null;
        public String password = null;
    }
}
