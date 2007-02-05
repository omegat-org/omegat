/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2007 Didier Briel
 
               Home page: http://www.omegat.org/omegat/omegat.html
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

import java.util.regex.Pattern;

import org.xml.sax.InputSource;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies XLIFF XML Dialect.
 *
 * @author Didier Briel
 */
public class XLIFFDialect extends DefaultXMLDialect
{
    public XLIFFDialect()
    {      
        defineParagraphTags(new String[]
        {
            "source",                                                           // NOI18N
            "target",                                                           // NOI18N
        });
        
        defineOutOfTurnTags(new String[]
        {
            "sub",                                                               // NOI18N
        });
      
        defineIntactTags(new String[]
        {
            "source",                                                           // NOI18N
            "header",                                                           // NOI18N
            "bin-unit",                                                         // NOI18N
            "prop-group",                                                       // NOI18N
            "count-group",                                                      // NOI18N
            "alt-trans",                                                        // NOI18N
            "note",                                                             // NOI18N
            "mrk",                                                              // NOI18N
            "ph",                                                               // NOI18N
            "bpt",                                                              // NOI18N
            "ept",                                                              // NOI18N
            "it",                                                               // NOI18N
        });
    }
}
