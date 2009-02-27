/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import javax.swing.text.Element;
import javax.swing.text.LabelView;

/**
 * Class for display segmentation marks. It better to paint marks by own
 * component, because we will not have problems with RTL writing in this case.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewSegmentMark extends LabelView {
    private final boolean beginMark;

    public ViewSegmentMark(Element elem) {
        super(elem);
        this.beginMark = ((OmDocument.OmElementSegmentMark) elem).isBeginMark;
    }

    public boolean isBeginMark() {
        return beginMark;
    }

    /**
     * Do not break view by several parts, because we will not be able to align
     * it in RTL presentation.
     */
    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        return BadBreakWeight;
    }
}
