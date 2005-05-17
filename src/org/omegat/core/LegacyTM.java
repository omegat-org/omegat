/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.core;

import java.util.List;

/**
 * Represents a legacy Translation Memory file.
 * These files generally reside in /tm subfolder of the project folder. 
 * 
 * @author Maxym Mykhalchuk
 */
public class LegacyTM
{
    
    /** name of TMX file */
    private String tmxname;
    /** list of StringEntry objects of the TM */
    private List tmstrings;
    
    /** 
     * Creates a new instance of Legacy Translation Memory
     *
     * @param tmxname     name of TMX file
     * @param tmstrings   list of StringEntry objects of the TM
     */
    public LegacyTM(String tmxname, List tmstrings)
    {
        this.tmxname = tmxname;
        this.tmstrings = tmstrings;
    }
    
    /** Returns the name of this legacy TMX file */
    public String getName()   { return tmxname; }
    
    /** Returns the list of StringEntry-es of this Translation Memory */
    public List getStrings()  { return tmstrings; }
    
}
