/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.text.JTextComponent;

/**
 * Class to paint a direction marker for bidirectional control characters
 * @author Martin Fleurke
 */
public class BidiPainter extends SymbolPainter {
	
	protected boolean rtl;
	protected boolean ltr;
	
	/**
	 * 
	 * @param c color to use when painting
	 * @param s Bidirectonal formatting character/code that is marked.
	 */
    public BidiPainter(Color c, String s) {
        super(c, s);
        if (symbol.equals("\u200F") || symbol.equals("\u202E") || symbol.equals("\u202B")) {
            rtl = true;
            ltr = false;
        } else if (symbol.equals("\u200E") || symbol.equals("\u202A") || symbol.equals("\u202D")) {
            ltr = true;
            rtl = false;
        }
    }

    protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
        g.setColor(color);
        Polygon p = new Polygon();
        p.addPoint(rect.x, rect.y+rect.height);
        p.addPoint(rect.x, rect.y);
        if (rtl) {
            p.addPoint(rect.x-4, rect.y);
            p.addPoint(rect.x, rect.y+4);
        } else if (ltr) {
            p.addPoint(rect.x+4, rect.y);
            p.addPoint(rect.x, rect.y+4);
        } else {
            p.addPoint(rect.x-2, rect.y);
            p.addPoint(rect.x, rect.y-4);
            p.addPoint(rect.x+2, rect.y);
            p.addPoint(rect.x, rect.y);
        }
        g.fillPolygon(p);
    }

}