/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.segmentation;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.omegat.core.Core;
import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.segmentation.datamodels.MappingRulesModel;
import org.omegat.core.segmentation.datamodels.SegmentationRulesModel;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * @author Maxym Mykhalchuk
 * @author Aaron Madlon-Kay
 */
public class SegmentationCustomizerController extends BasePreferencesController {

    private static final int MAX_ROW_COUNT = 10;

    private SegmentationCustomizerPanel panel;

    /** SRX from OmegaT. */
    private final SRX defaultSRX;
    /** SRX from user preferences. */
    private final SRX userSRX;
    /** Project-specific SRX */
    private final SRX projectSRX;
    /** SRX which editable now. */
    private SRX editableSRX;

    /**
     * Flag if this customizer shows project specific segmentation rules or not
     */
    private boolean isProjectSpecific;

    public SegmentationCustomizerController() {
        this(false, SRX.getDefault(), Preferences.getSRX(), null);
    }

    public SegmentationCustomizerController(boolean projectSpecific, SRX defaultSRX, SRX userSRX, SRX projectSRX) {
        this.isProjectSpecific = projectSpecific;
        this.defaultSRX = defaultSRX;
        this.userSRX = userSRX;
        this.projectSRX = projectSRX;
    }

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return isProjectSpecific ? OStrings.getString("GUI_SEGMENTATION_TITLE_PROJECTSPECIFIC")
                : OStrings.getString("GUI_SEGMENTATION_TITLE");
    }

    private void initGui() {
        panel = new SegmentationCustomizerPanel();
        panel.mapTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            panel.mapErrorsLabel.setText("");
            if (panel.mapTable.getSelectionModel().isSelectionEmpty()) {
                panel.mapDeleteButton.setEnabled(false);
                panel.mapUpButton.setEnabled(false);
                panel.mapDownButton.setEnabled(false);

                panel.ruleTable.setModel(new DefaultTableModel());
                panel.ruleInsertButton.setEnabled(false);
            } else {
                panel.mapDeleteButton.setEnabled(true);

                int selrow = panel.mapTable.getSelectedRow();
                int rows = panel.mapTable.getRowCount();

                if (selrow > 0) {
                    panel.mapUpButton.setEnabled(true);
                } else {
                    panel.mapUpButton.setEnabled(false);
                }
                if (selrow < (rows - 1)) {
                    panel.mapDownButton.setEnabled(true);
                } else {
                    panel.mapDownButton.setEnabled(false);
                }
                MapRule maprule = this.editableSRX.getMappingRules().get(selrow);
                SegmentationRulesModel model = new SegmentationRulesModel(maprule.getRules());
                panel.ruleTable.setModel(model);
                model.addExceptionListener(ex -> panel.ruleErrorsLabel.setText(ex.getLocalizedMessage()));
                panel.ruleInsertButton.setEnabled(true);
            }
        });
        panel.ruleTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            panel.ruleErrorsLabel.setText("");
            if (panel.ruleTable.getSelectionModel().isSelectionEmpty()) {
                panel.ruleDeleteButton.setEnabled(false);
                panel.ruleUpButton.setEnabled(false);
                panel.ruleDownButton.setEnabled(false);
                panel.ruleFirstButton.setEnabled(false);
                panel.ruleBottomButton.setEnabled(false);
            } else {
                panel.ruleDeleteButton.setEnabled(true);

                int rules = panel.ruleTable.getRowCount();
                int rulerow = panel.ruleTable.getSelectedRow();

                if (rulerow > 0) {
                    panel.ruleUpButton.setEnabled(true);
                    panel.ruleFirstButton.setEnabled(true);
                } else {
                    panel.ruleUpButton.setEnabled(false);
                    panel.ruleFirstButton.setEnabled(false);
                }
                if (rulerow < (rules - 1)) {
                    panel.ruleDownButton.setEnabled(true);
                    panel.ruleBottomButton.setEnabled(true);
                } else {
                    panel.ruleDownButton.setEnabled(false);
                    panel.ruleBottomButton.setEnabled(false);
                }
            }
        });
        panel.projectSpecificCB.addActionListener(e -> {
            panel.ruleTable.clearSelection();
            panel.mapTable.clearSelection();
            updateEnabledness();
        });
        panel.ruleDownButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            int selrow = panel.ruleTable.getSelectedRow();
            model.moveRowDown(selrow);
            panel.ruleTable.getSelectionModel().clearSelection();
            panel.ruleTable.getSelectionModel().addSelectionInterval(selrow + 1, selrow + 1);
        });
        panel.ruleUpButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            int selrow = panel.ruleTable.getSelectedRow();
            model.moveRowUp(selrow);
            panel.ruleTable.getSelectionModel().clearSelection();
            panel.ruleTable.getSelectionModel().addSelectionInterval(selrow - 1, selrow - 1);
        });
        panel.ruleBottomButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            int selrow = panel.ruleTable.getSelectedRow();
            model.moveRowToBottom(selrow);
            int rows = panel.ruleTable.getRowCount() - 1;
            panel.ruleTable.changeSelection(rows, 0, false, false);
            panel.ruleTable.changeSelection(rows, panel.ruleTable.getColumnCount() - 1, false, true);
        });
        panel.ruleFirstButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            int selrow = panel.ruleTable.getSelectedRow();
            model.moveRowFirst(selrow);
            panel.ruleTable.changeSelection(0, 0, false, false);
            panel.ruleTable.changeSelection(0, panel.ruleTable.getColumnCount() - 1, false, true);
        });
        panel.ruleDeleteButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            model.removeRow(panel.ruleTable.getSelectedRow());
        });
        panel.mapDownButton.addActionListener(e -> {
            commitTableEdits();
            MappingRulesModel model = (MappingRulesModel) panel.mapTable.getModel();
            int selrow = panel.mapTable.getSelectedRow();
            model.moveRowDown(selrow);
            panel.mapTable.getSelectionModel().clearSelection();
            panel.mapTable.getSelectionModel().addSelectionInterval(selrow + 1, selrow + 1);
        });
        panel.mapUpButton.addActionListener(e -> {
            commitTableEdits();
            MappingRulesModel model = (MappingRulesModel) panel.mapTable.getModel();
            int selrow = panel.mapTable.getSelectedRow();
            model.moveRowUp(selrow);
            panel.mapTable.getSelectionModel().clearSelection();
            panel.mapTable.getSelectionModel().addSelectionInterval(selrow - 1, selrow - 1);
        });
        panel.mapDeleteButton.addActionListener(e -> {
            commitTableEdits();
            MappingRulesModel model = (MappingRulesModel) panel.mapTable.getModel();
            String set = model.getValueAt(panel.mapTable.getSelectedRow(), 0).toString();
            String title = OStrings.getString("CONFIRM_DIALOG_TITLE");
            String message = StringUtil.format(OStrings.getString("SEG_CONFIRM_REMOVE_SENTSEG_SET"), set);
            if (JOptionPane.showConfirmDialog(panel, message, title,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.removeRow(panel.mapTable.getSelectedRow());
            }
        });
        panel.ruleInsertButton.addActionListener(e -> {
            commitTableEdits();
            SegmentationRulesModel model = (SegmentationRulesModel) panel.ruleTable.getModel();
            model.addRow();
            panel.ruleTable.changeSelection(panel.ruleTable.getRowCount() - 1, 0, false, false);
            panel.ruleTable.changeSelection(panel.ruleTable.getRowCount() - 1,
                    panel.ruleTable.getColumnCount() - 1, false,
                    true);
        });
        panel.mapInsertButton.addActionListener(e -> {
            commitTableEdits();
            MappingRulesModel model = (MappingRulesModel) panel.mapTable.getModel();
            model.addRow();
            panel.mapTable.changeSelection(panel.mapTable.getRowCount() - 1, 0, false, false);
            panel.mapTable.changeSelection(panel.mapTable.getRowCount() - 1,
                    panel.mapTable.getColumnCount() - 1, false, true);
        });
        Dimension mapTableSize = panel.mapTable.getPreferredSize();
        panel.mapTable.setPreferredScrollableViewportSize(
                new Dimension(mapTableSize.width, panel.mapTable.getRowHeight() * MAX_ROW_COUNT));
        Dimension ruleTableSize = panel.ruleTable.getPreferredSize();
        panel.ruleTable.setPreferredScrollableViewportSize(
                new Dimension(ruleTableSize.width, panel.ruleTable.getRowHeight() * MAX_ROW_COUNT));

    }

    /** Commits all pending edits on tables to allow up/down row movement */
    private void commitTableEdits() {
        if (panel.mapTable.getCellEditor() != null) {
            panel.mapTable.getCellEditor().stopCellEditing();
        }
        if (panel.ruleTable.getCellEditor() != null) {
            panel.ruleTable.getCellEditor().stopCellEditing();
        }
    }

    @Override
    protected void initFromPrefs() {
        panel.projectSpecificCB.setVisible(isProjectSpecific);
        panel.projectSpecificCB.setSelected(projectSRX != null);
        setEditableSRX(isProjectSpecific && projectSRX != null ? projectSRX : userSRX);
        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        if (isEditable()) {
            commitTableEdits();
            setEditableSRX(defaultSRX);
            updateEnabledness();
        }
    }

    private boolean isEditable() {
        return !isProjectSpecific || panel.projectSpecificCB.isSelected();
    }

    private void updateEnabledness() {
        boolean enabled = isEditable();
        panel.mapTable.setEnabled(enabled);
        panel.mapTable.setFocusable(enabled);
        panel.mapInsertButton.setEnabled(enabled);
    }

    protected void setEditableSRX(SRX srx) {
        editableSRX = srx.copy();
        MappingRulesModel model = new MappingRulesModel(editableSRX);
        panel.mapTable.setModel(model);
        model.addExceptionListener(ex -> panel.mapErrorsLabel.setText(ex.getLocalizedMessage()));
        panel.ruleTable.setModel(new DefaultTableModel());
    }

    @Override
    public void persist() {
        commitTableEdits();
        if (!isProjectSpecific) {
            Core.setSegmenter(new Segmenter(editableSRX));
            Preferences.setSRX(editableSRX);
        }
    }

    /**
     * Returns the SRX that was edited, so it can be used. If project-specific segmentation rules are
     * requested, and user has not checked 'enable project specific segmentation', then null is returned.
     */
    public SRX getResult() {
        if (isEditable()) {
            return editableSRX;
        } else {
            return null;
        }
    }
}
