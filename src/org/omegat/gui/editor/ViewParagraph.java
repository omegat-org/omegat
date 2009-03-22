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

import java.awt.Shape;

import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.Position.Bias;

import org.omegat.gui.editor.OmDocument.OmElementSegPart;

/**
 * Class for use some protected properties.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewParagraph extends ParagraphView {
    public ViewParagraph(final Element elem) {
        super(elem);
        OmElementSegPart segPart = (OmElementSegPart) elem.getParentElement();
        setJustification(segPart.isRightAligned() ? StyleConstants.ALIGN_RIGHT
                : StyleConstants.ALIGN_LEFT);
    }

    @Override
    protected int getNextEastWestVisualPositionFrom(int pos, Bias b, Shape a,
            int direction, Bias[] biasRet) throws BadLocationException {
        int r;
        switch (direction) {
        case SwingConstants.WEST:
            r = pos - 1;
            break;
        case SwingConstants.EAST:
            r = pos + 1;
            break;
        default:
            r = pos;
            break;
        }
        return r;
    }
}
