//-------------------------------------------------------------------------
//  
//  XMLStreamReader.java - 
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


import java.lang.String;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.Array;

class XMLStreamReader
{
	public XMLStreamReader()
	{
		m_pos = -1;
		m_stringStream = "";
		m_pushedChar = 0;
		m_ignoreReturn = false;
		m_killEmptyBlocks = false;
	}
	
	// set the stream that tags and text are read from
	public void setStream(String str)
	{
		m_pos = 0;
		m_stringStream = str;
	}

	public void setStream(File name)
		throws FileNotFoundException, UnsupportedEncodingException,
				IOException
	{
		setStream(name, "UTF-8");
	}
				
	public void setStream(String name, String encoding)
		throws FileNotFoundException, UnsupportedEncodingException,
				IOException
	{
		setStream(new File(name), encoding);
	}
	// opens and reads the file according to the specified encoding
	// should have this auto-sense the unicode file type, but keep
	//  it general as many 'xml' apps don't use unicode
	public void setStream(File name, String encoding)
		throws FileNotFoundException, UnsupportedEncodingException,
				IOException
	{
		m_pos = -1;
		FileInputStream fis = new FileInputStream(name);
		InputStreamReader isr = new InputStreamReader(fis, encoding);
		m_bufferedReader = new BufferedReader(isr);

		// make sure XML file and encoding is proper
		try 
		{
			XMLBlock blk = getNextBlock();
			if (blk == null)
			{
				throw new ParseException(
							"unable to initialize read of XML file", 0);
			}
			if (blk.getTagName().equals("xml") == true)
			{
				String ver = blk.getAttribute("version");
				String enc = blk.getAttribute("encoding");
				if ((ver == null) || (ver.equals("")))
				{
					// no version declared - assume it's readable
				}
				else if (ver.equals("1.0") == false)
				{
					throw new ParseException("Unsupported XML version (" + 
							ver + ")", 0);
				}
				// TODO check encoding - adjust to match
			}
			else
			{
				// not a valid XML file
				throw new ParseException("not a valid XML file", 0);
			}
		}
		catch (ParseException p1)
		{
			// TODO intelligently handle error
			throw new IOException("Cannot load specified file (" + name +
					")\n" + p1);
		}
	}
	
	// returns next object in stream - either a tag or a string
	public XMLBlock getNextBlock() throws ParseException
	{
		// begin reading text stream
		// if first char a '<' then we've got a tag
		// otherwise it's text
		// strip out any newline and multiple spaces (not valid xml)
		char c = getNextChar();
		if (c == '<')
		{
			return getNextTag();
		}
		else if (c != 0)
		{
			pushChar(c);
			XMLBlock blk = getNextText();
			if ((blk != null) && m_killEmptyBlocks)
			{
				String str = blk.getText();
				str = str.trim();
				if (str.length() == 0)
				{
					blk = getNextBlock();
				}
			}
			return blk;
		}
		else
			return null;
	}

	public void killEmptyBlocks(boolean kill)
	{
		m_killEmptyBlocks = kill;
	}

	public void ignoreReturn(boolean ignore)
	{
		m_ignoreReturn = ignore;
	}

	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	// protected routines

	protected void pushChar(char c)
	{
		if (m_pushedChar == 0)
			m_pushedChar = c;
		else
		{
			System.out.println("Internal Error - pushed multiple chars");
		}
	}
	
	protected char getNextChar()
	{
		if (m_pushedChar != 0)
		{
			// if character pushed back on the stream, return it
			char d = m_pushedChar;
			m_pushedChar = 0;
			return d;
		}
		else
		{
			if (m_pos >= 0)
			{
				// string
				if (m_pos < m_stringStream.length())
				{
					char c = m_stringStream.charAt(m_pos++);
					if (c == 13)
					{
						// convert 13 to 10 - or just omit 13
						// (XML specs instruct this)
						c = m_stringStream.charAt(m_pos);
						if (c == 10)
						{
							// simply drop 13
							m_pos++;
						}
						else
							c = 10;
					}
					if ((m_ignoreReturn) && (c == 10))
						return getNextChar();
					else
						return c;
				}
				else
					return 0;
			}
			else
			{
				// regular call to read returns int which can't be cast
				// ... so, get the next character in this roundabout fashion
				char[] c = new char[2];
				try
				{
					char b;
					int res = m_bufferedReader.read(c, 0, 1);
					if (res > 0)
					{
						b = c[0];
						if (b == 13)
						{
							// convert 13 10 to 10 and 13 to 10
							res = m_bufferedReader.read(c, 0, 1);
							if (res > 0)
							{
								b = c[0];
								if (b != 10)
								{
									// not a cr/lf pair - make sure not
									// another cr and then push char
									if (b == 13)
										pushChar((char) 0x0a);
									else
										pushChar(b);
								}
								// else - do nothing; swallow the 13
							}
							else
								b = 0;
						}
					}
					else
						return 0;
					
					if ((m_ignoreReturn) && (b == 10))
						return getNextChar();
					else
						return b;
				}
				catch (IOException e)
				{
					System.out.println("IOException encountered: " + e);
				}
				return 0;
			}
		}
	}
	
	protected XMLBlock getNextText() throws ParseException
	{
		XMLBlock blk = new XMLBlock();
		String str = "";
		char c;
		while (((c = getNextChar()) != '<') && ( c != 0))
		{
			if (c == '&')
				str += getEscChar();
			else
				str += c;
		}
		if (c == '<')
			pushChar(c);

		blk.setText(str);
		return blk;
	}

	// tags defined by <!
	protected XMLBlock getNextTagExclamation() throws ParseException
	{
		// for <!declaration .... >
		//  copy declaration into tagname and '...' into first attribute
		// for <![statement[ ... ]]>
		//  copy 'statement[ ... ]' as raw text

		final int state_start			= 1;
		final int state_name			= 2;
		final int state_finish			= 3;
		final int state_record			= 4;
		final int state_recordSingle	= 5;
		final int state_recordDouble	= 6;
		final int state_escSingle		= 7;
		final int state_escDouble		= 8;
		final int state_cdata			= 9;
		final int state_commentStart	= 10;
		final int state_comment			= 11;

		XMLBlock blk = new XMLBlock();
		blk.setTypeChar('!');
		String name = "";
		String data = "";
		int state = state_start;
		int type;
		boolean err = false;
		String msg = "";
		char c;

		// for CDATA and comments
		int bracCnt = 0;
		int dashCnt = 0;

		while ((c = getNextChar()) != 0)
		{
			type = getCharType(c);
			switch (state)
			{
				case state_start:
					switch (type)
					{
						case type_ws:
							// this is OK - do nothing
							break;
							
						case type_text:
							// name - start copying
							state = state_name;
							name += c;
							break;

						case type_opBrac:
							blk.setTagName("CDATA");
							state = state_cdata;
							break;

						case type_dash:
							state = state_commentStart;
							blk.setComment();
							break;

						default:
							err = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_commentStart:
					// verify start of comment string
					if (c == '-')
					{
						state = state_comment;
					}
					else
					{
						err = true;
						msg = "Confused by false start of comment tag";
					}
					break;

				case state_comment:
					// verify comment string - copy until -->
					switch (type)
					{
						case type_dash:
							if (dashCnt >= 2)
								data += c;
							else
								dashCnt++;
							break;

						case type_gt:
							if (dashCnt >= 2)
							{
								// all done 
								blk.setAttribute(data, "");
								state = state_finish;
							}
							break;
							
						default:
							if (dashCnt > 0)
							{
								// false signal for comment end - return '-'
								//  to stream
								while (dashCnt > 0)
								{
									data += '-';
									dashCnt--;
								}
							}
							data += c;
					}
					break;
					
				case state_cdata:
					// copy until ]]> encountered
					switch (type)
					{
						case type_clBrac:
							bracCnt++;
							break;

						case type_gt:
							if (bracCnt >= 2)
							{
								// all done - append all but last bracket
								while (bracCnt > 1)
								{
									data += ']';
									bracCnt--;
								}
								state = state_finish;
							}
							break;
							
						default:
							if (bracCnt > 0)
							{
								while (bracCnt > 0)
								{
									bracCnt--;
									data += ']';
								}
							}
							data += c;
					}
					break;
					
				case state_name:
					switch (type)
					{
						case type_text:
							// continue copying name
							name += c;
							break;

						case type_ws:
							// name done - store it and move on
							blk.setTagName(name);
							state = state_record;
							break;
							
						case type_gt:
							// no declared data - strange, but allow it
							state = state_finish;
							break;

						default:
							err = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;
					
				case state_record:
					switch (type)
					{
						case type_apos:
							// continue copying in 'safe' mode
							state = state_recordSingle;
							data += c;
							break;

						case type_quote:
							// continue copying in 'safe' mode
							state = state_recordDouble;
							data += c;
							break;
							
						case type_gt:
							// tag done - record data and close
							state = state_finish;
							blk.setAttribute(data, "");
							break;

						default:
							data += c;
					}
					break;
					
				case state_recordSingle:
					switch (type)
					{
						case type_apos:
							// continue copying normally
							state = state_record;
							data += c;
							break;

						case type_backSlash:
							// ignore meaning of next char
							state = state_escSingle;
							data += c;
							break;
							
						default:
							data += c;
					}
					break;

				case state_escSingle:
					// whatever happens, just remember character
					data += c;
					state = state_recordSingle;
					break;

				case state_recordDouble:
					switch (type)
					{
						case type_quote:
							// continue copying normally
							state = state_record;
							data += c;
							break;

						case type_backSlash:
							// ignore meaning of next char
							state = state_escDouble;
							data += c;
							break;
							
						default:
							data += c;
					}
					break;
					
				case state_escDouble:
					// whatever happens, just remember character
					data += c;
					state = state_recordDouble;
					break;

			}
			if (err == true)
			{
				// TODO construct error message with correct state data
				// for now, just throw a parse error
				String str = "\nTag name: ! " + blk.getTagName() + " ";
				if (blk.isComment())
					str += "(comment tag) ";
				if (blk.numAttributes() > 0)
					str += ((XMLAttribute) blk.getAttribute(0)).name;
				throw new ParseException(msg + str + "::" + data, 0);
			}
			else if (state == state_finish)
			{
				break;
			}
		}
		return blk;
	}

	protected XMLBlock getNextTag() throws ParseException
	{
		final int state_start				= 1;
		final int state_buildName			= 2;
		final int state_setCloseFlag		= 3;
		final int state_setEmptyFlag		= 4;
		final int state_attrStandby			= 5;
		final int state_buildAttr			= 6;
		final int state_transitionFromAttr	= 7;
		final int state_buildValue			= 8;
		final int state_closeValueQuote		= 9;
		final int state_finish				= 10;
		final int state_xmlDeclaration		= 11;

		char c = getNextChar();
		if (c == 0)
			return null;

		XMLBlock blk = new XMLBlock();

		// <! encountered - handle it seperately
		if (c == '!')
			return getNextTagExclamation();
		else if (c == '?')
		{
			// handle this like a normal tag - let stream class figure
			//  out its importance
			c = getNextChar();
			blk.setTypeChar('?');
		}
		
		int state = state_start;
		boolean error = false;
		String msg = "";
		String name = "";
		String attr = "";
		String val = "";
		int type = 0;
		while (c != 0)
		{
			type = getCharType(c);
			switch(state)
			{
				case state_start:
					switch (type)
					{
						case type_slash:
							blk.setCloseFlag();
							state = state_setCloseFlag;
							break;

						case type_text:
							name += c;
							state = state_buildName;
							break;

						case type_ws:
							// white space and no name yet - continue
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_buildName:
					switch (type)
					{
						case type_dash:
						case type_text:
							// more name text
							name += c;
							break;

						case type_ws:
							// name is done - move on
							state = state_attrStandby;
							blk.setTagName(name);
							break;

						case type_slash:
							// name done - empty tag slash encountered
							blk.setTagName(name);
							blk.setEmptyFlag();
							state = state_setEmptyFlag;
							break;

						case type_gt:
							// all done
							blk.setTagName(name);
							state = state_finish;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_setCloseFlag:
					switch (type)
					{
						case type_text:
							// close flag marked not text - start copy
							name += c;
							state = state_buildName;
							break;

						case type_ws:
							// space after close flag - ignore and continue
							break;

						default:
							msg = "Unexpected char '" + c + "' (" +state+ ")";
							error = true;
					}
					break;

				case state_setEmptyFlag:
					switch (type)
					{
						case type_ws:
							// allow white space to be lenient
							break;

						case type_gt:
							// all done with empty tag
							state = state_finish;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_attrStandby:
					switch (type)
					{
						case type_text:
							// start of attribute name - start recording
							attr += c;
							state = state_buildAttr;
							break;

						case type_ques:
							// allow question mark so <? ?> tags can
							// be read by standard parser
							state = state_xmlDeclaration;
							break;
							
						case type_ws:
							// unexpected space - allow for now because
							//  it isn't ambiguous (be lenient)
							break;

						case type_slash:
							blk.setEmptyFlag();
							state = state_setEmptyFlag;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_xmlDeclaration:
					if (c != '>')
					{
						// parse error - got '?' followed by something 
						//  unexpected
						error = true;
						msg = "Floating '?' character not tied to '>'";
					}
					else
						state = state_finish;
					break;

				case state_buildAttr:
					switch (type)
					{
						case type_dash:
						case type_text:
							// more name - keep recording
							attr += c;
							break;

						case type_equals:
							// attr done - begin move to value
							state = state_transitionFromAttr;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_transitionFromAttr:
					switch (type)
					{
						case type_quote:
							// the only valid next character
							state = state_buildValue;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				case state_buildValue:
					switch (type)
					{
						case type_quote:
							// done recording value
							// store it and move on
							blk.setAttribute(attr, val);
							attr = "";
							val = "";
							state = state_closeValueQuote;
							break;

						default:
							// this is a quoted value - be lenient on OK chars
							val += c;
							break;
					}
					break;

				case state_closeValueQuote:
					switch (type)
					{
						case type_text:
							// new attribute - start recording
							attr += c;
							state = state_buildAttr;
							break;

						case type_ws:
							// allow this for now
							break;

						case type_slash:
							// empty tag with attributes
							blk.setEmptyFlag();
							state = state_setEmptyFlag;
							break;

						case type_gt:
							// finished
							state = state_finish;
							break;

						case type_ques:
							// allow question mark so <? ?> tags can
							// be read by standard parser
							state = state_xmlDeclaration;
							break;

						default:
							error = true;
							msg = "Unexpected char '" + c + "' (" +state+ ")";
					}
					break;

				default:
					System.out.println("INTERNAL ERROR untrapped parse state "
								+ state);
			}

			if (error == true)
			{
				// TODO construct error message with correct state data
				// for now, just throw a parse error
				String data = "\nTag name: " + blk.getTagName() + " ";
				if (blk.isEmpty())
					data += "(empty tag) ";
				else if (blk.isClose())
					data += "(close tag) ";
				if (blk.numAttributes() > 0)
					data += "loaded " + blk.numAttributes() + " attributes.";
				throw new ParseException(msg + data, 0);
			}
			else if (state == state_finish)
			{
				break;
			}

			c = getNextChar();
		}

		return blk;
	}

	protected static final int	type_text		= 1;
	protected static final int	type_ws			= 2;
	protected static final int	type_apos		= 3;
	protected static final int	type_quote		= 4;
	protected static final int	type_lt			= 5;
	protected static final int	type_gt			= 6;
	protected static final int	type_amp		= 7;

	protected static final int	type_equals		= 8;
	protected static final int	type_ques		= 9;
	protected static final int	type_opBrac		= 10;
	protected static final int	type_clBrac		= 11;
	protected static final int	type_slash		= 12;
	protected static final int	type_backSlash	= 13;
	protected static final int	type_dash		= 14;

	// used by getNextTag for parsing of tag data
	protected int getCharType(char c)
	{
		int type = type_text;
		switch (c)
		{
			case 0x20:
			case 0x0a:
			case 0x0d:
			case 0x09:
				type = type_ws;
				break;

			case '"':
				type = type_quote;
				break;

			case '\'':
				type = type_apos;
				break;
				
			case '&':
				type = type_amp;
				break;
				
			case '<':
				type = type_lt;
				break;
				
			case '>':
				type = type_gt;
				break;

			case '?':
				type = type_ques;
				break;

			case '/':
				type = type_slash;
				break;

			case '=':
				type = type_equals;
				break;
				
			case '[':
				type = type_opBrac;
				break;
				
			case ']':
				type = type_clBrac;
				break;
				
			case '-':
				type = type_dash;
				break;
				
			case '\\':
				type = type_backSlash;
				break;
		}
		return type;
	}

	// converts a stream of plaintext into valid XML 
	// output stream must convert stream to UTF-8 when saving to disk
	public static String controlify(String plaintext)
	{
		char c;
		String out = "";
		for (int i=0; i<plaintext.length(); i++)
		{
			c = plaintext.charAt(i);
			if (c == '\'')
				out += "&apos;";
			else if (c == '&')
				out += "&amp;";
			else if (c == '>')
				out += "&gt;";
			else if (c == '<')
				out += "&lt;";
			else if (c == '"')
				out += "&quot;";
//			else if (c > 1270000)
//			{
//				if (c > 0x07ff)
//				{
//					// triple byte
//					out += createEscByte((byte) (0xe0 | ((c >>> 12) & 0x0f)));
//					out += createEscByte((byte) (0x80 | ((c >>>  6) & 0x3f)));
//					out += createEscByte((byte) (0x80 | ( c         & 0x3f)));
//				}
//				else
//				{
//					// double byte
//					out += createEscByte((byte) (0xc0 | ((c >>>  6) & 0x1f)));
//					out += createEscByte((byte) (0x80 | ( c         & 0x3f)));
//				}
//			}
			else
				out += c;
		}
		return out;
	}

	// return the list of blocks between the specified block and
	//  its matching close
	// if the provided block is not an empty tag, or if there are
	//  no elements between open and close, a null is returned
	public ArrayList closeBlock(XMLBlock block) throws ParseException
	{
		ArrayList lst = new ArrayList();
		
		// sanity check
		if (block == null)
			return lst;

		// if block is empty, return straight away
		if (block.isEmpty())
			return lst;

		// start search
		int depth = 0;
		XMLBlock blk = null;
		while (true)
		{
			blk = getNextBlock();
			if (blk == null)
			{
				// stream ended without finding match
				// TODO signal error
				break;
			}
			
			if ((blk.isTag()) && (blk.getTagName().equals(block.getTagName())))
			{
				if (blk.isClose())
				{
				   	if (depth == 0)
					{
						// found the closing tag
						break;
					}
					else
						depth--;
				}
				else
				{
					// imbedded tag of same name - increase stack count
					depth++;
				}
				lst.add(blk);
			}
			else
			{
				lst.add(blk);
			}
		}

		if ((blk == null) || (lst.size() == 0))
			return null;
		else
			return lst;
	}

	public XMLBlock advanceToTag(String tagname) throws ParseException
	{
		XMLBlock blk = null;
		while (true)
		{
			blk = getNextBlock();
			if (blk == null)
			{
				break;
			}
			
			if ((blk.isTag()) && (blk.getTagName().equals(tagname)))
			{
				break;
			}

		}
		return blk;
	}

	protected char getEscChar() throws ParseException
	{
		// look for amp, lt, gt, apos, quot and &#
		char c = getNextChar();
		String val = "";
		boolean hex = false;
		
		if (c == '#')
		{
			// char code
			c = getNextChar();
			if ((c == 'x') || (c == 'X'))
			{
				c = getNextChar();
				hex = true;
			}
		}
		while (c != ';')
		{
			val += c;
			if (c == 0)
			{
				throw new ParseException(
							"Escaped character never terminated", 0);
			}
			c = getNextChar();
		}

		if (val.equals("amp"))
			return '&';
		else if (val.equals("lt"))
			return '<';
		else if (val.equals("gt"))
			return '>';
		else if (val.equals("apos"))
			return '\'';
		else if (val.equals("quot"))
			return '"';

		// else, binary data
		char b;
		for (int i=0; i<val.length(); i++)
		{
			b = val.charAt(i);
			if (hex)
			{
				c *= 16;
				if ((b >= '0') && (b <= '9'))
					c += b;
				else if ((b >= 'A') && (b <= 'F'))
				{
					c += 10;
					c += b - 'A';
				}
				else if ((b >= 'a') && (b <= 'f'))
				{
					c += 10;
					c += b - 'a';
				}
				else
				{
					throw new ParseException("Escaped binary char contains " +
							"malformed data (&#x" + val + ";)", 0);
				}
			}
			else
			{
				c *= 10;
				if ((b >= '0') && (b <= '9'))
					c += b - '0';
				else
				{
					throw new ParseException("Escaped decimal char contains " +
							"malformed data (&#" + val + ";)", 0);
				}
			}
		}

		return c;
	}
	
	///////////////////////////////////////////////////////////////

	protected BufferedReader	m_bufferedReader;
	protected String			m_stringStream;

	protected int				m_pos;
	protected char				m_pushedChar;
	protected boolean			m_ignoreReturn; //swallow 0x0a?
	protected boolean			m_killEmptyBlocks;

	//////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		XMLBlock blk;
		String str = "<?xml version=\"1.0\"?><head>text here</head>" +
			"<body size=\"big\" color=\"fuschia\">there once was a " +
			"woman from venus<bold> whose </bold> body was</body>" +
			"<graph/><plot />";
		XMLStreamReader reader = new XMLStreamReader();
		String st = "test: <> &\"; ñ-éÑaâä ";
		System.out.println(st);
		System.out.println(controlify(st));
//		System.out.println(str);
//		reader.setStream(str);
		int ctr = 0;
		try 
		{
//			String name = "content.xml";
			String name = "test.xml";
			if (Array.getLength(args) > 0)
				name = args[0];
			System.out.println("opening file " + name + "\n");
			reader.setStream(new File(name), "UTF8");
			reader.ignoreReturn(true);
			while ((blk = reader.getNextBlock()) != null)
			{
				if (ctr++ > 2000)
					break;
				if (blk.isTag() && blk.getTagName().equals("head") && 
							blk.isClose())
				{
					printBlock(blk);
					blk = reader.advanceToTag("body");
					if (blk == null)
					{
						System.out.println("unable to find body tag");
						break;
					}
					ArrayList lst = reader.closeBlock(blk);
					if (lst == null)
					{
						System.out.println("unable to close block");
						break;
					}
					System.out.println("******** printing body block ********");
					for (int i=0; i<lst.size(); i++)
					{
						printBlock((XMLBlock) lst.get(i));
					}
					System.out.println("******** finished with body block ********");
				}
				else
					printBlock(blk);
			}
		}
		catch (ParseException e)
		{
			System.out.println("Parse exception\n" + e);
		}
		catch (FileNotFoundException e2)
		{
			System.out.println("File not found Exception\n" + e2);
		}
		catch (UnsupportedEncodingException e3)
		{
			System.out.println("Unsupported encoding exception\n" + e3);
		}
		catch (IOException e4)
		{
			System.out.println("IO exception: " + e4);
		}
	}

	static private void printBlock(XMLBlock blk)
	{
		if (blk == null)
			System.out.println("null block");
		else if (blk.isTag())
		{
			XMLAttribute attr;
			String mod = "";
			if (blk.getTypeChar() == '!')
			{
				System.out.println("tag ! '" + blk.getTagName() + "'");
				System.out.println("    " + 
							blk.getAttribute(0).name);
			}
			else if (blk.getTypeChar() == '?')
			{
				System.out.println("tag ? '" + blk.getTagName() + "'");
				for (int i=0; i<blk.numAttributes(); i++)
				{
					attr = blk.getAttribute(i);
					System.out.println("    " + attr.name + "=" + attr.value);
				}
			}
			else
			{
				if (blk.isEmpty())
					mod = " (empty tag)";
				else if (blk.isClose())
					mod = " (close tag)";
				System.out.println("tag   '" + blk.getTagName() + "' " + mod);
				for (int i=0; i<blk.numAttributes(); i++)
				{
					attr = blk.getAttribute(i);
					System.out.println("    " + attr.name + "=" + attr.value);
				}
			}
		}
		else
		{
			System.out.println("text: " + blk.getText());
		}
	}
	
}
