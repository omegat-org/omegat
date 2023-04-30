/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;

/**
 * Interface to calculate marks in editor.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IMarker {
    /**
     * Calculate marks for specific entry.
     * <p>
     * Method will be called NOT in Swing thread.
     * <p>
     * Note to implementers: Both <code>sourceText</code> and <code>translationText</code> might be null!

     * @param ste
     * @param sourceText might be null!
     * @param translationText might be null!
     * @param isActive is this an active segment in the document?
     * @return null if nothing changed, or list of new marks. Empty list must be returned if marks shouldn't
     *         be displayed
     * @throws Exception
     */
    List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception;
}
