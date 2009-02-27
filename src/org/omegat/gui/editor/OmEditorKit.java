/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import javax.swing.text.BoxView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Own implementation of EditorKit. Required for create custom views by own
 * Elements.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
class OmEditorKit extends DefaultEditorKit {
    protected static final OmViewFactory FACTORY = new OmViewFactory();

    @Override
    public ViewFactory getViewFactory() {
        return FACTORY;
    }
    
    /**
     * Factory for create UI Views by Element.
     */
    static class OmViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals("OmElementText")) {
                    return new ViewLabel(elem);
                } else if (kind.equals("OmElementSegmentsSeparator")) {
                    return new ViewSegmentsSeparator(elem);
                } else if (kind.equals("OmElementMain")) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals("OmElementParagraph")) {
                    return new ViewParagraph(elem);
                } else if (kind.equals("OmElementSegment")) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals("OmElementSegPart")) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals("OmElementSegmentMark")) {
                    return new ViewSegmentMark(elem);
                }
            }
            // used for empty document
            if (kind.equals("paragraph")) {
                return new PlainView(elem);
            }
            throw new RuntimeException("Unknown element type: " + kind);
        }
    }
}
