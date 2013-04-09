/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.core.segmentation.datamodels;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.table.AbstractTableModel;

import org.omegat.core.segmentation.Rule;
import org.omegat.util.OStrings;

/**
 * Table Model for Segmentation Rules.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class SegmentationRulesModel extends AbstractTableModel {
    private List<Rule> rules;

    /**
     * Creates a new instance of SegmentationRulesModel
     */
    public SegmentationRulesModel(List<Rule> rules) {
        this.rules = rules;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Rule rule = rules.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return new Boolean(rule.isBreakRule());
        case 1:
            return rule.getBeforebreak();
        case 2:
            return rule.getAfterbreak();
        }
        return null;
    }

    public int getRowCount() {
        return rules.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return Boolean.class;
        else
            return String.class;
    }

    /** The names of table columns */
    private static String[] COLUMN_NAMES = new String[] { OStrings.getString("CORE_SRX_TABLE_COLUMN_Break"),
            OStrings.getString("CORE_SRX_TABLE_COLUMN_Before_Break"),
            OStrings.getString("CORE_SRX_TABLE_COLUMN_After_Break") };

    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Rule rule = rules.get(rowIndex);
        switch (columnIndex) {
        case 0:
            rule.setBreakRule(((Boolean) aValue).booleanValue());
            break;
        case 1:
            try {
                rule.setBeforebreak((String) aValue);
            } catch (PatternSyntaxException pse) {
                fireException(pse);
            }
            break;
        case 2:
            try {
                rule.setAfterbreak((String) aValue);
            } catch (PatternSyntaxException pse) {
                fireException(pse);
            }
            break;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /** Adds a new empty segmentation rule. */
    public int addRow() {
        int rows = rules.size();
        rules.add(new Rule(false, "\\.", "\\s"));
        fireTableRowsInserted(rows, rows);
        return rows;
    }

    /** Removes a segmentation rule. */
    public void removeRow(int row) {
        rules.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /** Moves a segmentation rule up an order. */
    public void moveRowUp(int row) {
        Rule rulePrev = rules.get(row - 1);
        Rule rule = rules.get(row);
        rules.remove(row - 1);
        rules.add(row, rulePrev);
        fireTableRowsUpdated(row - 1, row);
    }

    /** Moves a segmentation rule down an order. */
    public void moveRowDown(int row) {
        Rule ruleNext = rules.get(row + 1);
        Rule rule = rules.get(row);
        rules.remove(row + 1);
        rules.add(row, ruleNext);
        fireTableRowsUpdated(row, row + 1);
    }

    //
    // Managing Listeners of Errorneous Input
    //

    /** List of listeners */
    protected List<ExceptionListener> listeners = new ArrayList<ExceptionListener>();

    public void addExceptionListener(ExceptionListener l) {
        listeners.add(l);
    }

    public void removeTableModelListener(ExceptionListener l) {
        listeners.remove(l);
    }

    public void fireException(Exception e) {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ExceptionListener l = listeners.get(i);
            l.exceptionThrown(e);
        }
    }
}
