/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

import org.omegat.util.ProjectFileData;

/* 
 * Source text entry represents an individual segment for
 * translation pulled directly from the input files.
 * There can be many SourceTextEntries having identical source
 * language strings
 */
public class SourceTextEntry
{
	public void set(StringEntry str, ProjectFileData file, int entryNum)
	{
		m_srcFile = file;
		m_strEntry = str;
		m_strEntry.addParent(this);
		m_entryNum = entryNum;
	}

	public ProjectFileData getSrcFile()	{ return m_srcFile;	}
//	public String getSrcFile()			{ return m_srcFile.filename;	}
	public int getFirstInFile()			{ return m_srcFile.firstEntry;	}
	public int getLastInFile()			{ return m_srcFile.lastEntry;	}

    public StringEntry getStrEntry()	{ return m_strEntry;	}
	// NOTE: the uncloned reference to m_strEntry is returned on purpose

	public String getSrcText()
	{
		return m_strEntry.getSrcText();
	}

	public String getTranslation()
	{
//		if (m_strEntry != null)
			return m_strEntry.getTrans();
//		else
//			return "";
	}

	public void setTranslation(String t)
	{
		m_strEntry.setTranslation(t);
	}

    public int entryNum()			{ return m_entryNum;	}

	private	ProjectFileData m_srcFile;
	private StringEntry m_strEntry = null;
	private int m_entryNum;
}
