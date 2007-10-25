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

package org.omegat.filters3.xml.openxml;

import org.omegat.core.threads.CommandThread;
import org.omegat.filters3.xml.DefaultXMLDialect;
import org.omegat.filters3.xml.openxml.*;

/**
 * Dialect of Open XML files.
 *
 * @author Didier Briel
 */
public class OpenXMLDialect extends DefaultXMLDialect
{
 
    /** Creates a new instance of OpenXML */
    public OpenXMLDialect()
    {
       
        defineParagraphTags(new String[]
        {
            // Word
            "w:p",                                                              // NOI18N
            "w:tab",                                                            // NOI18N
            // Excel
            "si",                                                               // NOI18N
            // PowerPoint
            "a:p",                                                              // NOI18N
        });
        
        defineOutOfTurnTags(new String[]
        {
            // Word
            "w:instrText",                                                      // NOI18N
        });
        
        defineIntactTags(new String[]
        {
            // Excel
            "authors",                                                          // NOI18N
        });
   }
}
