/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2009 Didier Briel, Guido Leenders
 
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

package org.omegat.filters3.xml.helpandmanual;

import java.util.regex.Pattern;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Help&Manual XML Dialect.
 *
 * @author Guido Leenders
 * @author Didier Briel
 */
public class HelpAndManualDialect extends DefaultXMLDialect
{
    public static final Pattern HAM_ROOT_TAG = Pattern.compile("topic|map|helpproject");

    public HelpAndManualDialect()
    {
        defineConstraint(CONSTRAINT_ROOT, HAM_ROOT_TAG);

        defineParagraphTags(new String[] {
            "caption", "config-value", "variable", "para", "title", "keyword", "li", 
        });

        defineShortcut("link", "li");
    }
                                       
}
