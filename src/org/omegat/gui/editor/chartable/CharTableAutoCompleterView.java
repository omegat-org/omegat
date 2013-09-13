/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

package org.omegat.gui.editor.chartable;

import java.awt.Color;
import java.awt.Point;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.omegat.gui.editor.autocompleter.AutoCompleterTableView;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Character table auto-completer view.
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public class CharTableAutoCompleterView extends AutoCompleterTableView {

    private CharTableModel model;
    
    DefaultTableCellRenderer renderer;
    
    public CharTableAutoCompleterView(AutoCompleter ac) {
        super(OStrings.getString("AC_CHARTABLE_VIEW"), ac);
        model = new CharTableModel(null);
        getTable().setModel(model);
        getTable().setShowGrid(true);
        getTable().setGridColor(Color.gray);
        renderer = (DefaultTableCellRenderer) getTable().getDefaultRenderer(getTable().getColumnClass(0));
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        setSelection(new Point(0, 0));
    }

    @Override
    public boolean updateViewData() {
        updateModel();
        
        int offset = completer.getEditor().getCaretPosition();
        
        completer.setWordChunkStart(offset);
        return true;
    }

    /**
     * Update the model.
     */
    public void updateModel() {
        String input = null;
        if (Preferences.isPreference(Preferences.AC_CHARTABLE_USE_CUSTOM_CHARS)) {
            String customChars = Preferences.getPreference(Preferences.AC_CHARTABLE_CUSTOM_CHAR_STRING);
            if (!customChars.isEmpty()) {
                input = customChars;
            }
        }
        
        if (model.setData(input)) {
            setSelection(new Point(0,0));
        }
    }

    @Override
    public void setSelection(Point p) {
        super.setSelection(model.modifyPoint(p));
    }
}
