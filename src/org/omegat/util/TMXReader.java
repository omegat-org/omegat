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

package org.omegat.util;

import org.omegat.filters.xml.XMLBlock;
import org.omegat.filters.xml.XMLStreamReader;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Class that loads up TMX 1.1 (Translation Memory) files
 *
 * @author Keith Godfrey
 */
public class TMXReader
{

    // to allow specification of alternative encodings (i.e. wordfast)
	public TMXReader(String encoding)
	{
		m_reader = new XMLStreamReader();
		m_reader.killEmptyBlocks();
		m_encoding = encoding;
		m_srcList = new ArrayList(512);
		m_tarList = new ArrayList(512);
	}

	public int numSegments()	{ return m_srcList.size();		}
	
	public String getSourceSegment(int n)	
	{
		if (n < 0)
			return "";															// NOI18N
		else if (n >= m_srcList.size())
			return "";															// NOI18N
		else
			return (String) m_srcList.get(n);	
	}
	
	public String getTargetSegment(int n)	
	{
		if (n < 0)
			return "";															// NOI18N
		else if (n >= m_tarList.size())
			return "";															// NOI18N
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
		if ((blk = m_reader.advanceToTag("tmx")) == null)						// NOI18N
			throw new ParseException( MessageFormat.format(OStrings.getString("TMXR_ERROR_INVALID_TMX"), new Object[]{filename}), 0);

		// check version
		String ver = blk.getAttribute("version");								// NOI18N
		if (ver != null && !ver.equals("1.1"))						// NOI18N
		{
			throw new ParseException( MessageFormat.format(OStrings.getString("TMXR_ERROR_UNSUPPORTED_TMX_VERSION"), new Object[]{ver}), 0);
		}
		
		// advance to header  
		if ((blk=m_reader.advanceToTag("header")) == null)						// NOI18N
			throw new ParseException( MessageFormat.format(OStrings.getString("TMXR_ERROR_INVALID_TMX"), new Object[]{filename}), 0);
		
		// TODO handle header specific information, as appropriate
		String src = blk.getAttribute("srclang");								// NOI18N

		// advance to body
		if (m_reader.advanceToTag("body") == null)						// NOI18N
			throw new ParseException( MessageFormat.format(OStrings.getString("TMXR_ERROR_INVALID_TMX"), new Object[]{filename}), 0);

		int seg = 0;
		int ctr;
		int srcPos;
		String tarSeg;
		String srcSeg;
		String lang;
		while (true)
		{
			seg++;
			// advance to next tu element
			if ((blk=m_reader.advanceToTag("tu")) == null)						// NOI18N
				break;

			lst = m_reader.closeBlock(blk);
			tarSeg = "";														// NOI18N
			srcSeg = "";														// NOI18N

			try
			{
				// now go through tu block
				// accept first non-src lang as target
				srcPos = -1;
				ctr = 0;

				// tuv 1
				while (!blk.getTagName().equals("tuv"))					// NOI18N
					blk = (XMLBlock) lst.get(ctr++);
				lang = blk.getAttribute("lang");								// NOI18N
				if (src.regionMatches(0, lang, 0, 2))
					srcPos = 0;

				// advance to segment marker
				while (!blk.getTagName().equals("seg"))					// NOI18N
					blk = (XMLBlock) lst.get(ctr++);
				
				// next non-tag block is text
				blk = (XMLBlock) lst.get(ctr++);
				while (blk.isTag())
					blk = (XMLBlock) lst.get(ctr++);
				if (srcPos == 0)
					srcSeg = blk.getText();
				else
					tarSeg = blk.getText();

				// close tuv tag
				while (!blk.getTagName().equals("tuv"))					// NOI18N
					blk = (XMLBlock) lst.get(ctr++);

				// open next tuv tag
				blk = (XMLBlock) lst.get(ctr++);
				while (!blk.getTagName().equals("tuv"))					// NOI18N
					blk = (XMLBlock) lst.get(ctr++);
				
				lang = blk.getAttribute("lang");								// NOI18N
				if (src.regionMatches(0, lang, 0, 2))
				{
					if (srcPos == -1)
						srcPos = 1;
					// else, src lang declared twice - use first instance
					//  for source, second for target
				}
				else if (srcPos == -1)
				{
					// source language segment not specified 
					System.out.println( MessageFormat.format(OStrings.getString("TMXR_WARNING_UNABLE_TO_LOCATE_SRC_LANG"), new Object[]{new Integer(seg)}) );
					continue;
				}

				// advance to segment marker
				while (!blk.getTagName().equals("seg"))					// NOI18N
					blk = (XMLBlock) lst.get(ctr++);
				
				// next non-tag block is text
				blk = (XMLBlock) lst.get(ctr++);
				while (blk.isTag())
					blk = (XMLBlock) lst.get(ctr++);
				if (srcPos == 1)
					srcSeg = blk.getText();
				else
					tarSeg = blk.getText();

				// ignore the rest
			}
			catch (IndexOutOfBoundsException e)
			{
				System.out.println( MessageFormat.format(OStrings.getString("TMXR_WARNING_SKIPPING_SEGMENT"), new Object[]{new Integer(seg), filename}) );
				continue;
			}

			m_srcList.add(srcSeg);
			m_tarList.add(tarSeg);
		}
	}

    private XMLStreamReader		m_reader;
	private String		m_encoding;
	private ArrayList		m_srcList;
	private ArrayList		m_tarList;
}


