/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Thomas CORDONNIER
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core.data;

import java.util.Collections;
import java.util.List;

/**
 * Class for store data from TMs from the <code>/tm</code> folder. They are used only for fuzzy matches.
 * <p>
 * Note that the name includes "TMX" for historical reasons; the source may not have been an actual TMX file.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas CORDONNIER
 * @author Aaron Madlon-Kay
 */
public class ExternalTMX {

    private final String name;

    private final List<PrepareTMXEntry> entries;

    ExternalTMX(String name, List<PrepareTMXEntry> entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public List<PrepareTMXEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
