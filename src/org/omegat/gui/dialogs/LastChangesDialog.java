/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Didier Briel
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.commons.io.IOUtils;
import org.omegat.help.Help;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

/**
 * Dialog showing Last changes.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class LastChangesDialog extends JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form LastChanges */
    public LastChangesDialog(Frame parent) {
        super(parent, true);
        initComponents();
        lastChangesTextPane.setCaretPosition(0);
        setLocationRelativeTo(parent);
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
        setTitle(OStrings.getString("LASTCHANGESDIALOG_TITLE"));
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                doClose(RET_CANCEL);
            }
        });

        StaticUIUtils.setEscapeClosable(this);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(this::okButtonActionPerformed);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        lastChangesTextPane.setEditable(false);
        lastChangesTextPane.setText(loadLastChangesText());
        scroll.setViewportView(lastChangesTextPane);

        getContentPane().add(scroll, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
    }

    private String loadLastChangesText() {
        URI helpFileUri = Help.getHelpFileURI(OConsts.LAST_CHANGES_FILE);
        if (helpFileUri == null) {
            Log.logWarningRB("LASTCHANGESDIALOG_ERROR");
            return Help.errorHaiku();
        }

        try {
            return IOUtils.toString(helpFileUri, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.logErrorRB(ex, "LASTCHANGESDIALOG_ERROR");
            return Help.errorHaiku();
        }
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(RET_OK);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    private final JPanel buttonPanel = new JPanel();
    private final JScrollPane scroll = new JScrollPane();
    private final JTextPane lastChangesTextPane = new JTextPane();
    private final JButton okButton = new JButton();

    private int returnStatus = RET_CANCEL;
}
