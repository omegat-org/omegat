/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik, Aaron Madlon-Kay
               2015 Hiroshi Miura, Aaron Madlon-Kay
               2022 Thomas Cordonnier
               2021-2023 Hiroshi Miura
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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.team2.TeamSettings;
import org.omegat.core.team2.gui.PassphraseDialog;
import org.omegat.core.team2.gui.UserPassDialog;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Git repository credentials provider. One credential provider created for all
 * git instances.
 * <p>
 * Git supports these protocols:
 * <ul>
 * <li>file://
 * <li>ssh://
 * <li>git://
 * <li>https://
 * </ul>
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 * @see <a href=
 *      "https://www.codeaffine.com/2014/12/09/jgit-authentication/">JGit
 *      Authentication Explained</a>
 * @see <a href=
 *      "https://www.matez.de/index.php/2020/06/22/the-future-of-jsch-without-ssh-rsa/">matez
 *      blog</a>
 * @see <a href=
 *      "https://github.com/apache/mina-sshd/blob/master/docs/git.md">MINA-SSHD
 *      Git support</a>
 */

public class GITCredentialsProvider extends CredentialsProvider {

    private static final String KEY_USERNAME_SUFFIX = "username";
    private static final String KEY_PASSWORD_SUFFIX = "password";
    private static final String KEY_FINGERPRINT_SUFFIX = "fingerprint";
    private static final Pattern[] fingerPrintRegex = new Pattern[] {
            Pattern.compile("The authenticity of host '" + /* host */ ".*" + "' can't be established\\.\\n" +
            /* key_type */ "(RSA|DSA|ECDSA|EDDSA)" + " key fingerprint is " +
            /* key fprint */ "(?<fingerprint>([0-9a-f]{2}:){15}[0-9a-f]{2})" + "\\.\\n"
                    + "Are you sure you want to continue connecting\\?"),
            Pattern.compile("The authenticity of host '" + /* host */ ".*" + "' can't be established\\.\\n" +
            /* key_type */ "(RSA|DSA|ECDSA|EDDSA)" + " key fingerprint is " +
            /* key fprint */"SHA256:(?<fingerprint>[0-9a-zA-Z/+]+)" + "\\.\\n"
                    + "Are you sure you want to continue connecting\\?"),
            Pattern.compile("The authenticity of host '.*' cannot be established\\.\\n"
                    + "The EC key's fingerprints are:\\n"
                    + "MD5:([0-9a-f]{2}:){15}[0-9a-f]{2}\\nSHA256:(?<fingerprint>[0-9a-zA-Z/+]+)\\n"
                    + "Accept and store this key, and continue connecting\\?") };

    private static final Pattern[] PASSPHRASE_REGEX = new Pattern[] {
            Pattern.compile("Key '" + /* key file path */ ".*" + "'"
                    + " is encrypted\\. Enter the passphrase to decrypt it\\.\\n?"),
            Pattern.compile("Encrypted key " + /* key file path */ "'.*'"
                    + " could not be decrypted\\. Enter the passphrase again\\.\\n?") };

    private static final String PASSWORD_PROMPT = "Password: ";
    private static final int MAX_RETRY = 5;

    /** Predefined in the omegat.project file. */
    private final Map<String, String> predefined = Collections.synchronizedMap(new HashMap<>());

    /**
     * Installation of credentials provider.
     * <p>
     * This static method installs GITCredentialsProvider object as default.
     */
    public static void install() {
        final GITCredentialsProvider c = new GITCredentialsProvider();
        CredentialsProvider.setDefault(c);
    }

    /**
     * Set predefined git+ssh or https credentials.
     * <p>
     * these credentials are automatically passed to ssh/https connection layer.
     * 
     * @param url
     *            target url.
     * @param predefinedUser
     *            predefined username.
     * @param predefinedPass
     *            predefined password.
     * @param predefinedFingerprint
     *            predefined fingerprint of host.
     */
    public void setPredefinedCredentials(String url, String predefinedUser, String predefinedPass,
            String predefinedFingerprint) {
        predefined.put("user." + url, predefinedUser);
        predefined.put("pass." + url, predefinedPass);
        predefined.put("fingerprint." + url, predefinedFingerprint);
    }

    private Credentials loadCredentials(URIish uri) {
        Credentials credentials = new Credentials();
        // we use "schema://server:port" or "/path/to/.ssh/id_rsa"
        // and backward compatible "schema://server:port/path"
        // check following order
        String url = uri.toString();
        credentials.username = TeamSettings.get(url + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = TeamUtils.decodePassword(TeamSettings.get(url + "!" + KEY_PASSWORD_SUFFIX));
        if (credentials.password == null) {
            if (uri.getScheme() == null) {
                url = uri.getPath();
            } else {
                url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
            }
            credentials.username = TeamSettings.get(url + "!" + KEY_USERNAME_SUFFIX);
            credentials.password = TeamUtils
                    .decodePassword(TeamSettings.get(url + "!" + KEY_PASSWORD_SUFFIX));
        }
        return credentials;
    }

    private void saveCredentials(URIish uri, Credentials credentials) {
        String url;
        if (uri.getScheme() == null) {
            url = uri.getPath();
        } else if (uri.getScheme() != null && uri.getHost() != null) {
            url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
        } else {
            url = uri.getRawPath();
        }
        try {
            if (!StringUtil.isEmpty(credentials.username)) {
                TeamSettings.set(url + "!" + KEY_USERNAME_SUFFIX, credentials.username);
            }
            TeamSettings.set(url + "!" + KEY_PASSWORD_SUFFIX, TeamUtils.encodePassword(credentials.password));
        } catch (Exception e) {
            Log.logErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS");
        }
    }

    private String loadFingerprint(URIish uri) {
        String url = uri.toString();
        return TeamSettings.get(url + "!" + KEY_FINGERPRINT_SUFFIX);
    }

    private void saveFingerprint(URIish uri, String fingerprint) {
        String url = uri.toString();
        try {
            TeamSettings.set(url + "!" + KEY_FINGERPRINT_SUFFIX, fingerprint);
        } catch (Exception e) {
            Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
        }
    }

    /**
     * Ask for the credential items to be populated.
     *
     * @param uri
     *            the URI of the remote resource that needs authentication.
     * @param items
     *            the items the application requires to complete authentication.
     * @return {@code true} if the request was successful and values were
     *         supplied; {@code false} if the user canceled the request and did
     *         not supply all requested values.
     * @throws org.eclipse.jgit.errors.UnsupportedCredentialItem
     *             if one of the items supplied is not supported.
     */
    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        // get predefined if exist
        String url = uri.toString();
        String predefinedUser = predefined.get("user." + url);
        String predefinedPass = predefined.get("pass." + url);
        String predefinedFingerprint = predefined.get("fingerprint." + url);

        // get saved
        Credentials credentials = loadCredentials(uri);
        StringBuilder sb = new StringBuilder();
        boolean askKeyPassphrase = (uri.getScheme() == null);
        // theoretically, username can be unknown, but in practice it is always
        // set, so not requested.
        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.Username) {
                if (predefinedUser != null && predefinedPass != null) {
                    ((CredentialItem.Username) item).setValue(predefinedUser);
                    continue;
                }
                if (credentials.username == null) {
                    credentials = askCredentials(uri, credentials, false, null);
                }
                ((CredentialItem.Username) item).setValue(credentials.username);
                continue;
            } else if (item instanceof CredentialItem.Password) {
                if (predefinedUser != null && predefinedPass != null) {
                    ((CredentialItem.Password) item).setValue(predefinedPass.toCharArray());
                    continue;
                }
                if (credentials.password == null) {
                    credentials = askCredentials(uri, credentials, askKeyPassphrase, sb.toString());
                    sb = new StringBuilder();
                }
                ((CredentialItem.Password) item).setValue(credentials.password.toCharArray());
                continue;
            } else if (item instanceof CredentialItem.StringType) {
                if (!item.getPromptText().equals(PASSWORD_PROMPT) || !isPassphraseQuery(item.getPromptText())) {
                    Log.log("Git: Ignore credentials query: " + item.getPromptText());
                    continue;
                }
                if (predefinedUser != null && predefinedPass != null) {
                    ((CredentialItem.StringType) item).setValue(predefinedPass);
                    continue;
                }
                if (credentials.password == null) {
                    credentials = askCredentials(uri, credentials, askKeyPassphrase, null);
                }
                ((CredentialItem.StringType) item).setValue(credentials.password);
                continue;
            } else if (item instanceof CredentialItem.YesNoType) {
                // @see constant array fingerPrintRegex
                // e.g.: The authenticity of host 'mygitserver' can't be
                // established.
                // RSA key fingerprint is
                // e2:d3:84:d5:86:e7:68:69:a0:aa:a6:ad:a3:a0:ab:a2.
                // Are you sure you want to continue connecting?
                String storedFingerprint = loadFingerprint(uri);
                // exhausted messages to promptText and reset sb
                String promptText = sb.append(item.getPromptText()).toString();
                sb = new StringBuilder();
                String promptedFingerprint = extractFingerprint(promptText);
                if (promptedFingerprint == null) {
                    throw new UnsupportedCredentialItem(uri,
                            String.format("Unknown pattern to ask acceptance of host key fingerprint "
                                    + "\n%s", promptText));
                }
                if (predefinedFingerprint != null) {
                    ((CredentialItem.YesNoType) item)
                            .setValue(promptedFingerprint.equals(predefinedFingerprint));
                    continue;
                }
                if (promptedFingerprint.equals(storedFingerprint)) {
                    ((CredentialItem.YesNoType) item).setValue(true);
                    continue;
                }
                askYesNo(item, promptText, uri, promptedFingerprint);
                continue;
            } else if (item instanceof CredentialItem.InformationalMessage) {
                sb.append(item.getPromptText()).append("\n");
                continue;
            }
            throw new UnsupportedCredentialItem(uri, item.getClass().getName() + ":" + item.getPromptText());
        }
        if (sb.length() > 0) {
            Log.logInfoRB("GIT_CREDENTIAL_MESSAGE", sb.toString());
        }
        return true;
    }

    /**
     * Check if the provider is interactive with the end-user.
     * <p>
     * GITCredentialsProvider is always interactive.
     * 
     * @return true
     */
    @Override
    public boolean isInteractive() {
        return true;
    }

    /**
     * Check if the credentials are supported.
     * 
     * @param items
     *            credential items
     * @return true when asked username and/or password. Otherwise, false.
     */
    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialItem i : items) {
            if (!(i instanceof CredentialItem.Username || i instanceof CredentialItem.Password)) {
                return false;
            }
        }
        return true;
    }

    private boolean isGUI() {
        return Core.getMainWindow() != null;
    }

    private void askYesNoGUI(CredentialItem item, String promptText, URIish uri, String promptedFingerprint) {
        IMainWindow mw = Core.getMainWindow();
        int choice = mw.showConfirmDialog(promptText, null, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            ((CredentialItem.YesNoType) item).setValue(true);
            saveFingerprint(uri, promptedFingerprint);
        } else {
            ((CredentialItem.YesNoType) item).setValue(false);
        }
    }

    private void askYesNoCUI(CredentialItem item, String promptText, URIish uri, String promptedFingerprint) {
        Console console = System.console();
        if (console != null) {
            try (PrintWriter printWriter = console.writer()) {
                boolean succeeded = false;
                for (int i = 0; i < MAX_RETRY; i++) {
                    printWriter.print(promptText);
                    String answer = console.readLine("([y]es or [n]o): ");
                    if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                        ((CredentialItem.YesNoType) item).setValue(true);
                        saveFingerprint(uri, promptedFingerprint);
                        succeeded = true;
                        break;
                    } else if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) {
                        ((CredentialItem.YesNoType) item).setValue(false);
                        succeeded = true;
                        break;
                    }
                    printWriter.println(OStrings.getString("TEAM_YESNO_AGAIN"));
                }
                if (!succeeded) {
                    printWriter.println(OStrings.getString("TEAM_YESNO_ABORT"));
                    ((CredentialItem.YesNoType) item).setValue(false);
                }
            }
        } else {
            // When there is no console, aborting...
            ((CredentialItem.YesNoType) item).setValue(false);
        }
    }

    private void askYesNo(CredentialItem item, String promptText, URIish uri, String promptedFingerprint) {
        if (isGUI()) {
            askYesNoGUI(item, promptText, uri, promptedFingerprint);
        } else {
            askYesNoCUI(item, promptText, uri, promptedFingerprint);
        }
    }

    /**
     * GUI component to ask credentials.
     * <p>
     * If username is already available in uri, then we will not be asked for a
     * username, and keep it.
     *
     * @param uri
     *            the repository URI
     * @param credentials
     *            credentials holder
     * @param passwordOnly
     *            true when want to ask only password, otherwise false.
     * @return result as a credential object.
     */
    private Credentials askCredentialsGUI(URIish uri, Credentials credentials, boolean passwordOnly,
            String msg) {
        IMainWindow mw = Core.getMainWindow();
        if (passwordOnly) {
            PassphraseDialog passphraseDialog = new PassphraseDialog(mw.getApplicationFrame());
            passphraseDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
            if (uri.getScheme() == null) {
                // asked passphrase
                passphraseDialog.setTitleDesc(OStrings.getString(
                        credentials.password == null ? "TEAM_PASSPHRASE_FIRST" : "TEAM_PASSPHRASE_WRONG",
                        uri.toString()));
            } else {
                // asked password
                passphraseDialog.setTitleDesc(OStrings.getString(
                        credentials.password == null ? "TEAM_PASS_FIRST" : "TEAM_PASS_WRONG",
                        uri.toString()));
            }
            passphraseDialog.setDescription(msg);
            passphraseDialog.setVisible(true);
            if (passphraseDialog.getReturnStatus()) {
                credentials.password = passphraseDialog.getPassword();
                return credentials;
            }
        } else {
            UserPassDialog userPassDialog = new UserPassDialog(mw.getApplicationFrame());
            userPassDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
            userPassDialog.descriptionTextArea.setText(OStrings.getString(
                    credentials.username == null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG",
                    uri.getHumanishName()));
            if (uri.getUser() != null && !"".equals(uri.getUser())) {
                userPassDialog.setUsername(uri.getUser());
                userPassDialog.enableUsernameField(false);
            }
            if (credentials.username != null) {
                userPassDialog.setUsername(credentials.username);
            }
            userPassDialog.setVisible(true);
            if (userPassDialog.getReturnStatus() == UserPassDialog.RET_OK) {
                credentials.username = userPassDialog.getUsername();
                credentials.password = userPassDialog.getPassword();
                return credentials;
            }
        }
        return null;
    }

    private Credentials askCredentialsCUI(URIish uri, Credentials credentials, boolean passwordOnly,
            String msg) {
        Console console = System.console();
        if (console != null) {
            if (msg != null) {
                console.printf(msg);
            }
            if (uri.getUser() != null && !"".equals(uri.getUser())) {
                credentials.username = uri.getUser();
            } else {
                if (!passwordOnly) {
                    credentials.username = console
                            .readLine(OStrings.getString("TEAM_USER_FIRST", uri.getHumanishName()));
                }
            }
            char[] pass = console.readPassword(OStrings.getString("TEAM_PASS_FIRST", uri.getHumanishName()));
            credentials.password = Arrays.toString(pass);
            return credentials;
        }
        Log.log("No console found.");
        return null;
    }

    private Credentials askCredentials(URIish uri, Credentials credentials, boolean passwordOnly,
            String msg) {
        Credentials result;
        if (isGUI()) {
            result = askCredentialsGUI(uri, credentials, passwordOnly, msg);
        } else {
            result = askCredentialsCUI(uri, credentials, passwordOnly, msg);
        }
        if (result == null) {
            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
        }
        saveCredentials(uri, result);
        return result;
    }

    /**
     * Reset connection.
     * <p>
     * It is called after 5 authorization failures. The transport gives up after
     * 3 resets.
     *
     * @param uri
     *            target URI.
     */
    @Override
    public void reset(URIish uri) {
        String url = uri.toString();
        String predefinedUser = predefined.get("user." + url);
        String predefinedPass = predefined.get("pass." + url);
        if (predefinedUser != null && predefinedPass != null) {
            throw new KnownException("TEAM_PREDEFINED_CREDENTIALS_ERROR");
        }

        Credentials credentials = loadCredentials(uri);
        credentials.username = null;
        credentials.password = null;
        saveCredentials(uri, credentials);
    }

    /**
     * Extract fingerprint from a message.
     * 
     * @param text
     *            message text.
     * @return fingerprint hash string.
     */
    protected static String extractFingerprint(String text) {
        Matcher fingerprintMatcher;
        for (Pattern p : fingerPrintRegex) {
            fingerprintMatcher = p.matcher(text);
            if (fingerprintMatcher.find()) {
                int start = fingerprintMatcher.start("fingerprint");
                int end = fingerprintMatcher.end("fingerprint");
                return text.substring(start, end);
            }
        }
        return null;
    }

    private boolean isPassphraseQuery(String promptText) {
        Matcher passphraseMatcher;
        for (Pattern p : PASSPHRASE_REGEX) {
            passphraseMatcher = p.matcher(promptText);
            if (passphraseMatcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * POJO to hold credentials.
     */
    public static class Credentials {
        public String username = null;
        public String password = null;
    }
}
