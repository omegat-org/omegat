/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2010 Wildrich Fourie
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

package org.omegat.gui.editor.mark;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles;

/**
 * Collection of Markers for whitespace symbols.
 *
 * @author Martin Fleurke
 */
public class WhitespaceMarkerFactory {

    public static void init() throws Exception {
        Core.registerMarker(new SpaceMarker());
        Core.registerMarker(new TabMarker());
        Core.registerMarker(new LFMarker());
    }

    /**
     * Marker for a normal whitespace.
     *
     * @author Martin Fleurke
     */
    public static class SpaceMarker extends AbstractMarker {
        public SpaceMarker() throws Exception {
            painter = new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(), "\u00B7");
            toolTip = null; //don't overdo it. Space occurs many times.
            patternChar = ' ';
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkWhitespace();
        }
    }
    /**
     * Marker for tab
     * @author Martin Fleurke
     */
    public static class TabMarker extends AbstractMarker {
        public TabMarker() throws Exception {
            painter = new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(), "\u00BB");
            toolTip = OStrings.getString("MARKER_TAB");
            patternChar = '\t';
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkWhitespace();
        }
    }
    /**
     * Marker for linefeed.
     *
     * There is a linefeed symbol: U+240A. But it is so small / hard to see,
     * that instead we use U+00B6 as the symbol to show, like other applications do.
     *
     * @author Martin Fleurke
     */
    public static class LFMarker extends AbstractMarker {
        public LFMarker() throws Exception {
            painter = new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(), "\u00B6");
            toolTip = "LF";
            patternChar = '\n';
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkWhitespace();
        }
    }
    //no need for CR marker. There are no CR's.
}
