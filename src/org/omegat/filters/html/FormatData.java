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

package org.omegat.filters.html;

import org.omegat.filters.LBuffer;

public class FormatData
{
	public FormatData()
	{
		init();
	}

	public FormatData(boolean isCloseTag)
	{
		init();
		m_isTag = true;
		m_isCloseTag = isCloseTag;
	}

	public void setOrig(LBuffer data)
	{
		m_orig.reset();
		if (m_isTag)
			m_orig.append('<');
		if (m_isCloseTag)
			m_orig.append('/');
		m_orig.append(data);
	}

    public void finalize()
	{
		// finish tags by adding trailing '>'
		if (!m_isTag)
			return;
		if (m_isFinalized)
			return;

		m_orig.append('>');
		// don't finalize display if there's nothing there
		if (m_display.size() > 0)
			m_display.append('>');
		m_isFinalized = true;
	}

    public void appendOrig(char c)
	{
		if ((m_isTag) && (m_orig.size() == 0))
		{
			m_orig.append('<');
			if (m_isCloseTag)
				m_orig.append('/');
		}
		m_orig.append(c);
	}

    public void appendDisplay(char c)
	{
		if ((m_isTag) && (m_display.size() == 0))
		{
			m_display.append('<');
			if (m_isCloseTag)
				m_display.append('/');
		}
		m_display.append(c);
	}

	public LBuffer getDisplay()
	{
		LBuffer buf = m_display;
		if (m_display.size() == 0)
			buf = m_orig;
		return buf;
	}

	public LBuffer getOrig()
	{
		return m_orig;
	}

	public void setTagData(char shortcut, int num)
	{
		m_tagType = num & 0x0000ffff;
		m_tagType += shortcut << 16;
		appendDisplay(shortcut);
		m_display.appendInt(num);
	}

	public int tagData()		{ return m_tagType;	}
	public boolean isTag()		{ return m_isTag;	}
	public boolean isCloseTag()	{ return m_isCloseTag;	}

	public void setHasText(boolean b)	{ m_hasText = b;	}

	public boolean isWhiteSpace()
	{
		if (!m_isTag && !m_hasText)
			return true;
		return false;
	}

	private void init()
	{
		m_orig = new LBuffer(32);
		m_display = new LBuffer(32);
		m_isTag = false;
		m_isCloseTag = false;
		m_isFinalized = false;
		m_tagType = 0;
	}

	private LBuffer		m_orig;
	private LBuffer		m_display;
	
	private boolean		m_isTag;
	private boolean		m_isCloseTag;
	private boolean		m_hasText;
	private boolean		m_isFinalized;
	private int		m_tagType;
}
