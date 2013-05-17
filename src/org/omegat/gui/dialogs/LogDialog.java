/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Didier Briel
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Dialog showing the log.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class LogDialog extends JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form LastChanges */
    public LogDialog(Frame parent) {
        super(parent, true);
        initComponents();
        logTextPane.setCaretPosition(0);
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
        logTextPane = new javax.swing.JTextPane();

        setTitle(OStrings.getString("LOGDIALOG_TITLE"));
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
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
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        logTextPane.setEditable(false);
                
        File logLocation = new File (Log.getLogFilePath());
        try {
            setTitle(OStrings.getString("LOGDIALOG_TITLE") + " "+ Log.getLogFileName());
            logTextPane.setText(FileUtil.readTextFile(logLocation));
        } catch (Exception e) {
            // We'll get an empty page
        }
        
        scroll.setViewportView(logTextPane);

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
    private javax.swing.JTextPane logTextPane;
    private javax.swing.JButton okButton;

    private int returnStatus = RET_CANCEL;
}
