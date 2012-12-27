package org.omegat.gui.editor.mark;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.text.JTextComponent;

import org.omegat.gui.editor.UnderlineFactory.Underliner;

/**
 * Class to paint some character/symbol as highlight. E.g. to highlight whitespace characters.
 * @author Martin Fleurke
 */
public class SymbolPainter extends Underliner {
    protected final Color color;
    protected final String symbol;

    public SymbolPainter(final Color c, final String s) {
        this.color = c;
        this.symbol = s;
    }

    protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
        Font f = c.getFont();
        FontMetrics fm = c.getFontMetrics(f);
        int y = rect.y -1 + fm.getAscent();

        g.setFont(f);
        g.setColor(color);
        g.drawString(symbol, rect.x, y);
    }
}