package org.omegat.gui.editor.mark;

import org.omegat.core.Core;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.gui.Styles;

/**
 * Marker for all tags in segments.
 * 
 * @author Martin Fleurke
 */
public class TagMarker extends AbstractMarker {

    public TagMarker() throws Exception {
        //PAINTER = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Styles.COLOR_PLACEHOLDER);
        PAINTER = new TransparentHighlightPainter(Styles.COLOR_PLACEHOLDER, 0.2F);
        toolTip = OStrings.getString("MARKER_TAG");
        pattern = PatternConsts.getPlaceholderPattern();
    }

    @Override
    protected boolean isEnabled() {
        IEditor e = Core.getEditor();
        if (e != null && e instanceof EditorController) {
            return ((EditorController) e).getOrientation() != Document3.ORIENTATION.ALL_LTR;
        }
        return true;
    }
}
