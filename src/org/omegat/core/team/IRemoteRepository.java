/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

import org.omegat.core.Core;

/**
 * Interface for any remote repository implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IRemoteRepository {
    /**
     * Allow or disable file locking.
     * 
     * File locking should be disabled for some repositories which check full snapshots instead one file, like
     * git,
     */
    boolean isFilesLockingAllowed();

    /**
     * Set credentials for repository access.
     */    
    void setCredentials(Credentials credentials);

    /**
     * Set repository read-only mode, or read-write mode.
     */
    void setReadOnly(boolean value);

    /**
     * Check is file changed from BASE revision.
     */
    boolean isChanged(File file) throws Exception;

    /**
     * Check is file under version control.
     */
    boolean isUnderVersionControl(File file) throws Exception;

    /**
     * Update full project from remote repository.
     * PRE: no local changes if you don't want conflicts
     */
    void updateFullProject() throws NetworkException, Exception;

    /**
     * Initial project checkout.
     */
    void checkoutFullProject(String repositoryURL) throws Exception;

    /**
     * Get base revision ID of files from working copy.
     */
    String getBaseRevisionId(File file) throws Exception;

    /**
     * Restore base revision of files in working copy.
     */
    void restoreBase(File[] files) throws Exception;

    /**
     * Download HEAD revision of files from remote repository.
     * NB: Due to the nature of some VCS, it is possible that more files than the given files are updated to the head revision.
     * PRE: no local changes in files (except the given files), if you don't want conflicts.
     */
    void download(File[] files) throws NetworkException, Exception;

    /**
     * Undo all local changes
     * 
     * @throws Exception
     */
    void reset() throws Exception;


    /**
     * Upload local changes into remote repository.
     * 
     * If upload fails because in the mean time the remote was updated by others, this function should not throw an error.
     *
     * Every new implementation must be checked for conflicts resolving on upload, because some other users
     * able to change remote repository in time between download and upload. There are two critical ways which
     * should be tested:
     * 
     * 1. Somebody changed the same segment in repository.
     * 
     * 2. Somebody changed other segments in repository.
     */
    void upload(File file, String commitMessage) throws NetworkException, Exception;

    /**
     * Credentials are not provided or not correct. Should trigger credentials-prompt
     */
    @SuppressWarnings("serial")
    public static class AuthenticationException extends Exception {
        public AuthenticationException(Exception ex) {
            super(ex);
        }
    }
    /**
     * Given repository does not exist on the remote machine
     *
     */
    @SuppressWarnings("serial")
	public static class BadRepositoryException extends Exception {
        public BadRepositoryException(String message) {
            super(message);
        }
    }

    /**
     * Network problems. E.g. no internet available.
     */
    @SuppressWarnings("serial")
    public static class NetworkException extends Exception {
        public NetworkException(Throwable ex) {
            super(ex);
        }
    }
    
    public static class Credentials {
        public String username = null;
        public char[] password = null;
        public boolean saveAsPlainText = false;
        public boolean readOnly = false;
        public String fingerprint = null;
        
        public void clear() {
            username = null;
            if (password != null) {
                Arrays.fill(password, '0');
            }
            password = null;
            fingerprint = null;
        }
        
        /**
         * key in properties file that contains credentials
         */
        private static final String PKEY_USERNAME = "username";
        private static final String PKEY_PASSWORD = "password";
        private static final String PKEY_FINGERPRINT = "RSAkeyfingerprint";
        
        public static Credentials fromFile(File file) throws FileNotFoundException, IOException {
            Credentials result = new Credentials();
            if (!file.canRead()) {
                throw new IOException("Insufficient permissions to read file: " + file);
            }
            Properties p = new Properties();
            InputStream stream = new FileInputStream(file);
            try {
                p.load(stream);
            } finally {
                stream.close();
            }
            result.username = p.getProperty(PKEY_USERNAME);
            result.password = p.getProperty(PKEY_PASSWORD).toCharArray();
            result.fingerprint = p.getProperty(PKEY_FINGERPRINT);
            result.saveAsPlainText = true;
            return result;
        }
        
        /**
         * Saves username, password and fingerprint (if known) to plain text file.
         */
        public void saveToPlainTextFile(File file) throws FileNotFoundException, IOException {
            Properties p = new Properties();
            p.setProperty(PKEY_USERNAME, username);
            p.setProperty(PKEY_PASSWORD, String.valueOf(password));
            if (fingerprint != null) {
                p.setProperty(PKEY_FINGERPRINT, fingerprint);
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream stream = new FileOutputStream(file);
            try {
                p.store(stream, "Remote access credentials for OmegaT project");
            } finally {
                stream.close();
            }
        }
        
        public Credentials clone() {
            Credentials clone = new Credentials();
            clone.username = username;
            if (password != null) {
                clone.password = Arrays.copyOf(password, password.length);
            }
            clone.fingerprint = fingerprint;
            clone.saveAsPlainText = saveAsPlainText;
            clone.readOnly = readOnly;
            return clone;
        }
    }
}
