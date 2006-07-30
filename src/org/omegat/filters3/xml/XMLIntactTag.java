/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3.xml;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.Element;
import org.omegat.filters3.Entry;
import org.omegat.filters3.Tag;
import org.omegat.util.StaticUtils;

/**
 * XML Tag that surrounds intact portions of XML document.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLIntactTag extends Tag
{
    private XMLIntactEntry intactContents = new XMLIntactEntry();
    
    /** Returns the entry to collect text surrounded by intact tag. */
    public XMLIntactEntry getIntactContents()
    {
        return intactContents;
    }
    
    /** Creates a new instance of XML Tag */
    public XMLIntactTag(String tag, String shortcut, org.xml.sax.Attributes attributes)
    {
        super(tag, shortcut, TYPE_ALONE, XMLUtils.convertAttributes(attributes));
    }
    
    /**
     * Returns this tag and the intact contents it surrounds.
     * E.g. for 
     * <pre>&lt;style&gt;<br>html {<br>&nbsp;&nbsp;&nbsp;background-color: white;<br>}<br>&lt;/style&gt;</pre>
     * should return absolutely the same.
     */
    public String toOriginal() 
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<");                                                        // NOI18N
        buf.append(getTag());
        buf.append(getAttributes().toString());
        buf.append(">");                                                        // NOI18N
        
        buf.append(intactContents.sourceToOriginal());
        
        buf.append("<");                                                        // NOI18N
        buf.append("/");                                                        // NOI18N
        buf.append(getTag());
        buf.append(">");                                                        // NOI18N
        
        return buf.toString();
    }
}

