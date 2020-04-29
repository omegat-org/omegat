/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.text.JTextComponent;

import org.omegat.gui.editor.UnderlineFactory.Underliner;

/**
 * Class to paint a direction marker for bidirectional control characters
 */
public class BidiPainter extends Underliner {
    protected final Color color;
    protected final int bidi;

    // Height of the descending lines
    private static final int MARKER_HEIGHT = 6;

    private static final float STROKE_WIDTH = 1f;

    private static final BasicStroke BIDI_STROKE = new BasicStroke(STROKE_WIDTH);

    public BidiPainter(int b, Color c) {
        bidi = b;
        color = c;
    }

    @Override
    protected void paint(Graphics g, Rectangle rect, JTextComponent c) {
        g.setColor(color);

        int dir = bidi == BidiMarkers.LRE || bidi == BidiMarkers.LRM || bidi == BidiMarkers.LRO ? -1 : 1;

        int y = rect.y;
        int x1 = rect.x;
        int x2 = rect.x + rect.width;

        Stroke oldStroke = ((Graphics2D) g).getStroke();
        ((Graphics2D) g).setStroke(BIDI_STROKE);

        Polygon p = new Polygon();
        // Draw starting bidi mark
        switch (bidi) {

        case BidiMarkers.RLM:
        case BidiMarkers.LRM:
            p.addPoint(x1, y + MARKER_HEIGHT);
            p.addPoint(x1, y);
            p.addPoint(x1 - dir * MARKER_HEIGHT, y);
            p.addPoint(x1, y + MARKER_HEIGHT);
            g.fillPolygon(p);
            g.drawPolygon(p);
            break;

        case BidiMarkers.LRE:
            p.addPoint(x1 + 1, y + MARKER_HEIGHT);
            p.addPoint(x1 + 1, y);
            p.addPoint(x1 + 1 - dir * MARKER_HEIGHT, y);
            p.addPoint(x1 + 1, y + MARKER_HEIGHT);
            g.drawPolygon(p);

            // Draw PDF Mark
            g.drawLine(x2, y, x2, y + MARKER_HEIGHT);
            g.drawLine(x1 + 1, y, x2, y);
            break;

        case BidiMarkers.RLE:
            p.addPoint(x2 + 1, y + MARKER_HEIGHT);
            p.addPoint(x2 + 1, y);
            p.addPoint(x2 + 1 - dir * MARKER_HEIGHT, y);
            p.addPoint(x2 + 1, y + MARKER_HEIGHT);
            g.drawPolygon(p);

            // Draw PDF Mark
            g.drawLine(x1, y, x1, y + MARKER_HEIGHT);
            g.drawLine(x1, y, x2 + 1, y);
            break;

        case BidiMarkers.LRO:
            p.addPoint(x1 + 1, y + MARKER_HEIGHT);
            p.addPoint(x1 + 1 - dir * MARKER_HEIGHT, y + MARKER_HEIGHT);
            p.addPoint(x1 + 1, y);
            g.fillPolygon(p);
            g.drawPolygon(p);

            // Draw PDF Mark
            g.drawLine(x2, y, x2, y + MARKER_HEIGHT);
            g.drawLine(x1 + 1, y, x2, y);
            break;

        case BidiMarkers.RLO:
            p.addPoint(x2 + 1, y + MARKER_HEIGHT);
            p.addPoint(x2 + 1 - dir * MARKER_HEIGHT / 2, y + MARKER_HEIGHT);
            p.addPoint(x2 + 1, y);
            g.fillPolygon(p);
            g.drawPolygon(p);

            // Draw PDF Mark
            g.drawLine(x1, y, x1, y + MARKER_HEIGHT);
            g.drawLine(x1, y, x2 + 1, y);
            break;

        default:
            break;
        }

        ((Graphics2D) g).setStroke(oldStroke);
    }
}
