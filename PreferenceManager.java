//-------------------------------------------------------------------------
//  
//  PreferenceManager.java - 
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


import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

class PreferenceManager
{
	public PreferenceManager()
	{
		m_loaded = false;
		m_preferenceMap = new HashMap(64);
		m_nameList = new ArrayList(32);
		m_valList = new ArrayList(32);
		m_changed = false;
	}

	public String getPreference(String name)
	{
		return access(name, "", GET);
	}

	public void setPreference(String name, String val)
	{
		access(name, val, SET);
	}

	public void save()
	{
		access("", "", SAVE);
	}
	
	protected static final int GET	= 1;
	protected static final int SET	= 2;
	protected static final int SAVE	= 3;
	protected static final int RESET	= 4;

	public void reset()
	{
		access("", "", RESET);
	}
	
	protected synchronized String access(String name, String val, int mode)
	{
		switch (mode)
		{
			case GET:
				{
					if ((name == null) || (name.equals("")))
						break;
					if (m_loaded == false)
						doLoad();
					
					Integer i = (Integer) m_preferenceMap.get(name);
					String v = "";
					if (i != null)
					{
						// mapping exists - recover value
						v = (String) m_valList.get(i.intValue());
					}
					return v;
				}

			case SAVE:
				try 
				{
					if (m_changed)
						doSave();
				}
				catch (IOException e)
				{
					return "save failed";
				}
				break;

			case SET:
				if ((name != null) && (!name.equals("")) && (val != null))
				{
					if (m_loaded == false)
						doLoad();
					Integer i = (Integer) m_preferenceMap.get(name);
					if (i == null)
					{
						// value doesn't exist - add it
						i = new Integer(m_valList.size());
						m_preferenceMap.put(name, i);
						m_valList.add(val);
						m_nameList.add(name);
					}
					else
					{
						// mapping exists - reset value to new
						m_valList.set(i.intValue(), val);
					}
					m_changed = true;
				}
				break;

			case RESET:
				m_loaded = false;
				m_changed = false;
				m_preferenceMap.clear();
				m_valList.clear();
				m_nameList.clear();
				break;
		}
		return "";
	}

	protected void doLoad() 
	{
		try
		{
			// mark as loaded - if the load fails, there's no use 
			//  trying again later
			m_loaded = true;

			XMLStreamReader xml = new XMLStreamReader();
			xml.killEmptyBlocks(true);
			xml.setStream(new File(OConsts.PROJ_PREFERENCE));
			XMLBlock blk;
			ArrayList lst;

			m_preferenceMap.clear();
			String pref;
			String val;
			// advance to omegat tag
			if ((blk = xml.advanceToTag("omegat")) == null)
				return;
			
			// advance to project tag
			if ((blk = xml.advanceToTag("preference")) == null)
				return;
	
			String ver = blk.getAttribute("version");
			if ((ver != null) && (ver.equals("1.0") == false))
			{
				// unsupported preference file version - abort read
				return;
			}
	
			lst = xml.closeBlock(blk);
			if (lst == null)
				return;
	
			Integer j;
			for (int i=0; i<lst.size(); i++)
			{
				blk = (XMLBlock) lst.get(i);
				if (blk.isClose())
					continue;
	
				if (!blk.isTag())
					continue;

				pref = blk.getTagName();
				blk = (XMLBlock) lst.get(++i);
				if (blk.isTag())
				{
					// parse error - keep trying
					continue;
				}
				val = blk.getText();
				if ((pref != null) && (!pref.equals("")) && (val != null))
				{
					// valid match - record these
					j = new Integer(m_valList.size());
					m_preferenceMap.put(pref, j);
					m_nameList.add(pref);
					m_valList.add(val);
				}
			}
		}
		catch (ParseException e)
		{
			// error loading preference file - keep whatever was
			//  loaded then return gracefully to calling function
			// print an error to the console as an FYI
			System.out.println("WARNING: Parse error encountered when " 
					+ "reading preference file.\n  " + e);
		}
		catch (IndexOutOfBoundsException e3)
		{
			// error loading preference file - keep whatever was
			//  loaded then return gracefully to calling function
			// print an error to the console as an FYI
			System.out.println("WARNING: Parse error encountered when " 
					+ "reading preference file.\n  " + e3);
		}
		catch (UnsupportedEncodingException e3)
		{
			// unrecognized file - forget about it
		}
		catch (IOException e4)
		{
			// can't read file - forget about it and move on
		}
	}
	
	protected void doSave() throws IOException
	{
		String str = "";
		String name = "";
		String val = "";
		BufferedWriter out = new BufferedWriter(new FileWriter(
					OConsts.PROJ_PREFERENCE));
		
		str =  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

		str += "<omegat>\n";
		str += "  <preference version=\"1.0\">\n";
		out.write(str, 0, str.length());

		for (int i=0; i<m_nameList.size(); i++)
		{
			name = (String) m_nameList.get(i);
			val = XMLStreamReader.controlify((String) m_valList.get(i));
			if (val.equals(""))
				continue;	// don't write blank preferences
			str = "    <" + name + ">";
			str += val;
			str += "</" + name + ">\n";
			out.write(str, 0, str.length());
		}
		str =  "  </preference>\n";
		str += "</omegat>\n";
		out.write(str, 0, str.length());
		out.flush();
		m_changed = false;
	}

	
	protected boolean	m_loaded;
	protected boolean	m_changed;

	// use a hash map for fast lookup of data
	// use array lists for orderly recovery of it for saving to disk
	protected ArrayList	m_nameList;
	protected ArrayList	m_valList;
	protected HashMap	m_preferenceMap;

	///////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		PreferenceManager man = new PreferenceManager();
		man.setPreference("pref1", "val 1");
		man.setPreference("pref2", "val 2");
		man.setPreference("pref3", "val 3");
		man.save();
		man.reset();
		String str;

		str = man.getPreference("pref1");
		System.out.println("pref 1: '" + str + "'");
		str = man.getPreference("pref5");
		System.out.println("pref 5: '" + str + "'");
	}
}
