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
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.omegat.util.gui.ExtendedLabelView;

/**
 * Own implementation of EditorKit. Required for create custom views by own
 * Elements.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
class OmEditorKit extends DefaultEditorKit {
    private OmViewFactory fac = new OmViewFactory();

    public Document createDefaultDocument() {
        return new DefaultStyledDocument();
    }

    @Override
    public ViewFactory getViewFactory() {
        return fac;
    }

    /**
     * Factory for create UI Views by Element.
     */
    static class OmViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals("text")) {
                    return new ExtendedLabelView(elem);
                } else if (kind.equals("main")) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals("paragraph")) {
                    return new ParagraphView(elem);
                } else if (kind.equals("segment")) {
                    return new BoxView(elem, View.Y_AXIS);
                }
            }
            return null;
        }
    }
}
