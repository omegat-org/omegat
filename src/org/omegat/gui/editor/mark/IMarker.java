/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.gui.editor.mark;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.Mark;

/**
 * Interface for calculate marks in editor.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IMarker {
    /**
     * Calculate marks for inactive entry.
     * 
     * Method will be called NOT in Swing thread.
     * 
     * @param entry
     * @return
     */
    List<Mark> getMarksForInactiveEntry(SourceTextEntry entry);
}
