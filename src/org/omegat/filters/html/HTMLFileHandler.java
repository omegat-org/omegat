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

import org.omegat.filters.FileHandler;
import org.omegat.filters.LBuffer;
import org.omegat.util.OConsts;

import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omegat.util.OStrings;

/*
 * A filter to translate HTML files.
 * 
 * Credit to Maxym Mykhalchuk for paying attention to the encoding
 * declaration and doing something about it.
 */
public class HTMLFileHandler extends FileHandler
{
	public HTMLFileHandler()
	{
		super(OConsts.FH_HTML_TYPE, "html"); // NOI18N
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
					outBuf.append("&lt;"); // NOI18N
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
					if (tag.name().startsWith("/")) // NOI18N
						outBuf.append("<"); // NOI18N
					else
						outBuf.append("</"); // NOI18N
					outBuf.append(tag.name());
					outBuf.append('>'); // NOI18N
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
				outBuf.append(s);
				//if (s == null)
				//	outBuf.append(c);
				//else
				//{
				//	outBuf.append('&');
				//	outBuf.append(s);
				//	outBuf.append(';');
				//}
			}

			if (state < 0)
			{
				tagBuf.append(c);
				//outBuf.append(org.omegat.HTMLParser.convertAllToEsc(tagBuf));
				outBuf.append(tagBuf);
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
			fileWriteError(e1);
		}
		catch(ParseException e) 
		{
			throw new IOException(MessageFormat.format(OStrings.getString("HFH_ERROR_PARSEERROR"),
				new Object[] {m_file, new Integer(e.getErrorOffset() + line()), e.toString()} ));
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
					throw new ParseException("&---; " + buf.string(), 0); // NOI18N
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
			throw new IOException(OStrings.getString("HFH_ERROR_UNRECOGNIZED_ESCAPE_CHAR") + line());
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

		if (tag.isEqual("pre")) // NOI18N
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

	/** the pattern string to extract the encoding from HTML file, if any */
	private static String META_PATTERN = "(?is)<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"']\\s*>";  // NOI18N
	/** compiled pattern to extract the encoding from HTML file, if any */
	private static Pattern pattern = Pattern.compile(META_PATTERN);
	
	/** Return encoding of HTML file, if defined */
	private String fileEncoding(String filename) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuffer buffer = new StringBuffer();
		while( reader.ready() ) {
			buffer.append( reader.readLine().toUpperCase() );
			Matcher matcher = pattern.matcher(buffer);
			if( matcher.find() )
				return matcher.group(1);
			if( buffer.indexOf("</HEAD") >= 0 ) // NOI18N
				break;
		}
		reader.close();
		
		return ""; // NOI18N
	}
	
	/** Customized version of creatning input stream for HTML files, that is
	 *  reading a possible &lt;META http-equiv="content-type" content="text/html; charset=..."&gt;
	 *  first, and then opens a file in that encoding.
	 *  If there's no META in HTML file, or it is not supported by Java platform,
	 *  file is opened in default system encoding (ISO-8859-2 in USA, Windows-1251 on my OS).
	 */
	public BufferedReader createInputStream(String infile) throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
		InputStreamReader isr;
		try
		{
			isr = new InputStreamReader(fis, fileEncoding(infile));
		}
		catch( UnsupportedEncodingException uee )
		{
			isr = new InputStreamReader(fis);
		}
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
	
	/** This class acts as an interceptor of output:
	 *  First it collects all the output inside itself in a string.
	 *  then adds a META with UTF-8 charset (or replaces the charset to UTF-8)
	 */
	class UTF8Writer extends StringWriter
	{
		
		private String filename;
		private Writer out;
		
		public UTF8Writer(Writer out)
		{
			super();
			this.out = out;
		}
		
		/** when we clase an Output Stream, we replace charset to be UTF-8
		 * and write out the string
		 */
		public void close() throws IOException
		{
			String UTF8_META = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"; // NOI18N
			
			StringBuffer buffer = getBuffer();
			Matcher matcher = pattern.matcher(buffer);
			
			String contents;
			if( matcher.find() )
			{
				contents = matcher.replaceFirst(UTF8_META);
			}
			else
			{
				contents = Pattern.compile("(?i)<head\\s*?>") // NOI18N
					.matcher(buffer).replaceFirst("<head>\n    "+UTF8_META); // NOI18N
			}
			BufferedWriter writer = new BufferedWriter(out);
			writer.write(contents);
			writer.close();
			
			super.close();
		}
	}
	
	/** Customized version of creating an output stream for HTML files,
	 *  always UTF-8 and appending charset meta with UTF-8
	 */
	public BufferedWriter createOutputStream(String infile, String outfile) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); // NOI18N
		UTF8Writer uw = new UTF8Writer(osw);
		BufferedWriter bw = new BufferedWriter(uw);
		return bw;
	}

	private LinkedList 	m_tagList = null;

	private LinkedList	m_preNT;
	private LinkedList	m_fdList;
	private LinkedList	m_postNT;

	private int		m_ec = 0;
	private boolean		m_ws = false;
	private boolean		m_pre = false;
	private boolean 	m_hasText = false;

}
