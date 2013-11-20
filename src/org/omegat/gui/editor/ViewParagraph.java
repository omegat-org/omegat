/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.editor;

import java.awt.Shape;

import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.ViewFactory;

/**
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewParagraph extends ParagraphView {

    public ViewParagraph(Element elem) {
        super(elem);
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (isOutside(e)) {
            // workaround for performance issue in 1.7.0_45
            return;
        }
        super.removeUpdate(e, a, f);
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (isOutside(e)) {
            // workaround for performance issue in 1.7.0_45
            return;
        }
        super.insertUpdate(e, a, f);
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (isOutside(e)) {
            // workaround for performance issue in 1.7.0_45
            return;
        }
        super.changedUpdate(e, a, f);
    }

    private boolean isOutside(DocumentEvent e) {
        return e.getOffset() + e.getLength() < getStartOffset() || getEndOffset() < e.getOffset();
    }
}
