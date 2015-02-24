/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik, Didier Briel
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;


import org.omegat.gui.help.HelpFrame;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.LFileCopy;
import org.openide.awt.Mnemonics;
import org.omegat.util.FileUtil;
import org.omegat.util.gui.DockingUI;
import org.omegat.util.gui.StaticUIUtils;

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
        licenseTextPane
                .setText("===================================================\n\n"
                        + OStrings.getString("LICENSEDIALOG_PREFACE")
                        + "\n\n===================================================\n\n"
                        + FileUtil.loadTextFileFromDoc(OConsts.LICENSE_FILE));
        scroll.setViewportView(licenseTextPane);

        getContentPane().add(scroll, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
        DockingUI.displayCentered(this);
    }

    /**
     * Load license from file "license.txt" from the root of help.
     */
    private String loadLicense() {

        // Get the license
        URL url = HelpFrame.getHelpFileURL(null, OConsts.LICENSE_FILE);
        if (url == null) {
            return HelpFrame.errorHaiku();
        }

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(url.openStream(), OConsts.UTF8));
            try {
                StringWriter out = new StringWriter();
                LFileCopy.copy(rd, out);
                return out.toString();
            } finally {
                rd.close();
            }
        } catch (IOException ex) {
            return HelpFrame.errorHaiku();
        }

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
