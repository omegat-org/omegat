/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichick
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

package org.omegat.gui.dialogs;

import java.io.File;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.Core;
import org.omegat.core.team.GITRemoteRepository;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.IRemoteRepository.Credentials;
import org.omegat.core.team.RepositoryUtils.RepoTypeDetector;
import org.omegat.core.team.SVNRemoteRepository;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.StaticUIUtils;

/**
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class NewTeamProject extends javax.swing.JDialog {

    
    public Class<? extends IRemoteRepository> repoType = null;
    public Credentials credentials = null;
    private RepoTypeWorker repoTypeWorker = null;
    private boolean detecting = false;
    
    /**
     * Creates new form NewTeamProject
     */
    public NewTeamProject(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        txtRepositoryURL.getDocument().addDocumentListener(new DocumentListener() {
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
        
        StaticUIUtils.setEscapeClosable(this);
    }
    
    private void suggestSaveDirectory(String repoName) {
        if (repoName == null || !txtDirectory.getText().isEmpty()) {
            return;
        }
        String dir = Preferences.getPreferenceDefault(Preferences.CURRENT_FOLDER,
                System.getProperty("user.home"));
        File suggestion = new File(dir, repoName);
        int suff = 1;
        while (suggestion.exists()) {
            suggestion = new File(dir, repoName + "-" + suff++);
        }
        txtDirectory.setText(suggestion.getAbsolutePath());
    }
    
    private synchronized void detectRepo() {
        if (detecting || !isVisible()) {
            return;
        }
        String url = txtRepositoryURL.getText().trim();
        if (StringUtil.isEmpty(url)) {
            return;
        }
        if (url.startsWith("git!")) {
            txtRepositoryURL.setText(url.substring("git!".length()));
            detectedRepoLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_GIT"));
            repoType = GITRemoteRepository.class;
        } else if (url.startsWith("svn!")) {
            txtRepositoryURL.setText(url.substring("svn!".length()));
            detectedRepoLabel.setText(OStrings.getString("TEAM_DETECTED_REPO_SVN"));
            repoType = SVNRemoteRepository.class;
        } else {
            repoTypeWorker = new RepoTypeWorker(url);
            repoTypeWorker.execute();
        }
    }
    
    private synchronized void startDetectingRepo() {
        detecting = true;
    }
    
    private synchronized void stopDetectingRepo() {
        detecting = false;
    }
    
    private void clearRepo() {
        repoType = null;
        detectedRepoLabel.setText(" ");
        if (repoTypeWorker != null) {
            repoTypeWorker.cancel(true);
        }
        updateDialog();
    }
    
    
    private class RepoTypeWorker extends SwingWorker<RepoTypeDetector, Object> {

        private String url = null;

        public RepoTypeWorker(String url) {
            this.url = url;
        }
        
        @Override
        protected RepoTypeDetector doInBackground() throws Exception {
            startDetectingRepo();
            detectedRepoLabel.setText(OStrings.getString("TEAM_DETECTING_REPO"));
            RepoTypeDetector detector = new RepoTypeDetector(url, credentials);
            detector.execute();
            return detector;
        }

        @Override
        protected void done() {
            String resultText = OStrings.getString("TEAM_DETECTED_REPO_UNKNOWN");
            String repoName = null;
            try {
                RepoTypeDetector detector = get();
                repoType = detector.repoType;
                credentials = detector.credentials;
                if (repoType != null) {
                    if (repoType.equals(GITRemoteRepository.class)) {
                        resultText = OStrings.getString("TEAM_DETECTED_REPO_GIT");
                        repoName = GITRemoteRepository.guessRepoName(url);
                    } else if (repoType.equals(SVNRemoteRepository.class)) {
                        resultText = OStrings.getString("TEAM_DETECTED_REPO_SVN");
                        repoName = SVNRemoteRepository.guessRepoName(url);
                    }
                }
            } catch (CancellationException ex) {
                resultText = " ";
            } catch (Exception ex) {
                resultText = OStrings.getString("TEAM_ERROR_DETECTING_REPO");
                Log.logErrorRB(ex, "TEAM_ERROR_DETECTING_REPO");
            }
            detectedRepoLabel.setText(resultText);
            suggestSaveDirectory(repoName);
            updateDialog();
            stopDetectingRepo();
        }
    };

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        urlLabel = new javax.swing.JLabel();
        txtRepositoryURL = new javax.swing.JTextField();
        detectedRepoLabel = new javax.swing.JLabel();
        localFolderLabel = new javax.swing.JLabel();
        txtDirectory = new javax.swing.JTextField();
        btnDirectory = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("TEAM_NEW_HEADER")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, OStrings.getString("TEAM_NEW_REPOSITORY_URL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(urlLabel, gridBagConstraints);

        txtRepositoryURL.setColumns(40);
        txtRepositoryURL.setToolTipText("");
        txtRepositoryURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRepositoryURLFocusLost(evt);
            }
        });
        txtRepositoryURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRepositoryURLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txtRepositoryURL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(detectedRepoLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(detectedRepoLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(localFolderLabel, OStrings.getString("TEAM_NEW_DIRECTORY")); // NOI18N
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateDialog() {
        String repoUrl = txtRepositoryURL.getText();
        if (repoUrl.trim().equals(" ")) {
            detectedRepoLabel.setText("");
        }
        boolean enabled = repoType != null
                && !StringUtil.isEmpty(repoUrl)
                && !StringUtil.isEmpty(txtDirectory.getText());
        btnOk.setEnabled(enabled);
    }
    
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        repoType = null;
        if (credentials != null) {
            credentials.clear();
            credentials = null;
        }
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
        String saveDir = txtDirectory.getText();
        if (!saveDir.isEmpty()) {
            ndc.setSelectedFile(new File(saveDir));
        }
        int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
        if (ndcResult == OmegaTFileChooser.APPROVE_OPTION) {
            txtDirectory.setText(ndc.getSelectedFile().getPath());
        }
        updateDialog();
    }//GEN-LAST:event_btnDirectoryActionPerformed

    private void txtRepositoryURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRepositoryURLActionPerformed
        detectRepo();
    }//GEN-LAST:event_txtRepositoryURLActionPerformed

    private void txtRepositoryURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRepositoryURLFocusLost
        detectRepo();
    }//GEN-LAST:event_txtRepositoryURLFocusLost

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
    private javax.swing.JLabel detectedRepoLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel localFolderLabel;
    public javax.swing.JTextField txtDirectory;
    public javax.swing.JTextField txtRepositoryURL;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

    public boolean ok;
}
