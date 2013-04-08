/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.text.JTextComponent;

import org.omegat.gui.editor.UnderlineFactory.Underliner;

/**
 * Paints transparent background color
 * 
 * @author Martin Fleurke
 */
public class TransparentHighlightPainter extends Underliner {
    private Color color;
    private AlphaComposite alphaComposite;

    /**
     * 
     * @param color the color to paint the background in
     * @param alpha the transparency level (1.0 = not transparent, 0.0 = full transparency)
     */
    public TransparentHighlightPainter (Color color, float alpha) {
        super();
        this.color = color;
        this.alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
        Graphics2D g2d = (Graphics2D)g;
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(alphaComposite);
        g2d.setPaint(color);
        g2d.fill(rect);
        g2d.setComposite(originalComposite);
    }

}