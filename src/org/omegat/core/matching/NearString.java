/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.core.matching;

import org.omegat.core.*;

/**
 * Class to hold a single fuzzy match.
 *
 * @author Keith Godfrey
 */
public class NearString implements Comparable
{
    public NearString(StringEntry strEntry,
            int nearScore,
            byte[] nearData,
            String projName)
    {
        str = strEntry;
        score = nearScore;
        attr = nearData;
        if (projName != null)
            proj = projName;
    }
    
    public int compareTo(Object object)
    {
        NearString visitor = (NearString)object;
        return (visitor.score<score) ? -1 : 1 ;
    }
    
    public StringEntry str;
    public int score;
    public byte[] attr;	// matching attributes of near strEntry
    public String proj = ""; // NOI18N
}
