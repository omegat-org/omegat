/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik, Aaron Madlon-Kay
               2015 Hiroshi Miura, Aaron Madlon-Kay
               2021 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2.impl;

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
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.core.team2.TeamSettings;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.OStrings;

/**
 * Git repository credentials provider. One credentials provider created for all git instances.
 * <p>
 * Git supports these protocols:
 * <ul>
 * <li>file://
 * <li>ssh://
 * <li>http://
 * </ul>
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @see <a href="http://www.codeaffine.com/2014/12/09/jgit-authentication/">JGit Authentication Explained</a>
 * @see <a href="https://github.com/apache/mina-sshd/blob/master/docs/git.md">MINA-SSHD Git support</a>
 */
public class GITCredentialsProvider extends CredentialsProvider {

    static final String KEY_USERNAME_SUFFIX = "username";
    static final String KEY_PASSWORD_SUFFIX = "password";
    static final String KEY_FINGERPRINT_SUFFIX = "fingerprint";

    //private ProjectTeamSettings teamSettings;
    /** Predefined in the omegat.project file. */
    private final Map<String, String> predefined = Collections.synchronizedMap(new HashMap<>());

    public static void install() {
        final GITCredentialsProvider c = new GITCredentialsProvider();
        CredentialsProvider.setDefault(c);
    }

    public void setTeamSettings(ProjectTeamSettings teamSettings) {
        //this.teamSettings = teamSettings;
    }

    public void setPredefinedCredentials(String url, String predefinedUser, String predefinedPass,
            String predefinedFingerprint) {
        predefined.put("user." + url, predefinedUser);
        predefined.put("pass." + url, predefinedPass);
        predefined.put("fingerprint." + url, predefinedFingerprint);
    }

    private Credentials loadCredentials(URIish uri) {
        String url = uri.toString();
        Credentials credentials = new Credentials();
        credentials.username = TeamSettings.get(url + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = TeamUtils.decodePassword(TeamSettings.get(url + "!" + KEY_PASSWORD_SUFFIX));
        return credentials;
    }

    private void saveCredentials(URIish uri, Credentials credentials) {
        String url = uri.toString();
        try {
            TeamSettings.set(url + "!" + KEY_USERNAME_SUFFIX, credentials.username);
            TeamSettings.set(url + "!" + KEY_PASSWORD_SUFFIX, TeamUtils.encodePassword(credentials.password));
        } catch (Exception e) {
            Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
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

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {

        // get predefined if exist
        String url = uri.toString();
        String predefinedUser = predefined.get("user." + url);
        String predefinedPass = predefined.get("pass." + url);
        String predefinedFingerprint = predefined.get("fingerprint." + url);

        // get saved
        Credentials credentials = loadCredentials(uri);

        boolean ok = false;
        // theoretically, username can be unknown, but in practice it is always set, so not requested.
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                if (predefinedUser != null && predefinedPass != null) {
                    ((CredentialItem.Username) i).setValue(predefinedUser);
                    continue;
                }
                if (credentials.username == null) {
                    credentials = askCredentials(uri, credentials);
                    if (credentials == null) {
                        throw new UnsupportedCredentialItem(uri,
                                OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                    }
                    saveCredentials(uri, credentials);
                    ok = true;
                }
                ((CredentialItem.Username) i).setValue(credentials.username);
                continue;
            } else if (i instanceof CredentialItem.Password) {
                if (predefinedUser != null && predefinedPass != null) {
                    ((CredentialItem.Password) i).setValue(predefinedPass.toCharArray());
                    continue;
                }
                if (credentials.password == null) {
                    credentials = askCredentials(uri, credentials);
                    if (credentials == null) {
                        throw new UnsupportedCredentialItem(uri,
                                OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                    }
                    saveCredentials(uri, credentials);
                    ok = true;
                }
                ((CredentialItem.Password) i).setValue(credentials.password.toCharArray());
                continue;
            } else if (i instanceof CredentialItem.StringType) {
                if (i.getPromptText().equals("Password: ")) {
                    if (predefinedUser != null && predefinedPass != null) {
                        ((CredentialItem.StringType) i).setValue(predefinedPass);
                        continue;
                    }
                    if (credentials.password == null) {
                        if (!ok) {
                            credentials = askCredentials(uri, credentials);
                            if (credentials == null) {
                                throw new UnsupportedCredentialItem(uri,
                                        OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                            }
                            saveCredentials(uri, credentials);
                        }
                    }
                    ((CredentialItem.StringType) i).setValue(credentials.password);
                    continue;
                } else if (i.getPromptText().startsWith("Passphrase for ")) {
                    // Private key passphrase
                    if (!ok) {
                        String passphrase = askPassphrase(i.getPromptText());
                        if (passphrase == null) {
                            throw new UnsupportedCredentialItem(uri,
                                    OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                        ((CredentialItem.StringType) i).setValue(passphrase);
                        continue;
                    }
                }
            } else if (i instanceof CredentialItem.YesNoType) {
                // e.g.: The authenticity of host 'mygitserver' can't be established.
                // RSA key fingerprint is e2:d3:84:d5:86:e7:68:69:a0:aa:a6:ad:a3:a0:ab:a2.
                // Are you sure you want to continue connecting?
                String storedFingerprint = loadFingerprint(uri);
                String promptText = i.getPromptText();
                String promptedFingerprint = extractFingerprint(promptText);
                if (promptedFingerprint == null) {
                    throw new UnsupportedCredentialItem(uri, "Wrong fingerprint pattern");
                }
                if (predefinedFingerprint != null) {
                    ((CredentialItem.YesNoType) i).setValue(promptedFingerprint.equals(predefinedFingerprint));
                    continue;
                }
                if (promptedFingerprint.equals(storedFingerprint)) {
                    ((CredentialItem.YesNoType) i).setValue(true);
                    continue;
                }
                IMainWindow mw = Core.getMainWindow();
                if (mw != null) {  // on GUI application
                    int choice = mw.showConfirmDialog(promptText, null,
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        ((CredentialItem.YesNoType) i).setValue(true);
                        saveFingerprint(uri, promptedFingerprint);
                    } else {
                        ((CredentialItem.YesNoType) i).setValue(false);
                    }
                } else {
                    // FIXME: implement me.
                    // current silently agreed
                    ((CredentialItem.YesNoType) i).setValue(true);
                    saveFingerprint(uri, promptedFingerprint);
                }
                continue;
            } else if (i instanceof CredentialItem.InformationalMessage) {
                IMainWindow mw = Core.getMainWindow();
                if (mw != null) {  // on GUI application
                    mw.showMessageDialog(i.getPromptText());
                } else {
                    // implement me. show message to console
                }
                continue;
            }
            throw new UnsupportedCredentialItem(uri, i.getClass().getName() + ":" + i.getPromptText());
        }
        return true;
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialItem i : items) {
            if (!(i instanceof CredentialItem.Username || i instanceof CredentialItem.Password)) {
                return false;
            }
        }
        return true;
    }

    /**
     * shows dialog to ask for credentials, and stores credentials.
     *
     * @return true when entered, false on cancel.
     */
    private Credentials askCredentials(URIish uri, Credentials credentials) {
        IMainWindow mw = Core.getMainWindow();
        if (mw != null) {  // when GUI omegat application
            GITUserPassDialog userPassDialog = new GITUserPassDialog(mw.getApplicationFrame());
            userPassDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
            userPassDialog.descriptionTextArea.setText(OStrings
                    .getString(credentials.username == null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG", uri.getHumanishName()));
            // if username is already available in uri, then we will not be asked for an username, so we cannot
            // change it.
            if (uri.getUser() != null && !"".equals(uri.getUser())) {
                userPassDialog.userText.setText(uri.getUser());
                userPassDialog.userText.setEditable(false);
                userPassDialog.userText.setEnabled(false);
            }
            if (credentials.username != null) {
                userPassDialog.userText.setText(credentials.username);
            }
            userPassDialog.setVisible(true);
            if (userPassDialog.getReturnStatus() == GITUserPassDialog.RET_OK) {
                credentials.username = userPassDialog.userText.getText();
                credentials.password = new String(userPassDialog.passwordField.getPassword());
                return credentials;
            } else {
                return null;
            }
        } else {
            // FIXME: ask to user on console
        }
        return null;
    }

    private String askPassphrase(String prompt) {
        GITUserPassDialog userPassDialog = new GITUserPassDialog(Core.getMainWindow().getApplicationFrame());
        userPassDialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
        userPassDialog.descriptionTextArea.setText(prompt);
        userPassDialog.userText.setVisible(false);
        userPassDialog.userLabel.setVisible(false);
        userPassDialog.passwordField.requestFocusInWindow();
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() == GITUserPassDialog.RET_OK) {
            return new String(userPassDialog.passwordField.getPassword());
        } else {
            return null;
        }
    }

    @Override
    public void reset(URIish uri) {
        // reset is called after 5 authorization failures. After 3 resets, the transport gives up.
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

    private static String extractFingerprint(String text) {
        // FIXME: update pattern for MINA-SSHD
        Pattern p = Pattern
                .compile("The authenticity of host '.*' can't be established\\.\\nRSA key fingerprint is (([0-9a-f]{2}:){15}[0-9a-f]{2})\\.\\nAre you sure you want to continue connecting\\?");
        Matcher fingerprintMatcher = p.matcher(text);
        if (fingerprintMatcher.find()) {
            int start = fingerprintMatcher.start(1);
            int end = fingerprintMatcher.end(1);
            return text.substring(start, end);
        }
        return text;
    }

    public static class Credentials {
        public String username = null;
        public String password = null;
    }
}
