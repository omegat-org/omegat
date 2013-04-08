/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2013 Zoltan Bartko
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
package org.omegat.gui.matches;

import org.omegat.core.matching.NearString;

/**
 * Interface for access to editor functionality.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Zoltan Bartko
 */
public interface IMatcher {
    /**
     * Get selected match.
     * 
     * Must be called from UI thread only.
     */
    NearString getActiveMatch();

    /**
     * Set specified mathc as active.
     * 
     * @param index
     *            new active match
     * 
     *            Must be called from UI thread only.
     */
    void setActiveMatch(int index);
    
    String substituteNumbers(String source, String sourceMatch, String targetMatch);

    /**
     * Make the next match active
     */
    public void setNextActiveMatch();

    /**
     * Make the previous match active
     */
    public void setPrevActiveMatch();
}
