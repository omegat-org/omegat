/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

/**
 * View for represent eod-of-segment.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewSegmentsSeparator extends View {
    protected static final int SEPARATOR_HEIGHT = 16;

    public ViewSegmentsSeparator(Element elem) {
        super(elem);
    }

    @Override
    public float getPreferredSpan(int axis) {
        return SEPARATOR_HEIGHT;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Bias b)
            throws BadLocationException {
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
                .getBounds();

        Rectangle lineArea = new Rectangle();

        // fill in the results and return
        lineArea.x = alloc.x;
        lineArea.y = alloc.y;
        lineArea.width = 0;
        lineArea.height = SEPARATOR_HEIGHT;
        return lineArea;
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn) {
        return getStartOffset();
    }
}
