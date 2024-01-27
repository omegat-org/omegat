/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2011 Didier Briel

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

package org.omegat.filters3.xml.resx;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the ResX XML Dialect.
 *
 * @author Didier Briel
 */
public class ResXDialect extends DefaultXMLDialect {
    public ResXDialect() {
        defineParagraphTags(new String[] { "value", });

        defineIntactTags(new String[] { "resheader", "metadata", "comment", });
    }

    /**
     * In the ResX filter, content should be translated in the following
     * condition: It should be contained in &lt;data&gt;. If there is the
     * attribute "type" or "mimetype", the content shouldn't be translated. If
     * there is the attribute "name", the content shouldn't be translated if the
     * content of "name" starts with &gt; or ends with "FieldName"
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("data")) {
            return false;
        }
        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (oneAttribute.getName().equalsIgnoreCase("type")
                        || oneAttribute.getName().equalsIgnoreCase("mimetype")
                        || (oneAttribute.getName().equalsIgnoreCase("name") && (oneAttribute.getValue()
                                .startsWith("&gt;") || oneAttribute.getValue().endsWith("FieldName")))) {
                    return true;
                }
            }
        }
        return false;
    }
}
