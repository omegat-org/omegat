//-------------------------------------------------------------------------
//  
//  HTMLFileHandler.java - 
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
import java.util.*;
import java.text.*;

class HTMLFileHandler extends FileHandler
{
	public HTMLFileHandler()
	{
		super(OConsts.FH_HTML_TYPE, "html");
		m_tagList = new LinkedList();
		m_preNT = new LinkedList();
		m_fdList = new LinkedList();
		m_postNT = new LinkedList();
	}

	// convert simplified formatting tags to full originals
	public String formatString(String text)
	{
		// TODO - different formatting rules for tag imbedded text

		// pull labels from string, ident them in tag
		// and replace them w/ the original data
		char[] car = new char[text.length()];
		text.getChars(0, text.length(), car, 0);
		char c;
		int num = 0;
		String s = null;
		char shortcut = 0;
		int state = 0;
		boolean close = false;
		HTMLTag tag = null;
		LBuffer tagBuf = new LBuffer(8);
		LBuffer outBuf = new LBuffer(text.length() * 2);
		for (int i=0; i<car.length; i++)
		{
			c = car[i];
			// scan for <CN>
			if (c == '<')
			{
				if (state == 1)
				{
					// double < - convert to &lt;
					outBuf.append("&lt;");
					tagBuf.reset();
					state = 0;
				}
				else if (state > 1)
				{
					// format error
					state = -1;
				}
				else
				{
					tagBuf.append(c);
					state = 1;
				}
			}
			else if ((state == 1) && (c == '/'))
			{
				close = true;
			}
			else if ((state == 1) && (((c >= 'a') && (c <= 'z')) ||
					((c >= 'A') && (c <= 'Z'))))
			{
				state++;
				shortcut = c;
				if (shortcut < 'a')
					shortcut += ('a' - 'A');
				tagBuf.append(c);
			}
			else if (((state == 2) || (state == 3)) && 
						((c >= '0') && (c <= '9')))
			{
				state++;
				num = num*10 + (c - '0');
				tagBuf.append(c);
			}
			else if (((state == 3) || (state == 4)) && (c == '>'))
			{
				// found shortcut tag - look it up and 
				// replace it
				tag = null;
				if ((num >= 0) && (num < m_tagList.size()))
				{
					tag = (HTMLTag) m_tagList.get(num);
					if (tag.shortcut() != shortcut)
						tag = null;
				}
				if (tag == null)
				{
					state = -1;
				}
				else if (close == false)
				{
					outBuf.append('<');
					outBuf.append(tag.verbatum());
					outBuf.append('>');
				}
				else
				{
					// orphaned close tags will begin with a '/' already
					if (tag.name().startsWith("/"))
						outBuf.append("<");
					else
						outBuf.append("</");
					outBuf.append(tag.name());
					outBuf.append('>');
				}
				state = 0;
				num = 0;
				tagBuf.reset();
				close = false;
			}
			else if (state >= 0)
			{
				state = -1;
			}
			else
			{
				s = HTMLParser.convertToEsc(c);
				if (s == null)
					outBuf.append(c);
				else
				{
					outBuf.append('&');
					outBuf.append(s);
					outBuf.append(';');
				}
			}

			if (state < 0)
			{
				tagBuf.append(c);
				outBuf.append(HTMLParser.convertAllToEsc(tagBuf));
				tagBuf.reset();
				state = 0;
			}
		}
		return outBuf.string();
	}

	public void reset()
	{
		super.reset();
		m_tagList.clear();
		m_preNT.clear();
		m_postNT.clear();
		m_fdList.clear();
		m_ec = 0;
		m_ws = false;
		m_pre = false;
		m_hasText = false;
	}

	public void doLoad() throws IOException
	{
		char c;
		int i;
		int ctr = 0;
		FormatData fd = null;

		// for white space control

		LBuffer nt = new LBuffer(256);
		LBuffer t = new LBuffer(256);
		LBuffer tmp = new LBuffer(256);
		LBuffer esc = new LBuffer(16);
		try
		{
			while ((i = getNextChar()) >= 0)
			{
				ctr++;
				c = (char) i;

				if (c == '<')
				{
					// force a new format data element to be created
					//  if there's more text
					if (fd != null)
						fd = null;
					handleTag();
				}
				else if ((c == 10) || (c == 13) || (c == 9) ||
						(c == ' '))
				{
					// white space
					if ((m_ws == false) || (fd == null))
					{
						fd = new FormatData();
						if (m_hasText)
							m_fdList.add(fd);
						else
							m_preNT.add(fd);
					}
					fd.appendOrig(c);
					// only include 1 space unless part of
					// <pre>
					if ((m_ws == true) && (m_pre == false))
						continue;
					m_ws = true;

					if (m_pre == true)
						fd.appendDisplay(c);
					else
						fd.appendDisplay(' ');
				}
				else
				{
					// text
					if ((m_hasText == false) || 
						(m_ws == true) || (fd == null))
					{
						fd = new FormatData();
						m_fdList.add(fd);
					}
					fd.appendOrig(c);
					if (c == '&')
					{
						c = getEscChar(fd);
					}
					if ((c == 160) && (!m_hasText))	
					{
						// no text and a &nbsp;
						// make it white space
						fd.appendDisplay(c);
						m_ws = true;
						m_hasText = false;
					}
					else
					{
						fd.appendDisplay(c);
						fd.setHasText(true);
						m_hasText = true;
						m_ws = false;
					}
				}
			}
			writeEntry();
		}
		catch(IOException e1) 
		{
System.out.println("HTML file write error: '" + m_file + "' at line " + line());
			fileWriteError(e1);
		}
		catch(ParseException e) 
		{
System.out.println("HTML parse error: '" + m_file + "' at line " + (e.getErrorOffset() + line()));
			throw new IOException("parse error in file " + m_file +
				" at line " + (e.getErrorOffset() + line()) +
				" - " + e);
		}
	}

	protected char getEscChar(FormatData fd)
					throws IOException, ParseException
	{
		char c = 0;
		int i;
		int ctr = 0;
		int numeric = 0;
		int val = 0;
		markStream();
		LBuffer buf = new LBuffer(8);
		while ((i = getNextChar()) >= 0)
		{
			c = (char) i;
			fd.appendOrig(c);
			if (ctr == 0)
			{
				// allow ws to immediately follow '&'
				if ((c == 10) || (c == 13) || (c == 0) ||
							(c == ' '))
				{
					fd.appendDisplay('&');
					c = ' ';
					break;
				}
				// look for numeric values
				if (c == '#')
				{
					numeric = 1;
					ctr++;
					continue;
				}
			}
			else if ((ctr == 1) && (numeric > 0))
			{
				if ((c == 'x') || (c == 'X'))
				{
					numeric = 2;	// hex
					ctr++;
					continue;
				}
			}
			
			if (numeric > 0)
			{
				if (c == ';')
				{
					c = (char) val;
					break;
				}
				if (numeric == 1)
				{
					if ((c >= '0') && (c <= '9'))
						val = val*10 + (c - '0');
					else
					{
						pushNextChar(c);
						c = (char) val;
						break;
					}
				}
				else if (numeric == 2)
				{
					if (c > 'Z')
						c -= 'a' - 'A';
					if ((c >= '0') && (c <= '9')) 
						val = val*16 + (c - '0');
					else if ((c >= 'A') && (c <= 'F'))
						val = val*16 + (c - 'A');
					else
					{
						pushNextChar(c);
						c = (char) val;
						break;
					}
				}
				ctr++;
				if (ctr > 10)
					throw new ParseException("&---; " + 
							buf.string(), 0);
				continue;
			}
			else
			if ((c == 10) || (c == 13) || (ctr > 10))
			{ 
				// '&' encountered that's not a part of
				// an escaped character - rewind stream
				resetToMark();
				c = '&';
				break;
			}
			if (c == ';')
			{
				c = HTMLParser.convertToChar(buf.string());
				break;
			}
			buf.append(c);
			ctr++;
		}
		if (c == 0)
			throw new IOException("unrecognized escape character" +
					" at line " + line());
		return c;
	}

	protected void handleTag() 
			throws ParseException, IOException
	{
		HTMLTag tag = null;
		FormatData fd = null;

		tag = HTMLParser.identTag(this);
		if (tag.hasTrans() == true)
		{
			; // TODO - trans in tag
		}
		
		fd = new FormatData(tag.close());
		fd.setOrig(tag.verbatum());

		if (tag.type() == HTMLTag.TAG_FORMAT)
		{

			if (m_hasText)
			{
				m_fdList.add(fd);
			}
			else
			{
				m_preNT.add(fd);
			}

			// formatting tag - replace
			// raw HTML tag with simplified
			// version
			HTMLTag cand = null;
			if (tag.close())
			{
				ListIterator it;
				it = m_tagList.listIterator(m_tagList.size());
				// move backwards to find source tag
				boolean foundPartner = false;
				while (it.hasPrevious())
				{
					cand = (HTMLTag) it.previous();
					if (tag.willPartner(cand))
					{
						cand.setPartner(true);
						tag.setPartner(true);
						fd.setTagData(cand.shortcut(), cand.num());
						foundPartner = true;
						break;
					}
				}
				if (foundPartner == false)
				{
					// failed to find partner - probably a close format
					//  tag that starts in a previous paragraph
					// give the tag a shortcut and number
					tag.setNum(m_tagList.size());
					m_tagList.add(tag);
					fd.setTagData(tag.shortcut(), tag.num());
				}
			}
			else	// open
			{
				tag.setNum(m_tagList.size());
				m_tagList.add(tag);
				fd.setTagData(tag.shortcut(), tag.num());
			}
			fd.finalize();
		}
		else
		{
			// structural tag
			m_postNT.add(fd);
			fd.finalize();
			writeEntry();
		}

		if (tag.isEqual("pre"))
		{
			if (tag.close())
				m_pre = false;
			else
				m_pre = true;
		}
	}

	protected void writeEntry() throws IOException
	{
		ListIterator it;
		FormatData fd = null;
		LBuffer buf = null;

		// compress output data
		compressOutputData();

		// see if there's anything interesting
		if ((m_fdList.size() == 0) && (m_outFile == null))
		{
			if (m_outFile == null)
			{
				m_preNT.clear();
				m_postNT.clear();
			}
			else
			{
				// nothing interesting - move all postNT down
				it = m_postNT.listIterator(m_postNT.size());
				while (it.hasPrevious())
				{
					fd = (FormatData) it.previous();
					m_preNT.add(fd);
				}
			}
			m_postNT.clear();
			m_fdList.clear();
			m_tagList.clear();
			m_ws = false;
			m_hasText = false;

			return;
		}

		// write out ignored leading tags
		if (m_outFile != null)
		{
			it = m_preNT.listIterator();
			while (it.hasNext())
			{
				fd = (FormatData) it.next();
				buf = fd.getOrig();
				m_outFile.write(buf.getBuf(), 0, 
							buf.size());
			}
		}

		if (m_fdList.size() > 0)
		{
			// process display text
			it = m_fdList.listIterator();
			LBuffer out = new LBuffer(256);
			while (it.hasNext())
			{
				fd = (FormatData) it.next();
				out.append(fd.getDisplay());
			}
			processEntry(out, m_file);
		}

		// write out ignored trailing tags
		if (m_outFile != null)
		{
			it = m_postNT.listIterator(m_postNT.size());
			while (it.hasPrevious())
			{
				fd = (FormatData) it.previous();
				buf = fd.getOrig();
				m_outFile.write(buf.getBuf(), 0, 
							buf.size());
			}
		}
		m_preNT.clear();
		m_postNT.clear();
		m_fdList.clear();
		m_tagList.clear();
		m_ws = false;
		m_hasText = false;
	}

	protected void compressOutputData()
	{
		// remove leading and trailing white space, 
		// leading and trailing tags and tag pairs

		boolean change = true;
		FormatData fd_head = null;
		FormatData fd_tail = null;
		ListIterator it;
		int ctr = 0;
		int len;
		FormatData fd = null;
		while (change)
		{
			if (m_fdList.size() == 0)
				break;	// sanity check

			fd_head = (FormatData) m_fdList.getFirst();
			fd_tail = (FormatData) m_fdList.getLast();
//System.out.println(fd_head.getDisplay().string() + "\t" + fd_tail.getDisplay().string()); 
			// if leading white space, move to preNT
			if (fd_head.isWhiteSpace())
			{
//System.out.println("      head ws");
				m_preNT.add(fd_head);
				m_fdList.removeFirst();
				continue;
			}

			// if trailing ws, move to postNT
			if (fd_tail.isWhiteSpace())
			{
//System.out.println("      tail ws");
				m_postNT.add(fd_tail);
				m_fdList.removeLast();
				continue;
			}

			if (m_fdList.size() == 1)
			{
				// forego rest of processing - if it's text
				// handle it, if a tag, put it on preNT
				if (fd_head.isTag())
				{
					m_preNT.add(fd_head);
					m_fdList.clear();
				}
				break;
			}

			// if trailing tag is open, move to postNT
			if (fd_tail.isTag() && !fd_tail.isCloseTag())
			{
//System.out.println("      tail open");
				m_postNT.add(fd_tail);
				m_fdList.removeLast();
				continue;
			}

			// if leading tag is open, move to preNT
			if (fd_head.isTag() && fd_head.isCloseTag())
			{
//System.out.println("      head close");
				m_preNT.add(fd_head);
				m_fdList.removeFirst();
				continue;
			}

			// if first and last tags pair, move to lists
			if (fd_tail.isTag() && fd_head.isTag() && 
				(fd_tail.tagData() == fd_head.tagData()))
			{
//System.out.println("      matching pair");
				if (m_fdList.size() == 1)
					break;	// first = last
				m_postNT.add(fd_tail);
				m_preNT.add(fd_head);
				m_fdList.removeFirst();
				m_fdList.removeLast();
				continue;
			}

			// look for strings starting or ending w/ unmatched 
			// tags and remove the vile things

			// look for leading unmatched
			if (fd_head.isTag())
			{
				it = m_fdList.listIterator();
				ctr = 0;
				len = m_fdList.size();
				while (it.hasNext())
				{
					fd = (FormatData) it.next();
					if (ctr > 0)
					{
						if (fd_head.tagData() == 
								fd.tagData())
							break;	// match
					}
					ctr++;
				}
				if (ctr < len)
				{
					m_preNT.add(fd_head);
					m_fdList.removeFirst();
//System.out.println("      unmatched lead token");
					continue;
				}
			}

			// another sanity check in case list size changed
			if (m_fdList.size() == 1)
			{
				// forego rest of processing - if it's text
				// handle it, if a tag, put it on preNT
				if (fd_head.isTag())
				{
					m_preNT.add(fd_head);
					m_fdList.clear();
				}
				break;
			}

			// look for leading unmatched
			if (fd_tail.isTag())
			{
				it = m_fdList.listIterator();
				ctr = 0;
				len = m_fdList.size();
				while (it.hasNext())
				{
					fd = (FormatData) it.next();
					if (ctr < len)
					{
						if (fd_tail.tagData() == 
								fd.tagData())
							break;	// match
					}
					ctr++;
				}
				if (ctr < len)
				{
					m_postNT.add(fd_tail);
					m_fdList.removeLast();
//System.out.println("      unmatched tail token");
					continue;
				}
			}

			// if we made it here, nothing happened
			change = false;
		}
	}

	private LinkedList 	m_tagList = null;

	private LinkedList	m_preNT;
	private LinkedList	m_fdList;
	private LinkedList	m_postNT;

	private int		m_ec = 0;
	private boolean		m_ws = false;
	private boolean		m_pre = false;
	private boolean 	m_hasText = false;

///////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		HTMLFileHandler txt = new HTMLFileHandler();
		CommandThread.core = new CommandThread(null);
		String s;
		if (args.length > 0)
			s = args[0];
		else 
			s = "samp.html";
		try 
		{
			txt.setTestMode(true);
			txt.load(s);
			txt.write(s, "out.html");
		}
		catch (IOException e)
		{
			System.out.println("error - " + e);
		}
		System.exit(0);
	}
}
