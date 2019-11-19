/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichick
               2014 Aaron Madlon-Kay
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

package org.omegat.gui.dialogs;

import java.io.File;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.team2.RemoteRepositoryFactory;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StringUtil;
import org.omegat.util.WikiGet;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;

/**
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class NewTeamProject extends javax.swing.JDialog {

    private RepoTypeWorker repoTypeWorker = null;
    private String repoType;

    /**
     * Creates new form NewTeamProject
     */
    public NewTeamProject(java.awt.Frame parent) {
        super(parent, true);
        initComponents();

        txtRepositoryOrProjectFileURL.getDocument().addDocumentListener(new DocumentListener() {
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
        txtRepositoryOrProjectFileURL.addActionListener(e -> btnOk.doClick());
        txtDirectory.getDocument().addDocumentListener(new DocumentListener() {
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
        txtDirectory.addActionListener(e -> btnOk.doClick());

        StaticUIUtils.setEscapeClosable(this);
        getRootPane().setDefaultButton(btnOk);
        setLocationRelativeTo(parent);
    }

    public String getRepoType() {
        return repoType;
    }

    public String getRepoUrl() {
        String url = txtRepositoryOrProjectFileURL.getText().trim();
        if (url.startsWith("git!")) {
            return url.substring("git!".length());
        } else if (url.startsWith("svn!")) {
            return url.substring("svn!".length());
        } else {
            return url;
        }
    }

    public String getSaveLocation() {
        return txtDirectory.getText().trim();
    }

    private synchronized void detectRepoOrFile() {
        if (repoType != null || isDetectingRepo()) {
            return;
        }
        String url = txtRepositoryOrProjectFileURL.getText().trim();
        if (StringUtil.isEmpty(url)) {
            return;
        }
        if (url.startsWith("git!")) {
            detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_GIT"));
            repoType = "git";
            suggestLocalFolder();
        } else if (url.startsWith("svn!")) {
            detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_SVN"));
            repoType = "svn";
            suggestLocalFolder();
        } else {
            detectedRepoOrProjectFileLabel.setText(OStrings.getString("TEAM_DETECTING_REPO_OR_PROJECT_FILE"));
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
        String url = txtRepositoryOrProjectFileURL.getText().trim();
        String strippedUrl = StringUtil.stripFromEnd(url, ".git", "/", "trunk", "/", "svn");
        String dir = Preferences.getPreferenceDefault(Preferences.CURRENT_FOLDER, System.getProperty("user.home"));
        File suggestion = new File(dir, new File(strippedUrl).getName()).getAbsoluteFile();
        txtDirectory.setText(ensureUniquePath(suggestion).getPath());
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
        detectedRepoOrProjectFileLabel.setText(" ");
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
            byte[] file = WikiGet.getURLasByteArray(url);
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
            detectedRepoOrProjectFileLabel.setText(resultText);
            if (type != null) {
                suggestLocalFolder();
            }
            repoType = type;
            updateDialog();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        urlLabel = new javax.swing.JLabel();
        txtRepositoryOrProjectFileURL = new javax.swing.JTextField();
        detectedRepoOrProjectFileLabel = new javax.swing.JLabel();
        localFolderLabel = new javax.swing.JLabel();
        txtDirectory = new javax.swing.JTextField();
        btnDirectory = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("TEAM_NEW_HEADER")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, OStrings.getString("TEAM_NEW_PROJECT_URL_OR_FILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(urlLabel, gridBagConstraints);

        txtRepositoryOrProjectFileURL.setColumns(40);
        txtRepositoryOrProjectFileURL.setToolTipText("");
        txtRepositoryOrProjectFileURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtProjectFileURLFocusLost(evt);
            }
        });
        txtRepositoryOrProjectFileURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProjectFileURLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txtRepositoryOrProjectFileURL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(detectedRepoOrProjectFileLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(detectedRepoOrProjectFileLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(localFolderLabel, OStrings.getString("TEAM_NEW_PROJECT_DIRECTORY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(localFolderLabel, gridBagConstraints);

        txtDirectory.setToolTipText("");
        txtDirectory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDirectoryFocusLost(evt);
            }
        });
        txtDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(txtDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnDirectory, "...");
        btnDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        getContentPane().add(btnDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnOk, OStrings.getString("BUTTON_OK")); // NOI18N
        btnOk.setEnabled(false);
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });
        jPanel2.add(btnOk);

        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        jPanel2.add(btnCancel);
        btnCancel.getAccessibleContext().setAccessibleDescription("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateDialog() {
        String dir = getSaveLocation();
        boolean dirOK = !dir.isEmpty() && !new File(dir).exists();
        boolean typeDetected = repoType != null;
        btnOk.setEnabled(dirOK && typeDetected);
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (repoTypeWorker != null) {
            repoTypeWorker.cancel(true);
        }
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        dispose();
        ok = true;
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDirectoryActionPerformed
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        String saveDir = getSaveLocation();
        if (!saveDir.isEmpty()) {
            ndc.setSelectedFile(new File(saveDir));
        }
        int ndcResult = ndc.showSaveDialog(this);
        if (ndcResult == OmegaTFileChooser.APPROVE_OPTION) {
            txtDirectory.setText(ndc.getSelectedFile().getPath());
        }
        updateDialog();
    }//GEN-LAST:event_btnDirectoryActionPerformed

    private void txtProjectFileURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectFileURLActionPerformed
        detectRepoOrFile();
    }//GEN-LAST:event_txtProjectFileURLActionPerformed

    private void txtProjectFileURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtProjectFileURLFocusLost
        detectRepoOrFile();
    }//GEN-LAST:event_txtProjectFileURLFocusLost

    private void txtDirectoryFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDirectoryFocusLost
        updateDialog();
    }//GEN-LAST:event_txtDirectoryFocusLost

    private void txtDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDirectoryActionPerformed
        updateDialog();
    }//GEN-LAST:event_txtDirectoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDirectory;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel detectedRepoOrProjectFileLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel localFolderLabel;
    public javax.swing.JTextField txtDirectory;
    public javax.swing.JTextField txtRepositoryOrProjectFileURL;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

    public boolean ok;
}
