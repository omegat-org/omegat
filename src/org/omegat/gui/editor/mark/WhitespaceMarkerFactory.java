/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2010 Wildrich Fourie
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
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

    /**
     * Marker for a normal whitespace.
     * 
     * @author Martin Fleurke
     */
    public static class SpaceMarker extends AbstractMarker {
        public SpaceMarker() throws Exception {
            PAINTER = new SymbolPainter(Styles.COLOR_WHITESPACE, "\u00B7"); //·•
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
            PAINTER = new SymbolPainter(Styles.COLOR_WHITESPACE, "\u00BB"); //»
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
     * There is a linefeed symbol: '␊'. But it is so small / hard to see, 
     * that instead we use '¶' as the symbol to show, like other applications do.
     * 
     * @author Martin Fleurke
     */
    public static class LFMarker extends AbstractMarker {
        public LFMarker() throws Exception {
            PAINTER = new SymbolPainter(Styles.COLOR_WHITESPACE, "\u00B6"); //¶␊
            toolTip = "LF";
            patternChar = '\n';
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkWhitespace();
        }
    }
    //no need for CR marker. There are no CR's.
}
