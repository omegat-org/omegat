/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
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

import javax.swing.Icon;

import org.omegat.core.Core;
import org.omegat.gui.main.MainMenuIcons;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.Styles;

/**
 * Marker for Non-breakable space
 *
 * @author Martin Fleurke
 */
public class NBSPMarker extends AbstractMarker {

    public NBSPMarker() throws Exception {
        painter = new TransparentHighlightPainter(Styles.EditorColor.COLOR_NBSP.getColor(), 0.5F);
        toolTip = OStrings.getString("MARKER_NBSP");
        patternChar = '\u00a0';
    }

    public boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkNBSP();
    }

    public void setEnabled(boolean val) {
        Core.getEditor().getSettings().setMarkNBSP(val);
    }

    @Override
    public String getMarkerName() {
        return OStrings.getString("MW_VIEW_MENU_MARK_NBSP");
    }

    @Override
    public String getPreferenceKey() {
        return Preferences.MARK_NBSP;
    }

    @Override
    public Icon getIcon() {
        return MainMenuIcons.newColorIcon(Styles.EditorColor.COLOR_NBSP.getColor());
    }
}
