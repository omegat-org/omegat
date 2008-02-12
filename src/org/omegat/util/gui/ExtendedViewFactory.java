/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.util.gui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Custom view factory, which uses ExtendedLabelView for content elements, 
 * otherwise it uses the default behavior.
 *
 * As seen on 
 * http://forum.java.sun.com/thread.jspa?threadID=5168528&messageID=9647272
 *
 * @author bartkoz
 */
public class ExtendedViewFactory implements ViewFactory {
        public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                        if (kind.equals(AbstractDocument.ContentElementName)) {
                                return new ExtendedLabelView(elem);
                        } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                                return new ParagraphView(elem);
                        } else if (kind.equals(AbstractDocument.SectionElementName)) {
                                return new BoxView(elem, View.Y_AXIS);
                        } else if (kind.equals(StyleConstants.ComponentElementName)) {
                                return new ComponentView(elem);
                        } else if (kind.equals(StyleConstants.IconElementName)) {
                                return new IconView(elem);
                        }
                }
                return new LabelView(elem);
        }
}
