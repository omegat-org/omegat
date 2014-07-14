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

package org.omegat.util.gui;

import java.awt.Component;

import javax.swing.JList;

import org.omegat.util.Language;

/**
 * A class that renders a language combo box smartly.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class LanguageComboBoxRenderer extends DelegatingComboBoxRenderer {    
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, // value
                                                                            // to
                                                                            // display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus
    {
        if (!(value instanceof Language)) {
            throw new RuntimeException("Unsupported type in language combobox");
        }
        Language lang = (Language) value;
        return super.getListCellRendererComponent(list, lang + " - " + lang.getDisplayName(),
                index, isSelected, cellHasFocus);
    }
}
