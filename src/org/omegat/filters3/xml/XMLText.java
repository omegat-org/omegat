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

import org.omegat.filters3.Text;
import org.omegat.util.StaticUtils;

/**
 * Piece of text in XML.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLText extends Text
{
    private boolean inCDATA;
    /** Whether this text is inside XDATA section. */
    public boolean isInCDATA()
    {
        return inCDATA;
    }
    
    /** Creates a piece of XML text. */
    public XMLText(String text, boolean inCDATA)
    {
        super(text);
        this.inCDATA = inCDATA;
    }
    
    /**
     * Returns the text in its original form as it was in original document.
     * E.g. for <code>Rock&Roll</code> should return 
     * <code>Rock&amp;Roll</code>.
     */
    public String toOriginal() 
    {
        if (inCDATA)
        {
            StringBuffer res = new StringBuffer();
            res.append("<![CDATA[");                                            // NOI18N
            res.append(getText());
            res.append("]]>");                                                  // NOI18N
            return res.toString();
        }
        else
            return StaticUtils.makeValidXML(getText());
    }

    /**
     * Creates a new instance of XMLText class.
     */
    public Text createInstance(String text) 
    {
        return new XMLText(text, inCDATA);
    }
}
