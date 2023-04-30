/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.gui.filters2;

import java.awt.Dialog;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.openide.awt.Mnemonics;

/**
 * Editor for a single instance of the filter. E.g. HTML filter may have two
 * instances -- one for .html and other for .htm files.
 *
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class InstanceEditor extends JDialog {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    private void init2(boolean sourceEncodingVariable, boolean targetEncodingVariable, String hint) {
        getRootPane().setDefaultButton(addOrUpdateButton);
        this.sourceEncodingField.setEnabled(sourceEncodingVariable);
        this.targetEncodingField.setEnabled(targetEncodingVariable);
        ((TitledBorder) tfnpPanel.getBorder()).setTitle(OStrings
                .getString("INSTANCEEDITOR_Target_Filename_Pattern"));

        sourceFilenameMaskField.setText("*.*");
        targetFilenamePatternField.setText("${filename}");

        hintTextArea.setText(hint);
        hintTextArea.setVisible(!StringUtil.isEmpty(hint));

        StaticUIUtils.setEscapeClosable(this);

        pack();
    }

    /**
     * Creates an InstanceEditor form, that is used to add a new filter
     * instance.
     */
    public InstanceEditor(Dialog parent, boolean sourceEncodingVariable, boolean targetEncodingVariable,
            String hint) {
        super(parent, true);
        initComponents();
        init2(sourceEncodingVariable, targetEncodingVariable, hint);
        setLocationRelativeTo(parent);
        setTitle(OStrings.getString("INSTANCEEDITOR_TITLE_ADD"));
        Mnemonics.setLocalizedText(addOrUpdateButton, OStrings.getString("BUTTON_OK"));
    }

    /**
     * Creates an InstanceEditor form, that is used to edit an existing filter
     * instance.
     */
    public InstanceEditor(Dialog parent, boolean sourceEncodingVariable, boolean targetEncodingVariable,
            String hint, String sourceFilenameMask, String sourceEncoding, String targetEncoding,
            String targetFilenamePattern) {
        super(parent, true);
        initComponents();
        init2(sourceEncodingVariable, targetEncodingVariable, hint);
        setLocationRelativeTo(parent);
        setTitle(OStrings.getString("INSTANCEEDITOR_TITLE_UPDATE"));

        Mnemonics.setLocalizedText(addOrUpdateButton, OStrings.getString("BUTTON_OK"));

        sourceFilenameMaskField.setText(sourceFilenameMask);
        sourceEncodingField.setSelectedItem(sourceEncoding);
        targetEncodingField.setSelectedItem(targetEncoding);
        targetFilenamePatternField.setText(targetFilenamePattern);
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    private String sourceFilenameMask;

    public String getSourceFilenameMask() {
        return sourceFilenameMask;
    }

    private String sourceEncoding;

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    private String targetEncoding;

    public String getTargetEncoding() {
        return targetEncoding;
    }

    private String targetFilenamePattern;

    public String getTargetFilenamePattern() {
        return targetFilenamePattern;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        addOrUpdateButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        hintTextArea = new javax.swing.JTextArea();
        tfnpPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        substitute = new javax.swing.JComboBox<String>();
        jLabel4 = new javax.swing.JLabel();
        targetFilenamePatternField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        sourceFilenameMaskField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        sourceEncodingField = new javax.swing.JComboBox<String>();
        jLabel6 = new javax.swing.JLabel();
        targetEncodingField = new javax.swing.JComboBox<String>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("INSTANCEEDITOR_TITLE_ADD")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics.setLocalizedText(addOrUpdateButton, OStrings.getString("BUTTON_ADD")); // NOI18N
        addOrUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOrUpdateButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(addOrUpdateButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(buttonPanel, gridBagConstraints);

        hintTextArea.setEditable(false);
        hintTextArea.setFont(new JLabel().getFont());
        hintTextArea.setLineWrap(true);
        hintTextArea.setWrapStyleWord(true);
        hintTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(hintTextArea, gridBagConstraints);

        tfnpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Target Filename Pattern"));
        tfnpPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(insertButton, OStrings.getString("BUTTON_INSERT")); // NOI18N
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        tfnpPanel.add(insertButton, gridBagConstraints);

        substitute.setModel(new DefaultComboBoxModel<>(new Vector<>(AbstractFilter.getTargetFilenamePatterns())));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        tfnpPanel.add(substitute, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, OStrings.getString("INSTANCEEDITOR_Substituted_Variable")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        tfnpPanel.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        tfnpPanel.add(targetFilenamePatternField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(tfnpPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, OStrings.getString("INSTANCEEDITOR_SOURCE_MASK")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(sourceFilenameMaskField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, OStrings.getString("INSTANCEEDITOR_SOURCE_ENCODING")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(jLabel3, gridBagConstraints);

        sourceEncodingField.setModel(new DefaultComboBoxModel<>(FilterMaster.getSupportedEncodings().toArray(new String[0])));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(sourceEncodingField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, OStrings.getString("INSTANCEEDITOR_Target_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(jLabel6, gridBagConstraints);

        targetEncodingField.setModel(new DefaultComboBoxModel<>(FilterMaster.getSupportedEncodings().toArray(new String[0])));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(targetEncodingField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        int caret = targetFilenamePatternField.getCaretPosition();
        String oldtext = targetFilenamePatternField.getText();
        String newtext = oldtext.substring(0, caret) + substitute.getSelectedItem().toString()
                + oldtext.substring(caret);
        targetFilenamePatternField.setText(newtext);
        targetFilenamePatternField.setCaretPosition(caret + substitute.getSelectedItem().toString().length());
        targetFilenamePatternField.requestFocus();
    }//GEN-LAST:event_insertButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void addOrUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOrUpdateButtonActionPerformed
        sourceFilenameMask = sourceFilenameMaskField.getText();
        sourceEncoding = sourceEncodingField.getSelectedItem().toString();
        targetEncoding = targetEncodingField.getSelectedItem().toString();
        targetFilenamePattern = targetFilenamePatternField.getText();
        doClose(RET_OK);
    }//GEN-LAST:event_addOrUpdateButtonActionPerformed

    private int returnStatus = RET_CANCEL;

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addOrUpdateButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea hintTextArea;
    private javax.swing.JButton insertButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox<String> sourceEncodingField;
    private javax.swing.JTextField sourceFilenameMaskField;
    private javax.swing.JComboBox<String> substitute;
    private javax.swing.JComboBox<String> targetEncodingField;
    private javax.swing.JTextField targetFilenamePatternField;
    private javax.swing.JPanel tfnpPanel;
    // End of variables declaration//GEN-END:variables

}
