//-------------------------------------------------------------------------
//  
//  XmlFileHandler.java - 
//  
//  Copyright (C) 2004, Keith Godfrey
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
//  Copyright (C) 2004, Keith Godfrey, et al
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.zip.*;

class XmlFileHandler extends FileHandler
{
	public XmlFileHandler(String name, String ext)
	{
		super(name, ext);
		//m_tagList = new LinkedList();
		m_preTextList = new ArrayList();
		m_textList = new ArrayList();
		m_postTextList = new ArrayList();
		m_tagMap = new HashMap();

		m_formatList = new ArrayList();
		m_formatDisplayList = new ArrayList();
		m_verbatumList = new ArrayList();
		m_verbatumDisplayList = new ArrayList();
		m_compressWhitespace = false;
		m_streamFilter = null;
	}

	public void setStreamFilter(XMLStreamFilter filter)
	{
		m_streamFilter = filter;
	}

	public void reset()
	{
		super.reset();
		m_preTextList.clear();
		m_textList.clear();
		m_postTextList.clear();
		m_tagMap.clear();
	}

	public void defineVerbatumTag(String tag, String display)
	{
		m_verbatumList.add(tag);
		m_verbatumDisplayList.add(display);
	}

	public void defineFormatTag(String tag, String display)
	{
		m_formatList.add(tag);
		m_formatDisplayList.add(display);
	}

	// prepare string for user
	// for now this only entails doubling up '<'
	public String makeDisplayable(String text)
	{
		char c;
		StringBuffer out = new StringBuffer();
		for (int i=0; i<text.length(); i++)
		{
			c = text.charAt(i);
			out.append(c);
			if (c == '<')
				out.append('<');
		}
		return out.toString();
	}
	
	// convert simplified formatting tags to originals
	// convert characters to valid XML
	public String formatString(String text)
	{
		// pull labels from string, ident them in tag
		// and replace them w/ the original data
		char c;

		StringBuffer tagBuf = new StringBuffer();
		StringBuffer outBuf = new StringBuffer(text.length() * 2);

		final int TEXT = 1;
		final int TAG_START = 2;
		final int TAG = 3;

		int state = TEXT;
		for (int i=0; i<text.length(); i++)
		{
			c = text.charAt(i);
			switch (state)
			{
				case TAG_START:
					if (c == '<')
					{
						// double < encountered
						outBuf.append("&lt;");
						tagBuf.setLength(0);
						state = TEXT;
					}
					else
					{
						tagBuf.append(XMLStreamReader.makeValidXML(c, 
									m_streamFilter));
						state = TAG;
					}
					break;

				case TAG:
					if (c == '>')
					{
						String orig = (String) m_tagMap.get(tagBuf.toString());
						if ((orig == null) || (orig.equals("")))
						{
							orig = "<" + tagBuf.toString() + ">";
						}
						outBuf.append(orig);
						tagBuf.setLength(0);
						state = TEXT;
					}
					else
						tagBuf.append(c);
					break;

				case TEXT:
					if (c == '<')
						state = TAG_START;
					else
					{
						outBuf.append(XMLStreamReader.makeValidXML(c,
									m_streamFilter));
					}
					break;
			}
		}
		if (tagBuf.length() != 0)
		{
			// string corrupted - signal error
		}
		return outBuf.toString();
	}
	
	protected void initFormatTagList()
	{
		// there are no default format tags in XML - all subclasses
		//	will need to overwrite this function

		// format tags include those such as <bold>, <italic>, <font>, etc.
		m_formatList.clear();

		// verbatum tags indicate that all of the text between open and
		//	close tags should be copied into the entry verbatum.  this
		//	system was created to support OOo's habit of inserting 
		//	footnote data w/in logical text segments and including 
		//	breaking tags in this footnote data, thus forcing breakage 
		//	of the segment at very awkward places
		m_verbatumList.clear();
	}
	
	public void doLoad() throws IOException
	{
		int i;
		String s;

		XMLStreamReader xml = new XMLStreamReader();
		xml.setStreamFilter(m_streamFilter);
		xml.compressWhitespace(m_compressWhitespace);
		xml.breakOnWhitespace(m_breakWhitespace);
		XMLBlock blk = null;
		xml.setStream(m_in);

		// to keep track of blocks in current segment
		m_preTextList.clear();
		m_textList.clear();
		m_postTextList.clear();
		m_tagMap.clear();

		// this will be set to either text list or pretext list depending
		//	on whether text has been found or not
		ArrayList target = m_preTextList;

		// write original XML preamble as first block
		m_preTextList.add(xml.getHeadBlock());

		try
		{
			while ((blk = xml.getNextBlock()) != null)
			{
				if (target == m_preTextList)
				{
					if ((blk.hasText() == true) && (blk.isComment() == false))
					{
						// first real text encountered - switch list
						target = m_textList;
						target.add(blk);
					}
					else
					{
						// all tags before text are stored in pre list
						target.add(blk);
					}
				}
				else if (blk.isTag() == false)
				{
					target.add(blk);
				}
				else
				{
					// tag encountered
					// first cycle through verbatum tag list to see if match
					for (i=0; i<m_verbatumList.size(); i++)
					{
						s = (String) m_verbatumList.get(i);
						if (blk.getTagName().equals(s) == true)
						{
							// store the identifying tag
							target.add(blk);

							// give it a shortcut
							s = (String) m_verbatumDisplayList.get(i);
							blk.setShortcut(s);

							// copy everything until close block
							// give imbedded format tags a shortcut
							ArrayList lst = xml.closeBlock(blk, true);
							XMLBlock openBlock = blk;
							for (int j=0; j<lst.size(); j++)
							{
								blk = (XMLBlock) lst.get(j);
								// if format tag, write shortcut
								if ((target == m_textList) && 
										((blk.isTag()) || (blk.isComment())))
								{
									for (int k=0; k<m_formatList.size(); k++)
									{
										s = (String) m_formatList.get(k);
										if (blk.getTagName().equals(s) == true)
										{
											s = (String) 
												m_formatDisplayList.get(k);
											blk.setShortcut(s);
											break;
										}
									}
								}

								target.add(blk);
								if ((target == m_preTextList) && 
										(blk.hasText() == true))
								{
									// text encountered - switch to correct
									//	list (if not already done so)
									target = m_textList;
								}
							}
							blk.setShortcut(openBlock.getShortcut());

							break;
						}
					}
					// verbatum block handled - continue with new block
					if (i < m_verbatumList.size())
						continue;

					// cycle through format tag list to see if match
					for (i=0; i<m_formatList.size(); i++)
					{
						s = (String) m_formatList.get(i);
						if (blk.getTagName().equals(s) == true)
						{
							// give it a shortcut
							s = (String) m_formatDisplayList.get(i);
							blk.setShortcut(s);

							target.add(blk);
							break;
						}
					}
					// block handled - continue fresh processing of next
					if (i < m_formatList.size())
						continue;
					
					// if we've made it this far it must be a structural tag
					// consolidate lists and write entry
					// move empty blocks at end of text list to post list
					XMLBlock end = null;
					for (i=m_textList.size()-1; i>=0; i--)
					{
						end = (XMLBlock) m_textList.get(i);
						if (end.hasText() == false)
						{
							m_postTextList.add(0, end);
							m_textList.remove(i);
						}
						else
						{
							// last element is text - nothing there to move
							break;
						}
					}
					writeEntry(blk);
					m_preTextList.clear();
					m_textList.clear();
					m_postTextList.clear();
					m_tagMap.clear();
					target = m_preTextList;
				}
			}
			if (m_preTextList.size() > 0)
				writeEntry(null);
		}
		catch(ParseException e) 
		{
System.out.println("XML file handler parse error: '" + m_file + "' at line " + (e.getErrorOffset() + line()) + "\n" + e);
			throw new IOException("parse error in file " + m_file +
				" at line " + (e.getErrorOffset() + line()) +
				" - " + e);
		}
	}

	protected void writeEntry(XMLBlock breaker) throws IOException
	{
		ListIterator it;
		String str;
		XMLBlock blk;
		int ctr = 1;

		// if there's nothing interesting and no outfile, ignore it
		if ((m_textList.size() == 0) && (m_outFile == null))
		{
			m_preTextList.clear();
			m_postTextList.clear();
			m_tagMap.clear();

			return;
		}

		// write out ignored leading tags
		if ((m_preTextList.size() > 0) && (m_outFile != null))
		{
			it = m_preTextList.listIterator();
			while (it.hasNext())
			{
				blk = (XMLBlock) it.next();
				str = blk.getText();
				if (m_compressWhitespace)
					str += "\n";
				m_outFile.write(str, 0, str.length());
			}
		}

		if (m_textList.size() > 0)
		{
			// process display text
			it = m_textList.listIterator();
			StringBuffer out = new StringBuffer(256);
			while (it.hasNext())
			{
				blk = (XMLBlock) it.next();
				// need to convert non-tag chars to control values 
				//	when writing
				if ((blk.isTag()) || (blk.isComment()))
				{
					String sh = blk.getShortcut();
					String display;
					if ((sh != null) && (sh.equals("") == false))
					{
						display = blk.getShortcut() + ctr++;
						m_tagMap.put(display, blk.getText());
						display = "<" + display + ">";
					}
					else
						display = blk.getText();
					out.append(display);
				}
				else
				{
					out.append(makeDisplayable(blk.getText()));
				}
			}
			processEntry(out, m_file);
		}

		// write out ignored trailing tags
		if ((m_postTextList.size() > 0) && (m_outFile != null))
		{
			it = m_postTextList.listIterator();
			while (it.hasNext())
			{
				blk = (XMLBlock) it.next();
				str = blk.getText();
				m_outFile.write(str, 0, str.length());
			}
		}

		if ((m_outFile != null) && (breaker != null))
		{
			if (m_compressWhitespace)
				str = "\n" + breaker.getText();
			else
				str = breaker.getText();
			m_outFile.write(str, 0, str.length());
		}
	}

	public void compressWhitespace(boolean tof)
	{
		m_compressWhitespace = tof;
	}
	
	public void breakWhitespace(boolean tof)
	{
		m_breakWhitespace = tof;
	}
	
//	private LinkedList 	m_tagList = null;

	protected ArrayList	m_preTextList;
	protected ArrayList	m_textList;
	protected ArrayList	m_postTextList;

	protected HashMap	m_tagMap;	// associate block shortcut with text

	protected ArrayList	m_formatList;
	protected ArrayList	m_formatDisplayList;
	protected ArrayList	m_verbatumList;
	protected ArrayList	m_verbatumDisplayList;

	protected boolean	m_compressWhitespace;
	protected boolean	m_breakWhitespace;
	protected XMLStreamFilter	m_streamFilter = null;

	protected int		m_ec = 0;
	protected boolean		m_ws = false;
	protected boolean 	m_hasText = false;

///////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		XmlFileHandler txt = new XmlFileHandler("XmlFileHandler", "abw");
		CommandThread.core = new CommandThread(null);
		String s;
		if (args.length > 0)
			s = args[0];
		else 
			s = "test.abw";
		try 
		{
			txt.setTestMode(true);
			txt.load(s);
			txt.write(s, "out.abw");
		}
		catch (IOException e)
		{
			System.out.println("error - " + e);
		}
		System.exit(0);
	}
}
