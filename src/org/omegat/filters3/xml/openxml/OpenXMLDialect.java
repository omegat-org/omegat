/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
               2010 Antonio Vilei
               2011 Didier Briel
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

package org.omegat.filters3.xml.openxml;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * Dialect of Open XML files.
 * 
 * @author Didier Briel
 * @author Antonio Vilei
 */
public class OpenXMLDialect extends DefaultXMLDialect {

    /**
     * Actually defines the dialect. It cannot be done during creation, because
     * options are not known at that step.
     */
    public void defineDialect(OpenXMLOptions options) {
        defineParagraphTags(new String[] {
                // Word
                "w:p", "w:tab", "w:br",
                // Excel
                "si", "comment", "definedName",
                // PowerPoint
                "a:p", "c:v", });

        if (options.getTranslateHiddenText()) // Word
            defineOutOfTurnTag("w:instrText");
        else
            defineIntactTag("w:instrText");

        defineIntactTags(new String[] {
                // Excel
                "authors",
                // PowerPoint
                "p:attrName", "a:tableStyleId",
                // Charts
                "c:f", "c:formatCode",
                // Word
                "wp:align", "wp:posOffset",
                // Drawings
                "xdr:col", "xdr:row", "xdr:colOff", "xdr:rowOff", });

        defineTranslatableTagAttribute("sheet", "name"); // Excel

        boolean aggregationEnabled = options.getAggregateTags();
        /*
         * The current OpenXML filter finds too many tags, usually causing what
         * users call the "tag soup". Tags aggregation can help alleviate this
         * problem, but can sometimes lead to semantic issues. Aggregation is OK
         * only as a temporary hack, until we improve the OpenXML filter.
         */
        setTagsAggregationEnabled(aggregationEnabled);

        // If defined in the options, set space preserving for all tags
        setForceSpacePreserving(options.getSpacePreserving());
    }
}
