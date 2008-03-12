/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3;

/**
 * One attribute of a tag.
 *
 * @author Maxym Mykhalchuk
 */
public class Attribute
{
    private String name;

    /** Returns attribute's name. */
    public String getName()
    {
        return name;
    }
    
    private String value;
    
    /** Returns attribute's value. */
    public String getValue()
    {
        return value;
    }
    /** 
     * Sets attribute's value. 
     * <p>
     * Actually an ugly hack to allow quick & dirty translation of attributes.
     * Normal solution requires too much time :-(
     */
    public void setValue(String value)
    {
        this.value = value;
    }
    
    /** Creates a new instance of Attribute */
    public Attribute(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns a string representation of the attribute.
     * name="value".
     */
    public String toString()
    {
        return name+"=\""+value+"\"";                                           // NOI18N
    }
}
