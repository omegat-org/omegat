/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3.xml.opendoc;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OConsts;

/**
 * Filter for OpenDocument XML files that are inside there the OpenDocument file
 * (which is actually a ZIP file).
 * 
 * @author Maxym Mykhalchuk
 */
public class OpenDocXMLFilter extends XMLFilter {

    /** Creates a new instance of OpenDocXMLFilter */
    public OpenDocXMLFilter() {
        super(new OpenDocDialect());
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml", OConsts.UTF8, OConsts.UTF8), };
    }

    public String getFileFormatName() {
        throw new RuntimeException("Not implemented!");
    }

}
