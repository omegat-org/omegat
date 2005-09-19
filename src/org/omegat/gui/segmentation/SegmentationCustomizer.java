/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.gui.segmentation;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.datamodels.MappingRulesModel;
import org.omegat.core.segmentation.datamodels.SegmentationRulesModel;
import org.omegat.core.segmentation.datamodels.SRXOptionsModel;

/**
 * Main dialog for for setting up sentence segmenting.
 * The dialog is created as SRX-like as possible, but the segmentation 
 * is not (yet) SRX-compliant.
 *
 * @author  Maxym Mykhalchuk
 */
public class SegmentationCustomizer extends JDialog implements ListSelectionListener
{
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    /**
     * Creates new form SegmentationCustomizer 
     */
    public SegmentationCustomizer(Frame parent)
    {
        super(parent, true);
        initComponents();
        mapTable.getSelectionModel().addListSelectionListener(this);
        ruleTable.getSelectionModel().addListSelectionListener(this);
    }
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus()
    {
        return returnStatus;
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting()) return;
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if( e.getSource()==mapTable.getSelectionModel() )
        {
            if (lsm.isSelectionEmpty())
            {
                mapDeleteButton.setEnabled(false);
                mapUpButton.setEnabled(false);
                mapDownButton.setEnabled(false);
                
                ruleTable.setModel(new DefaultTableModel());
                ruleInsertButton.setEnabled(false);
            }
            else
            {
                mapDeleteButton.setEnabled(true);
                
                int selrow = mapTable.getSelectedRow();
                int rows = mapTable.getRowCount();
                
                if( selrow>0 )
                    mapUpButton.setEnabled(true);
                else
                    mapUpButton.setEnabled(false);
                
                if( selrow<(rows-1) )
                    mapDownButton.setEnabled(true);
                else
                    mapDownButton.setEnabled(false);
                
                MapRule maprule = (MapRule)SRX.getSRX().getMappingRules().get(selrow);
                ruleTable.setModel(new SegmentationRulesModel(maprule.getRules()));
                ruleInsertButton.setEnabled(true);
            }
        }
        else if( e.getSource()==ruleTable.getSelectionModel() )
        {
            if (lsm.isSelectionEmpty())
            {
                ruleDeleteButton.setEnabled(false);
                ruleUpButton.setEnabled(false);
                ruleDownButton.setEnabled(false);
            }
            else
            {
                ruleDeleteButton.setEnabled(true);

                int rules = ruleTable.getRowCount();
                int rulerow = ruleTable.getSelectedRow();
                
                if( rulerow>0 )
                    ruleUpButton.setEnabled(true);
                else
                    ruleUpButton.setEnabled(false);
                
                if( rulerow<(rules-1) )
                    ruleDownButton.setEnabled(true);
                else
                    ruleDownButton.setEnabled(false);
            }
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        rulePanel = new javax.swing.JPanel();
        ruleScrollPane = new javax.swing.JScrollPane();
        ruleTable = new javax.swing.JTable();
        ruleUpButton = new javax.swing.JButton();
        ruleDeleteButton = new javax.swing.JButton();
        ruleInsertButton = new javax.swing.JButton();
        ruleDownButton = new javax.swing.JButton();
        mapPanel = new javax.swing.JPanel();
        mapScrollPane = new javax.swing.JScrollPane();
        mapTable = new javax.swing.JTable();
        mapUpButton = new javax.swing.JButton();
        mapDeleteButton = new javax.swing.JButton();
        mapInsertButton = new javax.swing.JButton();
        mapDownButton = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        segmentSubflowsCheckBox = new javax.swing.JCheckBox();
        terminalThingDescLabel = new javax.swing.JLabel();
        includeStartingTagsCheckBox = new javax.swing.JCheckBox();
        includeEndingTagsCheckBox = new javax.swing.JCheckBox();
        includeIsolatedTagsCheckBox = new javax.swing.JCheckBox();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Segmentation Setup");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics.setLocalizedText(okButton, "OK");
        okButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, "Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        rulePanel.setLayout(new java.awt.GridBagLayout());

        rulePanel.setBorder(new javax.swing.border.TitledBorder("Segmentation rules are applied in the following order:"));
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

        org.openide.awt.Mnemonics.setLocalizedText(ruleUpButton, "&Move Up");
        ruleUpButton.setEnabled(false);
        ruleUpButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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

        org.openide.awt.Mnemonics.setLocalizedText(ruleDeleteButton, "Dele&te");
        ruleDeleteButton.setEnabled(false);
        ruleDeleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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

        org.openide.awt.Mnemonics.setLocalizedText(ruleInsertButton, "I&nsert");
        ruleInsertButton.setEnabled(false);
        ruleInsertButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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

        org.openide.awt.Mnemonics.setLocalizedText(ruleDownButton, "Mo&ve Down");
        ruleDownButton.setEnabled(false);
        ruleDownButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(rulePanel, gridBagConstraints);

        mapPanel.setLayout(new java.awt.GridBagLayout());

        mapPanel.setBorder(new javax.swing.border.TitledBorder("Different segmentation rules are applied for the following languages:"));
        mapScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        mapTable.setModel(new MappingRulesModel(SRX.getSRX()));
        mapScrollPane.setViewportView(mapTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapUpButton, "Move &Up");
        mapUpButton.setEnabled(false);
        mapUpButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mapUpButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapUpButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapDeleteButton, "D&elete");
        mapDeleteButton.setEnabled(false);
        mapDeleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mapDeleteButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapDeleteButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapInsertButton, "&Insert");
        mapInsertButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mapInsertButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapInsertButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mapDownButton, "Move &Down");
        mapDownButton.setEnabled(false);
        mapDownButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mapDownButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mapPanel.add(mapDownButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(mapPanel, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setBorder(new javax.swing.border.TitledBorder("Segmentation options:"));
        segmentSubflowsCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(segmentSubflowsCheckBox, "Segment the subflows (footnotes, images alt text)");
        segmentSubflowsCheckBox.setModel(SRXOptionsModel.getSegmentSubflowsModel(SRX.getSRX()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(segmentSubflowsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(terminalThingDescLabel, "Segments should include the following formatting that falls on the terminal segment boundary:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(terminalThingDescLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(includeStartingTagsCheckBox, "Starting tags");
        includeStartingTagsCheckBox.setToolTipText("Unchecked means that the starting formatting tag will be included into the following segment.");
        includeStartingTagsCheckBox.setModel(SRXOptionsModel.getIncludeStartingTagsModel(SRX.getSRX()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(includeStartingTagsCheckBox, gridBagConstraints);

        includeEndingTagsCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(includeEndingTagsCheckBox, "Ending tags");
        includeEndingTagsCheckBox.setModel(SRXOptionsModel.getIncludeEndingTagsModel(SRX.getSRX()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(includeEndingTagsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(includeIsolatedTagsCheckBox, "Isolated tags");
        includeIsolatedTagsCheckBox.setModel(SRXOptionsModel.getIncludeIsolatedTagsModel(SRX.getSRX()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(includeIsolatedTagsCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(optionsPanel, gridBagConstraints);

        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }
    // </editor-fold>//GEN-END:initComponents

    private void ruleDownButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ruleDownButtonActionPerformed
    {//GEN-HEADEREND:event_ruleDownButtonActionPerformed
        SegmentationRulesModel model = (SegmentationRulesModel)ruleTable.getModel();
        int selrow = ruleTable.getSelectedRow();
        model.moveRowDown(selrow);
        ruleTable.getSelectionModel().clearSelection();
        ruleTable.getSelectionModel().addSelectionInterval(selrow+1, selrow+1);
    }//GEN-LAST:event_ruleDownButtonActionPerformed

    private void ruleUpButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ruleUpButtonActionPerformed
    {//GEN-HEADEREND:event_ruleUpButtonActionPerformed
        SegmentationRulesModel model = (SegmentationRulesModel)ruleTable.getModel();
        int selrow = ruleTable.getSelectedRow();
        model.moveRowUp(selrow);
        ruleTable.getSelectionModel().clearSelection();
        ruleTable.getSelectionModel().addSelectionInterval(selrow-1, selrow-1);
    }//GEN-LAST:event_ruleUpButtonActionPerformed

    private void ruleDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ruleDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_ruleDeleteButtonActionPerformed
        SegmentationRulesModel model = (SegmentationRulesModel)ruleTable.getModel();
        model.removeRow(ruleTable.getSelectedRow());
    }//GEN-LAST:event_ruleDeleteButtonActionPerformed

    private void mapDownButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mapDownButtonActionPerformed
    {//GEN-HEADEREND:event_mapDownButtonActionPerformed
        MappingRulesModel model = (MappingRulesModel)mapTable.getModel();
        int selrow = mapTable.getSelectedRow();
        model.moveRowDown(selrow);
        mapTable.getSelectionModel().clearSelection();
        mapTable.getSelectionModel().addSelectionInterval(selrow+1, selrow+1);
    }//GEN-LAST:event_mapDownButtonActionPerformed

    private void mapUpButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mapUpButtonActionPerformed
    {//GEN-HEADEREND:event_mapUpButtonActionPerformed
        MappingRulesModel model = (MappingRulesModel)mapTable.getModel();
        int selrow = mapTable.getSelectedRow();
        model.moveRowUp(selrow);
        mapTable.getSelectionModel().clearSelection();
        mapTable.getSelectionModel().addSelectionInterval(selrow-1, selrow-1);
    }//GEN-LAST:event_mapUpButtonActionPerformed

    private void mapDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mapDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_mapDeleteButtonActionPerformed
        MappingRulesModel model = (MappingRulesModel)mapTable.getModel();
        model.removeRow(mapTable.getSelectedRow());
    }//GEN-LAST:event_mapDeleteButtonActionPerformed

    private void ruleInsertButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ruleInsertButtonActionPerformed
    {//GEN-HEADEREND:event_ruleInsertButtonActionPerformed
        SegmentationRulesModel model = (SegmentationRulesModel)ruleTable.getModel();
        model.addRow();
    }//GEN-LAST:event_ruleInsertButtonActionPerformed

    private void mapInsertButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mapInsertButtonActionPerformed
    {//GEN-HEADEREND:event_mapInsertButtonActionPerformed
        MappingRulesModel model = (MappingRulesModel)mapTable.getModel();
        model.addRow();
    }//GEN-LAST:event_mapInsertButtonActionPerformed
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {
        SRX.getSRX().save();
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {
        SRX.reload();
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus)
    {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox includeEndingTagsCheckBox;
    private javax.swing.JCheckBox includeIsolatedTagsCheckBox;
    private javax.swing.JCheckBox includeStartingTagsCheckBox;
    private javax.swing.JButton mapDeleteButton;
    private javax.swing.JButton mapDownButton;
    private javax.swing.JButton mapInsertButton;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JScrollPane mapScrollPane;
    private javax.swing.JTable mapTable;
    private javax.swing.JButton mapUpButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton ruleDeleteButton;
    private javax.swing.JButton ruleDownButton;
    private javax.swing.JButton ruleInsertButton;
    private javax.swing.JPanel rulePanel;
    private javax.swing.JScrollPane ruleScrollPane;
    private javax.swing.JTable ruleTable;
    private javax.swing.JButton ruleUpButton;
    private javax.swing.JCheckBox segmentSubflowsCheckBox;
    private javax.swing.JLabel terminalThingDescLabel;
    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
    
}
