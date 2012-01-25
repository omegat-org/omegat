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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.segmentation;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.ExceptionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.omegat.core.Core;
import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.datamodels.MappingRulesModel;
import org.omegat.core.segmentation.datamodels.SegmentationRulesModel;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * Main dialog for for setting up sentence segmenting. The dialog is created as
 * SRX-like as possible, but the segmentation is not (yet) SRX-compliant.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class SegmentationCustomizer extends JDialog implements ListSelectionListener {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    private SRX srx;
    
    /**
     * Flag if this customizer shows project specific segmentation rules or not
     */
    private boolean isProjectSpecific;

    private void constructor(boolean projectSpecific, String configDir) {
        
        isProjectSpecific = projectSpecific;
        
        SRX srxProjectInstance = null;
        if (projectSpecific) {
            srxProjectInstance = Core.getProject().getSRX();
            if (srxProjectInstance == null) {
                srx = SRX.getProjectSRX(configDir);
            } else {
                srx = srxProjectInstance;
            }
        } else {
            srx = SRX.getSRX();
        }
        
        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed(null);
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        // END HP

        initComponents();
        hintTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));

        getRootPane().setDefaultButton(okButton);

        mapTable.getSelectionModel().addListSelectionListener(this);
        ruleTable.getSelectionModel().addListSelectionListener(this);

        MappingRulesModel model = (MappingRulesModel) mapTable.getModel();
        model.addExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {
                mapErrorsLabel.setText(e.getLocalizedMessage());
            }
        });
        
        if (isProjectSpecific) {
            setTitle(OStrings.getString("GUI_SEGMENTATION_TITLE_PROJECTSPECIFIC")); // NOI18N
        } else {
            projectSpecificCB.setVisible(false);
        }
        if (projectSpecific && srxProjectInstance == null) {
            mapTable.setEnabled(false);
            mapTable.setFocusable(false);
            mapInsertButton.setEnabled(false);
        } else {
            if (projectSpecific) projectSpecificCB.setSelected(true);
            mapTable.setEnabled(true);
            mapTable.setFocusable(true);
            mapInsertButton.setEnabled(true);
        }

        pack();
        setSize(getWidth() * 5 / 4, getHeight() * 5 / 4);
        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    /**
     * Creates new form SegmentationCustomizer (to be called from a frame)
     */
    public SegmentationCustomizer(Frame parent, boolean projecSpecific, String configDir) {
        super(parent, true);
        constructor(projecSpecific, configDir);
    }

    /**
     * Creates new form SegmentationCustomizer (to be called from a dialog)
     */
    public SegmentationCustomizer(Dialog parent, boolean projecSpecific, String configDir) {
        super(parent, true);
        constructor(projecSpecific, configDir);
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    /**
     * Returns the SRX that was edited, so it can be used. If project-specific 
     * segmentation rules are requested, and user has not checked 
     * 'enable project specific segmentation', then null is returned.
     * @return
     */
    public SRX getSRX() {
        if (isProjectSpecific) {
            if (projectSpecificCB.isSelected()) {
                return this.srx;
            } else {
                return null;
            }
        } else {
            return this.srx;
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (e.getSource() == mapTable.getSelectionModel()) {
            mapErrorsLabel.setText("");
            if (lsm.isSelectionEmpty()) {
                mapDeleteButton.setEnabled(false);
                mapUpButton.setEnabled(false);
                mapDownButton.setEnabled(false);

                ruleTable.setModel(new DefaultTableModel());
                ruleInsertButton.setEnabled(false);
            } else {
                mapDeleteButton.setEnabled(true);

                int selrow = mapTable.getSelectedRow();
                int rows = mapTable.getRowCount();

                if (selrow > 0)
                    mapUpButton.setEnabled(true);
                else
                    mapUpButton.setEnabled(false);

                if (selrow < (rows - 1))
                    mapDownButton.setEnabled(true);
                else
                    mapDownButton.setEnabled(false);

                MapRule maprule = this.srx.getMappingRules().get(selrow);
                SegmentationRulesModel model = new SegmentationRulesModel(maprule.getRules());
                ruleTable.setModel(model);
                model.addExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ruleErrorsLabel.setText(e.getLocalizedMessage());
                    }
                });
                ruleInsertButton.setEnabled(true);
            }
        } else if (e.getSource() == ruleTable.getSelectionModel()) {
            ruleErrorsLabel.setText("");
            if (lsm.isSelectionEmpty()) {
                ruleDeleteButton.setEnabled(false);
                ruleUpButton.setEnabled(false);
                ruleDownButton.setEnabled(false);
            } else {
                ruleDeleteButton.setEnabled(true);

                int rules = ruleTable.getRowCount();
                int rulerow = ruleTable.getSelectedRow();

                if (rulerow > 0)
                    ruleUpButton.setEnabled(true);
                else
                    ruleUpButton.setEnabled(false);

                if (rulerow < (rules - 1))
                    ruleDownButton.setEnabled(true);
                else
                    ruleDownButton.setEnabled(false);
            }
        }
    }

    /** Commits all pending edits on tables to allow up/down row movement */
    private void commitTableEdits() {
        if (mapTable.getCellEditor() != null)
            mapTable.getCellEditor().stopCellEditing();
        if (ruleTable.getCellEditor() != null)
            ruleTable.getCellEditor().stopCellEditing();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        toDefaultsButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        mapPanel = new javax.swing.JPanel();
        mapScrollPane = new javax.swing.JScrollPane();
        mapTable = new javax.swing.JTable();
        mapUpButton = new javax.swing.JButton();
        mapDeleteButton = new javax.swing.JButton();
        mapInsertButton = new javax.swing.JButton();
        mapDownButton = new javax.swing.JButton();
        hintTextArea = new javax.swing.JTextArea();
        mapErrorsLabel = new javax.swing.JLabel();
        projectSpecificCB = new javax.swing.JCheckBox();
        rulePanel = new javax.swing.JPanel();
        ruleScrollPane = new javax.swing.JScrollPane();
        ruleTable = new javax.swing.JTable();
        ruleUpButton = new javax.swing.JButton();
        ruleDeleteButton = new javax.swing.JButton();
        ruleInsertButton = new javax.swing.JButton();
        ruleDownButton = new javax.swing.JButton();
        ruleErrorsLabel = new javax.swing.JLabel();

        setTitle(OStrings.getString("GUI_SEGMENTATION_TITLE")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics.setLocalizedText(toDefaultsButton, OStrings.getString("BUTTON_TO_DEFAULTS")); // NOI18N
        toDefaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toDefaultsButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(toDefaultsButton);

        jLabel1.setPreferredSize(new java.awt.Dimension(20, 0));
        buttonPanel.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        mapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_SEGMENTATION_RULESETS"))); // NOI18N
        mapPanel.setLayout(new java.awt.GridBagLayout());

        mapScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));

        mapTable.setModel(new MappingRulesModel(this.srx));
        mapScrollPane.setViewportView(mapTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapUpButton, OStrings.getString("GUI_SEGMENTATION_BUTTON_UP_1")); // NOI18N
        mapUpButton.setEnabled(false);
        mapUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapUpButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapDeleteButton, OStrings.getString("BUTTON_REMOVE")); // NOI18N
        mapDeleteButton.setEnabled(false);
        mapDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapDeleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapDeleteButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapInsertButton, OStrings.getString("BUTTON_ADD_NODOTS")); // NOI18N
        mapInsertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapInsertButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapInsertButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapDownButton, OStrings.getString("GUI_SEGMENTATION_BUTTON_DOWN_1")); // NOI18N
        mapDownButton.setEnabled(false);
        mapDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapDownButton, gridBagConstraints);

        hintTextArea.setEditable(false);
        hintTextArea.setFont(new JLabel().getFont());
        hintTextArea.setLineWrap(true);
        hintTextArea.setText(OStrings.getString("GUI_SEGMENTATION_NOTE")); // NOI18N
        hintTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(hintTextArea, gridBagConstraints);

        mapErrorsLabel.setForeground(new java.awt.Color(255, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        mapPanel.add(mapErrorsLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectSpecificCB, OStrings.getString("PP_CHECKBOX_PROJECT_SPECIFIC_SEGMENTATION_RULES")); // NOI18N
        projectSpecificCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectSpecificCBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mapPanel.add(projectSpecificCB, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        getContentPane().add(mapPanel, gridBagConstraints);

        rulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_SEGMENTATION_RULEORDER"))); // NOI18N
        rulePanel.setLayout(new java.awt.GridBagLayout());

        ruleScrollPane.setPreferredSize(new java.awt.Dimension(300, 120));
        ruleScrollPane.setViewportView(ruleTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        rulePanel.add(ruleScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ruleUpButton, OStrings.getString("GUI_SEGMENTATION_BUTTON_UP_2")); // NOI18N
        ruleUpButton.setEnabled(false);
        ruleUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        rulePanel.add(ruleUpButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ruleDeleteButton, OStrings.getString("BUTTON_REMOVE_2")); // NOI18N
        ruleDeleteButton.setEnabled(false);
        ruleDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleDeleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        rulePanel.add(ruleDeleteButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ruleInsertButton, OStrings.getString("BUTTON_ADD_NODOTS2")); // NOI18N
        ruleInsertButton.setEnabled(false);
        ruleInsertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleInsertButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        rulePanel.add(ruleInsertButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ruleDownButton, OStrings.getString("GUI_SEGMENTATION_BUTTON_DOWN_2")); // NOI18N
        ruleDownButton.setEnabled(false);
        ruleDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        rulePanel.add(ruleDownButton, gridBagConstraints);

        ruleErrorsLabel.setForeground(new java.awt.Color(255, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        rulePanel.add(ruleErrorsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(rulePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void projectSpecificCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectSpecificCBActionPerformed
        if (projectSpecificCB.isSelected()) {
            mapTable.setEnabled(true);
            mapTable.setFocusable(true);
            mapInsertButton.setEnabled(true);
        } else {
            mapTable.setEnabled(false);
            mapTable.setFocusable(false);
            mapInsertButton.setEnabled(false);
        }
    }//GEN-LAST:event_projectSpecificCBActionPerformed

    private void toDefaultsButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_toDefaultsButtonActionPerformed
    {// GEN-HEADEREND:event_toDefaultsButtonActionPerformed
        commitTableEdits();
        this.srx.init();
        MappingRulesModel model = new MappingRulesModel(SRX.getSRX());
        mapTable.setModel(model);
        model.addExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {
                mapErrorsLabel.setText(e.getLocalizedMessage());
            }
        });
        ruleTable.setModel(new DefaultTableModel());
    }// GEN-LAST:event_toDefaultsButtonActionPerformed

    private void ruleDownButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ruleDownButtonActionPerformed
    {// GEN-HEADEREND:event_ruleDownButtonActionPerformed
        commitTableEdits();
        SegmentationRulesModel model = (SegmentationRulesModel) ruleTable.getModel();
        int selrow = ruleTable.getSelectedRow();
        model.moveRowDown(selrow);
        ruleTable.getSelectionModel().clearSelection();
        ruleTable.getSelectionModel().addSelectionInterval(selrow + 1, selrow + 1);
    }// GEN-LAST:event_ruleDownButtonActionPerformed

    private void ruleUpButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ruleUpButtonActionPerformed
    {// GEN-HEADEREND:event_ruleUpButtonActionPerformed
        commitTableEdits();
        SegmentationRulesModel model = (SegmentationRulesModel) ruleTable.getModel();
        int selrow = ruleTable.getSelectedRow();
        model.moveRowUp(selrow);
        ruleTable.getSelectionModel().clearSelection();
        ruleTable.getSelectionModel().addSelectionInterval(selrow - 1, selrow - 1);
    }// GEN-LAST:event_ruleUpButtonActionPerformed

    private void ruleDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ruleDeleteButtonActionPerformed
    {// GEN-HEADEREND:event_ruleDeleteButtonActionPerformed
        commitTableEdits();
        SegmentationRulesModel model = (SegmentationRulesModel) ruleTable.getModel();
        model.removeRow(ruleTable.getSelectedRow());
    }// GEN-LAST:event_ruleDeleteButtonActionPerformed

    private void mapDownButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_mapDownButtonActionPerformed
    {// GEN-HEADEREND:event_mapDownButtonActionPerformed
        commitTableEdits();
        MappingRulesModel model = (MappingRulesModel) mapTable.getModel();
        int selrow = mapTable.getSelectedRow();
        model.moveRowDown(selrow);
        mapTable.getSelectionModel().clearSelection();
        mapTable.getSelectionModel().addSelectionInterval(selrow + 1, selrow + 1);
    }// GEN-LAST:event_mapDownButtonActionPerformed

    private void mapUpButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_mapUpButtonActionPerformed
    {// GEN-HEADEREND:event_mapUpButtonActionPerformed
        commitTableEdits();
        MappingRulesModel model = (MappingRulesModel) mapTable.getModel();
        int selrow = mapTable.getSelectedRow();
        model.moveRowUp(selrow);
        mapTable.getSelectionModel().clearSelection();
        mapTable.getSelectionModel().addSelectionInterval(selrow - 1, selrow - 1);
    }// GEN-LAST:event_mapUpButtonActionPerformed

    private void mapDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_mapDeleteButtonActionPerformed
    {// GEN-HEADEREND:event_mapDeleteButtonActionPerformed
        commitTableEdits();
        MappingRulesModel model = (MappingRulesModel) mapTable.getModel();
        String set = model.getValueAt(mapTable.getSelectedRow(), 0).toString();
        String title = OStrings.getString("CONFIRM_DIALOG_TITLE");
        String message = StaticUtils.format(OStrings.getString("SEG_CONFIRM_REMOVE_SENTSEG_SET"),
                new Object[] { set });
        if (JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            model.removeRow(mapTable.getSelectedRow());
    }// GEN-LAST:event_mapDeleteButtonActionPerformed

    private void ruleInsertButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ruleInsertButtonActionPerformed
    {// GEN-HEADEREND:event_ruleInsertButtonActionPerformed
        commitTableEdits();
        SegmentationRulesModel model = (SegmentationRulesModel) ruleTable.getModel();
        model.addRow();
    }// GEN-LAST:event_ruleInsertButtonActionPerformed

    private void mapInsertButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_mapInsertButtonActionPerformed
    {// GEN-HEADEREND:event_mapInsertButtonActionPerformed
        commitTableEdits();
        MappingRulesModel model = (MappingRulesModel) mapTable.getModel();
        model.addRow();
    }// GEN-LAST:event_mapInsertButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_okButtonActionPerformed
    {
        commitTableEdits();
        this.srx.save();
        doClose(RET_OK);
    }// GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_cancelButtonActionPerformed
    {
        if (!this.isProjectSpecific) SRX.reload();
        doClose(RET_CANCEL);
    }// GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)// GEN-FIRST:event_closeDialog
    {
        doClose(RET_CANCEL);
    }// GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea hintTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton mapDeleteButton;
    private javax.swing.JButton mapDownButton;
    private javax.swing.JLabel mapErrorsLabel;
    private javax.swing.JButton mapInsertButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JScrollPane mapScrollPane;
    private javax.swing.JTable mapTable;
    private javax.swing.JButton mapUpButton;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox projectSpecificCB;
    private javax.swing.JButton ruleDeleteButton;
    private javax.swing.JButton ruleDownButton;
    private javax.swing.JLabel ruleErrorsLabel;
    private javax.swing.JButton ruleInsertButton;
    private javax.swing.JPanel rulePanel;
    private javax.swing.JScrollPane ruleScrollPane;
    private javax.swing.JTable ruleTable;
    private javax.swing.JButton ruleUpButton;
    private javax.swing.JButton toDefaultsButton;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;

}
