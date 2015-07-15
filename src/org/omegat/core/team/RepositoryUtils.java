/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Aaron Madlon-Kay
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
package org.omegat.core.team;

import org.omegat.core.Core;
import org.omegat.core.team.IRemoteRepository.AuthenticationException;
import org.omegat.core.team.IRemoteRepository.Credentials;
import org.omegat.gui.dialogs.TeamUserPassDialog;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Some utility methods for working with remote repository.
 *  
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Aaron Madlon-Kay
 */
public class RepositoryUtils {
    /**
     * Display dialog for credentials.
     * 
     * @return true if user entered credentials, otherwise - false
     */
    public static boolean askForCredentials(Credentials credentials, String message) {
        TeamUserPassDialog userPassDialog = new TeamUserPassDialog(Core.getMainWindow().getApplicationFrame());
        if (!StringUtil.isEmpty(credentials.username)) {
            userPassDialog.setFixedUsername(credentials.username);
        }
        userPassDialog.descriptionTextArea.setText(message);
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
            credentials.username = userPassDialog.userText.getText();
            credentials.password = userPassDialog.getPasswordCopy();
            credentials.saveAsPlainText = userPassDialog.cbForceSavePlainPassword.isSelected();
            credentials.readOnly = userPassDialog.cbReadOnly.isSelected();
            return true;
        } else {
            return false;
        }
    }
    
    private static String getUsernameFromUrl(String url) {
        int at = url.indexOf('@');
        if (at == -1) {
            return null;
        }
        String username = url.substring(0, at);
        int slashes = username.indexOf("://");
        if (slashes != -1) {
            username = username.substring(slashes + "://".length());
        }
        return username;
    }

    /**
     * Class to execute a repository command that can throw a IRemoteRepository.AuthenticationException.
     * In that case, a username/password dialog will be shown.
     */
    public static abstract class AskCredentials {
        
        public Credentials credentials = null;

       /**
         * wrapper around callRepository to execute some repository command. 
         * On IRemoteRepository.AuthenticationException, show username/password dialog and try again.
         * @param repository
         * @throws Exception when no credentials entered.
         */
        public void execute(IRemoteRepository repository) throws Exception {
            boolean firstPass = true;
            while (true) {
                try {
                    callRepository();
                    break;
                } catch (IRemoteRepository.AuthenticationException ex) {
                    if (credentials == null) {
                        credentials = new Credentials();
                    }
                    boolean entered = RepositoryUtils.askForCredentials(credentials,
                            OStrings.getString(firstPass ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
                    repository.setCredentials(credentials);
                    if (!entered) {
                        throw ex;
                    }
                    firstPass = false;
                }
            }
        }

        /**
         * Implement here a function to execute, which can throw an IRemoteRepository.AuthenticationException.
         * It is called by the execute() function which will show an username/password dialog on this exeption.
         * Other execptions are thrown.
         * @throws Exception Any exception;
         * can even be IRemoteRepository.AuthenticationException in case user did not enter his credentials.
         */
        abstract protected void callRepository() throws Exception;
    }
    

    /**
     * A class to facilitate detecting the type of a remote repository.
     * <p>
     * Instantiate with the URL of the repository and initial default credentials (may be null).
     * After running {@link #execute(IRemoteRepository)}, the results will be available in the
     * {@link #credentials} and {@link #repoType} members.
     */
    public static class RepoTypeDetector {
        
        private String url = null;
        public Credentials credentials = null;
        public Class<? extends IRemoteRepository> repoType = null;
        
        public RepoTypeDetector(String url, Credentials credentials) {
            this.url = url;
            this.credentials = credentials;
        }

        public void execute() throws Exception {
            boolean firstPass = true;
            while (true) {
                try {
                    repoType = detect(credentials);
                    break;
                } catch (IRemoteRepository.AuthenticationException ex) {
                    if (credentials == null) {
                        credentials = new Credentials();
                        credentials.username = getUsernameFromUrl(url);
                    }
                    boolean entered = RepositoryUtils.askForCredentials(credentials,
                            OStrings.getString(firstPass ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
                    if (!entered) {
                        throw new RuntimeException("User declined to enter credentials.", ex);
                    }
                    firstPass = false;
                }
            }
        }

        private Class<? extends IRemoteRepository> detect(Credentials credentials) throws Exception {
            Exception thrown = null;
            try {
                if (GITRemoteRepository.isGitRepository(url, credentials)) {
                    return GITRemoteRepository.class;
                }
            } catch (AuthenticationException ex) {
                thrown = ex;
            }
            if (SVNRemoteRepository.isSVNRepository(url, credentials)) {
                return SVNRemoteRepository.class;
            }
            if (thrown != null) {
                throw thrown;
            }
            return null;
        }
    }
}
