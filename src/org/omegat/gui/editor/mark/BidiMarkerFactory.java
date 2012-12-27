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

import java.util.regex.Pattern;
import org.omegat.core.Core;
import org.omegat.util.gui.Styles;

/**
 * Collection of Markers for Bidirectional control characters.
 * 
 * @author Martin Fleurke
 */
public class BidiMarkerFactory {

    /**
     * Marker for Right-to-Left Marker
     * @author Martin Fleurke
     */
    public static class RLMMarker extends AbstractMarker {
        public RLMMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u200F");
            toolTip = "RLM";
            pattern = Pattern.compile("\u200F");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Left-to-Right Marker
     * @author Martin Fleurke
     */
    public static class LRMMarker extends AbstractMarker {
        public LRMMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u200E");
            toolTip = "LRM";
            pattern = Pattern.compile("\u200E");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Left-to-Right Embedding
     * @author Martin Fleurke
     */
    public static class LREMarker extends AbstractMarker {
        public LREMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u202A");
            toolTip = "LRE";
            pattern = Pattern.compile("\u202A");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Right-to-Left Embedding
     * @author Martin Fleurke
     */
    public static class RLEMarker extends AbstractMarker {
        public RLEMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u202B");
            toolTip = "RLE";
            pattern = Pattern.compile("\u202B");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Pop Directional Formatting
     * @author Martin Fleurke
     */
    public static class PDFMarker extends AbstractMarker {
        public PDFMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u202C");
            toolTip = "PDF";
            pattern = Pattern.compile("\u202C");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Left-to-Right Override
     * @author Martin Fleurke
     */
    public static class LROMarker extends AbstractMarker {
        public LROMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u202D");
            toolTip = "LRO";
            pattern = Pattern.compile("\u202D");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
    /**
     * Marker for Right-to-Left Override
     * @author Martin Fleurke
     */
    public static class RLOMarker extends AbstractMarker {
        public RLOMarker() throws Exception {
            PAINTER = new BidiPainter(Styles.COLOR_BIDIMARKERS, "\u202E");
            toolTip = "RLO";
            pattern = Pattern.compile("\u202E");
        }
        protected boolean isEnabled() {
            return Core.getEditor().getSettings().isMarkBidi();
        }
    }
}
