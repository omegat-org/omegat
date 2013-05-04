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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.omegat.core.Core;
import org.omegat.gui.dialogs.TeamUserPassDialog;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;

/**
 * GIT repository connection implementation.
 * 
 * Project should use "autocrlf=true" options. Otherwise, repository can be changed every 5 minutes. This
 * property will be setted by OmegaT on checkoutFullProject().
 * 
 * GIT project can't be locked, because git requires to update full snapshot.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 */
public class GITRemoteRepository implements IRemoteRepository {
    protected static String LOCAL_BRANCH = "master";
    protected static String REMOTE_BRANCH = "origin/master";
    protected static String REMOTE = "origin";
    boolean readOnly;

    File localDirectory;
    protected Repository repository;

    private MyCredentialsProvider myCredentialsProvider;

    public static boolean isGITDirectory(File localDirectory) {
        return getLocalRepositoryRoot(localDirectory) != null;
    }

    public boolean isFilesLockingAllowed() {
        return true;
    }

    public GITRemoteRepository(File localDirectory) throws Exception {

        try {
            //workaround for: file c:\project\omegat\project_save.tmx is not contained in C:\project\.
            //The git repo uses the canonical path for some actions, and if c: != C: then an error is raised.
            //if we make it canonical already here, then we don't have that problem.
            localDirectory  = localDirectory.getCanonicalFile();
        } catch (Exception e) {}

        this.localDirectory = localDirectory;
        myCredentialsProvider = new MyCredentialsProvider(this);
        CredentialsProvider.setDefault(myCredentialsProvider);
        File localRepositoryDirectory = getLocalRepositoryRoot(localDirectory);
        if (localRepositoryDirectory != null) {
            repository = Git.open(localRepositoryDirectory).getRepository();
        }
    }

    public void checkoutFullProject(String repositoryURL) throws Exception {
        CloneCommand c = Git.cloneRepository();
        c.setURI(repositoryURL);
        c.setDirectory(localDirectory);
        try {
            c.call();
        } catch (InvalidRemoteException e) {
            if (localDirectory.exists()) {
                deleteDirectory(localDirectory);
            }
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof org.eclipse.jgit.errors.NoRemoteRepositoryException) {
                BadRepositoryException bre = new BadRepositoryException(((org.eclipse.jgit.errors.NoRemoteRepositoryException)cause).getLocalizedMessage());
                bre.initCause(e);
                throw bre;
            }
            throw e;
        }
        repository = Git.open(localDirectory).getRepository();
        new Git(repository).submoduleInit().call();
        new Git(repository).submoduleUpdate().call();

        //Deal with line endings. A normalized repo has LF line endings. 
        //OmegaT uses line endings of OS for storing tmx files.
        //To do auto converting, we need to change a setting:
        StoredConfig config = repository.getConfig();
        if ("\r\n".equals(FileUtil.LINE_SEPARATOR)) {
            //on windows machines, convert text files to CRLF
            config.setBoolean("core", null, "autocrlf", true);
        } else {
            //on Linux/Mac machines (using LF), don't convert text files
            //but use input format, unchanged.
            //NB: I don't know correct setting for OS'es like MacOS <= 9, 
            // which uses CR. Git manual only speaks about converting from/to
            //CRLF, so for CR, you probably don't want conversion either.
            config.setString("core", null, "autocrlf", "input");
        }
        config.save();
    }

    static public boolean deleteDirectory(File path) {
        if( path.exists() ) {
          File[] files = path.listFiles();
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
               deleteDirectory(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() );
      }

    public boolean isChanged(File file) throws Exception {
        String relativeFile = FileUtil.computeRelativePath(repository.getWorkTree(), file);
        Status status = new Git(repository).status().call();
        return status.getModified().contains(relativeFile);
    }

    public boolean isUnderVersionControl(File file) throws Exception {
        String relativeFile = FileUtil.computeRelativePath(repository.getWorkTree(), file);
        Status status = new Git(repository).status().call();
        return status.getUntracked().contains(relativeFile);
    }

    public void setCredentials(String username, String password, boolean forceSavePlainPassword) {
        //we use internal credentials provider, so this function is never called. Nothing to implement.
        //if this function IS called, then we should implement myCredentialsProvider.setUsername/password()
    }

    public void setReadOnly(boolean value) {
        readOnly = value;
    }

    public String getBaseRevisionId(File file) throws Exception {
        RevWalk walk = new RevWalk(repository);

        Ref localBranch = repository.getRef("HEAD");
        Ref remoteBranch = repository.getRef(REMOTE_BRANCH);
        RevCommit headCommit = walk.lookupCommit(localBranch.getObjectId());
        RevCommit upstreamCommit = walk.lookupCommit(remoteBranch.getObjectId());

        LogCommand cmd = new Git(repository).log().addRange(upstreamCommit, headCommit);
        Iterable<RevCommit> commitsToUse = cmd.call();
        RevCommit last = null;
        for (RevCommit commit : commitsToUse) {
            last = commit;
        }
        RevCommit commonBase = last != null ? last.getParent(0) : upstreamCommit;
        return commonBase.getName();
    }

    public void restoreBase(File[] files) throws Exception {
        String baseRevisionId = getBaseRevisionId(files[0]);
        //undo local changes of specific file.
        CheckoutCommand checkoutCommand = new Git(repository).checkout();
        for (File f: files) {
            String relativeFileName = FileUtil.computeRelativePath(repository.getWorkTree(), f);
            checkoutCommand.addPath(relativeFileName);
        }
        checkoutCommand.call();
        //reset repo to previous version. Can cause conflicts for other files!
        new Git(repository).checkout().setName(baseRevisionId).call();
    }

    public void reset() throws Exception {
        Log.logInfoRB("GIT_START", "reset");
        try {
            new Git(repository).reset().setMode(ResetCommand.ResetType.HARD).call();
            Log.logInfoRB("GIT_FINISH", "reset");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "reset", ex.getMessage());
            checkAndThrowException(ex);
        }
    }

    public void updateFullProject() throws NetworkException, Exception {
        Log.logInfoRB("GIT_START", "pull");
        try {
            new Git(repository).fetch().call();
            new Git(repository).checkout().setName(REMOTE_BRANCH).call();
            new Git(repository).branchDelete().setBranchNames(LOCAL_BRANCH).setForce(true).call();
            new Git(repository).checkout().setStartPoint(REMOTE_BRANCH).setCreateBranch(true)
                    .setName(LOCAL_BRANCH).setForce(true).call();
            new Git(repository).submoduleUpdate().call();
            Log.logInfoRB("GIT_FINISH", "pull");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "pull", ex.getMessage());
            checkAndThrowException(ex);
        }
    }

    public void download(File[] files) throws NetworkException, Exception {
        Log.logInfoRB("GIT_START", "download");
        try {
            new Git(repository).fetch().call();
            new Git(repository).checkout().setName(REMOTE_BRANCH).call();
            new Git(repository).branchDelete().setBranchNames(LOCAL_BRANCH).setForce(true).call();
            new Git(repository).checkout().setStartPoint(REMOTE_BRANCH).setCreateBranch(true)
                    .setName(LOCAL_BRANCH).setForce(true).call();
            Log.logInfoRB("GIT_FINISH", "download");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "download", ex.getMessage());
            checkAndThrowException(ex);
        }
    }

    public void upload(File file, String commitMessage) throws NetworkException, Exception {
        if (readOnly) {
            // read-only - upload disabled
            Log.logInfoRB("GIT_READONLY");
            return;
        }

        boolean ok = true;
        Log.logInfoRB("GIT_START", "upload");
        try {
            if (!isChanged(file)) {
                Log.logInfoRB("GIT_FINISH", "upload(not changed)");
                return;
            }
            String filePattern = FileUtil.computeRelativePath(repository.getWorkTree(), file);
            new Git(repository).add().addFilepattern(filePattern).call();
            new Git(repository).commit().setMessage(commitMessage).call();
            Iterable<PushResult> results = new Git(repository).push().setRemote(REMOTE).add(LOCAL_BRANCH)
                    .call();
            int count = 0;
            for (PushResult r : results) {
                for (RemoteRefUpdate update : r.getRemoteUpdates()) {
                    count++;
                    if (update.getStatus() != RemoteRefUpdate.Status.OK) {
                        ok = false;
                    }
                }
            }
            if (count < 1) {
                ok = false;
            }
            Log.logInfoRB("GIT_FINISH", "upload");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "upload", ex.getMessage());
            checkAndThrowException(ex);
        }
        if (!ok) {
            Log.logWarningRB("GIT_CONFLICT");
        }
    }

    private void checkAndThrowException(Exception ex) throws NetworkException, Exception {
        if (ex instanceof TransportException) {
            throw new NetworkException(ex);
        } else {
            throw ex;
        }
    }

    private static File getLocalRepositoryRoot(File path) {
        if (path == null) {
            return null;
        }
        File possibleControlDir = new File(path, ".git");
        if (possibleControlDir.exists() && possibleControlDir.isDirectory()) {
            return path;
        } else {
            // We need to call getAbsoluteFile() because "path" can be relative. In this case, we will have
            // "null" instead real parent directory.
            return getLocalRepositoryRoot(path.getAbsoluteFile().getParentFile());
        }
    }

    static ProgressMonitor gitProgress = new ProgressMonitor() {
        public void update(int completed) {
            System.out.println("update: " + completed);
        }

        public void start(int totalTasks) {
            System.out.println("start: " + totalTasks);
        }

        public boolean isCancelled() {
            return false;
        }

        public void endTask() {
            System.out.println("endTask");
        }

        public void beginTask(String title, int totalWork) {
            System.out.println("beginTask: " + title + " total: " + totalWork);
        }
    };

    /**
     * CredentialsProvider that will ask user for credentials when required,
     * and can store the credentials to plain text file.
      */
    private class MyCredentialsProvider extends CredentialsProvider {
        GITRemoteRepository gitRemoteRepository;
        /**
         * Name of file to store credentials in (when asked)
         */
        String credentialsFilename;

        /**
         * key in properties file that contains credentials
         */
        private final String pkey_username = "username";
        private final String pkey_password = "password";
        private final String pkey_fingerprint = "RSAkeyfingerprint";

        /**
         * Currently used username
         */
        private String username;
        /**
         * Currently used password
         */
        private char[] password;

        /**
         * Fingerprint of git server.
         */
        private String fingerprint;

        private boolean saveCredentialsToPlainText = false;

        public MyCredentialsProvider(GITRemoteRepository repo) {
            super();
            this.gitRemoteRepository = repo;
            readCredentials();
        }

        /**
         * reads username, password and host fingerprint from plain text file.
         */
        private void readCredentials() {
            credentialsFilename = gitRemoteRepository.localDirectory+File.separator+"credentials.properties";
            File credentialsFile = new File(credentialsFilename);
            if (credentialsFile.canRead()) {
                Properties p = new Properties();
                try {
                    p.load(new FileInputStream(credentialsFile));
                    username = p.getProperty(pkey_username);
                    password = p.getProperty(pkey_password).toCharArray();
                    fingerprint = p.getProperty(pkey_fingerprint);
                    saveCredentialsToPlainText = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Saves username, password and fingerprint (if known) to plain text file.
         */
        private void saveCredentialsToPlainTextFile() {
            Properties p = new Properties();
            p.setProperty(pkey_username, username);
            p.setProperty(pkey_password, String.valueOf(password));
            if (fingerprint != null) {
                p.setProperty(pkey_fingerprint, fingerprint);
            }
            File credentialsFile = new File(credentialsFilename);
            try {
                if (!credentialsFile.exists()) {
                    credentialsFile.createNewFile();
                }
                p.store(new FileOutputStream(credentialsFile), "git remote access credentials for OmegaT project");
            } catch (FileNotFoundException e) {
                Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
            } catch (IOException e) {
                Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
            }
        }

        @Override
        public boolean get(URIish uri, CredentialItem... items)
                throws UnsupportedCredentialItem {
            boolean ok = false;
            //theoretically, username can be unknown, but in practice it is always set, so not requested.
            for (CredentialItem i : items) {
                if (i instanceof CredentialItem.Username) {
                    if (username==null) {
                        ok = askCredentials(uri.getUser());
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Username) i).setValue(username);
                    continue;
                } else if (i instanceof CredentialItem.Password) {
                    if (password==null) {
                        ok = askCredentials(uri.getUser());
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Password) i).setValue(password);
                    if (password != null) {
                        uri.setPass(new String(password));
                    }
                    continue;
                } else if (i instanceof CredentialItem.StringType) {
                    if (i.getPromptText().equals("Password: ")) {
                        if (password==null) {
                            if (!ok) {
                                ok = askCredentials(uri.getUser());
                                if (!ok) {
                                    throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                                }
                            }
                        }
                        ((CredentialItem.StringType) i).setValue(new String(password));
                        continue;
                    }
                } else if (i instanceof CredentialItem.YesNoType) {
                    //e.g.: The authenticity of host 'mygitserver' can't be established.
                    //RSA key fingerprint is e2:d3:84:d5:86:e7:68:69:a0:aa:a6:ad:a3:a0:ab:a2.
                    //Are you sure you want to continue connecting?
                    String promptText = i.getPromptText();
                    String promptedFingerprint = null;
                    Pattern p = Pattern.compile("The authenticity of host '.*' can't be established\\.\\nRSA key fingerprint is (([0-9a-f]{2}:){15}[0-9a-f]{2})\\.\\nAre you sure you want to continue connecting\\?");
                    Matcher fingerprintMatcher = p.matcher(promptText);
                    if (fingerprintMatcher.find()) {
                        int start = fingerprintMatcher.start(1);
                        int end = fingerprintMatcher.end(1);
                        promptedFingerprint = promptText.substring(start, end);
                        if (promptedFingerprint.equals(this.fingerprint)) {
                            ((CredentialItem.YesNoType) i).setValue(true);
                            continue;
                        }
                    }
                    int choice = Core.getMainWindow().showConfirmDialog(promptText, null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice==JOptionPane.YES_OPTION) {
                        ((CredentialItem.YesNoType) i).setValue(true);
                        if (promptedFingerprint != null) {
                            this.fingerprint = promptedFingerprint;
                        }
                        if (saveCredentialsToPlainText) {
                            saveCredentialsToPlainTextFile();
                        }
                    } else {
                        ((CredentialItem.YesNoType) i).setValue(false);
                    }
                    continue;
                } else if (i instanceof CredentialItem.InformationalMessage) {
                    Core.getMainWindow().showMessageDialog(i.getPromptText());
                    continue;
                }
                throw new UnsupportedCredentialItem(uri, i.getClass().getName()
                        + ":" + i.getPromptText());
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
         * @return true when entered, false on cancel.
         */
        private boolean askCredentials(String usernameInUri) {
            TeamUserPassDialog userPassDialog = new TeamUserPassDialog(Core.getMainWindow().getApplicationFrame());
            DockingUI.displayCentered(userPassDialog);
            userPassDialog.descriptionTextArea.setText(OStrings.getString(this.username==null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
            //if username is already available in uri, then we will not be asked for an username, so we cannot change it.
            if (usernameInUri != null && !"".equals(usernameInUri)) {
                userPassDialog.userText.setText(usernameInUri);
                userPassDialog.userText.setEditable(false);
                userPassDialog.userText.setEnabled(false);
            }
            userPassDialog.setVisible(true);
            if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
                username = userPassDialog.userText.getText();
                password = userPassDialog.passwordField.getPassword();
                gitRemoteRepository.setReadOnly(userPassDialog.cbReadOnly.isSelected());
                saveCredentialsToPlainText = userPassDialog.cbForceSavePlainPassword.isSelected();
                if (saveCredentialsToPlainText) {
                    saveCredentialsToPlainTextFile();
                }
                return true;
            } else {
                return false;
            }
        }

        public void reset(URIish uri) {
            //reset is called after 5 authorization failures. After 3 resets, the transport gives up.
            username = null;
            password = null;
        }

    }
}
