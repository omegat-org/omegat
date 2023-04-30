/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013-2014 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

/**
 * A class that renders a tokenizer combo box.
 *
 * @author Aaron Madlon-Kay
 */
public class TokenizerComboBoxRenderer extends DelegatingComboBoxRenderer<Object, String> {

    @Override
    protected String getDisplayText(Object value) {
        if (value instanceof Class<?>) {
            Class<?> cls = (Class<?>) value;
            return cls.getSimpleName();
        }
        if (value instanceof String) {
            return (String) value;
        }

        throw new RuntimeException("Unsupported type in tokenizer combobox");
    }
}
