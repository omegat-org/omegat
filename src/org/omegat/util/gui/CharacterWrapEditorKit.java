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

import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * An editor kit that allows wrapping at character boundaries rather than word boundaries.
 *
 * @author Aaron Madlon-Kay
 * @see <a href="http://stackoverflow.com/a/13375811/448068">StackOverflow: Wrap long words in JTextPane (Java 7)</a>
 */
@SuppressWarnings("serial")
public class CharacterWrapEditorKit extends StyledEditorKit {

    private static final ViewFactory FACTORY = new ViewFactoryHelper(
            ViewFactoryHelper.getDefaultMappings(CharacterWrapLabelView::new, ParagraphView::new),
            CharacterWrapLabelView::new // Default fallback to CharacterWrapLabelView
    );

    @Override
    public ViewFactory getViewFactory() {
        return FACTORY;
    }

    static class CharacterWrapLabelView extends LabelView {

        CharacterWrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
            case View.X_AXIS:
                return 0;
            case View.Y_AXIS:
                return super.getMinimumSpan(axis);
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }
}
