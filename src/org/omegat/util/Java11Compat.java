/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Aaron Madlon-Kay
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

package org.omegat.util;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

/**
 * Compatibility class for java 11 and later.
 * All deprecated java library class and methods which OmegaT depends on
 * that are managed in the compatibility class.
 */
public class Java11Compat {

    public static int getMenuShortcutKeyMaskEx() {
        // getMenuShortcutKeyMaskEx() is introduced in Java10
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    public static Rectangle modelToView(JTextComponent comp, int pos) throws BadLocationException {
        return comp.modelToView2D(pos).getBounds();
    }

    public static Rectangle modelToView(TextUI ui, JTextComponent comp, int pos) throws BadLocationException {
        return ui.modelToView2D(comp, pos, Position.Bias.Forward).getBounds();
    }
}
