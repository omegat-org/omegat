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