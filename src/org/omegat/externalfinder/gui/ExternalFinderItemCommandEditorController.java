/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.externalfinder.gui;

import java.awt.Color;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.externalfinder.item.ExternalFinderItem;
import org.omegat.externalfinder.item.ExternalFinderItemCommand;
import org.omegat.externalfinder.item.ExternalFinderValidationException;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Editor GUI for a single {@link ExternalFinderItemCommand}.
 *
 * @author Aaron Madlon-Kay
 */
public class ExternalFinderItemCommandEditorController {

    private final ExternalFinderSubItemEditorPanel panel;
    private final ExternalFinderItemCommand.Builder builder;
    private boolean userDidConfirm;

    public ExternalFinderItemCommandEditorController() {
        this(new ExternalFinderItemCommand.Builder().setCommand(""));
    }

    public ExternalFinderItemCommandEditorController(ExternalFinderItemCommand item) {
        this(ExternalFinderItemCommand.Builder.from(item));
    }

    public ExternalFinderItemCommandEditorController(ExternalFinderItemCommand.Builder builder) {
        this.builder = builder;
        this.panel = new ExternalFinderSubItemEditorPanel();
    }

    public boolean show(Window parent) {
        JDialog dialog = new JDialog(parent, OStrings.getString("EXTERNALFINDER_COMMANDEDITOR_TITLE"));
        dialog.setModal(true);
        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setWindowIcon(dialog);
        StaticUIUtils.setEscapeClosable(dialog);

        panel.urlPanel.setVisible(false);

        panel.commandTextArea.setText(builder.getCommand());
        panel.commandTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                builder.setCommand(panel.commandTextArea.getText().trim());
                validate();
            }
        });

        panel.delimiterTextField.setText(builder.getDelimiter());
        panel.delimiterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                builder.setDelimiter(panel.delimiterTextField.getText());
                validate();
            }
        });

        panel.targetComboBox.setModel(new DefaultComboBoxModel<>(ExternalFinderItem.TARGET.values()));
        panel.targetComboBox.setSelectedItem(builder.getTarget());
        panel.targetComboBox.addActionListener(e -> {
            builder.setTarget((ExternalFinderItem.TARGET) panel.targetComboBox.getSelectedItem());
            validate();
        });

        panel.encodingComboBox.setModel(new DefaultComboBoxModel<>(ExternalFinderItem.ENCODING.values()));
        panel.encodingComboBox.setSelectedItem(builder.getEncoding());
        panel.encodingComboBox.addActionListener(e -> {
            builder.setEncoding((ExternalFinderItem.ENCODING) panel.encodingComboBox.getSelectedItem());
            validate();
        });

        panel.okButton.addActionListener(e -> {
            if (validate()) {
                userDidConfirm = true;
                StaticUIUtils.closeWindowByEvent(dialog);
            }
        });
        dialog.getRootPane().setDefaultButton(panel.okButton);

        panel.cancelButton.addActionListener(e -> {
            userDidConfirm = false;
            StaticUIUtils.closeWindowByEvent(dialog);
        });

        panel.testButton.addActionListener(e -> {
            try {
                Runtime.getRuntime().exec(builder.generateSampleCommand());
            } catch (Exception ex) {
                Logger.getLogger(ExternalFinderItemCommandEditorController.class.getName()).log(Level.SEVERE,
                        null, ex);
                JOptionPane.showMessageDialog(dialog, ex.getLocalizedMessage(),
                        OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        });

        validate();

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }

    public ExternalFinderItemCommand getResult() {
        return builder.build();
    }

    private boolean validate() {
        boolean isValid = true;
        String sampleOutput = null;
        try {
            String[] args = builder.validate();
            sampleOutput = String.join("\u2423", args);
        } catch (ExternalFinderValidationException e) {
            isValid = false;
            sampleOutput = e.getLocalizedMessage();
        }
        panel.okButton.setEnabled(isValid);
        panel.testButton.setEnabled(isValid);
        panel.sampleOutputTextArea.setText(sampleOutput);
        panel.sampleOutputTextArea.setForeground(isValid ? Color.BLACK : Color.RED);
        return isValid;
    }
}
