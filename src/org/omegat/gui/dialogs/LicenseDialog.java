/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik, Didier Briel
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.omegat.help.Help;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

/**
 * Dialog showing GNU Public License.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class LicenseDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form LicenseDialog */
    public LicenseDialog(java.awt.Dialog parent) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        licenseTextPane.setCaretPosition(0);
    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        licenseTextPane = new javax.swing.JTextPane();

        setTitle(OStrings.getString("LICENSEDIALOG_TITLE"));
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        StaticUIUtils.setEscapeClosable(this);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        licenseTextPane.setEditable(false);
        StringBuilder sb = new StringBuilder("===================================================\n\n");
        sb.append(OStrings.getString("LICENSEDIALOG_PREFACE"));
        sb.append("\n\n===================================================\n\n");
        try {
            String text = IOUtils.toString(Help.getHelpFileURI(OConsts.LICENSE_FILE), StandardCharsets.UTF_8);
            sb.append(text);
        } catch (NullPointerException | IOException ex) {
            sb.append(Help.errorHaiku());
        }
        licenseTextPane.setText(sb.toString());
        scroll.setViewportView(licenseTextPane);

        getContentPane().add(scroll, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(RET_OK);
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    private javax.swing.JPanel buttonPanel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTextPane licenseTextPane;
    private javax.swing.JButton okButton;

    private int returnStatus = RET_CANCEL;
}
