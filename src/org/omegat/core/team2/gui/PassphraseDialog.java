/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
               2012 Alex Buloichick, Didier Briel
               2023 Hiroshi Miura
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

package org.omegat.core.team2.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class PassphraseDialog extends JDialog {

    private JTextPane titleText;
    private JTextArea descriptionText;
    private JPasswordField passwordField;
    private boolean returnStatus = false;

    /**
     * Creates PassphraseDialog.
     */
    public PassphraseDialog(Frame parent) {
        super(parent, true);
        StaticUIUtils.setEscapeClosable(this);
        initComponents();
        setLocationRelativeTo(parent);
    }

    /**
     * @return true when OK, otherwise false.
     */
    public boolean getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        titleText = new JTextPane();
        descriptionText = new JTextArea();
        passwordField = new JPasswordField();

        Font font = Core.getMainWindow().getApplicationFont();
        setFont(font);
        setTitle(OStrings.getString("TEAM_USERPASS_TITLE")); // NOI18N
        setMinimumSize(new java.awt.Dimension(500, 250));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.setLayout(new GridBagLayout());

        titleText.setEditable(false);
        titleText.setFont(font);
        titleText.setOpaque(false);
        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        mainPanel.add(titleText, gridBagConstraints);

        JLabel passwordLabel = new JLabel();
        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, OStrings.getString("LOGIN_PASSWORD")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        mainPanel.add(passwordLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        mainPanel.add(passwordField, gridBagConstraints);
        descriptionText.setEditable(false);
        descriptionText.setFont(font);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        mainPanel.add(descriptionText, gridBagConstraints);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(this::okButtonActionPerformed);
        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        invalidate();
        pack();
    }

    public void setTitleDesc(String title) {
        titleText.setText(title);
    }

    public void setDescription(String desc) {
        descriptionText.setText(desc);
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(true);
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(false);
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        doClose(false);
    }

    private void doClose(boolean retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
}
