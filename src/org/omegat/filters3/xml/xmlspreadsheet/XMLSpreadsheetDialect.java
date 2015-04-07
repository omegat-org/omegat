/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2011-2012 Didier Briel
               2015 Didier Briel
 
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

package org.omegat.filters3.xml.xmlspreadsheet;

import java.util.regex.Pattern;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the XML Spreadsheet Dialect.
 * 
 * @author Didier Briel
 */
public class XMLSpreadsheetDialect extends DefaultXMLDialect {
    public static final Pattern ROOT_PATTERN = Pattern.compile("Workbook");
    
    public XMLSpreadsheetDialect() {
        defineConstraint(CONSTRAINT_ROOT, ROOT_PATTERN);
        defineParagraphTags(new String[] { "Workbook", "Cell", });
        defineIntactTags(new String[] { "DocumentProperties", "ExcelWorkbook", "WorksheetOptions", });

    }
    
    /**
     * In the XML Spreadsheet filter, content should not be translated in the following
     * condition: When the tag is &lt;Data&gt; with the attribute ss:Type="Number". 
     * 
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("Data")) // We test only "data"
            return false;

        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (oneAttribute.getName().equalsIgnoreCase("ss:type") &&
                    oneAttribute.getValue().equalsIgnoreCase("number") )
                    return true;
            }
        }
        return false;
    }

}
