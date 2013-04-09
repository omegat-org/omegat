/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

/**
 * Class for store information about one mark.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class Mark {
    public enum ENTRY_PART {
        SOURCE, TRANSLATION
    };

    public final ENTRY_PART entryPart;
    public final int startOffset, endOffset;

    /**
     * Painter for specific Mark. For better performance, Painter should be
     * instantiated once, then used always. It could be created in IMarker
     * constructor.
     */
    public HighlightPainter painter;
    /**
     * Tooltip text for specific Mark. Will be displayed when mouse will moving
     * over Mark.
     */
    public String toolTipText;
    /**
     * Text attributes for specific Mark. Will be added to text by
     * Document.setCharacterAttributes() without replacement.
     */
    public AttributeSet attributes;

    public Mark(ENTRY_PART entryPart, int start, int end) {
        this.entryPart = entryPart;
        this.startOffset = start;
        this.endOffset = end;
    }
}
