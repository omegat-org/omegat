/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2013 Yu Tang
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

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.omegat.util.Platform;
import org.omegat.util.StringUtil;

/**
 * @author Henry Pijffers
 * @author Yu-Tang
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
    @SuppressWarnings("serial")
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
        
        Graphics graphics = comp.getGraphics();
        if (graphics == null) {
            return text;
        }
        FontMetrics metrics = graphics.getFontMetrics();
        final int fullWidth = metrics.stringWidth(text);
        // Early out if string + margin already fits
        if (fullWidth + margin < targetWidth) {
            return text;
        }
        
        final int truncateCharWidth = metrics.charWidth(StringUtil.TRUNCATE_CHAR);
        final int middle = text.offsetByCodePoints(0, text.codePointCount(0, text.length()) / 2);
        int chompStart = middle, chompEnd = middle;
        String chomp = null;
        
        // Calculate size when removing progressively larger chunks from the middle
        while (true) {
            if (chompStart == 0 || chompEnd == text.length()) {
                break;
            }
            chomp = text.substring(chompStart, chompEnd);
            int newWidth = fullWidth - metrics.stringWidth(chomp) + truncateCharWidth + margin;
            if (newWidth <= comp.getWidth()) {
                break;
            }
            chompStart = text.offsetByCodePoints(chompStart, -1);
            chompEnd = text.offsetByCodePoints(chompEnd, 1);
        }
        
        if (chomp != null) {
            text = text.substring(0, chompStart) + StringUtil.TRUNCATE_CHAR + text.substring(chompEnd, text.length());
        }
        return text;
    }
    
    public static void forwardMouseWheelEvent(Component target, MouseWheelEvent evt) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                new MouseWheelEvent(target, evt.getID(), evt.getWhen(),
                        evt.getModifiers(), evt.getX(), evt.getY(),
                        evt.getClickCount(), evt.isPopupTrigger(),
                        evt.getScrollType(), evt.getScrollAmount(), evt.getWheelRotation()));
    }

    public static void fitInScreen(Component comp) {
        Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (comp.getHeight() > rect.height) {
            comp.setSize(comp.getWidth(), rect.height);
        }
    }

    public static void setCaretUpdateEnabled(JTextComponent comp, boolean updateEnabled) {
        Caret caret = comp.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret) caret).setUpdatePolicy(updateEnabled ? DefaultCaret.UPDATE_WHEN_ON_EDT
                    : DefaultCaret.NEVER_UPDATE);
        }
    }

    /**
     * Make caret visible even when the {@link JTextComponent} is not editable.
     */
    public static FocusListener makeCaretAlwaysVisible(final JTextComponent comp) {
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Caret caret = comp.getCaret();
                caret.setVisible(true);
                caret.setSelectionVisible(true);
            }
        };
        comp.addFocusListener(listener);
        return listener;
    }

    /**
     * Ensure the frame width is OK. This is really just a workaround for
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8065739">JDK-8065739
     * </a>, a Java bug specific to Java 1.8 on OS X whereby a frame too close
     * to the width of the screen will warp to one corner with tiny dimensions.
     * 
     * @param width
     *            Proposed window width
     * @return A safe window width
     */
    public static int correctFrameWidth(int width) {
        if (Platform.isMacOSX() && System.getProperty("java.version").startsWith("1.8")) {
            int screenWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
            // 50 is a magic number. Can be as low as 11 (tested on OS X
            // 10.10.2, Java 1.8.0_31).
            width = Math.min(width, screenWidth - 50);
        }
        return width;
    }
}
