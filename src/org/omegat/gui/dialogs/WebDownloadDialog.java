/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

public class WebDownloadDialog extends JDialog {

    static final int RET_CANCEL = WebDownloadDialogController.RET_CANCEL;
    static final int RET_OK = WebDownloadDialogController.RET_OK;
    int returnStatus;
    JTextField urlInputTextField;

    public WebDownloadDialog(Frame frame) {
        super(frame, true);
        StaticUIUtils.setEscapeClosable(this);
        initComponent();
        returnStatus = RET_CANCEL;
    }

    private void initComponent() {
        setTitle(OStrings.getString("TF_WIKI_IMPORT_TITLE"));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });

        java.awt.GridBagConstraints gridBagConstraints;
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new GridBagLayout());

        JLabel urlInputLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(urlInputLabel, OStrings.getString("TF_WIKI_IMPORT_PROMPT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        jPanel2.add(urlInputLabel, gridBagConstraints);
        urlInputTextField = new JTextField();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(urlInputTextField, gridBagConstraints);

        JButton okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(okButton, gridBagConstraints);
        okButton.addActionListener(this::okButtonActionPerformed);

        JButton cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(cancelButton, gridBagConstraints);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        getContentPane().add(jPanel2);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        doClose(RET_OK);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    private void closeDialog(WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

}
