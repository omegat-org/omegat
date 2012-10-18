/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.core.team;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
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
 * Please, do not use it with autocrlf option, since jgit not supported it yet.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 */
public class GITRemoteRepository implements IRemoteRepository {
    static String LOCAL_BRANCH = "master";
    static String REMOTE_BRANCH = "origin/master";
    static String REMOTE = "origin";
    boolean readOnly;

    File localDirectory;
    Repository repository;

    private MyCredentialsProvider myCredentialsProvider;

    public static boolean isGITDirectory(File localDirectory) {
        return getLocalRepositoryRoot(localDirectory) != null;
    }

    public GITRemoteRepository(File localDirectory) throws Exception {
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
        c.call();
        repository = Git.open(localDirectory).getRepository();
    }

    public boolean isChanged(File file) throws Exception {
        String relativeFile = FileUtil.computeRelativePath(repository.getWorkTree(), file);
        Status status = new Git(repository).status().call();
        return status.getModified().contains(relativeFile);
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

    public void restoreBase(File file) throws Exception {
        new Git(repository).reset().setMode(ResetCommand.ResetType.HARD).call();
        new Git(repository).checkout().setName(getBaseRevisionId(file)).call();
    }

    public void updateFullProject() throws Exception {
        Log.logInfoRB("GIT_START", "pull");
        try {
            new Git(repository).fetch().call();
            new Git(repository).checkout().setName(REMOTE_BRANCH).call();
            new Git(repository).branchDelete().setBranchNames(LOCAL_BRANCH).setForce(true).call();
            new Git(repository).checkout().setStartPoint(REMOTE_BRANCH).setCreateBranch(true)
                    .setName(LOCAL_BRANCH).setForce(true).call();
            Log.logInfoRB("GIT_FINISH", "pull");
        } catch (Exception ex) {
            Log.logErrorRB("GIT_ERROR", "pull", ex.getMessage());
            throw ex;
        }
    }

    public void download(File file) throws Exception {
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
        }
    }

    public void upload(File file, String commitMessage) throws Exception {
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
            throw ex;
        }
        if (!ok) {
            Log.logWarningRB("GIT_CONFLICT");
            throw new Exception("Conflict");
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

        /**
         * Currently used username
         */
        private String username;
        /**
         * Currently used password
         */
        private char[] password;

        public MyCredentialsProvider(GITRemoteRepository repo) {
            super();
            this.gitRemoteRepository = repo;
            credentialsFilename = gitRemoteRepository.localDirectory+File.separator+"credentials.properties";
            File credentialsFile = new File(credentialsFilename);
            if (credentialsFile.canRead()) {
                Properties p = new Properties();
                try {
                    p.load(new FileInputStream(credentialsFile));
                    username = p.getProperty(pkey_username);
                    password = p.getProperty(pkey_password).toCharArray();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean get(URIish uri, CredentialItem... items)
                throws UnsupportedCredentialItem {
            boolean ok = false;
            for (CredentialItem i : items) {
                if (i instanceof CredentialItem.Username) {
                    if (username==null) {
                        ok = askCredentials();
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Username) i).setValue(username);
                    continue;
                } else if (i instanceof CredentialItem.Password) {
                    if (password==null) {
                        ok = askCredentials();
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Password) i).setValue(password);
                    continue;
                } else if (i instanceof CredentialItem.StringType) {
                    if (i.getPromptText().equals("Password: ")) {
                        if (password==null) {
                            if (!ok) {
                                ok = askCredentials();
                                if (!ok) {
                                    throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                                }
                            }
                        }
                        ((CredentialItem.StringType) i).setValue(new String(password));
                        continue;
                    }
                } else if (i instanceof CredentialItem.YesNoType) {
                    int choice = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(), i.getPromptText(),null , JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice==JOptionPane.YES_OPTION) {
                        ((CredentialItem.YesNoType) i).setValue(true);
                    } else {
                        ((CredentialItem.YesNoType) i).setValue(false);
                    }
                    continue;
                } else if (i instanceof CredentialItem.InformationalMessage) {
                    JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(), i.getPromptText());
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
        private boolean askCredentials() {
            TeamUserPassDialog userPassDialog = new TeamUserPassDialog(Core.getMainWindow().getApplicationFrame());
            DockingUI.displayCentered(userPassDialog);
            userPassDialog.descriptionTextArea.setText(OStrings.getString(username==null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
            userPassDialog.setVisible(true);
            if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
                username = userPassDialog.userText.getText();
                password = userPassDialog.passwordField.getPassword();
                gitRemoteRepository.setReadOnly(userPassDialog.cbReadOnly.isSelected());
                if (userPassDialog.cbForceSavePlainPassword.isSelected()) {
                    Properties p = new Properties();
                    p.setProperty(pkey_username, username);
                    p.setProperty(pkey_password, String.valueOf(password));
                    File credentialsFile = new File(credentialsFilename);
                    try {
                        if (!credentialsFile.exists()) {
                            credentialsFile.createNewFile();
                        }
                        p.store(new FileOutputStream(credentialsFile), "git remote access credentials for OmegaT project");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(), "could not save credentials to textfile");
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(), "could not save credentials to textfile");
                        e.printStackTrace();
                    }
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
