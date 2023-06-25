/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2013 Yu Tang
               2014-2015 Aaron Madlon-Kay
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

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.omegat.core.Core;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.util.Java8Compat;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * @author Henry Pijffers
 * @author Yu-Tang
 * @author Aaron Madlon-Kay
 */
public final class StaticUIUtils {

    private StaticUIUtils() {
    }

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
     * Make a dialog closeable by pressing the Esc key. {@link JFrame#dispose()}
     * will be called.
     *
     * @param frame
     */
    public static void setEscapeClosable(JFrame frame) {
        setEscapeAction(frame.getRootPane(), makeCloseAction(frame));
    }

    @SuppressWarnings("serial")
    public static Action makeCloseAction(final Window window) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindowByEvent(window);
            }
        };
    }

    /**
     * Send a {@link WindowEvent#WINDOW_CLOSING} event to the supplied window.
     * This mimics closing by clicking the window close button.
     */
    public static void closeWindowByEvent(Window window) {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
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
     * Truncate the supplied text so that it fits within the width (minus
     * margin) of the supplied component. Truncation is achieved by replacing a
     * chunk from the center of the string with an ellipsis.
     *
     * @param text
     *            Text to truncate
     * @param comp
     *            Component to fit text into
     * @param margin
     *            Additional space to leave empty
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

        // Calculate size when removing progressively larger chunks from the
        // middle
        while (true) {
            if (chompStart == 0 || chompEnd == text.length()) {
                break;
            }
            chomp = text.substring(chompStart, chompEnd);
            int newWidth = fullWidth - metrics.stringWidth(chomp) + truncateCharWidth + margin;
            if (newWidth <= targetWidth) {
                break;
            }
            chompStart = text.offsetByCodePoints(chompStart, -1);
            chompEnd = text.offsetByCodePoints(chompEnd, 1);
        }

        if (chomp != null) {
            text = text.substring(0, chompStart) + StringUtil.TRUNCATE_CHAR
                    + text.substring(chompEnd, text.length());
        }
        return text;
    }

    public static void forwardMouseWheelEvent(Component target, MouseWheelEvent evt) {
        Toolkit.getDefaultToolkit().getSystemEventQueue()
                .postEvent(new MouseWheelEvent(target, evt.getID(), evt.getWhen(), evt.getModifiersEx(),
                        evt.getX(), evt.getY(), evt.getClickCount(), evt.isPopupTrigger(),
                        evt.getScrollType(), evt.getScrollAmount(), evt.getWheelRotation()));
    }

    public static void fitInScreen(Component comp) {
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Rectangle compBounds = comp.getBounds();
        Rectangle newBounds = new Rectangle(Math.max(compBounds.x, maxBounds.x),
                Math.max(compBounds.y, maxBounds.y),
                Math.min(compBounds.width, maxBounds.width - maxBounds.y),
                Math.min(compBounds.height, maxBounds.height - maxBounds.y));
        if (newBounds.x + newBounds.width > maxBounds.width) {
            newBounds.x = Math.max(maxBounds.x, maxBounds.width - newBounds.width);
        }
        if (newBounds.y + newBounds.height > maxBounds.height) {
            newBounds.y = Math.max(maxBounds.y, maxBounds.height - newBounds.height);
        }
        if (!newBounds.equals(compBounds)) {
            comp.setBounds(newBounds);
        }
    }

    public static void setCaretUpdateEnabled(JTextComponent comp, boolean updateEnabled) {
        Caret caret = comp.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret) caret).setUpdatePolicy(
                    updateEnabled ? DefaultCaret.UPDATE_WHEN_ON_EDT : DefaultCaret.NEVER_UPDATE);
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
            int screenWidth = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds().width;
            // 50 is a magic number. Can be as low as 11 (tested on OS X
            // 10.10.2, Java 1.8.0_31).
            width = Math.min(width, screenWidth - 50);
        }
        return width;
    }

    /**
     * Toggle the enabled property of an entire hierarchy of components.
     *
     * @param parent
     *            The parent component, which will also be toggled
     * @param isEnabled
     *            Enabled or not
     */
    public static void setHierarchyEnabled(Component parent, boolean isEnabled) {
        visitHierarchy(parent, c -> c.setEnabled(isEnabled));
    }

    public static void visitHierarchy(Component parent, Consumer<Component> consumer) {
        visitHierarchy(parent, c -> true, consumer);
    }

    public static void visitHierarchy(Component parent, Predicate<Component> filter,
            Consumer<Component> consumer) {
        if (filter.test(parent)) {
            consumer.accept(parent);
            if (parent instanceof JComponent) {
                for (Component child : ((JComponent) parent).getComponents()) {
                    visitHierarchy(child, filter, consumer);
                }
            }
        }
    }

    public static List<Component> listHierarchy(Component parent) {
        List<Component> cs = new ArrayList<>();
        visitHierarchy(parent, cs::add);
        return cs;
    }

    private static Optional<Rectangle> getStoredRectangle(String key) {
        try {
            int x = Integer.parseInt(Preferences.getPreference(key + "_x"));
            int y = Integer.parseInt(Preferences.getPreference(key + "_y"));
            int w = correctFrameWidth(Integer.parseInt(Preferences.getPreference(key + "_width")));
            int h = Integer.parseInt(Preferences.getPreference(key + "_height"));
            return Optional.of(new Rectangle(x, y, w, h));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static void persistGeometry(Window window, String key) {
        persistGeometry(window, key, null);
    }

    public static void persistGeometry(Window window, String key, Runnable extraProcessing) {
        getStoredRectangle(key).ifPresent(window::setBounds);
        String xKey = key + "_x";
        String yKey = key + "_y";
        String widthKey = key + "_width";
        String heightKey = key + "_height";
        Timer timer = new Timer(500, e -> {
            Rectangle bounds = window.getBounds();
            Preferences.setPreference(xKey, bounds.x);
            Preferences.setPreference(yKey, bounds.y);
            Preferences.setPreference(widthKey, bounds.width);
            Preferences.setPreference(heightKey, bounds.height);
            if (extraProcessing != null) {
                extraProcessing.run();
            }
        });
        timer.setRepeats(false);
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                timer.restart();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                timer.restart();
            }
        });
    }

    public static void setWindowIcon(Window window) {
        List<Image> icons;
        if (Platform.isMacOSX()) {
            icons = Arrays.asList(OSXIntegration.APP_ICON_MAC);
        } else {
            icons = Arrays.asList(ResourcesUtil.APP_ICON_16X16, ResourcesUtil.APP_ICON_32X32);
        }
        window.setIconImages(icons);
    }

    /**
     * Calculate a highlight color from a base color, with a given amount of
     * adjustment.
     * <p>
     * The adjustment is added to each of the base color's RGB values and
     * rebounds from the boundaries [0, 255]. E.g. 250 + 4 -&gt; 254 but 250 +
     * 11 -&gt; 249, and 5 - 4 -&gt; 1 but 5 - 11 -&gt; 6.
     */
    public static Color getHighlightColor(Color base, int adjustment) {
        return new Color(reboundClamp(0, 255, base.getRed() + adjustment),
                reboundClamp(0, 255, base.getGreen() + adjustment),
                reboundClamp(0, 255, base.getBlue() + adjustment), base.getAlpha());
    }

    /**
     * Convenience method for {@link #getHighlightColor(Color, int)} using the
     * default adjustment (10 darker than base).
     */
    public static Color getHighlightColor(Color base) {
        return getHighlightColor(base, -10);
    }

    /**
     * Clamp value between min and max by "rebounding" within the range [min,
     * max].
     */
    static int reboundClamp(int min, int max, int value) {
        if (value < min) {
            return reboundClamp(min, max, min + (min - value));
        } else if (value > max) {
            return reboundClamp(min, max, max - (value - max));
        } else {
            return value;
        }
    }

    public static String getKeyStrokeText(KeyStroke ks) {
        StringBuilder sb = new StringBuilder();
        String modifierText = KeyEvent.getModifiersExText(ks.getModifiers());
        sb.append(modifierText);
        String keyText = KeyEvent.getKeyText(ks.getKeyCode());
        if (!keyText.isEmpty() && !modifierText.contains(keyText)) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append(keyText);
        }
        return sb.toString();
    }

    @SuppressWarnings("serial")
    public static void makeUndoable(JTextComponent comp) {
        UndoManager manager = new UndoManager();
        comp.getDocument().addUndoableEditListener(manager);

        // Handle undo (Ctrl/Cmd+Z);
        KeyStroke undo = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Java8Compat.getMenuShortcutKeyMaskEx(), false);
        Action undoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (manager.canUndo()) {
                    manager.undo();
                }
            }
        };
        comp.getInputMap().put(undo, "UNDO");
        comp.getActionMap().put("UNDO", undoAction);

        // Handle redo (Ctrl/Cmd+Y);
        KeyStroke redo = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Java8Compat.getMenuShortcutKeyMaskEx(), false);
        Action redoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (manager.canRedo()) {
                    manager.redo();
                }
            }
        };
        comp.getInputMap().put(redo, "REDO");
        comp.getActionMap().put("REDO", redoAction);
    }

    /**
     * Request to show the pane when it is hidden as a tab.
     */
    public static void requestVisible(DockableScrollPane scrollPane) {
        if (scrollPane.getDockKey().getLocation().equals(DockableState.Location.HIDDEN)) {
            DockingDesktop desktop = Core.getMainWindow().getDesktop();
            Dockable dockable = desktop.getContext().getDockableByKey(scrollPane.getDockKey().getKey());
            desktop.setAutoHide(dockable, false);
        }
    }
}
