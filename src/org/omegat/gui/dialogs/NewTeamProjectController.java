/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichick
               2014 Aaron Madlon-Kay
               2022 Hiroshi Miura
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
 *************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.Core;
import org.omegat.core.team2.RemoteRepositoryFactory;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;

import gen.core.project.RepositoryDefinition;

public class NewTeamProjectController {

    final private IMainWindow mw;
    private NewTeamProject dialog;
    private RepoTypeWorker repoTypeWorker = null;
    private String repoType;
    private boolean ok;

    public NewTeamProjectController(IMainWindow mainWindow) {
        mw = mainWindow;
    }

    public File show() {
        ok = false;
        initComponents();

        if (!ok) {
            Core.getMainWindow().showStatusMessageRB("TEAM_CANCELLED");
            return null;
        }

        return new File(getSaveLocation());
    }

    public RepositoryDefinition getRepo() {
        RepositoryDefinition repo = new RepositoryDefinition();
        repo.setType(getRepoType());
        repo.setUrl(getRepoUrl());
        if (isCustomBranch()) {
            repo.setBranch(getBranchName());
        }
        return repo;
    }

    private void initComponents() {
        dialog = new NewTeamProject(mw.getApplicationFrame());
        dialog.defaultBranchRB.setSelected(true);
        dialog.txtBranchName.setEnabled(false);
        // Add action listeners
        dialog.defaultBranchRB.addActionListener(this::branchRBActionPerformed);
        dialog.customBranchRB.addActionListener(this::branchRBActionPerformed);
        dialog.customBranchRB.setEnabled(false);
        dialog.btnOk.addActionListener(this::btnOkActionPerformed);
        dialog.btnCancel.addActionListener(this::btnCancelActionPerformed);
        dialog.btnDirectory.addActionListener(this::btnDirectoryActionPerformed);
        dialog.txtRepositoryOrProjectFileURL.addActionListener(this::txtProjectFileURLActionPerformed);
        dialog.txtRepositoryOrProjectFileURL.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                txtProjectFileURLFocusLost(evt);
            }
        });
        dialog.txtRepositoryOrProjectFileURL.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearRepo();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                clearRepo();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                clearRepo();
            }
        });
        dialog.txtRepositoryOrProjectFileURL.addActionListener(e -> dialog.btnOk.doClick());
        dialog.txtDirectory.addActionListener(this::txtDirectoryActionPerformed);
        dialog.txtBranchName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                txtBranchNameFocusLost(evt);
            }
        });
        dialog.txtDirectory.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                txtDirectoryFocusLost(evt);
            }
        });
        dialog.txtDirectory.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDialog();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDialog();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDialog();
            }
        });
        dialog.txtDirectory.addActionListener(e -> dialog.btnOk.doClick());
        dialog.setVisible(true);
    }

    public String getRepoType() {
        return repoType;
    }

    public String getRepoUrl() {
        String url = dialog.txtRepositoryOrProjectFileURL.getText().trim();
        if (url.startsWith("git!")) {
            return url.substring("git!".length());
        } else if (url.startsWith("svn!")) {
            return url.substring("svn!".length());
        } else {
            return url;
        }
    }

    public String getSaveLocation() {
        return dialog.txtDirectory.getText().trim();
    }

    public boolean isCustomBranch() {
        return dialog.customBranchRB.isSelected();
    }

    public String getBranchName() {
        return dialog.txtBranchName.getText();
    }

    private synchronized void detectRepoOrFile() {
        if (repoType != null || isDetectingRepo()) {
            return;
        }
        String url = dialog.txtRepositoryOrProjectFileURL.getText().trim();
        if (StringUtil.isEmpty(url)) {
            return;
        }
        if (url.startsWith("git!")) {
            dialog.detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_GIT"));
            repoType = "git";
            suggestLocalFolder();
        } else if (url.startsWith("svn!")) {
            dialog.detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_SVN"));
            repoType = "svn";
            suggestLocalFolder();
        } else {
            dialog.detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTING_REPO_OR_PROJECT_FILE"));
            repoTypeWorker = new RepoTypeWorker(url);
            repoTypeWorker.execute();
        }
    }

    private synchronized boolean isDetectingRepo() {
        return repoTypeWorker != null && !repoTypeWorker.isDone();
    }

    private static String getMessageForRepoType(String type) {
        if ("svn".equals(type)) {
            return OStrings.getString("TEAM_DETECTED_REPO_SVN");
        } else if ("git".equals(type)) {
            return OStrings.getString("TEAM_DETECTED_REPO_GIT");
        } else if ("project-file".equals(type)) {
            return OStrings.getString("TEAM_DETECTED_PROJECT_FILE");
        } else {
            return OStrings.getString("TEAM_DETECTED_REPO_UNKNOWN");
        }
    }

    private void suggestLocalFolder() {
        if (!getSaveLocation().isEmpty()) {
            return;
        }
        String url = dialog.txtRepositoryOrProjectFileURL.getText().trim();
        String strippedUrl = StringUtil.stripFromEnd(url, ".git", "/", "trunk", "/", "svn");
        String dir = Preferences.getPreferenceDefault(Preferences.CURRENT_FOLDER, System.getProperty("user.home"));
        File suggestion = new File(dir, new File(strippedUrl).getName()).getAbsoluteFile();
        dialog.txtDirectory.setText(ensureUniquePath(suggestion).getPath());
    }

    private static File ensureUniquePath(File path) {
        File result = path;
        int suff = 2;
        while (result.exists()) {
            result = new File(path.getPath() + suff);
            suff++;
            if (suff > 1000) {
                // Give up after 1000
                break;
            }
        }
        return result;
    }

    private void clearRepo() {
        repoType = null;
        dialog.detectedRepoOrProjectFileLabel.setText(" ");
        if (repoTypeWorker != null) {
            repoTypeWorker.cancel(true);
        }
        updateDialog();
    }

    private class RepoTypeWorker extends SwingWorker<String, Void> {

        private final String url;

        RepoTypeWorker(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground() throws Exception {
            if ((url.startsWith("http://") || url.startsWith("https://")) && url.endsWith("/omegat.project")) {
                return detectProjectFile();
            }
            return RemoteRepositoryFactory.detectRepositoryType(url);
        }

        protected String detectProjectFile() throws Exception {
            byte[] file = HttpConnectionUtils.getURLasByteArray(url);
            ProjectFileStorage.parseProjectFile(file);
            return "project-file";
        }

        @Override
        protected void done() {
            String type, resultText;
            try {
                type = get();
                resultText = getMessageForRepoType(type);
            } catch (CancellationException ex) {
                type = null;
                resultText = " ";
            } catch (Throwable ex) {
                type = null;
                // Error strings are project-file-specific because
                // RemoteRepositoryFactory.detectRepositoryType() doesn't throw
                // exceptions, so any thrown must be from detectProjectFile().
                resultText = OStrings.getString("TEAM_ERROR_DETECTING_PROJECT_FILE");
                Log.logErrorRB(ex, "TEAM_ERROR_DETECTING_PROJECT_FILE");
            }
            dialog.detectedRepoOrProjectFileLabel.setText(resultText);
            if (type != null) {
                suggestLocalFolder();
            }
            repoType = type;
            dialog.customBranchRB.setEnabled(repoType != null && repoType.equals("git"));
            updateDialog();
        }
    }

    void updateDialog() {
        String dir = getSaveLocation();
        boolean dirOK = !dir.isEmpty() && !new File(dir).exists();
        boolean typeDetected = repoType != null;
        boolean branchSpecified = !StringUtil.isEmpty(getBranchName()) || !isCustomBranch();
        dialog.btnOk.setEnabled(dirOK && typeDetected && branchSpecified);
    }

    private void btnCancelActionPerformed(ActionEvent evt) {
        if (repoTypeWorker != null) {
            repoTypeWorker.cancel(true);
        }
        ok = false;
        dialog.dispose();
    }

    private void btnOkActionPerformed(ActionEvent evt) {
        ok = true;
        dialog.dispose();
    }

    private void btnDirectoryActionPerformed(ActionEvent evt) {
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        String saveDir = getSaveLocation();
        if (!saveDir.isEmpty()) {
            ndc.setSelectedFile(new File(saveDir));
        }
        int ndcResult = ndc.showSaveDialog(dialog);
        if (ndcResult == OmegaTFileChooser.APPROVE_OPTION) {
            dialog.txtDirectory.setText(ndc.getSelectedFile().getPath());
        }
        updateDialog();
    }

    private void txtProjectFileURLActionPerformed(ActionEvent evt) {
        detectRepoOrFile();
    }

    private void txtProjectFileURLFocusLost(FocusEvent evt) {
        detectRepoOrFile();
    }

    private void txtDirectoryFocusLost(FocusEvent evt) {
        updateDialog();
    }

    private void txtDirectoryActionPerformed(ActionEvent evt) {
        updateDialog();
    }

    private void txtBranchNameFocusLost(FocusEvent evt) {
        updateDialog();
    }

    private void branchRBActionPerformed(ActionEvent evt) {
        dialog.txtBranchName.setEnabled(dialog.customBranchRB.isSelected());
        updateDialog();
    }
}
