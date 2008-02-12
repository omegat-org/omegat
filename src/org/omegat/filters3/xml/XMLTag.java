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

import org.omegat.filters3.Tag;

/**
 * XML Tag.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLTag extends Tag
{
    /** Creates a new instance of XML Tag */
    public XMLTag(String tag, String shortcut, int type, org.xml.sax.Attributes attributes)
    {
        super(tag, shortcut, type, XMLUtils.convertAttributes(attributes));
    }
    
    /**
     * Returns the tag in its original form as it was in original document.
     * E.g. for &lt;strong&gt; tag should return 
     * &lt;strong&gt;.
     */
    public String toOriginal() 
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<");                                                        // NOI18N
        if (TYPE_END == getType())
            buf.append("/");                                                    // NOI18N
        buf.append(getTag());
        buf.append(getAttributes().toString());
        if (TYPE_ALONE == getType())
            buf.append("/");                                                    // NOI18N
        buf.append(">");                                                        // NOI18N
        
        return buf.toString();
    }

}

