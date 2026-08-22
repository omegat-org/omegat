/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               2013 Alex Buloichik (alex73mail@gmail.com)
               2015 Aaron Madlon-Kay
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

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.gui.Styles;

/**
 * Marker for all parts in segments that have to be removed.
 *
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class RemoveTagMarker extends AbstractMarker {

    public RemoveTagMarker() throws Exception {
        toolTip = OStrings.getString("MARKER_REMOVETAG");
        CoreEvents.registerProjectChangeListener(e -> pattern = PatternConsts.getRemovePattern());
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    protected void initDrawers(boolean isSource, boolean isActive) {
        // The target attributes bind the foreground to the palette entry so
        // the mark is not shadowed by the bound state color of the segment
        // text (see Mark#attributes). The painter still snapshots the color
        // and is created per call so that preference changes take effect.
        if (Core.getEditor().isOrientationAllLtr()) {
            attributes = isSource ? Styles.createAttributeSet(null, null, null, true)
                    : Styles.createBoundAttributeSet(Styles.EditorColor.COLOR_REMOVETEXT_TARGET,
                            null, null, null);
            painter = null;
        } else {
            attributes = null;
            painter = new TransparentHighlightPainter(
                    Styles.EditorColor.COLOR_REMOVETEXT_TARGET.getColor(), 0.2f);
        }
    }
}
