/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.util.Map;
import java.util.function.Function;

/**
 * An editor kit that prevents line wrapping entirely.
 *
 * @author Aaron Madlon-Kay
 * @see <a href="http://java-sl.com/wrap.html">"Forced line wrap" and "No wrap" in the JEditorPane/JTextPane</a>
 */
@SuppressWarnings("serial")
public class NoWrapEditorKit extends StyledEditorKit {

    private static final ViewFactory FACTORY = new MyViewFactory();

    @Override
    public ViewFactory getViewFactory() {
        return FACTORY;
    }

    static class NoWrapParagraphView extends ParagraphView {

        NoWrapParagraphView(Element elem) {
            super(elem);
        }

        @Override
        protected void layout(int width, int height) {
            super.layout(Short.MAX_VALUE, height);
        }
    }

    static class MyViewFactory implements ViewFactory {

        private static final Map<String, Function<Element, View>> VIEW_CREATORS = Map.of(
                AbstractDocument.ContentElementName, LabelView::new,
                AbstractDocument.ParagraphElementName, NoWrapParagraphView::new,
                AbstractDocument.SectionElementName, elem -> new BoxView(elem, View.Y_AXIS),
                StyleConstants.ComponentElementName, ComponentView::new,
                StyleConstants.IconElementName, IconView::new
        );

        @Override
        public View create(Element elem) {
            String elementName = elem.getName();
            return VIEW_CREATORS.getOrDefault(elementName, LabelView::new).apply(elem);
        }
    }
}
