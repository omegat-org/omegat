/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014, 2016 Aaron Madlon-Kay
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
public abstract class DelegatingComboBoxRenderer<T, U> implements ListCellRenderer<T> {

    private final ListCellRenderer<Object> original = new JComboBox<Object>().getRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
            boolean cellHasFocus) {
        return original.getListCellRendererComponent(list, getDisplayText(value), index, isSelected, cellHasFocus);
    }

    protected abstract U getDisplayText(T value);
}
