//-------------------------------------------------------------------------
//  
//  EntryData.java - 
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
//  Build date:  23Feb2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;

// EntryData is an information package used to transmit data back
//  and forth between internal processing elements.  There is no
//  master list of EntryData objects within a project - see
//  SourceTextEntries for the actual project data
class EntryData
{
	public EntryData()
	{
		entryNum = -1;
		srcText = "";
		trans = "";
		file = "";
		glosTerms = null;
		nearTerms = null;	//new LinkedList();
		totalWords = 0;
		partialWords = 0;
		currentWords = 0;
	}

	public void addNearTerms(LinkedList nearStrings)
	{
		if (nearTerms == null)
			nearTerms = new LinkedList();

		nearTerms.clear();
		NearString near;

		// only add terms that have translations
		ListIterator it = nearStrings.listIterator(0);
		while (it.hasNext())
		{
			near = (NearString) it.next();
			if (near.str.getTrans().equals("") == false)
				nearTerms.add(near);
		}
	}

	public void setGlosTerms(LinkedList glosStrings)
	{
		glosTerms = (LinkedList) glosStrings.clone();
	}

	public LinkedList getNearTermsClone()
	{
		if (nearTerms != null)
			return (LinkedList) nearTerms.clone();
		else
			return null;
	}

	public int getNearTermsSize()
	{
		if (nearTerms == null)
			return 0;
		else
			return nearTerms.size();
	}
	
	public LinkedList getNearTerms()	
	{
		if (nearTerms == null)
		{
			nearTerms = new LinkedList();
		}
		return nearTerms;
	}

	public int	entryNum;
	public String	srcText;
	public String	trans;
	public String	file;
	// these return StringPair references
	public LinkedList	glosTerms;
	private LinkedList	nearTerms;
	public int		totalWords;
	public int		partialWords;
	public int		currentWords;
}
