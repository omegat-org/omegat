/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               2026 Stephan Pakebusch
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

package org.omegat.gui.editor.mark;

import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles;

/**
 * Marker for no-break spaces.
 *
 * Marks the regular NO-BREAK SPACE (U+00A0), the NARROW NO-BREAK SPACE
 * (U+202F), which is the recommended space before tall punctuation in
 * French typography and therefore common in translated text, and the
 * FIGURE SPACE (U+2007), a non-breaking space as wide as a digit that is
 * used to group digits in numbers.
 *
 * @author Martin Fleurke
 * @author stephan.pakebusch at zollsoft.de
 */
public class NBSPMarker extends AbstractMarker {
    public NBSPMarker() throws Exception {
        toolTip = OStrings.getString("MARKER_NBSP");
        pattern = Pattern.compile("[\u00a0\u202f\u2007]");
    }

    protected boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkNBSP();
    }

    @Override
    protected void initDrawers(boolean isSource, boolean isActive) {
        // created per call so that color preference changes take effect
        // without restarting the application
        painter = new TransparentHighlightPainter(Styles.EditorColor.COLOR_NBSP.getColor(), 0.5F);
    }
}
