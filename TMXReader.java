//-------------------------------------------------------------------------
//  
//  TMXReader.java - 
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
//  Build date:  9Jan2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
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

class TMXReader 
{
	public TMXReader()
	{
		m_reader = new XMLStreamReader();
		m_reader.killEmptyBlocks(true);
		m_encoding = "UTF-8";
		m_srcList = new ArrayList(512);
		m_tarList = new ArrayList(512);
	}

	// to allow specification of alternative encodings (i.e. wordfast)
	public TMXReader(String encoding)
	{
		m_reader = new XMLStreamReader();
		m_reader.killEmptyBlocks(true);
		m_encoding = encoding;
		m_srcList = new ArrayList(512);
		m_tarList = new ArrayList(512);
	}

	public int numSegments()	{ return m_srcList.size();		}
	
	public String getSourceSegment(int n)	
	{
		if (n < 0)
			return "";
		else if (n >= m_srcList.size())
			return "";
		else
			return (String) m_srcList.get(n);	
	}
	
	public String getTargetSegment(int n)	
	{
		if (n < 0)
			return "";
		else if (n >= m_tarList.size())
			return "";
		else
			return (String) m_tarList.get(n);	
	}
	
	public void loadFile(String filename)
		throws IOException, ParseException
	{
		m_reader.setStream(filename, m_encoding);
		
		// verify valid project file
		XMLBlock blk;
		ArrayList lst;
		m_tarList.clear();
		m_srcList.clear();

		// advance to tmx tag
		if ((blk = m_reader.advanceToTag("tmx")) == null)
			throw new ParseException("invalid tmx file '" + filename + "'", 0);

		// check version
		String ver = blk.getAttribute("version");
		if ((ver != null) && (ver.equals("1.1") == false))
		{
			throw new ParseException("unrecognized tmx version (" + 
					ver + ")", 0);
		}
		
		// advance to header  
		if ((blk=m_reader.advanceToTag("header")) == null)
			throw new ParseException("invalid tmx file '" + filename + "'", 0);
		// TODO handle header specific information, as appropriate
		String src = blk.getAttribute("srclang");

		// advance to body
		if ((blk=m_reader.advanceToTag("body")) == null)
			throw new ParseException("invalid tmx file '" + filename + "'", 0);

		int seg = 0;
		int ctr = 0;
		int srcPos = -1;
		String tarSeg;
		String srcSeg;
		String lang;
		while (true)
		{
			seg++;
			// advance to next tu element
			if ((blk=m_reader.advanceToTag("tu")) == null)
				break;

			lst = m_reader.closeBlock(blk);
			tarSeg = "";
			srcSeg = "";

			try
			{
				// now go through tu block
				// accept first non-src lang as target
				srcPos = -1;
				ctr = 0;

				// tuv 1
				while (blk.getTagName().equals("tuv") == false)
					blk = (XMLBlock) lst.get(ctr++);
				lang = blk.getAttribute("lang");
				if (src.regionMatches(0, lang, 0, 2))
					srcPos = 0;

				// advance to segment marker
				while (blk.getTagName().equals("seg") == false)
					blk = (XMLBlock) lst.get(ctr++);
				
				// next non-tag block is text
				blk = (XMLBlock) lst.get(ctr++);
				while (blk.isTag() == true)
					blk = (XMLBlock) lst.get(ctr++);
				if (srcPos == 0)
					srcSeg = blk.getText();
				else
					tarSeg = blk.getText();

				// close tuv tag
				while (blk.getTagName().equals("tuv") == false)
					blk = (XMLBlock) lst.get(ctr++);

				// open next tuv tag
				blk = (XMLBlock) lst.get(ctr++);
				while (blk.getTagName().equals("tuv") == false)
					blk = (XMLBlock) lst.get(ctr++);
				lang = blk.getAttribute("lang");
				if (src.regionMatches(0, lang, 0, 2))
//				if (src.equals(blk.getAttribute("lang")))
				{
					if (srcPos == -1)
						srcPos = 1;
					// else, src lang declared twice - use first instance
					//  for source, second for target
				}
				else if (srcPos == -1)
				{
					// source language segment not specified 
					String err = "WARNING: Unable to locate source " +
						"language text in segment " + seg + ".";
					System.out.println(err);
					continue;
				}

				// advance to segment marker
				while (blk.getTagName().equals("seg") == false)
					blk = (XMLBlock) lst.get(ctr++);
				
				// next non-tag block is text
				blk = (XMLBlock) lst.get(ctr++);
				while (blk.isTag() == true)
					blk = (XMLBlock) lst.get(ctr++);
				if (srcPos == 1)
					srcSeg = blk.getText();
				else
					tarSeg = blk.getText();

				// ignore the rest
			}
			catch (IndexOutOfBoundsException e)
			{
				System.out.println("WARNING: Skipping segment " + seg +
						" in TMX file '" + filename + "' after parse error.");
				continue;
			}

			m_srcList.add(srcSeg);
			m_tarList.add(tarSeg);
//			System.out.println("\nsrc: " + srcSeg + "\ntar: " + tarSeg);
		}
	}

	protected XMLStreamReader		m_reader;
	protected String		m_encoding;
	protected ArrayList		m_srcList;
	protected ArrayList		m_tarList;

	////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		try 
		{
			TMXReader reader = new TMXReader("ISO-8859-1");
			reader.loadFile("laptop.tmx");
//			TMXReader reader = new TMXReader();
//			reader.loadFile("en-it.tmx");
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


