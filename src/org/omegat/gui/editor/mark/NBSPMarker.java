package org.omegat.gui.editor.mark;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.gui.Styles;

/**
 * Marker for Non-breakable space
 * 
 * @author Martin Fleurke
 */
public class NBSPMarker extends AbstractMarker {
    public NBSPMarker() throws Exception {
        PAINTER = new TransparentHighlightPainter(Styles.COLOR_NBSP, 0.5F);
        toolTip = OStrings.getString("MARKER_NBSP");
        pattern = PatternConsts.NBSP;
    }
    protected boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkNBSP();
    }
}
