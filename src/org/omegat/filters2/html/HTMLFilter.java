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

package org.omegat.filters2.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import org.omegat.filters2.LBuffer;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.AntiCRReader;
import org.omegat.util.EncodingAwareReader;
import org.omegat.util.OStrings;
import org.omegat.util.UTF8Writer;

/*
 * A filter to translate HTML files.
 * 
 * Credit to Maxym Mykhalchuk for paying attention to the encoding
 * declaration and doing something about it.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class HTMLFilter extends AbstractFilter
{
	public HTMLFilter()
	{
		m_tagList = new LinkedList();
		m_preNT = new LinkedList();
		m_fdList = new LinkedList();
		m_postNT = new LinkedList();
	}

	/** 
     * Convert simplified formatting tags to full originals.
     */
	public String formatString(String text)
	{
		// TODO - different formatting rules for tag imbedded text

		// pull labels from string, ident them in tag
		// and replace them w/ the original data
		char[] car = new char[text.length()];
		text.getChars(0, text.length(), car, 0);
		char c;
		int num = 0;
		String s;
		char shortcut = 0;
		int state = 0;
		boolean close = false;
		HTMLTag tag;
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
			else if (state == 1 && c == '/')
			{
				close = true;
			}
			else if (state == 1 && (c >= 'a' && c <= 'z' ||
                    c >= 'A' && c <= 'Z'))
			{
				state++;
				shortcut = c;
				if (shortcut < 'a')
					shortcut += 'a' - 'A';
				tagBuf.append(c);
			}
			else if ((state == 2 || state == 3) &&
						(c >= '0' && c <= '9'))
			{
				state++;
				num = num*10 + (c - '0');
				tagBuf.append(c);
			}
			else if ((state == 3 || state == 4) && c == '>')
			{
				// found shortcut tag - look it up and 
				// replace it
				tag = null;
				if (num >= 0 && num < m_tagList.size())
				{
					tag = (HTMLTag) m_tagList.get(num);
					if (tag.shortcut() != shortcut)
						tag = null;
				}
				if (tag == null)
				{
					//state = -1;
				}
				else if (!close)
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
				s = convertToEsc(c);
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
		m_tagList.clear();
		m_preNT.clear();
		m_postNT.clear();
		m_fdList.clear();
        m_ws = false;
		m_pre = false;
		m_hasText = false;
	}
    
    char pushedChar;
    
	/** push one character "back" the active stream */
	protected void pushNextChar(char c)
	{
		pushedChar = c;
	}

    /** Returns the last char, supports pushing the char "back" */
	private int getNextChar(Reader infile) throws IOException
	{
		if( pushedChar!=0 )
		{
			int res = pushedChar;
			pushedChar = 0;
            return res;
		}
		else
            return infile.read();
	}


	private char getEscChar(Reader reader, FormatData fd)
					throws IOException, TranslationException
	{
		char c = 0;
		int i;
		int ctr = 0;
		int numeric = 0;
		int val = 0;
		reader.mark(16);
		LBuffer buf = new LBuffer(8);
		while( (i = getNextChar(reader))>=0 )
		{
			c = (char) i;
			fd.appendOrig(c);
			if (ctr == 0)
			{
				// allow ws to immediately follow '&'
				if (c == 10 || c == 13 || c == 0 ||
                        c == ' ')
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
			else if (ctr == 1 && numeric > 0)
			{
				if (c == 'x' || c == 'X')
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
					if (c >= '0' && c <= '9')
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
					if (c >= '0' && c <= '9')
						val = val*16 + (c - '0');
					else if (c >= 'A' && c <= 'F')
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
					throw new TranslationException("Illegal HTML entity (&---;):" + buf.string());
				continue;
			}
			else
			if (c == 10 || c == 13 || ctr > 10)
			{ 
				// '&' encountered that's not a part of
				// an escaped character - rewind stream
				reader.reset();
				c = '&';
				break;
			}
			if (c == ';')
			{
				c = convertToChar(buf.string());
				break;
			}
			buf.append(c);
			ctr++;
		}
		if (c == 0)
			throw new IOException(OStrings.getString("HFH_ERROR_UNRECOGNIZED_ESCAPE_CHAR"));
		return c;
	}

	private void handleTag(Reader reader, Writer writer)
			throws IOException, TranslationException
	{
		HTMLTag tag;
		FormatData fd;

		tag = identTag(reader);

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
			HTMLTag cand;
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
						cand.setPartner();
						tag.setPartner();
						fd.setTagData(cand.shortcut(), cand.num());
						foundPartner = true;
						break;
					}
				}
				if (!foundPartner)
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
			writeEntry(writer);
		}

		if (tag.isPreTag()) // NOI18N
		{
            m_pre = !tag.close();
		}
	}

	private void writeEntry(Writer writer) throws IOException
	{
		ListIterator it;
		FormatData fd;
		LBuffer buf;

		// compress output data
		compressOutputData();

		// see if there's anything interesting
		if (m_fdList.size() == 0 && writer == null)
		{
			m_preNT.clear();
			m_postNT.clear();
			m_fdList.clear();
			m_tagList.clear();
			m_ws = false;
			m_hasText = false;

			return;
		}

		// write out ignored leading tags
		if (writer != null)
		{
			it = m_preNT.listIterator();
			while (it.hasNext())
			{
				fd = (FormatData) it.next();
				buf = fd.getOrig();
				writer.write(buf.getBuf(), 0, 
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
			writer.write(convertToEsc(processEntry(out.string())));
		}

		// write out ignored trailing tags
		if (writer != null)
		{
			it = m_postNT.listIterator(m_postNT.size());
			while (it.hasPrevious())
			{
				fd = (FormatData) it.previous();
				buf = fd.getOrig();
				writer.write(buf.getBuf(), 0, 
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

	private void compressOutputData()
	{
		// remove leading and trailing white space, 
		// leading and trailing tags and tag pairs

		boolean change = true;
		FormatData fd_head;
		FormatData fd_tail;
		ListIterator it;
		int ctr;
		int len;
		FormatData fd;
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
                    fd_tail.tagData() == fd_head.tagData())
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

	/**
	 * Customized version of creatning input stream for HTML files,
	 * aware of encoding by using <code>EncodingAwareReader</code> class.
	 *
	 * @see org.omegat.util.EncodingAwareReader
	 */
	public Reader createInputStream(String infile) throws IOException
	{
		Reader ear = new AntiCRReader( new EncodingAwareReader(infile, EncodingAwareReader.ST_HTML) );
		return ear;
	}
	
	/** 
	 * Customized version of creating an output stream for HTML files,
	 * always UTF-8 and appending charset meta with UTF-8
	 */
	public Writer createOutputStream(String outfile) throws IOException
	{
		UTF8Writer uw = new UTF8Writer(outfile, UTF8Writer.ST_HTML);
		return uw;
	}

	private LinkedList 	m_tagList;

	private LinkedList	m_preNT;
	private LinkedList	m_fdList;
	private LinkedList	m_postNT;

    private boolean		m_ws;
	private boolean		m_pre;
	private boolean 	m_hasText;

    public Instance[] getDefaultInstances()
    {
        return new Instance[] {
            new Instance("*.html", ENCODING_AUTO, "UTF-8"),
            new Instance("*.htm", ENCODING_AUTO, "UTF-8", "${nameOnly}.html")
        };
    }

    public String getFileFormatName()
    {
        return "HTML files";
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public boolean isTargetEncodingVariable()
    {
        return false;
    }

    public void processFile(Reader reader2, Writer writer2) 
        throws IOException, TranslationException
    {
        reset();
		char c;
		int i;
		FormatData fd = null;

        BufferedReader reader = new BufferedReader(reader2);
        BufferedWriter writer = new BufferedWriter(writer2);
		try
		{
			while ((i = getNextChar(reader)) >= 0)
			{
				c = (char) i;

				if (c == '<')
				{
					// force a new format data element to be created
					//  if there's more text
					if (fd != null)
						fd = null;
					handleTag(reader, writer);
				}
				else if (c == 10 || c == 13 || c == 9 ||
                        c == ' ')
				{
					// white space
					if (!m_ws || fd == null)
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
					if (m_ws && !m_pre)
						continue;
					m_ws = true;

					if (m_pre)
						fd.appendDisplay(c);
					else
						fd.appendDisplay(' ');
				}
				else
				{
					// text
					if (!m_hasText ||
                            m_ws || fd == null)
					{
						fd = new FormatData();
						m_fdList.add(fd);
					}
					fd.appendOrig(c);
					if (c == '&')
					{
						c = getEscChar(reader, fd);
					}
					if (c == 160 && !m_hasText)
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
						fd.setHasText();
						m_hasText = true;
						m_ws = false;
					}
				}
			}
			writeEntry(writer);
            writer.flush();
		}
		catch( IOException ioe ) 
		{
    		String str = OStrings.FH_ERROR_WRITING_FILE;
            throw new IOException(str + " - " + ioe);	// NOI18N
		}
        /*
		catch(ParseException pe) 
		{
			throw new IOException(MessageFormat.format(OStrings.getString("HFH_ERROR_PARSEERROR"),
				new Object[] {m_file, new Integer(e.getErrorOffset() + line()), e.toString()} ));
		}
         */
    }

    public Reader createReader(File infile, String encoding) throws UnsupportedEncodingException, IOException
    {
		return new AntiCRReader( new EncodingAwareReader(infile.getAbsolutePath(), EncodingAwareReader.ST_HTML) );
    }
    public Writer createWriter(File outfile, String encoding) throws UnsupportedEncodingException, IOException
    {
		return new UTF8Writer(outfile.getAbsolutePath(), UTF8Writer.ST_HTML);
    }

	private HTMLTag identTag(Reader reader)
				throws IOException, TranslationException
	{
		char c;
		int ctr = 0;
		int lines = 0;
		int state = STATE_START;
		HTMLTag tag = new HTMLTag();
		int charType;
		HTMLTagAttr tagAttr = null;
		int i;
		int excl = 0;

		while (true)
		{
			ctr++;
			i = getNextChar(reader);
			if (i < 0)
				break;
			c = (char) i;
			if (c == 10 || c == 13)
				lines++;

			if (ctr == 1 && c == '/')
			{
				tag.setClose();
				continue;
			}

			if (ctr == 1 && c == '!')	// script
			{
				excl = 1;
				tag.verbatumAppend(c);
			}

			if (ctr == 1)
			{
				if (c == '/')
				{
					tag.setClose();
					continue;
				}
				else if (c == '!')
				{
					excl = 1;
					continue;
				}
			}
			else if (ctr == 2 && excl == 1)
			{
				if (c == '-')
					excl = 2;
			}

			if (excl > 1)
			{
				// loop until '-->' encountered
				if (c == '-')
					excl++;
				else if (c == '>' && excl >= 4)
					break;
				else 
					excl = 2;

				tag.verbatumAppend(c);
				continue;
			}

			switch (c)
			{
				case 9:
				case 10:
				case 13:
				case ' ':
					charType = TYPE_WS;
					break;
				case '=':
					charType = TYPE_EQUAL;
					break;
				case '\'':
					charType = TYPE_QUOTE_SINGLE;
					break;
				case '"': 
					charType = TYPE_QUOTE_DOUBLE;
					break;
				case '>':
					charType = TYPE_CLOSE;
					break;
				case 0:
					// unexpected null
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_NULL"), 
                            new Object[]{ new Integer(lines) }));
				default:
					charType = TYPE_NON_IDENT;
					break;
			}

			switch (state) 
			{
			case STATE_START:
				switch (charType) 
				{
				case TYPE_NON_IDENT:
					tag.nameAppend(c);
					state = STATE_TOKEN;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_BAD_TAG_START"), 
                            new Object[]{ new Integer(lines) }));
				}
				break;

			case STATE_TOKEN:
				switch (charType)
				{
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_WS:
					if (excl > 0)
						state = STATE_RECORD;
					else
						state = STATE_WS;
					break;
				case TYPE_NON_IDENT:
					tag.nameAppend(c);
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_BAD_TAG"), 
                            new Object[]{ new Integer(lines) }));
				}
				break;

			case STATE_RECORD:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_RECORD_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_RECORD_QUOTE_DOUBLE;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				}
				break;

			case STATE_RECORD_QUOTE_SINGLE:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_RECORD;
					break;
				}
				break;

			case STATE_RECORD_QUOTE_DOUBLE:
				switch (charType)
				{
				case TYPE_QUOTE_DOUBLE:
					state = STATE_RECORD;
					break;
				}
				break;

			case STATE_WS:
				switch (charType)
				{
				case TYPE_WS:
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_NON_IDENT:
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_ATTR_WS:
				// space after an attribute marker
				switch (charType)
				{
				case TYPE_WS:
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_NON_IDENT:
					// another attribute
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				case TYPE_EQUAL:
					state = STATE_EQUAL;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_EQUAL_WS:
				// space after an equal sign
				switch (charType)
				{
				case TYPE_WS:
					// poor formatting but OK
					break;
				case TYPE_NON_IDENT:
					state = STATE_VAL;
					tagAttr.valAppend(c);
					break;
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_DOUBLE;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_ATTR:
				switch (charType)
				{
				case TYPE_NON_IDENT:
					tagAttr.attrAppend(c);
					break;
				case TYPE_WS:
					state = STATE_ATTR_WS;
					break;
				case TYPE_EQUAL:
					state = STATE_EQUAL;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_EQUAL:
				switch (charType)
				{
				case TYPE_WS:
					state = STATE_EQUAL_WS;
					break;
				case TYPE_NON_IDENT:
					state = STATE_VAL;
					tagAttr.valAppend(c);
					break;
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_DOUBLE;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_VAL:
				switch (charType)
				{
				case TYPE_NON_IDENT:
					tagAttr.valAppend(c);
					break;
				case TYPE_WS:
					state = STATE_WS;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			case STATE_VAL_QUOTE_SINGLE:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_CLOSE;
					break;
				default:
					// anything else is fair game
					tagAttr.valAppend(c);
				}
				break;

			case STATE_VAL_QUOTE_DOUBLE:
				switch (charType)
				{
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_CLOSE;
					break;
				default:
					// anything else is fair game
					tagAttr.valAppend(c);
				}
				break;

			case STATE_VAL_QUOTE_CLOSE:
				switch (charType)
				{
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_WS:
					state = STATE_WS;
					break;
				case TYPE_NON_IDENT:
					// treat as new param
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				default:
					throw new TranslationException(
                            MessageFormat.format(OStrings.getString("HP_ERROR_UNEXPECTED_CHAR"), 
                            new Object[] { tag.verbatum().string(), new Integer(lines) }));
				}
				break;

			default:
				throw new TranslationException(
                        MessageFormat.format(OStrings.getString("HP_ERROR_UNKNOWN_STATE"), 
                        new Object[] { new Integer(lines) }));
			}

			if (state == STATE_CLOSE)
				break;
			else
				tag.verbatumAppend(c);

		}	
		tag.finalize();
		return tag;
	}

	private static final int TYPE_WS			= 1;
	private static final int TYPE_NON_IDENT		= 2;
	private static final int TYPE_EQUAL			= 3;
	private static final int TYPE_CLOSE			= 4;
	private static final int TYPE_QUOTE_SINGLE	= 5;
	private static final int TYPE_QUOTE_DOUBLE	= 6;
	
	private static final int STATE_START			= 0;
	private static final int STATE_TOKEN			= 1;
	private static final int STATE_WS				= 2;
	private static final int STATE_ATTR			= 3;
	private static final int STATE_ATTR_WS		= 5;
	private static final int STATE_EQUAL			= 6;
	private static final int STATE_EQUAL_WS		= 7;
	private static final int STATE_VAL_QUOTE_SINGLE			= 10;
	private static final int STATE_VAL_QUOTE_DOUBLE			= 11;
	private static final int STATE_VAL_QUOTE_CLOSE			= 15;
	private static final int STATE_VAL			= 20;
	private static final int STATE_RECORD			= 30;
	private static final int STATE_RECORD_QUOTE_SINGLE		= 31;
	private static final int STATE_RECORD_QUOTE_DOUBLE		= 32;
	private static final int STATE_CLOSE			= 40;

    private static HashMap	m_charMap;
	private static HashMap	m_escMap;

	static
	{
		m_escMap = new HashMap(512);
		m_charMap = new HashMap(512);

		addMapEntry((char)34, "quot");		 // NOI18N
		addMapEntry((char)38, "amp");		 // NOI18N
		addMapEntry((char)60, "lt");		 // NOI18N
		addMapEntry((char)62, "gt");		 // NOI18N
		addMapEntry((char)160, "nbsp");		 // NOI18N
		addMapEntry((char)161, "iexcl");	 // NOI18N
		addMapEntry((char)162, "cent");		 // NOI18N
		addMapEntry((char)163, "pound");	 // NOI18N
		addMapEntry((char)164, "curren");	 // NOI18N
		addMapEntry((char)165, "yen");		 // NOI18N
		addMapEntry((char)166, "brvbar");	 // NOI18N
		addMapEntry((char)167, "sect");		 // NOI18N
		addMapEntry((char)168, "uml");		 // NOI18N
		addMapEntry((char)169, "copy");		 // NOI18N
		addMapEntry((char)170, "ordf");		 // NOI18N
		addMapEntry((char)171, "laquo");	 // NOI18N
		addMapEntry((char)172, "not");		 // NOI18N
		addMapEntry((char)173, "shy");		 // NOI18N
		addMapEntry((char)174, "reg");		 // NOI18N
		addMapEntry((char)175, "macr");		 // NOI18N
		addMapEntry((char)176, "deg");		 // NOI18N
		addMapEntry((char)177, "plusmn");	 // NOI18N
		addMapEntry((char)178, "sup2");		 // NOI18N
		addMapEntry((char)179, "sup3");		 // NOI18N
		addMapEntry((char)180, "acute");	 // NOI18N
		addMapEntry((char)181, "micro");	 // NOI18N
		addMapEntry((char)182, "para");		 // NOI18N
		addMapEntry((char)183, "middot");	 // NOI18N
		addMapEntry((char)184, "cedil");	 // NOI18N
		addMapEntry((char)185, "sup1");		 // NOI18N
		addMapEntry((char)186, "ordm");		 // NOI18N
		addMapEntry((char)187, "raquo");	 // NOI18N
		addMapEntry((char)188, "frac14");	 // NOI18N
		addMapEntry((char)189, "frac12");	 // NOI18N
		addMapEntry((char)190, "frac34");	 // NOI18N
		addMapEntry((char)191, "iquest");	 // NOI18N
		addMapEntry((char)192, "Agrave");	 // NOI18N
		addMapEntry((char)193, "Aacute");	 // NOI18N
		addMapEntry((char)194, "Acirc");	 // NOI18N
		addMapEntry((char)195, "Atilde");	 // NOI18N
		addMapEntry((char)196, "Auml");		 // NOI18N
		addMapEntry((char)197, "Aring");	 // NOI18N
		addMapEntry((char)198, "AElig");	 // NOI18N
		addMapEntry((char)199, "Ccedil");	 // NOI18N
		addMapEntry((char)200, "Egrave");	 // NOI18N
		addMapEntry((char)201, "Eacute");	 // NOI18N
		addMapEntry((char)202, "Ecirc");	 // NOI18N
		addMapEntry((char)203, "Euml");		 // NOI18N
		addMapEntry((char)204, "Igrave");	 // NOI18N
		addMapEntry((char)205, "Iacute");	 // NOI18N
		addMapEntry((char)206, "Icirc");	 // NOI18N
		addMapEntry((char)207, "Iuml");		 // NOI18N
		addMapEntry((char)208, "ETH");		 // NOI18N
		addMapEntry((char)209, "Ntilde");	 // NOI18N
		addMapEntry((char)210, "Ograve");	 // NOI18N
		addMapEntry((char)211, "Oacute");	 // NOI18N
		addMapEntry((char)212, "Ocirc");	 // NOI18N
		addMapEntry((char)213, "Otilde");	 // NOI18N
		addMapEntry((char)214, "Ouml");		 // NOI18N
		addMapEntry((char)215, "times");	 // NOI18N
		addMapEntry((char)216, "Oslash");	 // NOI18N
		addMapEntry((char)217, "Ugrave");	 // NOI18N
		addMapEntry((char)218, "Uacute");	 // NOI18N
		addMapEntry((char)219, "Ucirc");	 // NOI18N
		addMapEntry((char)220, "Uuml");		 // NOI18N
		addMapEntry((char)221, "Yacute");	 // NOI18N
		addMapEntry((char)222, "THORN");	 // NOI18N
		addMapEntry((char)223, "szlig");	 // NOI18N
		addMapEntry((char)224, "agrave");	 // NOI18N
		addMapEntry((char)225, "aacute");	 // NOI18N
		addMapEntry((char)226, "acirc");	 // NOI18N
		addMapEntry((char)227, "atilde");	 // NOI18N
		addMapEntry((char)228, "auml");		 // NOI18N
		addMapEntry((char)229, "aring");	 // NOI18N
		addMapEntry((char)230, "aelig");	 // NOI18N
		addMapEntry((char)231, "ccedil");	 // NOI18N
		addMapEntry((char)232, "egrave");	 // NOI18N
		addMapEntry((char)233, "eacute");	 // NOI18N
		addMapEntry((char)234, "ecirc");	 // NOI18N
		addMapEntry((char)235, "euml");		 // NOI18N
		addMapEntry((char)236, "igrave");	 // NOI18N
		addMapEntry((char)237, "iacute");	 // NOI18N
		addMapEntry((char)238, "icirc");	 // NOI18N
		addMapEntry((char)239, "iuml");		 // NOI18N
		addMapEntry((char)240, "eth");		 // NOI18N
		addMapEntry((char)241, "ntilde");	 // NOI18N
		addMapEntry((char)242, "ograve");	 // NOI18N
		addMapEntry((char)243, "oacute");	 // NOI18N
		addMapEntry((char)244, "ocirc");	 // NOI18N
		addMapEntry((char)245, "otilde");	 // NOI18N
		addMapEntry((char)246, "ouml");		 // NOI18N
		addMapEntry((char)247, "divide");	 // NOI18N
		addMapEntry((char)248, "oslash");	 // NOI18N
		addMapEntry((char)249, "ugrave");	 // NOI18N
		addMapEntry((char)250, "uacute");	 // NOI18N
		addMapEntry((char)251, "ucirc");	 // NOI18N
		addMapEntry((char)252, "uuml");		 // NOI18N
		addMapEntry((char)253, "yacute");	 // NOI18N
		addMapEntry((char)254, "thorn");	 // NOI18N
		addMapEntry((char)255, "yuml");		 // NOI18N
		addMapEntry((char)338, "OElig");	 // NOI18N
		addMapEntry((char)339, "oelig");	 // NOI18N
		addMapEntry((char)352, "Scaron");	 // NOI18N
		addMapEntry((char)353, "scaron");	 // NOI18N
		addMapEntry((char)376, "Yuml");		 // NOI18N
		addMapEntry((char)402, "fnof");		 // NOI18N
		addMapEntry((char)710, "circ");		 // NOI18N
		addMapEntry((char)732, "tilde");	 // NOI18N
		addMapEntry((char)913, "Alpha");	 // NOI18N
		addMapEntry((char)914, "Beta");		 // NOI18N
		addMapEntry((char)915, "Gamma");	 // NOI18N
		addMapEntry((char)916, "Delta");	 // NOI18N
		addMapEntry((char)917, "Epsilon");	 // NOI18N
		addMapEntry((char)918, "Zeta");		 // NOI18N
		addMapEntry((char)919, "Eta");		 // NOI18N
		addMapEntry((char)920, "Theta");	 // NOI18N
		addMapEntry((char)921, "Iota");		 // NOI18N
		addMapEntry((char)922, "Kappa");	 // NOI18N
		addMapEntry((char)923, "Lambda");	 // NOI18N
		addMapEntry((char)924, "Mu");		 // NOI18N
		addMapEntry((char)925, "Nu");		 // NOI18N
		addMapEntry((char)926, "Xi");		 // NOI18N
		addMapEntry((char)927, "Omicron");	 // NOI18N
		addMapEntry((char)928, "Pi");		 // NOI18N
		addMapEntry((char)929, "Rho");		 // NOI18N
		addMapEntry((char)931, "Sigma");	 // NOI18N
		addMapEntry((char)932, "Tau");		 // NOI18N
		addMapEntry((char)933, "Upsilon");	 // NOI18N
		addMapEntry((char)934, "Phi");		 // NOI18N
		addMapEntry((char)935, "Chi");		 // NOI18N
		addMapEntry((char)936, "Psi");		 // NOI18N
		addMapEntry((char)937, "Omega");	 // NOI18N
		addMapEntry((char)945, "alpha");	 // NOI18N
		addMapEntry((char)946, "beta");		 // NOI18N
		addMapEntry((char)947, "gamma");	 // NOI18N
		addMapEntry((char)948, "delta");	 // NOI18N
		addMapEntry((char)949, "epsilon");	 // NOI18N
		addMapEntry((char)950, "zeta");		 // NOI18N
		addMapEntry((char)951, "eta");		 // NOI18N
		addMapEntry((char)952, "theta");	 // NOI18N
		addMapEntry((char)953, "iota");		 // NOI18N
		addMapEntry((char)954, "kappa");	 // NOI18N
		addMapEntry((char)955, "lambda");	 // NOI18N
		addMapEntry((char)956, "mu");		 // NOI18N
		addMapEntry((char)957, "nu");		 // NOI18N
		addMapEntry((char)958, "xi");		 // NOI18N
		addMapEntry((char)959, "omicron");	 // NOI18N
		addMapEntry((char)960, "pi");		 // NOI18N
		addMapEntry((char)961, "rho");		 // NOI18N
		addMapEntry((char)962, "sigmaf");	 // NOI18N
		addMapEntry((char)963, "sigma");	 // NOI18N
		addMapEntry((char)964, "tau");		 // NOI18N
		addMapEntry((char)965, "upsilon");	 // NOI18N
		addMapEntry((char)966, "phi");		 // NOI18N
		addMapEntry((char)967, "chi");		 // NOI18N
		addMapEntry((char)968, "psi");		 // NOI18N
		addMapEntry((char)969, "omega");	 // NOI18N
		addMapEntry((char)977, "thetasym");	 // NOI18N
		addMapEntry((char)978, "upsih");	 // NOI18N
		addMapEntry((char)982, "piv");		 // NOI18N
		addMapEntry((char)8194, "ensp");	 // NOI18N
		addMapEntry((char)8195, "emsp");	 // NOI18N
		addMapEntry((char)8201, "thinsp");	 // NOI18N
		addMapEntry((char)8204, "zwnj");	 // NOI18N
		addMapEntry((char)8205, "zwj");		 // NOI18N
		addMapEntry((char)8206, "lrm");		 // NOI18N
		addMapEntry((char)8207, "rlm");		 // NOI18N
		addMapEntry((char)8211, "ndash");	 // NOI18N
		addMapEntry((char)8212, "mdash");	 // NOI18N
		addMapEntry((char)8216, "lsquo");	 // NOI18N
		addMapEntry((char)8217, "rsquo");	 // NOI18N
		addMapEntry((char)8218, "sbquo");	 // NOI18N
		addMapEntry((char)8220, "ldquo");	 // NOI18N
		addMapEntry((char)8221, "rdquo");	 // NOI18N
		addMapEntry((char)8222, "bdquo");	 // NOI18N
		addMapEntry((char)8224, "dagger");	 // NOI18N
		addMapEntry((char)8225, "Dagger");	 // NOI18N
		addMapEntry((char)8226, "bull");	 // NOI18N
		addMapEntry((char)8230, "hellip");	 // NOI18N
		addMapEntry((char)8240, "permil");	 // NOI18N
		addMapEntry((char)8242, "prime");	 // NOI18N
		addMapEntry((char)8243, "Prime");	 // NOI18N
		addMapEntry((char)8249, "lsaquo");	 // NOI18N
		addMapEntry((char)8250, "rsaquo");	 // NOI18N
		addMapEntry((char)8254, "oline");	 // NOI18N
		addMapEntry((char)8260, "frasl");	 // NOI18N
		addMapEntry((char)8364, "euro");	 // NOI18N
		addMapEntry((char)8465, "image");	 // NOI18N
		addMapEntry((char)8472, "weierp");	 // NOI18N
		addMapEntry((char)8476, "real");	 // NOI18N
		addMapEntry((char)8482, "trade");	 // NOI18N
		addMapEntry((char)8501, "alefsym");	 // NOI18N
		addMapEntry((char)8592, "larr");	 // NOI18N
		addMapEntry((char)8593, "uarr");	 // NOI18N
		addMapEntry((char)8594, "rarr");	 // NOI18N
		addMapEntry((char)8595, "darr");	 // NOI18N
		addMapEntry((char)8596, "harr");	 // NOI18N
		addMapEntry((char)8629, "crarr");	 // NOI18N
		addMapEntry((char)8656, "lArr");	 // NOI18N
		addMapEntry((char)8657, "uArr");	 // NOI18N
		addMapEntry((char)8658, "rArr");	 // NOI18N
		addMapEntry((char)8659, "dArr");	 // NOI18N
		addMapEntry((char)8660, "hArr");	 // NOI18N
		addMapEntry((char)8704, "forall");	 // NOI18N
		addMapEntry((char)8706, "part");	 // NOI18N
		addMapEntry((char)8707, "exist");	 // NOI18N
		addMapEntry((char)8709, "empty");	 // NOI18N
		addMapEntry((char)8711, "nabla");	 // NOI18N
		addMapEntry((char)8712, "isin");	 // NOI18N
		addMapEntry((char)8713, "notin");	 // NOI18N
		addMapEntry((char)8715, "ni");		 // NOI18N
		addMapEntry((char)8719, "prod");	 // NOI18N
		addMapEntry((char)8721, "sum");		 // NOI18N
		addMapEntry((char)8722, "minus");	 // NOI18N
		addMapEntry((char)8727, "lowast");	 // NOI18N
		addMapEntry((char)8730, "radic");	 // NOI18N
		addMapEntry((char)8733, "prop");	 // NOI18N
		addMapEntry((char)8734, "infin");	 // NOI18N
		addMapEntry((char)8736, "ang");		 // NOI18N
		addMapEntry((char)8743, "and");		 // NOI18N
		addMapEntry((char)8744, "or");		 // NOI18N
		addMapEntry((char)8745, "cap");		 // NOI18N
		addMapEntry((char)8746, "cup");		 // NOI18N
		addMapEntry((char)8747, "int");		 // NOI18N
		addMapEntry((char)8756, "there4");	 // NOI18N
		addMapEntry((char)8764, "sim");		 // NOI18N
		addMapEntry((char)8773, "cong");	 // NOI18N
		addMapEntry((char)8776, "asymp");	 // NOI18N
		addMapEntry((char)8800, "ne");		 // NOI18N
		addMapEntry((char)8801, "equiv");	 // NOI18N
		addMapEntry((char)8804, "le");		 // NOI18N
		addMapEntry((char)8805, "ge");		 // NOI18N
		addMapEntry((char)8834, "sub");		 // NOI18N
		addMapEntry((char)8835, "sup");		 // NOI18N
		addMapEntry((char)8836, "nsub");	 // NOI18N
		addMapEntry((char)8838, "sube");	 // NOI18N
		addMapEntry((char)8839, "supe");	 // NOI18N
		addMapEntry((char)8853, "oplus");	 // NOI18N
		addMapEntry((char)8855, "otimes");	 // NOI18N
		addMapEntry((char)8869, "perp");	 // NOI18N
		addMapEntry((char)8901, "sdot");	 // NOI18N
		addMapEntry((char)8968, "lceil");	 // NOI18N
		addMapEntry((char)8969, "rceil");	 // NOI18N
		addMapEntry((char)8970, "lfloor");	 // NOI18N
		addMapEntry((char)8971, "rfloor");	 // NOI18N
		addMapEntry((char)9001, "lang");	 // NOI18N
		addMapEntry((char)9002, "rang");	 // NOI18N
		addMapEntry((char)9674, "loz");		 // NOI18N
		addMapEntry((char)9824, "spades");	 // NOI18N
		addMapEntry((char)9827, "clubs");	 // NOI18N
		addMapEntry((char)9829, "hearts");	 // NOI18N
		addMapEntry((char)9830, "diams");	 // NOI18N
	}

	private static void addMapEntry(char val, String name)
	{
		m_escMap.put(name, new Character(val));
		m_charMap.put(new Character(val), name);
	}
	
	public static char convertToChar(String tok)
	{
		Character c = (Character) m_escMap.get(tok);
		if (c == null)
		{
			Integer i = new Integer(tok);
			return (char) i.intValue();
		}
		else
			return c.charValue();
	}

	public static String convertToEsc(char c)
	{
		String s = (String) m_charMap.get(new Character(c));
        if (s != null)
		{
			return "&" + s + ";";	 // NOI18N
		}
        else
            return ""+c;
	}

	public static String convertToEsc(String s)
	{
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<s.length(); i++)
            sb.append(convertToEsc(s.charAt(i)));
        return sb.toString();
	}

}
