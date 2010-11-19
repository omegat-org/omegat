/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2009 Didier Briel
 
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

package org.omegat.filters3.xml.xliff;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies XLIFF XML Dialect.
 * 
 * @author Didier Briel
 */
public class XLIFFDialect extends DefaultXMLDialect {
    public XLIFFDialect() {
        defineParagraphTags(new String[] { "source", "target", });

        defineOutOfTurnTags(new String[] { "sub", });

        defineIntactTags(new String[] { "source", "header", "bin-unit", "prop-group", "count-group",
                "alt-trans", "note",
                // "mrk", only <mrk mtype="protected"> should be an intact tag
                "ph", "bpt", "ept", "it", "context", "seg-source", });

    }

    /**
     * In the XLIFF filter, the tag &lt;mrk&gt; is a preformat tag when the
     * attribute "mtype" contains "seg".
     * 
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>true</code> if this tag should be a preformat tag,
     *         <code>false</code> otherwise
     */
    public Boolean validatePreformatTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("mrk")) // We test only "mrk"
            return false;

        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (oneAttribute.getName().equalsIgnoreCase("mtype")
                        && oneAttribute.getValue().equalsIgnoreCase("seg"))
                    return true;
            }
        }
        return false;
    }

}
