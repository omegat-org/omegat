/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               2025 Hiroshi Miura
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
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.util.Map;
import java.util.function.Function;

public class ViewFactoryHelper implements ViewFactory {

    private final Map<String, Function<Element, View>> viewCreators;
    private final Function<Element, View> defaultViewCreator;

    public ViewFactoryHelper(Map<String, Function<Element, View>> customMappings, Function<Element, View> defaultViewCreator) {
        this.viewCreators = customMappings;
        this.defaultViewCreator = defaultViewCreator;
    }

    @Override
    public View create(Element elem) {
        String elementName = elem.getName();
        // Use the provided mappings or fall back to the default view
        return viewCreators.getOrDefault(elementName, defaultViewCreator).apply(elem);
    }

    public static Map<String, Function<Element, View>> getDefaultMappings(Function<Element, View> contentViewCreator,
                                                                          Function<Element, View> paragraphViewCreator) {
        return Map.of(
                AbstractDocument.ContentElementName, contentViewCreator,
                AbstractDocument.ParagraphElementName, paragraphViewCreator,
                AbstractDocument.SectionElementName, elem -> new BoxView(elem, View.Y_AXIS),
                StyleConstants.ComponentElementName, ComponentView::new,
                StyleConstants.IconElementName, IconView::new
        );
    }
}
