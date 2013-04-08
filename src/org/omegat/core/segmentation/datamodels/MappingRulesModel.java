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

 This program is distributed in the hope that it will be useful,
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

import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.SRX;
import org.omegat.util.OStrings;

/**
 * Table Model for Sets of segmentation rules.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class MappingRulesModel extends AbstractTableModel {
    private SRX srx;

    /**
     * Creates a new instance of MappingRulesModel
     */
    public MappingRulesModel(SRX srx) {
        this.srx = srx;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        MapRule maprule = srx.getMappingRules().get(rowIndex);
        switch (columnIndex) {
        case 0:
            return maprule.getLanguage();
        case 1:
            return maprule.getPattern();
        }
        return null;
    }

    public int getRowCount() {
        return srx.getMappingRules().size();
    }

    public int getColumnCount() {
        return 2;
    }

    /** The names of table columns */
    private static String[] COLUMN_NAMES = new String[] {
            OStrings.getString("CORE_SRX_TABLE_HEADER_Language_Name"),
            OStrings.getString("CORE_SRX_TABLE_HEADER_Language_Pattern") };

    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MapRule maprule = srx.getMappingRules().get(rowIndex);
        switch (columnIndex) {
        case 0:
            maprule.setLanguage((String) aValue);
            break;
        case 1:
            try {
                maprule.setPattern((String) aValue);
            } catch (PatternSyntaxException pse) {
                fireException(pse);
            }
            break;
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /** Adds a new empty mapping rule. */
    public int addRow() {
        int rows = srx.getMappingRules().size();
        srx.getMappingRules().add(
                new MapRule(OStrings.getString("SEG_NEW_LN_CO"), "LN-CO", new ArrayList<Rule>()));
        fireTableRowsInserted(rows, rows);
        return rows;
    }

    /** Removes a mapping rule. */
    public void removeRow(int row) {
        srx.getMappingRules().remove(row);
        fireTableRowsDeleted(row, row);
    }

    /** Moves a mapping rule up an order. */
    public void moveRowUp(int row) {
        MapRule maprulePrev = srx.getMappingRules().get(row - 1);
        MapRule maprule = srx.getMappingRules().get(row);
        srx.getMappingRules().remove(row - 1);
        srx.getMappingRules().add(row, maprulePrev);
        fireTableRowsUpdated(row - 1, row);
    }

    /** Moves a mapping rule down an order. */
    public void moveRowDown(int row) {
        MapRule mapruleNext = srx.getMappingRules().get(row + 1);
        MapRule maprule = srx.getMappingRules().get(row);
        srx.getMappingRules().remove(row + 1);
        srx.getMappingRules().add(row, mapruleNext);
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
