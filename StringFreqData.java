//-------------------------------------------------------------------------
//  
//  StringFreqData.java - 
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

class StringFreqData 
{
	public StringFreqData()
	{
		m_map = new HashMap();
		m_list = new ArrayList();
		m_counter = 0;
		m_initCap = -1;
	}

	public StringFreqData(int initialCapacity)
	{
		m_map = new HashMap(initialCapacity);
		m_list = new ArrayList(initialCapacity/2);
		m_counter = 0;
		m_initCap = initialCapacity;
	}

	protected StringFreqData(HashMap map, ArrayList lst, int ctr, int ic)
	{
		if (ic > 0)
		{
			m_map = new HashMap(ic);
			m_list = new ArrayList(ic/2);
		}
		else
		{
			m_map = new HashMap();
			m_list = new ArrayList();
		}
		m_counter = ctr;
		m_initCap = ic;

		// now do a deep copy of list and map
		StringData sd;
		for (int i=0; i<lst.size(); i++)
		{
			sd = (StringData) ((StringData) lst.get(i)).clone();
			m_list.add(sd);
			m_map.put(new Long(sd.getDigest()), sd);
		}
	}

	public int add(long key, String str)
	{
		Long k = new Long(key);
		StringData sd = (StringData) m_map.get(k);
		if (sd == null)
		{
			sd = new StringData(key, str);
			m_list.add(sd);
			m_map.put(k, sd);
		}
		else if (str != null)	
		{
			if (str.compareTo(sd.getString()) != 0)
				sd.setAttr(StringData.NEAR);
		}

		if ((sd.getCount() < 0) && (sd.hasAttr(StringData.UNIQ)))
		{
			sd.clearAttr(StringData.UNIQ);
		}
		m_counter++;
		return sd.incCount();
	}

	public int sub(long key, String str)
	{
		Long k = new Long(key);
		StringData sd = (StringData) m_map.get(k);
		if (sd == null)
		{
			sd = new StringData(key, str);
			m_list.add(sd);
			m_map.put(k, sd);
		}
		else if (str != null)	
		{
			if (str.compareTo(sd.getString()) != 0)
				sd.setAttr(StringData.NEAR);
		}

		if ((sd.getCount() > 0) && (sd.hasAttr(StringData.UNIQ)))
		{
			sd.clearAttr(StringData.UNIQ);
		}
		m_counter++;
		return sd.decCount();
	}

	public double getMatchRatio()
	{
		int i;
		int cnt = 0;
		int val;
		for (i=0; i<m_list.size(); i++)
		{
			val = ((StringData) m_list.get(i)).getCount();
			if (val < 0)
				cnt -= val;
			else
				cnt += val;
		}
		return (1.0 * (m_counter - cnt) / m_counter);
	}

	public int getCount(long key)
	{
		Long k = new Long(key);
		StringData sd = (StringData) m_map.get(k);
		if (sd == null)
			return 0;
		else
			return sd.getCount();
	}

	public StringData getObj(long key)
	{
		Long k = new Long(key);
		return (StringData) m_map.get(k);
	}

	public StringData getN(int pos)
	{
		return (StringData) m_list.get(pos);
	}

	public int len()	{ return m_list.size();			}
	public void reset()	
	{ 
		m_list.clear(); 
		m_map.clear();	
		m_counter = 0;
	}

	public Object clone()
	{
		return new StringFreqData(m_map, m_list, m_counter, m_initCap);
	}

	private HashMap m_map;
	private ArrayList m_list;
	private int	m_counter;
	private int	m_initCap;
};
