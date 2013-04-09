/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Yu Tang
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

import java.awt.event.FocusEvent;
import javax.swing.JTextPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

/**
 * Make caret visible even if JTextPane is not editable
 * 
 * @author Yu-Tang
 */
public class AlwaysVisibleCaret extends DefaultCaret {

    public static void apply(JTextPane text) {
        Caret caret = text.getCaret();
        if (!(caret instanceof AlwaysVisibleCaret)) {
            int rate = caret.getBlinkRate();
            AlwaysVisibleCaret newCaret = new AlwaysVisibleCaret();
            newCaret.setBlinkRate(rate);
            text.setCaret(newCaret);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        setVisible(true);
        setSelectionVisible(true);
    }
}
