//-------------------------------------------------------------------------
//  
//  StringData.java - 
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
//  Build date:  21Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.zip.*;

// tracks usage and frequency of words and word pairs
class StringData 
{
	public StringData(long key, String str)
	{
		m_cnt = 0;
		m_orig = str;
		m_attr = UNIQ;
		m_digestLow = 0;
		m_digestHigh = 0;
		m_digest = key;
	}

	public StringData(int c, String s, byte a, long dl, long dh, long d)
	{
		m_cnt = c;
		m_orig = s;
		m_attr = a;
		m_digestLow = dl;
		m_digestHigh = dh;
		m_digest = d;
	}

	public Object clone()
	{
		return new StringData(m_cnt, m_orig, m_attr, m_digestLow,
				m_digestHigh, m_digest);
	}

	public void setHigh(long dig)	{ m_digestHigh = dig;	}
	public void setLow(long dig)	{ m_digestLow = dig;	}
	public long getHigh()		{ return m_digestHigh;	}
	public long getLow()		{ return m_digestLow;	}

	public long	getDigest()		{ return m_digest;	}
	public String	getString()		{ return m_orig;	}
	public byte	getAttr()		{ return m_attr;	}
	public void	setAttr(byte b)		{ m_attr |= b;		}
	public void	clearAttr(byte b)	{ m_attr &= ~b;		}
	public boolean	hasAttr(byte b)		{ return ((b & m_attr) != 0); }

	public int	getCount()		{ return m_cnt;		}
	public int	incCount()		{ return ++m_cnt;	}
	public int	decCount()		{ return --m_cnt;	}

	public boolean isUnique()
	{
		return ((m_attr & UNIQ) != 0);
	}

	// 
	// uniq flag set indicates that a given string doesn't occur 
	//    elsewhere, flag clear indicates it has a (at least one) partner
	// near flag set indicates that a string is not exactly like 
	//    it's neigbors, clear indicates all are alike (i.e. same style)
	// pair flag set indicates that word pair represented by either 
	//    digest low or high is not unique
	public static final byte UNIQ = 0x01;
	public static final byte NEAR = 0x02;
	public static final byte PAIR = 0x04;

	private String	m_orig;
	private byte	m_attr;
	private int	m_cnt;
	
	private long	m_digestHigh;
	private long	m_digestLow;
	private long	m_digest;
}
