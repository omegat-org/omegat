/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
               2012 Alex Buloichick, Didier Briel
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 *
 * @author alex
 */
@SuppressWarnings("serial")
public class UserPassDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /**
     * Creates new form TeamUserPassDialog
     */
    public UserPassDialog(java.awt.Frame parent) {
        super(parent, true);

        StaticUIUtils.setEscapeClosable(this);

        initComponents();
        getRootPane().setDefaultButton(okButton);
        setActions();

        invalidate();
        pack();
        setLocationRelativeTo(parent);

        usernameField.requestFocusInWindow();
    }

    abstract static class CheckDocumentAction implements DocumentListener {

        @Override
        public void insertUpdate(final DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            update();
        }

        abstract void update();
    }

    private void setActions() {
        usernameField.getDocument().addDocumentListener(new CheckDocumentAction() {
            @Override
            protected void update() {
                SwingUtilities.invokeLater(() -> checkCredentials());
            }
        });
        passwordField.getDocument().addDocumentListener(new CheckDocumentAction() {
            @Override
            protected void update() {
                SwingUtilities.invokeLater(() -> checkCredentials());
            }
        });

        toggleButton.addActionListener(e -> {
            // Toggle between showing/hiding the password
            if (toggleButton.isSelected()) {
                passwordField.setEchoChar((char) 0);
                toggleButton.setIcon(new ImageIcon(
                        Objects.requireNonNull(getClass().getResource("/org/omegat/gui/resources/eye.png")))); // NOI18N
            } else {
                passwordField.setEchoChar('●');
                toggleButton.setIcon(new ImageIcon(Objects
                        .requireNonNull(getClass().getResource("/org/omegat/gui/resources/eye-slash.png")))); // NOI18N
            }
        });
    }

    private void checkCredentials() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();
        boolean isUsernameValid = !username.startsWith(" ") && !username.endsWith(" ");
        if (!isUsernameValid) {
            usernamePanel.setBackground(Color.YELLOW);
            messageArea.setText(OStrings.getString("TEAM_USERPASS_EXTRA_SPACE"));
            okButton.setEnabled(false);
            return;
        } else {
            usernamePanel.setBackground(mainPanel.getBackground());
        }

        // check if the username or password is empty
        if (username.trim().isEmpty() || password.length == 0) {
            passwordPanel.setBackground(mainPanel.getBackground());
            messageArea.setText("");
            okButton.setEnabled(false);
            return;
        }

        // check if the password has a preceding or trailing space character(s)
        boolean isPasswordValid = password[0] != ' ' && password[password.length - 1] != ' ';
        if (!isPasswordValid) {
            passwordPanel.setBackground(Color.YELLOW);
            messageArea.setText(OStrings.getString("TEAM_USERPASS_EXTRA_SPACE"));
            okButton.setEnabled(false);
            return;
        } else {
            passwordPanel.setBackground(mainPanel.getBackground());
        }

        // everything is ok.
        messageArea.setText("");
        okButton.setEnabled(true);
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
        GridBagConstraints gridBagConstraints;

        JPanel northPanel = new JPanel();
        mainPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel southPanel = new JPanel();
        descriptionTextArea = new JTextArea();
        JLabel usernameLabel = new JLabel();
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(352, 30));
        usernamePanel = new JPanel();
        JLabel passwordLabel = new JLabel();
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(320, 30));
        passwordPanel = new JPanel();
        toggleButton = new JToggleButton();
        perHostCheckBox = new JCheckBox();
        messageArea = new JTextArea();
        okButton = new JButton();
        JButton cancelButton = new JButton();
        Icon eyeSlash = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/org/omegat/gui/resources/eye-slash.png")));
        toggleButton.setIcon(eyeSlash);
        toggleButton.setDisabledIcon(eyeSlash);
        toggleButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        toggleButton.setBorderPainted(false);
        toggleButton.setBorder(null);

        setTitle(OStrings.getString("TEAM_USERPASS_TITLE")); // NOI18N
        setMinimumSize(new Dimension(450, 200));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        northPanel.setLayout(new BorderLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setFont(new JLabel().getFont());
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setOpaque(false);
        northPanel.add(descriptionTextArea, BorderLayout.CENTER);

        getContentPane().add(northPanel, BorderLayout.NORTH);

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, OStrings.getString("LOGIN_USER")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        mainPanel.add(usernameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel, gridBagConstraints);

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, OStrings.getString("LOGIN_PASSWORD")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        mainPanel.add(passwordLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        passwordPanel.add(passwordField);
        passwordPanel.add(toggleButton);
        mainPanel.add(passwordPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        mainPanel.add(perHostCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 16, 4, 4);
        messageArea.setBackground(mainPanel.getBackground());
        mainPanel.add(messageArea, gridBagConstraints);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        southPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southPanel.setLayout(new BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(this::okButtonActionPerformed);
        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonPanel.add(cancelButton);

        southPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(southPanel, BorderLayout.SOUTH);

        pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        doClose(RET_OK);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    private void formWindowClosing(WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    private JPanel mainPanel;
    private JButton okButton;
    private JTextArea descriptionTextArea;
    private JTextField usernameField;
    private JPanel usernamePanel;
    private JPasswordField passwordField;
    private JPanel passwordPanel;
    private JCheckBox perHostCheckBox;
    private JTextArea messageArea;
    private JToggleButton toggleButton;

    private int returnStatus = RET_CANCEL;

    public void setUsername(String txt) {
        usernameField.setText(txt);
    }

    public void enableUsernameField(boolean enabled) {
        usernameField.setEditable(enabled);
        usernameField.setEnabled(enabled);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public boolean isPerHost() {
        return perHostCheckBox.isSelected();
    }

    public void setDescription(String description) {
        descriptionTextArea.setText(description);
    }

    public void setPerHostCheckBoxText(String text) {
        org.openide.awt.Mnemonics.setLocalizedText(perHostCheckBox, text);
    }
}
