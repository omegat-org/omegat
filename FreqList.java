//-------------------------------------------------------------------------
//  
//  FreqList.java - 
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

// keeps track of how many times objects occur
class FreqList 
{
	public FreqList()
	{
		m_map = new HashMap();
		m_list = new ArrayList();
	}

	public FreqList(int initialCapacity)
	{
		m_map = new HashMap(initialCapacity);
		m_list = new ArrayList(initialCapacity/2);
	}

	public int add(Object key)
	{
		FreqObj fo = (FreqObj) m_map.get(key);
		if (fo == null)
		{
			fo = new FreqObj(key);
			m_list.add(fo);
		}
		fo.cnt++;
		m_map.put(key, fo);
		return fo.cnt;
	}

	public int getCount(Object key)
	{
		FreqObj fo = (FreqObj) m_map.get(key);
		if (fo == null)
			return 0;
		else
			return fo.cnt;
	}

	public int getCountN(int num)
	{
		FreqObj fo = (FreqObj) m_list.get(num);
		if (fo == null)
			return 0;
		else
			return fo.cnt;
	}

	public Object getObj(int num)
	{
		FreqObj fo = (FreqObj) m_list.get(num);
		if (fo == null)
			return null;
		else
			return fo.obj;
	}

	public int len()	{ return m_list.size();			}
	public void reset()	{ m_list.clear(); m_map.clear();	}

	class FreqObj {
		public FreqObj(Object o)	{ cnt = 0; obj = o;	}
		public int	cnt;
		public Object	obj;
	};

	private HashMap m_map;
	private ArrayList m_list;

////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		FreqList lst = new FreqList();
		int i;
		String s;
		for (i=0; i<10; i++)
		{
			s = new String(String.valueOf(i));
			lst.add(s);
			if (i%4 == 0)
				lst.add(s);
			if (i%3 == 0)
				lst.add(s);
			if (i%2 == 0)
				lst.add(s);
		}
		for (i=0; i<10; i++)
		{
			s = new String(String.valueOf(i));
			System.out.println(i+": "+lst.getCount(s));
		}
		for (i=0; i<10; i++)
		{
			s = (String) lst.getObj(i);
			System.out.println(i+": "+lst.getCountN(i)+"("+s+")");
		}
		System.out.println("total count: " + lst.len());
	}
};
