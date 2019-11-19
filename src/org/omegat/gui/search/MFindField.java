/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey
               2006 Henry Pijffers
               2009-2012 Didier Briel
               2013-2014 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.search;

import java.awt.Font;

import org.omegat.util.gui.HintTextField;
import org.omegat.util.gui.StaticUIUtils;

/**
 * "Default text" feature inspired by http://stackoverflow.com/a/1739037/448068
 *
 *  @author Keith Godfrey
 *  @author Henry Pijffers (henry.pijffers@saxnot.com)
 *  @author Didier Briel
 *  @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class MFindField extends HintTextField {

    private final Font normalFont;

    public MFindField() {
        normalFont = getFont();
        StaticUIUtils.makeUndoable(this);
    }

    @Override
    protected void applyHintStyle() {
        super.applyHintStyle();
        setFont(normalFont.deriveFont(Font.ITALIC));
    }

    @Override
    protected void restoreNormalStyle() {
        setFont(normalFont);
        super.restoreNormalStyle();
    }
}
