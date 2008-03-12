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

package org.omegat.core.glossary;


/**
 * An entry in the glossary.
 *
 * @author Keith Godfrey
 */
public class GlossaryEntry
{
	public GlossaryEntry(String src, String loc, String com)
	{
		m_src = src;
		m_loc = loc;
		m_com = com;
	}

	public String	getSrcText()	{ return m_src;		}
	public String	getLocText()	{ return m_loc;		}
	public String	getCommentText()	{ return m_com;		}
    
	private String	m_src;
	private String	m_loc;
	private String	m_com;
}
