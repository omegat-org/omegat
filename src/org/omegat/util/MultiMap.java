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

package org.omegat.util;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A map that maps keys to sets of values.
 * Does NOT allow null keys/values.
 *
 * @author Maxym Mykhalchuk
 */
public class MultiMap
{
    /** We're backed up by a HashMap<key, HashSet>. */
    HashMap map;
    
    /** Creates an empty MultiMap. */
    public MultiMap()
    {
        map = new HashMap();
    }
            
    /** Returns <tt>true</tt> if this map contains a mapping for the specified key. */
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }
    
    /** Returns <tt>true</tt> if this map maps the specified key to the specified value. */
    public boolean containsPair(Object key, Object value)
    {
        if (containsKey(key))
        {
            HashSet values = (HashSet)map.get(key);
            return values.contains(value);
        }
        else
            return false;
    }
    
    /** 
     * Associates the specified value with the specified key in this multi-map. 
     * Unlike normal Map, if the map previously contained a mapping for
     * this key, the new value is appended to the list of the values
     * mapped from this key.
     */
    public void put(Object key, Object value)
    {
        if (containsKey(key))
        {
            HashSet values = (HashSet)map.get(key);
            values.add(value);
        }
        else
        {
            HashSet values = new HashSet();
            values.add(value);
            map.put(key, values);
        }
    }
    
    /** Removes all the mappings for this key from this map if it is present. */
    public void remove(Object key)
    {
        map.remove(key);
    }
    
}
