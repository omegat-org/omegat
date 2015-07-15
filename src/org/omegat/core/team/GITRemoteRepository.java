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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
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
import org.omegat.util.StringUtil;
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
 * @author Aaron Madlon-Kay
 */
public class GITRemoteRepository implements IRemoteRepository {
    private static final Logger LOGGER = Logger.getLogger(GITRemoteRepository.class.getName());

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
        CredentialsProvider prevProvider = CredentialsProvider.getDefault();
        myCredentialsProvider = new MyCredentialsProvider(this);
        if (prevProvider instanceof MyCredentialsProvider) {
            myCredentialsProvider.setCredentials(((MyCredentialsProvider)prevProvider).credentials);
        }
        CredentialsProvider.setDefault(myCredentialsProvider);
        File localRepositoryDirectory = getLocalRepositoryRoot(localDirectory);
        if (localRepositoryDirectory != null) {
            repository = Git.open(localRepositoryDirectory).getRepository();
        }
    }

    public void checkoutFullProject(String repositoryURL) throws Exception {
        Log.logInfoRB("GIT_START", "clone");
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
        myCredentialsProvider.saveCredentials();
        Log.logInfoRB("GIT_FINISH", "clone");
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
        Log.logInfoRB("GIT_START", "status");
        String relativeFile = FileUtil.computeRelativePath(repository.getWorkTree(), file);
        Status status = new Git(repository).status().call();
        Log.logInfoRB("GIT_FINISH", "status");
        boolean result = status.getModified().contains(relativeFile);
        Log.logDebug(LOGGER, "GIT modified status of {0} is {1}", relativeFile, result);
        return result;
    }

    public boolean isUnderVersionControl(File file) throws Exception {
        boolean result = file.exists();
        String relativeFile = FileUtil.computeRelativePath(repository.getWorkTree(), file);
        Status status = new Git(repository).status().call();

        if (status.getAdded().contains(relativeFile) || status.getModified().contains(relativeFile)
                || status.getChanged().contains(relativeFile)
                || status.getConflicting().contains(relativeFile)
                || status.getMissing().contains(relativeFile) || status.getRemoved().contains(relativeFile)) {
            result = true;
        }
        if (status.getUntracked().contains(relativeFile)) {
            result = false;
        }
        Log.logDebug(LOGGER, "GIT file {0} is under version control: {1}", relativeFile, result);
        return result;
    }
    
    public void setCredentials(Credentials credentials) {
        if (credentials == null) {
            return;
        }
        myCredentialsProvider.setCredentials(credentials);
        setReadOnly(credentials.readOnly);
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
        Log.logDebug(LOGGER, "GIT HEAD rev: {0}", headCommit.getName());
        Log.logDebug(LOGGER, "GIT origin/master rev: {0}", upstreamCommit.getName());

        LogCommand cmd = new Git(repository).log().addRange(upstreamCommit, headCommit);
        Iterable<RevCommit> commitsToUse = cmd.call();
        RevCommit last = null;
        for (RevCommit commit : commitsToUse) {
            last = commit;
        }
        RevCommit commonBase = last != null ? last.getParent(0) : upstreamCommit;
        Log.logDebug(LOGGER, "GIT commonBase rev: {0}", commonBase.getName());
        return commonBase.getName();
    }

    public void restoreBase(File[] files) throws Exception {
        String baseRevisionId = getBaseRevisionId(files[0]);
        Log.logDebug(LOGGER, "GIT restore base {0} for {1}", baseRevisionId, (Object) files);
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
//            if (!isChanged(file)) {
//                Log.logInfoRB("GIT_FINISH", "upload(not changed)");
//                return;
//            }
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
    private static class MyCredentialsProvider extends CredentialsProvider {
        
        GITRemoteRepository gitRemoteRepository;
        File credentialsFile;
        
        private Credentials credentials;

        public MyCredentialsProvider(GITRemoteRepository repo) {
            super();
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
        
        private void saveCredentials() {
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
        public boolean get(URIish uri, CredentialItem... items)
                throws UnsupportedCredentialItem {
            if (credentials == null) {
                loadCredentials();
            }
            boolean ok = false;
            //theoretically, username can be unknown, but in practice it is always set, so not requested.
            for (CredentialItem i : items) {
                if (i instanceof CredentialItem.Username) {
                    if (credentials.username==null) {
                        ok = askCredentials(uri.getUser());
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Username) i).setValue(credentials.username);
                    continue;
                } else if (i instanceof CredentialItem.Password) {
                    if (credentials.password==null) {
                        ok = askCredentials(uri.getUser());
                        if (!ok) {
                            throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                        }
                    }
                    ((CredentialItem.Password) i).setValue(credentials.password);
                    if (credentials.password != null) {
                        uri.setPass(new String(credentials.password));
                    }
                    continue;
                } else if (i instanceof CredentialItem.StringType) {
                    if (i.getPromptText().equals("Password: ")) {
                        if (credentials.password==null) {
                            if (!ok) {
                                ok = askCredentials(uri.getUser());
                                if (!ok) {
                                    throw new UnsupportedCredentialItem(uri, OStrings.getString("TEAM_CREDENTIALS_DENIED"));
                                }
                            }
                        }
                        ((CredentialItem.StringType) i).setValue(new String(credentials.password));
                        continue;
                    }
                } else if (i instanceof CredentialItem.YesNoType) {
                    //e.g.: The authenticity of host 'mygitserver' can't be established.
                    //RSA key fingerprint is e2:d3:84:d5:86:e7:68:69:a0:aa:a6:ad:a3:a0:ab:a2.
                    //Are you sure you want to continue connecting?
                    String promptText = i.getPromptText();
                    String promptedFingerprint = extractFingerprint(promptText);
                    if (promptedFingerprint.equals(credentials.fingerprint)) {
                        ((CredentialItem.YesNoType) i).setValue(true);
                        continue;
                    }
                    int choice = Core.getMainWindow().showConfirmDialog(promptText, null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice==JOptionPane.YES_OPTION) {
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
            userPassDialog.descriptionTextArea.setText(OStrings.getString(credentials.username==null ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
            //if username is already available in uri, then we will not be asked for an username, so we cannot change it.
            if (!StringUtil.isEmpty(usernameInUri)) {
                userPassDialog.setFixedUsername(usernameInUri);
            }
            userPassDialog.setVisible(true);
            if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
                credentials.username = userPassDialog.userText.getText();
                credentials.password = userPassDialog.getPasswordCopy();
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
            //reset is called after 5 authorization failures. After 3 resets, the transport gives up.
            credentials.clear();
        }

    }

    private static String extractFingerprint(String text) {
        Pattern p = Pattern.compile("The authenticity of host '.*' can't be established\\.\\nRSA key fingerprint is (([0-9a-f]{2}:){15}[0-9a-f]{2})\\.\\nAre you sure you want to continue connecting\\?");
        Matcher fingerprintMatcher = p.matcher(text);
        if (fingerprintMatcher.find()) {
            int start = fingerprintMatcher.start(1);
            int end = fingerprintMatcher.end(1);
            return text.substring(start, end);
        }
        return null;
    }
    

    /**
     * Determines whether or not the supplied URL represents a valid Git repository.
     * 
     * <p>Does the equivalent of <code>git ls-remote <i>url</i></code>.
     * 
     * @param url URL of supposed remote repository
     * @return true if repository appears to be valid, false otherwise
     */
    public static boolean isGitRepository(String url, Credentials credentials)
            throws AuthenticationException {
        // Heuristics to save some waiting time
        if (url.startsWith("svn://") || url.startsWith("svn+")) {
            return false;
        }
        try {
            if (credentials != null) {
                MyCredentialsProvider provider = new MyCredentialsProvider(null);
                provider.setCredentials(credentials);
                CredentialsProvider.setDefault(provider);
            }
            Collection<Ref> result = new LsRemoteCommand(null).setRemote(url).call();
            return !result.isEmpty();
        } catch (TransportException ex) {
            String message = ex.getMessage();
            if (message.endsWith("not authorized") || message.endsWith("Auth fail")
            		|| message.contains("Too many authentication failures")
            		|| message.contains("Authentication is required")) {
                throw new AuthenticationException(ex);
            }
            return false;
        } catch (GitAPIException ex) {
            throw new AuthenticationException(ex);
        } catch (JGitInternalException ex) {
            // Happens if the URL is a Subversion URL like svn://...
            return false;
        }
    }

    public static String guessRepoName(String url) {
        url = StringUtil.stripFromEnd(url, "/", ".git");
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
