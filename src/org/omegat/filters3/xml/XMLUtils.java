/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
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

package org.omegat.filters3.xml;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.util.StaticUtils;

/**
 * Static XML utility methods.
 * 
 * @author Maxym Mykhalchuk
 */
public final class XMLUtils {

    /** Private to disallow creation. */
    private XMLUtils() {
    }

    /** Converts attributes from org.xml.sax package to OmegaT's. */
    public static Attributes convertAttributes(org.xml.sax.Attributes attributes) {
        Attributes res = new Attributes();
        if (attributes == null)
            return res;

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = StaticUtils.makeValidXML(attributes.getQName(i));
            String value = StaticUtils.makeValidXML(attributes.getValue(i));
            Attribute attr = new Attribute(name, value);
            res.add(attr);
        }
        return res;
    }

}
