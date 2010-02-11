/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2010 Wildrich Fourie
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

package org.omegat.gui.editor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

import org.omegat.util.Log;

/**
 * Supported underlines.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author W. Fourie
 */
public class UnderlineFactory {

    public static class SolidUnderliner extends Underliner {
        protected final Color color;

        public SolidUnderliner(final Color c) {
            color = c;
        }

        @Override
        protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
            g.setColor(color);

            FontMetrics fm = c.getFontMetrics(c.getFont());
            int baseline = rect.y + rect.height - fm.getDescent() + 1;
            g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
        }
    }

    public static class SolidBoldUnderliner extends Underliner {
        protected final Color color;

        public SolidBoldUnderliner(final Color c) {
            color = c;
        }

        @Override
        protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
            g.setColor(color);

            FontMetrics fm = c.getFontMetrics(c.getFont());
            int baseline = rect.y + rect.height - fm.getDescent() + 1;
            g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
            g.drawLine(rect.x, baseline + 1, rect.x + rect.width, baseline + 1);
        }
    }

    public static class WaveUnderline extends Underliner {
        protected final Color color;

        public WaveUnderline(final Color c) {
            color = c;
        }

        @Override
        protected void paint(Graphics g, Rectangle a, JTextComponent c) {
            g.setColor(color);

            int y = a.y + a.height - 2;
            int x1 = a.x;
            int x2 = a.x + a.width;

            int w = 3;
            int h = 2;

            Shape prevClip = g.getClip();
            g.setClip(a);
            for (int i = x1; i <= x2; i += w * 2) {
                g.drawArc(i + 0, y - h, w, h, 0, 180);
                g.drawArc(i + w, y - h, w, h, 180, 181);
            }
            g.setClip(prevClip);
        }
    }

    public abstract static class Underliner extends
            LayeredHighlighter.LayerPainter {

        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c, View view) {
            Rectangle rect = null;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                if (bounds instanceof Rectangle)
                    rect = (Rectangle) bounds;
                else
                    rect = bounds.getBounds();
            } else {
                try {
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    rect = (shape instanceof Rectangle) ? (Rectangle) shape
                            : shape.getBounds();
                } catch (BadLocationException ex) {
                    Log.log(ex);
                    return null;
                }
            }

            paint(g, rect, c);

            return rect;
        }

        abstract protected void paint(Graphics g, Rectangle rect,
                JTextComponent c);

        public void paint(Graphics g, int p0, int p1, Shape bounds,
                JTextComponent c) {
        }
    }
}
