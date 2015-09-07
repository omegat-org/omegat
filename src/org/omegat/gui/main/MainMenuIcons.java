/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.main;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.util.Map;

import javax.swing.Icon;

/**
 * @author Aaron Madlon-Kay
 */
public class MainMenuIcons {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    abstract private static class BaseIcon implements Icon {
        
        private Map defaultHints;
        private Map originalHints;
        
        public BaseIcon() {
            originalHints = new RenderingHints(null);
            try {
                defaultHints = (Map) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
            } catch(AWTError ignore) {
            } finally {
                if (defaultHints == null) {
                    defaultHints = new RenderingHints(null);
                }
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            getRenderingHints(g2, defaultHints, originalHints);
            g2.addRenderingHints(defaultHints);
            doPaint(g2, x, y);
            g2.addRenderingHints(originalHints);
        }
        
        abstract void doPaint(Graphics2D g2, int x, int y);

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        
        public int getIconHeight() {
            return ICON_SIZE;
        }
        
        /**
         * Get rendering hints from a Graphics instance.
         * "hintsToSave" is a Map of RenderingHint key-values.
         * For each hint key present in that map, the value of that
         * hint is obtained from the Graphics and stored as the value
         * for the key in savedHints.
         * 
         * From: http://docs.oracle.com/javase/7/docs/api/java/awt/doc-files/DesktopProperties.html
         */
        private Map getRenderingHints(Graphics2D g2d, Map hintsToSave, Map savedHints) {
            if (savedHints == null) {
                savedHints = new RenderingHints(null);
            } else {
                savedHints.clear();
            }
            if (hintsToSave.isEmpty()) {
                return savedHints;
            }
            /* RenderingHints.keySet() returns Set */
            for (Object o : hintsToSave.keySet()) {
                RenderingHints.Key key = (RenderingHints.Key) o;
                Object value = g2d.getRenderingHint(key);
                savedHints.put(key, value);
            }
            return savedHints;
       }
        
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
                return ICON_SIZE;
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
                g2.fillRect(x, y, ICON_SIZE, ICON_SIZE);
            }
        };
    }

    /**
     * Creates icon with a char in the specified color and font.
     * Convenience method for {@link #newTextIcon(Color, Font, char)}
     * that uses the default font.
     * @param color color of font
     * @param text char to draw 
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
        final char[] chars = new char[] {c};
        return new BaseIcon() {
            @Override
            void doPaint(Graphics2D g2, int x, int y) {
                if (color != null) {
                    g2.setColor(color);
                }
                Font originalFont = g2.getFont();
                if (font != null) {
                    g2.setFont(font);
                }
                GlyphVector gv = g2.getFont().layoutGlyphVector(g2.getFontRenderContext(),
                        chars, 0, 1, Font.LAYOUT_LEFT_TO_RIGHT);
                Rectangle r = gv.getPixelBounds(g2.getFontRenderContext(), 0, 0);
                int dx = x + r.x + (ICON_SIZE - r.width) / 2;
                int dy = y + ICON_SIZE - (r.y + r.height) - (ICON_SIZE - r.height) / 2;
                g2.drawGlyphVector(gv, dx, dy);
                g2.setFont(originalFont);
            }
        };
    }
}
