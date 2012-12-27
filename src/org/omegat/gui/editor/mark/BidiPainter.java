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