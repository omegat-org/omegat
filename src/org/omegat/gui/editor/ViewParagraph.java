/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
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
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Custom breakspots processing required only for word wrapping issue fix: on
 * the editing line with wordwrap, add a new line at the beginning of the line
 * and write something, the word wrapping behaves in a strange way. If you paste
 * a long line (wrapped one), the word wrap disappears and the line gets looong.
 * 
 * JDK bug 6539700(http://bugs.sun.com/view_bug.do?bug_id=6539700) : JTextPane
 * line wrap radically different from previous versions in jre 1.5.0_10+. Fixed
 * in Java 7b70.
 * 
 * @author bartkoz
 */
public class ViewParagraph extends ParagraphView {

    public ViewParagraph(Element elem) {
        super(elem);
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
        resetBreakSpots();
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
        resetBreakSpots();
    }

    private void resetBreakSpots() {
        for (int i = 0; i < layoutPool.getViewCount(); i++) {
            View v = layoutPool.getView(i);
            if (v instanceof ViewLabel) {
                ((ViewLabel) v).resetBreakSpots();
            }
        }
    }
}
