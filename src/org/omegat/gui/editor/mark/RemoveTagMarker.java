package org.omegat.gui.editor.mark;

import org.omegat.core.Core;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.gui.Styles;

/**
 * Marker for all parts in segments that have to be removed.
 * 
 * @author Martin Fleurke
 */
public class RemoveTagMarker extends AbstractMarker {

    public RemoveTagMarker() throws Exception {
        //PAINTER = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Styles.COLOR_REMOVETEXT_TARGET);
        PAINTER = new TransparentHighlightPainter(Styles.COLOR_REMOVETEXT_TARGET, 0.2F);
        toolTip = OStrings.getString("MARKER_REMOVETAG");
        pattern = PatternConsts.getRemovePattern();
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
