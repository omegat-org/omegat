//-------------------------------------------------------------------------
//  
//  IndexEntry.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  8Mar2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;

class IndexEntry extends Object
{
	public IndexEntry(String wrd)
	{
		m_word = wrd;
		m_refTree = new TreeMap();
	}

	public TreeMap getTreeMap()	
			{ return (TreeMap) m_refTree.clone();	}

	public void addReference(StringEntry ref)
	{
		// make sure reference doesn't already exist
		// (from repeated words)
		String s = String.valueOf(ref.digest());
		if (!m_refTree.containsKey(s))
			m_refTree.put(s, ref);
	}

	public String getWord()
	{
		return m_word;
	}

	private TreeMap		m_refTree;
	private String 		m_word;
}
