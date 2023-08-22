/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.omegat.util.Log;

/**
 * @author Aaron Madlon-Kay
 */
public final class MainMenuIcons {

    private MainMenuIcons() {
    }

    private abstract static class BaseIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.addRenderingHints(DEFAULT_HINTS);
            g2.setClip(x, y, getIconWidth(), getIconHeight());
            doPaint(g2, x, y);
            g2.dispose();
        }

        abstract void doPaint(Graphics2D g2, int x, int y);

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override

        public int getIconHeight() {
            return getIconWidth();
        }
    }

    private static final Map<?, ?> DEFAULT_HINTS;
    static {
        Map<?, ?> defaultHints = null;
        try {
            defaultHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        } catch (Throwable e) {
            Log.log(e);
        }
        DEFAULT_HINTS = defaultHints != null ? defaultHints : new RenderingHints(null);
    }

    /**
     * Size of icons (both height and width) of menu entries.
     */
    private static final int ICON_SIZE = 12;

    /**
     * Creates an empty icon of the default size.
     */
    static Icon newBlankIcon() {
        return new Icon() {

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
            }

            @Override
            public int getIconWidth() {
                return ICON_SIZE;
            }

            @Override
            public int getIconHeight() {
                return getIconWidth();
            }
        };
    }

    /**
     * Creates an icon to show color of background marking
     * @param color background color
     * @return
     */
    static Icon newColorIcon(final Color color) {
        return new BaseIcon() {
            @Override
            void doPaint(Graphics2D g2, int x, int y) {
                if (color != null) {
                    g2.setColor(color);
                }
                g2.fillRect(x, y, getIconWidth(), getIconHeight());
            }
        };
    }

    /**
     * Create icon with an image fit with menu items.
     */
    public static Icon newImageIcon(final Image image) {
        return new ImageIcon() {
            @Override
            public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                super.paintIcon(c, g, x, y);
            }

            @Override
            public int getIconWidth() {
                return ICON_SIZE;
            }

            @Override
            public int getIconHeight() {
                return getIconWidth();
            }
        };
    }

    /**
     * Creates icon with a char in the specified color and font.
     * Convenience method for {@link #newTextIcon(Color, Font, char)}
     * that uses the default font.
     * @param color color of font
     * @param c char to draw
     */
    static Icon newTextIcon(final Color color, final char c) {
        return newTextIcon(color, null, c);
    }

    /**
     * Creates icon with a char in the specified color and font
     * @param color color of font
     * @param font font to use
     * @param text char to draw
     */
    static Icon newTextIcon(final Color color, final Font font, final char c) {
        final char[] chars = new char[] { c };
        return new BaseIcon() {
            @Override
            void doPaint(Graphics2D g2, int x, int y) {
                if (color != null) {
                    g2.setColor(color);
                }
                if (font != null) {
                    g2.setFont(font);
                }
                GlyphVector gv = g2.getFont().layoutGlyphVector(g2.getFontRenderContext(), chars, 0, 1,
                        Font.LAYOUT_LEFT_TO_RIGHT);
                Rectangle r = gv.getPixelBounds(g2.getFontRenderContext(), x, y);
                int dx = x + (getIconWidth() - r.width) / 2;
                int dy = y + getIconHeight() - (getIconHeight() - r.height) / 2;
                g2.drawGlyphVector(gv, dx, dy);
            }
        };
    }
}
