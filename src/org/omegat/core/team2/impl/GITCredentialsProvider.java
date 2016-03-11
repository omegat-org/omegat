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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.omegat.core.Core;
import org.omegat.core.team2.IRemoteRepository2.Credentials;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;

/**
 * GIT repository connection implementation.
 * 
 * GIT supports protocols: file://, ssh:// git:// http://.
 * 
 * See JGit Authentication Explained : http://www.codeaffine.com/2014/12/09/jgit-authentication/
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class GITCredentialsProvider extends CredentialsProvider {

    GITRemoteRepository2 gitRemoteRepository;
    File credentialsFile;

    Credentials credentials;

    public GITCredentialsProvider(GITRemoteRepository2 repo) {
        this.gitRemoteRepository = repo;
        if (repo != null) {
            credentialsFile = new File(gitRemoteRepository.localDirectory, "credentials.properties");
        }
    }

    public void setCredentials(Credentials credentials) {
        if (credentials == null) {
            return;
        }
        this.credentials = credentials.clone();
    }

    private void loadCredentials() {
        if (credentialsFile == null || !credentialsFile.exists()) {
            credentials = new Credentials();
            return;
        }
        try {
            credentials = Credentials.fromFile(credentialsFile);
        } catch (FileNotFoundException ex) {
            credentials = new Credentials();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveCredentials() {
        if (credentials == null || credentialsFile == null || !credentials.saveAsPlainText) {
            return;
        }
        try {
            credentials.saveToPlainTextFile(credentialsFile);
        } catch (FileNotFoundException e) {
            Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
        } catch (IOException e) {
            Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
        }
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        if (credentials == null) {
            loadCredentials();
        }
        boolean ok = false;
        // theoretically, username can be unknown, but in practice it is always set, so not requested.
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                if (credentials.username == null) {
                    ok = askCredentials(uri.getUser());
                    if (!ok) {
                        throw new UnsupportedCredentialItem(uri,
                                OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                    }
                }
                ((CredentialItem.Username) i).setValue(credentials.username);
                continue;
            } else if (i instanceof CredentialItem.Password) {
                if (credentials.password == null) {
                    ok = askCredentials(uri.getUser());
                    if (!ok) {
                        throw new UnsupportedCredentialItem(uri,
                                OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                    }
                }
                ((CredentialItem.Password) i).setValue(credentials.password);
                if (credentials.password != null) {
                    uri.setPass(new String(credentials.password));
                }
                continue;
            } else if (i instanceof CredentialItem.StringType) {
                if (i.getPromptText().equals("Password: ")) {
                    if (credentials.password == null) {
                        if (!ok) {
                            ok = askCredentials(uri.getUser());
                            if (!ok) {
                                throw new UnsupportedCredentialItem(uri,
                                        OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                            }
                        }
                    }
                    ((CredentialItem.StringType) i).setValue(new String(credentials.password));
                    continue;
                }
            } else if (i instanceof CredentialItem.YesNoType) {
                // e.g.: The authenticity of host 'mygitserver' can't be established.
                // RSA key fingerprint is e2:d3:84:d5:86:e7:68:69:a0:aa:a6:ad:a3:a0:ab:a2.
                // Are you sure you want to continue connecting?
                String promptText = i.getPromptText();
                String promptedFingerprint = extractFingerprint(promptText);
                if (promptedFingerprint.equals(credentials.fingerprint)) {
                    ((CredentialItem.YesNoType) i).setValue(true);
                    continue;
                }
                int choice = Core.getMainWindow().showConfirmDialog(promptText, null,
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    ((CredentialItem.YesNoType) i).setValue(true);
                    if (promptedFingerprint != null) {
                        credentials.fingerprint = promptedFingerprint;
                    }
                    saveCredentials();
                } else {
                    ((CredentialItem.YesNoType) i).setValue(false);
                }
                continue;
            } else if (i instanceof CredentialItem.InformationalMessage) {
                Core.getMainWindow().showMessageDialog(i.getPromptText());
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
            if (i instanceof CredentialItem.Username)
                continue;

            else if (i instanceof CredentialItem.Password)
                continue;

            else
                return false;
        }
        return true;
    }

    /**
     * shows dialog to ask for credentials, and stores credentials.
     * 
     * @return true when entered, false on cancel.
     */
    private boolean askCredentials(String usernameInUri) {
        GITUserPassDialog userPassDialog = new GITUserPassDialog(Core.getMainWindow().getApplicationFrame());
        DockingUI.displayCentered(userPassDialog);
        userPassDialog.descriptionTextArea.setText(OStrings
                .getString(credentials.username == null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
        // if username is already available in uri, then we will not be asked for an username, so we cannot
        // change it.
        if (usernameInUri != null && !"".equals(usernameInUri)) {
            userPassDialog.userText.setText(usernameInUri);
            userPassDialog.userText.setEditable(false);
            userPassDialog.userText.setEnabled(false);
        }
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() == GITUserPassDialog.RET_OK) {
            credentials.username = userPassDialog.userText.getText();
            credentials.password = userPassDialog.passwordField.getPassword();
            credentials.readOnly = userPassDialog.cbReadOnly.isSelected();
            if (gitRemoteRepository != null) {
                gitRemoteRepository.setReadOnly(credentials.readOnly);
            }
            credentials.saveAsPlainText = userPassDialog.cbForceSavePlainPassword.isSelected();
            saveCredentials();
            return true;
        } else {
            return false;
        }
    }

    public void reset(URIish uri) {
        // reset is called after 5 authorization failures. After 3 resets, the transport gives up.
        credentials.clear();
    }

    private static String extractFingerprint(String text) {
        Pattern p = Pattern
                .compile("The authenticity of host '.*' can't be established\\.\\nRSA key fingerprint is (([0-9a-f]{2}:){15}[0-9a-f]{2})\\.\\nAre you sure you want to continue connecting\\?");
        Matcher fingerprintMatcher = p.matcher(text);
        if (fingerprintMatcher.find()) {
            int start = fingerprintMatcher.start(1);
            int end = fingerprintMatcher.end(1);
            return text.substring(start, end);
        }
        return null;
    }
}
