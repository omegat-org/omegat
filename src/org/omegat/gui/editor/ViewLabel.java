/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
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

package org.omegat.gui.editor;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.text.Element;
import javax.swing.text.LabelView;

/**
 * Custom implementation of view.
 * 
 * @author bartkoz
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewLabel extends LabelView {
    /** Maximum font height for display labels. */
    protected static int fontHeight;

    public ViewLabel(final Element el) {
        super(el);
    }

    @Override
    public void paint(Graphics g, Shape a) {
        // draw text
        super.paint(g, a);

        if (!(getElement().getDocument() instanceof Document3)) {
            // document didn't created yet
            return;
        }

        if (fontHeight == 0) {
            FontMetrics fm = g.getFontMetrics();
            fontHeight = fm.getHeight();
        }
    }

    public float getPreferredSpan(int axis) {
        if (fontHeight > 0 && axis == ViewLabel.Y_AXIS) {
            // System.out.println("Calculated: " + super.getPreferredSpan(axis)
            // + " height: " + fontHeight);
            return fontHeight;
        } else {
            return super.getPreferredSpan(axis);
        }
    }
}
