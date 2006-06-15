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

package org.omegat.filters3;

import java.util.ArrayList;

/**
 * A list of Tag's attritutes.
 *
 * @author Maxym Mykhalchuk
 */
public class Attributes
{
    ArrayList list = new ArrayList();
    
    /** Number of attributes. */
    public int size()
    {
        return list.size();
    }
    
    /** Adds an attribute to the list. */
    public void add(Attribute attr)
    {
        list.add(attr);
    }

    /** Gets one of the attributes from the list. */
    public Attribute get(int index)
    {
        return (Attribute)list.get(index);
    }

    /**
     * Returns a string representation of the list of attributes.
     * ' name1="value1" name2="value2" ...'
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        if (list.size()>0)
        {
            buf.append(' ');
            for (int i=0; i<list.size(); i++)
                buf.append(((Attribute)list.get(i)).toString());
        }
        return buf.toString();
    }
    
    
    
}
