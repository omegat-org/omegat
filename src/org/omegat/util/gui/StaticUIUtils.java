/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2014-2015 Aaron Madlon-Kay
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

import java.awt.FontMetrics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
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

    private static final KeyStroke ESC_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
    
    /**
     * Make a dialog closeable by pressing the Esc key.
     * {@link JDialog#dispose()} will be called.
     * 
     * @param dialog
     */
    public static void setEscapeClosable(JDialog dialog) {
        setEscapeAction(dialog.getRootPane(), makeCloseAction(dialog));
    }
    
    /**
     * Make a dialog closeable by pressing the Esc key.
     * {@link JFrame#dispose()} will be called.
     * 
     * @param frame
     */
    public static void setEscapeClosable(JFrame frame) {
        setEscapeAction(frame.getRootPane(), makeCloseAction(frame));
    }
    
    /**
     * Create an action that sends a {@link WindowEvent#WINDOW_CLOSING} event
     * to the supplied window. This mimics closing by clicking the window close button.
     * @param window
     * @return action
     */
    public static AbstractAction makeCloseAction(final Window window) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
            }
        };
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
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESC_KEYSTROKE, "ESCAPE");
        pane.getActionMap().put("ESCAPE", action);
    }

    private final static char TRUNCATE_CHAR = '\u2026';
    
    /**
     * Truncate the supplied text so that it fits within the width (minus margin) 
     * of the supplied component. Truncation is achieved by replacing a chunk from
     * the center of the string with an ellipsis.
     * 
     * @param text Text to truncate
     * @param comp Component to fit text into
     * @param margin Additional space to leave empty
     * @return A truncated string
     */
    public static String truncateToFit(String text, JComponent comp, int margin) {
        if (text == null || text.isEmpty() || comp == null) {
            return text;
        }
        
        final int targetWidth = comp.getWidth();
        
        // Early out if component is not visible
        if (targetWidth < 1) {
            return text;
        }
                
        FontMetrics metrics = comp.getGraphics().getFontMetrics();
        final int fullWidth = metrics.stringWidth(text);
        // Early out if string + margin already fits
        if (fullWidth + margin < targetWidth) {
            return text;
        }
        
        final int truncateCharWidth = metrics.charWidth(TRUNCATE_CHAR);
        final int middle = text.length() / 2;
        int spread = 0, chompStart = 0, chompEnd = 0;
        String chomp = null;
        
        // Calculate size when removing progressively larger chunks from the middle
        while (true) {
            chompStart = middle - spread;
            if (chompStart < 1) {
                break;
            }
            chompEnd = chompStart + (2 * spread + 1);
            if (chompEnd >= text.length() - 1) {
                break;
            }
            chomp = text.substring(chompStart, chompEnd);
            int newWidth = fullWidth - metrics.stringWidth(chomp) + truncateCharWidth + margin;
            if (newWidth <= comp.getWidth()) {
                break;
            }
            spread++;
        }
        
        if (chomp != null) {
            text = text.substring(0, chompStart) + TRUNCATE_CHAR + text.substring(chompEnd, text.length());
        }
        return text;
    }
}
