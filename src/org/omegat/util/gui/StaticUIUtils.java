/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2014 Aaron Madlon-Kay
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class StaticUIUtils {

    /**
     * Make a dialog closeable by pressing the Esc key.
     * {@link JDialog#dispose()} will be called.
     * 
     * @param dialog
     */
    public static void setEscapeClosable(final JDialog dialog) {
        setEscapeAction(dialog.getRootPane(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }
    
    /**
     * Make a dialog closeable by pressing the Esc key.
     * {@link JFrame#dispose()} will be called.
     * 
     * @param dialog
     */
    public static void setEscapeClosable(final JFrame frame) {
        setEscapeAction(frame.getRootPane(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }
    
    /**
     * Associate a custom action to be called when the Esc key is pressed.
     * 
     * @param dialog
     * @param action 
     */
    public static void setEscapeAction(JDialog dialog, Action action) {
        setEscapeAction(dialog.getRootPane(), action);
    }
    
    /**
     * Associate a custom action to be called when the Esc key is pressed.
     * 
     * @param frame
     * @param action 
     */
    public static void setEscapeAction(JFrame frame, Action action) {
        setEscapeAction(frame.getRootPane(), action);
    }

    /**
     * Associate a custom action to be called when the Esc key is pressed.
     * 
     * @param pane
     * @param action 
     */
    public static void setEscapeAction(JRootPane pane, Action action) {
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        pane.getActionMap().put("ESCAPE", action);
    }
}
