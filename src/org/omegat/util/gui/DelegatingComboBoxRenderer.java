/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A combo box renderer that delegates rendering to the Look And Feel's default
 * renderer. This preserves OS styling and highlighting on e.g. Mac OS X.
 * 
 * @author Aaron Madlon-Kay
 */
abstract public class DelegatingComboBoxRenderer<T> implements ListCellRenderer {

    private final ListCellRenderer original = new JComboBox().getRenderer();
    
    @SuppressWarnings("unchecked")
	@Override
    public Component getListCellRendererComponent(JList list, Object value, // value
                                                                            // to
                                                                            // display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus
    {
        return original.getListCellRendererComponent(list, getDisplayText((T)value),
        		index, isSelected, cellHasFocus);
    }
    
    protected abstract Object getDisplayText(T value);
}
