/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               2013 Alex Buloichik (alex73mail@gmail.com)
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.mark;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

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
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RemoveTagMarker extends AbstractMarker {
    HighlightPainter PAINTERrtl;
    AttributeSet ATTRIBUTESltrSource;
    AttributeSet ATTRIBUTESltrTranslation;

    public RemoveTagMarker() throws Exception {
        // PAINTER = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Styles.COLOR_REMOVETEXT_TARGET);
        PAINTERrtl = new TransparentHighlightPainter(Styles.COLOR_REMOVETEXT_TARGET, 0.2F);
        toolTip = OStrings.getString("MARKER_REMOVETAG");

        ATTRIBUTESltrSource = Styles.createAttributeSet(null, null, null, true);
        ATTRIBUTESltrTranslation = Styles.createAttributeSet(Styles.COLOR_REMOVETEXT_TARGET, null, null, null);
    }

    @Override
    protected boolean isEnabled() {
        IEditor e = Core.getEditor();
        if (e != null && e instanceof EditorController) {
            // return ((EditorController) e).getOrientation() !=
            // Document3.ORIENTATION.ALL_LTR;
        }
        return true;
    }

    @Override
    protected void initDrawers(boolean isSource, boolean isActive) {
        pattern = PatternConsts.getRemovePattern();
        if (((EditorController) Core.getEditor()).getOrientation() == Document3.ORIENTATION.ALL_LTR) {
            ATTRIBUTES = isSource ? ATTRIBUTESltrSource : ATTRIBUTESltrTranslation;
            PAINTER = null;
        } else {
            ATTRIBUTES = null;
            PAINTER = PAINTERrtl;
        }
    }
}
