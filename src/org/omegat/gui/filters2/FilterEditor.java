/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.filters2;

import gen.core.filters.Files;
import gen.core.filters.Filter;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.omegat.filters2.IFilter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.OneFilterTableModel;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * Editor for a single filter. Filter is a class that allows for reading and
 * writing a single file format.
 * 
 * @author Maxym Mykhalchuk
 */
public class FilterEditor extends JDialog implements ListSelectionListener {

    private Filter filter;

    public Filter result;

    /** Creates new form SingleFilterEditor */
    public FilterEditor(Dialog parent, Filter filter) {
        super(parent, true);
        this.filter = FilterMaster.getInstance().cloneFilter(filter);

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

        initComponents();

        IFilter f = FilterMaster.getInstance().getFilterInstance(filter.getClassName());
        fileFormatTextField.setText(f.getFileFormatName());
        if (!StringUtil.isEmpty(f.getHint()))
            hintTextArea.setText(f.getHint());
        else
            hintTextArea.setVisible(false);

        getRootPane().setDefaultButton(okButton);

        instances.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instances.getSelectionModel().addListSelectionListener(this);

        TableColumn sourceEnc = instances.getColumnModel().getColumn(1);
        sourceEnc.setCellEditor(new DefaultCellEditor(encodingComboBox()));
        TableColumn targetEnc = instances.getColumnModel().getColumn(2);
        targetEnc.setCellEditor(new DefaultCellEditor(encodingComboBox()));

        // hack for "autoresizing" the dialog
        // accomodating table dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = instancesScrollPane.getPreferredSize().width + 200;
        if (width > screenSize.width)
            width = screenSize.width - addButton.getWidth() - 50;
        instancesScrollPane.setPreferredSize(new Dimension(width, instances.getPreferredSize().height + 70));
        pack();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
    }

    private JComboBox encodingComboBox() {
        return new JComboBox(new Vector<String>(FilterMaster.getSupportedEncodings()));
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else {
            editButton.setEnabled(true);
            removeButton.setEnabled(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        toDefaultsButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        fileFormatLabel = new javax.swing.JLabel();
        fileFormatTextField = new javax.swing.JTextField();
        descTextArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        instancesScrollPane = new javax.swing.JScrollPane();
        instances = new javax.swing.JTable();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        hintTextArea = new javax.swing.JTextArea();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle(OStrings.getString("FILTEREDITOR_Edit_a_single_file_filter"));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics
                .setLocalizedText(toDefaultsButton, OStrings.getString("BUTTON_TO_DEFAULTS"));
        toDefaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toDefaultsButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(toDefaultsButton);

        jLabel1.setPreferredSize(new java.awt.Dimension(20, 0));
        buttonPanel.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(buttonPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fileFormatLabel,
                OStrings.getString("FILTEREDITOR_File_Format"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(fileFormatLabel, gridBagConstraints);

        fileFormatTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(fileFormatTextField, gridBagConstraints);

        descTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        descTextArea.setEditable(false);
        descTextArea.setFont(new JLabel().getFont());
        descTextArea.setLineWrap(true);
        descTextArea.setText(OStrings.getString("FILTEREDITOR_DESC"));
        descTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(descTextArea, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        instances.setModel(new OneFilterTableModel(filter));
        instancesScrollPane.setViewportView(instances);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(instancesScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, OStrings.getString("BUTTON_REMOVE"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, OStrings.getString("BUTTON_EDIT"));
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(editButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, OStrings.getString("BUTTON_ADD"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(addButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(jPanel3, gridBagConstraints);

        hintTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        hintTextArea.setEditable(false);
        hintTextArea.setFont(new JLabel().getFont());
        hintTextArea.setLineWrap(true);
        hintTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(hintTextArea, gridBagConstraints);

        pack();
    }

    // </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_removeButtonActionPerformed
    {// GEN-HEADEREND:event_removeButtonActionPerformed
        int row = instances.getSelectedRow();
        Files instance = filter.getFiles().get(row);
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, StaticUtils.format(
                OStrings.getString("FILTEREDITOR_really_delete_filter_instance"),
                new Object[] { instance.getSourceFilenameMask() }), OStrings
                .getString("FILTEREDITOR_Confirm_deletion_TITLE"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE)) {
            filter.getFiles().remove(row);
            instances.setModel(new OneFilterTableModel(filter));
        }
    }// GEN-LAST:event_removeButtonActionPerformed

    private void toDefaultsButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_toDefaultsButtonActionPerformed
    {// GEN-HEADEREND:event_toDefaultsButtonActionPerformed
        try {
            filter = FilterMaster.getInstance().getDefaultSettingsFromFilter(filter.getClassName());
            instances.setModel(new OneFilterTableModel(filter));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    OStrings.getString("FILTEREDITOR_ERROR_Reverting_To_Def") + e,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_toDefaultsButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_addButtonActionPerformed
    {// GEN-HEADEREND:event_addButtonActionPerformed
        IFilter f = FilterMaster.getInstance().getFilterInstance(filter.getClassName());

        InstanceEditor ie = new InstanceEditor(this, f.isSourceEncodingVariable(),
                f.isTargetEncodingVariable(), f.getHint());
        ie.setVisible(true);
        if (ie.getReturnStatus() == InstanceEditor.RET_OK) {
            Files ff = new Files();
            ff.setSourceEncoding(setEncodingName(ie.getSourceEncoding()));
            ff.setSourceFilenameMask(ie.getSourceFilenameMask());
            ff.setTargetEncoding(setEncodingName(ie.getTargetEncoding()));
            ff.setTargetFilenamePattern(ie.getTargetFilenamePattern());
            filter.getFiles().add(ff);
            instances.setModel(new OneFilterTableModel(filter));
        }
    }// GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_editButtonActionPerformed
    {// GEN-HEADEREND:event_editButtonActionPerformed
        int row = instances.getSelectedRow();

        IFilter f = FilterMaster.getInstance().getFilterInstance(filter.getClassName());
        InstanceEditor ie = new InstanceEditor(this, f.isSourceEncodingVariable(),
                f.isTargetEncodingVariable(), f.getHint(),
                instances.getModel().getValueAt(row, 0).toString(), instances.getModel().getValueAt(row, 1)
                        .toString(), instances.getModel().getValueAt(row, 2).toString(), instances.getModel()
                        .getValueAt(row, 3).toString());
        ie.setVisible(true);
        if (ie.getReturnStatus() == InstanceEditor.RET_OK) {
            Files ff = new Files();
            ff.setSourceEncoding(setEncodingName(ie.getSourceEncoding()));
            ff.setSourceFilenameMask(ie.getSourceFilenameMask());
            ff.setTargetEncoding(setEncodingName(ie.getTargetEncoding()));
            ff.setTargetFilenamePattern(ie.getTargetFilenamePattern());
            filter.getFiles().set(row, ff);
            instances.setModel(new OneFilterTableModel(filter));
        }
    }// GEN-LAST:event_editButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
        doClose(filter);
    }// GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        doClose(null);
    }// GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
        doClose(null);
    }// GEN-LAST:event_closeDialog

    private void doClose(Filter r) {
        result = r;
        setVisible(false);
        dispose();
    }

    private String setEncodingName(final String encName) {
        return OStrings.getString("ENCODING_AUTO").equals(encName) ? null : encName;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JTextField fileFormatTextField;
    private javax.swing.JTextArea hintTextArea;
    private javax.swing.JTable instances;
    private javax.swing.JScrollPane instancesScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton toDefaultsButton;
    // End of variables declaration//GEN-END:variables
}
