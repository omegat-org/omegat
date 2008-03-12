/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util.gui;

import javax.swing.text.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;

/**
 * Special label view drawing custom colored normal and jagged underlines, as
 * seen on http://forum.java.sun.com/thread.jspa?threadID=5168528&messageID=9647272.
 *
 * If you want to add new types, add a new unused constant and modify paint()
 *
 * @author bartkoz
 */
public class ExtendedLabelView extends LabelView {
    
    /** no custom underline */
    public static final int NO_CUSTOM_UNDERLINE = -1;
    
    /** red underline */
    public static final int RED_UNDERLINE = 1;
    
    /** red custom underline */
    public static final int RED_JAGGED_UNDERLINE = 2;
    
    /**
     * the string used to identify the attribute
     */
    public static final String CustomUnderline = "customUnderline";
    
    /** the element we are wrapping. We need to remember this because LabelView 
     * keeps it secret. */
    private Element element;
    
    public ExtendedLabelView(Element elem) {
        super(elem);
        element = elem;
    }
    
    /**
     * custom paint the thing. Except for the default it checks for any custom
     * underline prescription and then uses paintLine() or paintJaggedLine()
     */
    public void paint(Graphics g, Shape allocation) {
        super.paint(g, allocation);
        int customUnderlineType = getCustomUnderline(element.getAttributes());
        switch (customUnderlineType) {
            case RED_UNDERLINE:
                paintLine(g, allocation, Color.red);
                break;
            case RED_JAGGED_UNDERLINE:
                paintJaggedLine(g, allocation, Color.red);
                break;
            // insert any further custom underline types here
        };
    }
    
    /**
     * paint a jagged underline
     */
    public void paintJaggedLine(Graphics g, Shape a, Color color) {
        int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
        int x1 = (int) a.getBounds().getX();
        int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());
        
        Color old = g.getColor();
        g.setColor(color);
        
        int w = 3;
        int h = 2;
        
        for (int i = x1; i <= x2; i += w * 2) {
            g.drawArc(i + 0, y - h, w, h, 0, 180);
            g.drawArc(i + w, y - h, w, h, 180, 181);
        }
        
        g.setColor(old);
    }
    
    /**
     * paint a custom underline
     */
    public void paintLine(Graphics g, Shape a, Color color) {
        int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
        int x1 = (int) a.getBounds().getX();
        int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());
        
        Color old = g.getColor();
        g.setColor(color);
        
        g.drawLine(x1, y - 2, x2, y - 2);
        g.setColor(old);
    }
    
    /**
     * see if the attribute set has any custom underline.
     * @return NO_CUSTOM_UNDERLINE if nothing found
     */
    public static int getCustomUnderline(AttributeSet a) {
                Object o = a.getAttribute(CustomUnderline);
                if (o != null) {
                        try {
                                return Integer.parseInt(o.toString());
                        } catch (Exception x) {
                                return NO_CUSTOM_UNDERLINE;
                        }
                }
                return NO_CUSTOM_UNDERLINE;
        }
    
    /**
     * sets the custom underline type specified to the mutable attribute set
     */
    public static void setCustomUnderline(MutableAttributeSet a, int type) {
                a.addAttribute(CustomUnderline, new Integer(type));
        }
}
