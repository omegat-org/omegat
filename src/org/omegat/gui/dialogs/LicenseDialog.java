/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.omegat.gui.help.HelpFrame;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Dialog showing GNU Public License.
 * 
 * @author Maxym Mykhalchuk
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
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        // END HP

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
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
                        + loadLicense());
        scroll.setViewportView(licenseTextPane);

        getContentPane().add(scroll, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
    }

    /**
     * Load license from file "license.txt" from help. This method doesn't check
     * help version, but just load license.txt using current locale..
     */
    private String loadLicense() {
        // Get the system locale (language and country)
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.ENGLISH);
        String country = Locale.getDefault().getCountry().toUpperCase(Locale.ENGLISH);

        // Check if there's a translation for the full locale (lang + country)
        URL url = HelpFrame.getHelpFileURL(language + "_" + country, OConsts.LICENSE_FILE);
        if (url == null) {
            url = HelpFrame.getHelpFileURL(language, OConsts.LICENSE_FILE);
            if (url == null) {
                url = HelpFrame.getHelpFileURL("en", OConsts.LICENSE_FILE);
            }
        }
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
