//-------------------------------------------------------------------------
//  
//  ProjFileReader.java - 
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
//  Build date:  16Sep2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------


import java.io.*;
import java.lang.*;
import java.util.*;
import java.text.ParseException;

class ProjFileReader 
{
	public ProjFileReader()
	{
		m_reader = new XMLStreamReader();
		m_reader.killEmptyBlocks(true);
		reset();
	}

	public boolean loadProjectFile(String filename)
		throws IOException, ParseException
	{
		m_reader.setStream(filename, "UTF8");
		
		// verify valid project file
		XMLBlock blk;
		ArrayList lst;

		// advance to omegat tag
		if ((blk = m_reader.advanceToTag("omegat")) == null)
			return false;
		
		// advance to project tag
		if ((blk=m_reader.advanceToTag("project")) == null)
			return false;

		String ver = blk.getAttribute("version");
		if ((ver != null) && (ver.equals(OConsts.PROJ_CUR_VERSION) == false))
		{
			throw new ParseException("unsupported project file version (" + 
					ver + ")", 0);
		}
		
		// if folder is in default locations, name stored as __DEFAULT__
		m_root = filename.substring(0, 
					filename.lastIndexOf(File.separator)) + File.separator;

		lst = m_reader.closeBlock(blk);
		if (lst == null)
			return false;

		for (int i=0; i<lst.size(); i++)
		{
			blk = (XMLBlock) lst.get(i);
			if (blk.isClose())
				continue;

			if (blk.getTagName().equals("target_dir"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_target = translateBlock(blk, OConsts.DEFAULT_LOC);
			}
			else if (blk.getTagName().equals("source_dir"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_source = translateBlock(blk, OConsts.DEFAULT_SRC);
			}
			else if (blk.getTagName().equals("tm_dir"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_tm = translateBlock(blk, OConsts.DEFAULT_TM);
			}
			else if (blk.getTagName().equals("glossary_dir"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_glossary = translateBlock(blk, OConsts.DEFAULT_GLOS);
			}
			else if (blk.getTagName().equals("source_lang"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				if (blk != null)
					m_srcLang = blk.getText();
			}
			else if (blk.getTagName().equals("target_lang"))
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				if (blk != null)
					m_locLang = blk.getText();
			}
		}

		return true;
	}

	protected String translateBlock(XMLBlock blk, String def)
	{
		if (blk == null)
			return "";
		else if (blk.getText().equals(OConsts.DEFAULT_FOLDER_MARKER))
			return m_root + def + File.separator;
		else
			return blk.getText();
	}

	public void writeProjectFile(String filename) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		String str;

		// if folders are in default locations, store name as 
		//  __DEFAULT__
		String root = filename.substring(0, 
					filename.lastIndexOf(File.separator));
		root += File.separator;

		String source = "";
		String target = "";
		String tm = "";
		String glossary = "";
		if (m_source.equals(root + OConsts.DEFAULT_SRC + File.separator))
			source = OConsts.DEFAULT_FOLDER_MARKER;
		else
			source = m_source;
		
		if (m_target.equals(root + OConsts.DEFAULT_LOC + File.separator))
			target = OConsts.DEFAULT_FOLDER_MARKER;
		else
			target = m_target;
		
		if (m_glossary.equals(root + OConsts.DEFAULT_GLOS + File.separator))
			glossary = OConsts.DEFAULT_FOLDER_MARKER;
		else
			glossary = m_glossary;
		
		if (m_tm.equals(root + OConsts.DEFAULT_TM + File.separator))
			tm = OConsts.DEFAULT_FOLDER_MARKER;
		else
			tm = m_tm;
		
		str = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
		str += "<omegat>\n";
		str += "  <project version=\"1.0\">\n";
		str += "    <source_dir>" + source + "</source_dir>\n";
		str += "    <target_dir>" + target + "</target_dir>\n";
		str += "    <tm_dir>" + tm + "</tm_dir>\n";
		str += "    <glossary_dir>" + glossary + "</glossary_dir>\n";
		str += "    <source_lang>" + m_srcLang + "</source_lang>\n";
		str += "    <target_lang>" + m_locLang + "</target_lang>\n";
		str += "  </project>\n";
		str += "</omegat>\n";

		out.write(str, 0, str.length());
		out.flush();
	}
	
	public void reset()
	{
		m_target = "";
		m_source = "";
		m_tm = "";
		m_glossary = "";
		m_srcLang = "";
		m_locLang = "";
		m_root = "";
	}
	
	public void	setTarget(String x)		{ m_target = x;		}
	public void	setSource(String x)		{ m_source = x;		}
	public void	setTM(String x)			{ m_tm = x;			}
	public void	setGlossary(String x)	{ m_glossary = x;	}

	public void setSourceLang(String x)	{ m_srcLang = x;	}
	public void setTargetLang(String x)	{ m_locLang = x;	}

	public String getTarget()		{ return m_target;		}
	public String getSource()		{ return m_source;		}
	public String getTM()			{ return m_tm;			}
	public String getGlossary()		{ return m_glossary;	}
	public String getSourceLang()	{ return m_srcLang;		}
	public String getTargetLang()	{ return m_locLang;		}
	
	protected XMLStreamReader		m_reader;
	protected String		m_target;
	protected String		m_source;
	protected String		m_tm;
	protected String		m_glossary;
	protected String		m_locLang;
	protected String		m_srcLang;

	protected String		m_root;	

	////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		try 
		{
			ProjFileReader prj = new ProjFileReader();
			prj.setTarget("target dir");
			prj.setSource("source dir");
			prj.setTM("tm dir");
			prj.setGlossary("glossary dir");
			prj.setSourceLang("source language");
			prj.setTargetLang("target language");
			prj.writeProjectFile("test.proj");
			prj.reset();
			prj.loadProjectFile("test.proj");
			System.out.println("target:   " + prj.getTarget());
			System.out.println("source:   " + prj.getSource());
			System.out.println("tm:       " + prj.getTM());
			System.out.println("glossary: " + prj.getGlossary());
			System.out.println("source lang: " + prj.getSourceLang());
			System.out.println("target lang: " + prj.getTargetLang());
		}
		catch (IOException e1)
		{
			System.out.println("io exception: " + e1);
		}
		catch (ParseException e2)
		{
			System.out.println("parse exception: " + e2);
		}
	}
}


