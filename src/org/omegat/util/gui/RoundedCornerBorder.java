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

package org.omegat.util.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

/**
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
class RoundedCornerBorder extends AbstractBorder {
    
    public static final int SIDE_TOP = 0;
    public static final int SIDE_LEFT = 1;
    public static final int SIDE_BOTTOM = 2;
    public static final int SIDE_RIGHT = 3;
    public static final int SIDE_ALL = 4;
    
    private final int radius;
    private final Color color;
    private final int side;
    
    public RoundedCornerBorder() {
        this(-1, Color.GRAY, SIDE_ALL);
    }
    
    public RoundedCornerBorder(int radius, Color color, int side) {
        this.radius = radius;
        this.color = color;
        this.side = side;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int r = radius == -1 ? height - 1 : radius;
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(x, y, width - 0.5f, height - 1, r, r);
        Rectangle2D sharpRect = new Rectangle2D.Float(x, y, width, height);
        Area corners = new Area(sharpRect);
        corners.subtract(new Area(roundRect));
        Color background = c.getParent() == null ? null : c.getParent().getBackground();
        
        if (side == SIDE_ALL) {
            drawCorners(g2, background, corners, roundRect);
            g2.dispose();
            return;
        }
        
        Shape initialClip = g2.getClip();
        Rectangle2D roundedHalfClip;
        Shape line1;
        Shape line2;
        if (side == SIDE_TOP) {
            roundedHalfClip = new Rectangle2D.Float(x, y, width, height / 2);
            line1 = new Line2D.Float(x, y, x, y + height);
            line2 = new Line2D.Float(x + width - 0.5f, y, x + width - 0.5f, y + height);
        } else if (side == SIDE_LEFT) {
            roundedHalfClip = new Rectangle2D.Float(x, y, width / 2 + 1, height);
            line1 = new Line2D.Float(x, y, x + width, y);
            line2 = new Line2D.Float(x, y + height - 1, x + width, y + height - 1);
        } else if (side == SIDE_BOTTOM) {
            roundedHalfClip = new Rectangle2D.Float(x, y + height / 2, width, height / 2 + 1);
            line1 = new Line2D.Float(x, y, x, y + height);
            line2 = new Line2D.Float(x + width - 0.5f, y, x + width - 0.5f, y + height);
        } else if (side == SIDE_RIGHT) {
            roundedHalfClip = new Rectangle2D.Float(x + width / 2, y, width / 2 + 1, height);
            line1 = new Line2D.Float(x, y, x + width, y);
            line2 = new Line2D.Float(x, y + height - 1, x + width, y + height - 1);
        } else {
            throw new IllegalArgumentException();
        }
        g2.clip(roundedHalfClip);
        drawCorners(g2, background, corners, roundRect);
        
        Area inverseClip = new Area(sharpRect);
        inverseClip.subtract(new Area(roundedHalfClip));
        g2.setClip(initialClip);
        g2.clip(inverseClip);
        g2.draw(line1);
        g2.draw(line2);
        
        g2.dispose();
    }
    
    private void drawCorners(Graphics2D g2, Color background, Area corners, Shape shape) {
        if (background != null) {
            g2.setColor(background);
            g2.fill(corners);
        }
        g2.setColor(color);
        g2.draw(shape);
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(4, 8, 4, 8);
        return insets;
    }
}
