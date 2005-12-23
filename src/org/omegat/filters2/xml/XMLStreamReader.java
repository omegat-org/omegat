/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2005  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Stack;

import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml.XMLReader;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * A reader for XML stream.
 *
 * @author Keith Godfrey
 */
public class XMLStreamReader
{
    private DefaultEntityFilter	entityFilter;
    
    public XMLStreamReader()
    {
        m_pos = -1;
        m_stringStream = "";	// NOI18N
        m_charStack = new Stack();
        m_charCache = new ArrayList();
        m_killEmptyBlocks = false;
        m_ignoreWhiteSpace = false;
        m_breakWhitespace = false;
        m_compressWhitespace = false;
        m_headBlock = null;
    }
    
    public void setStream(File name) throws FileNotFoundException, UnsupportedEncodingException, IOException, TranslationException
    {
        setStream(name, "UTF-8");	// NOI18N
    }
    
    public void setStream(String name, String encoding) throws FileNotFoundException, UnsupportedEncodingException, IOException, TranslationException
    {
        setStream(new File(name), encoding);
    }
    
    
    /**
     * Opens and reads the XML file according to the specified encoding.
     * You may pass <code>null</code> as encoding, then we'll try
     * to auto-sense the encoding.
     */
    private void setStream(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException, IOException, TranslationException
    {
        XMLReader ear = new XMLReader(file.getAbsolutePath(), encoding);
        m_bufferedReader = new BufferedReader( ear );
        _setStream();
    }
    
    /**
     * Provide an interface where stream can be opened elsewhere.
     */
    public void setStream(BufferedReader rdr) throws IOException, TranslationException
    {
        m_bufferedReader = rdr;
        _setStream();
    }
    
    // do the work here
    private void _setStream() throws IOException, TranslationException
    {
        m_pos = -1;
        // make sure XML file is proper
        XMLBlock blk = getNextBlock();
        if (blk == null)
        {
            throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML") +
                    "\n" +                                                      // NOI18N
                    OStrings.getString("XSR_ERROR_UNABLE_INIT_READ_XML"));
        }
        if (blk.getTagName().equals("xml"))	// NOI18N
        {
            String ver = blk.getAttribute("version");	// NOI18N
            //String enc = blk.getAttribute("encoding");	// NOI18N
            if (ver == null || ver.equals(""))	// NOI18N
            {
                // no version declared - assume it's readable
            }
            else if (!ver.equals("1.0"))	// NOI18N
            {
                throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML") +
                        "\n" +                                                  // NOI18N
                        MessageFormat.format(OStrings.getString("XSR_ERROR_UNSUPPORTED_XML_VERSION"),
                        new Object[] {ver}));
            }
            m_headBlock = blk;
        }
        else
        {
            // not a valid XML file
            throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML") +
                    "\n" +                                                      // NOI18N
                    OStrings.getString("XSR_ERROR_NONVALID_XML"));
        }
    }
    
    /**
     * Returns next object in stream - either a tag or a string.
     */
    public XMLBlock getNextBlock() throws TranslationException
    {
        // begin reading text stream
        // if first char a '<' then we've got a tag
        // otherwise it's text
        // strip out any newline and multiple spaces (not valid xml)
        char c = getNextChar();
        
        if (c == 0)
        {
            return null;
        }
        else if (c == '<')
        {
            // be lenient on incorrectly formatted XML - if a space
            //	follows the < then treat it as a literal character
            c = getNextChar();
            pushChar(c);
            if (c != ' ')
            {
                XMLBlock b = getNextTag();
                return b;
            }
        }
        else if( c==']' && end_cdata_flag )
        {
            // very, very special case -- the end of CDATA block
            // is handled completely separately
            XMLBlock b = getNextTagCDATAEnd();
            return b;
        }
        
        pushChar(c);
        XMLBlock blk = getNextText();
        if (blk != null && m_killEmptyBlocks)
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
    
    public void killEmptyBlocks()
    {
        m_killEmptyBlocks = true;
    }
    
    public void breakOnWhitespace(boolean brk)
    {
        m_breakWhitespace = brk;
    }
    
    public void compressWhitespace(boolean tof)
    {
        m_compressWhitespace = tof;
    }
    
    public void setEntityFilter(DefaultEntityFilter filter)
    {
        entityFilter = filter;
    }
    
    //////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////
    // protected routines
    
    /*
     * Pushing chars and marking stream is to allow rewind.
     * Mark is to try to back up to correct for incorrectly formatted document
     */
    private void pushChar(char c)
    {
        m_charStack.push(new Character(c));
    }
    
    /**
     * Caches the current character in case rewind later desired.
     */
    private char getNextCharCache()
    {
        char c = getNextChar();
        m_charCache.add(new Character(c));
        return c;
    }
    
    /**
     * Clears the character cache.
     */
    private void clearCache()
    {
        m_charCache.clear();
    }
    
    /**
     * Pushes cached chars onto the stack, in effect rewinding stream.
     */
    private void revertToCached()
    {
        for (int i=m_charCache.size()-1; i>=0; i--)
            m_charStack.push(m_charCache.get(i));
    }
    
    /**
     * Returns the next character,
     * either from cache (if the cache is non-empty) or from the underlying
     * file reader.
     */
    private char getNextChar()
    {
        if( !m_charStack.empty() )
        {
            Character ch = (Character) m_charStack.pop();
            return ch.charValue();
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
                        if (c == '\n')
                        {
                            // simply drop 13
                            m_pos++;
                        }
                        else
                            c = '\n';
                    }
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
                                if (b != '\n')
                                {
                                    // not a cr/lf pair - make sure not
                                    // another cr and then push char
                                    if (b == 13)
                                        pushChar('\n');
                                }
                                // else - do nothing; swallow the 13
                            }
                            else
                                b = 0;
                        }
                    }
                    else
                        return 0;
                    
                    return b;
                }
                catch (IOException e)
                {
                    StaticUtils.log(OStrings.getString("XSR_ERROR_IOEXCEPTION") + e);
                }
                return 0;
            }
        }
    }
    
    private XMLBlock getNextText() throws TranslationException
    {
        XMLBlock blk = new XMLBlock();
        StringBuffer strBuf = new StringBuffer();
        char c;
        int wsCnt = 0;
        int wsBreak = 0;
        while ((c = getNextChar()) != '<' && c != 0)
        {
            if (c == '&')
            {
                wsCnt = 0;
                if (wsBreak == 1)
                {
                    // ws only tag - push char and bail out
                    pushChar(c);
                    break;
                }
                char c2 = getEscChar();
                if (c2 == 0)
                    strBuf.append('&');
                else
                    strBuf.append(c2);
            }
            else if (c == ' ' || c == '\n' || c == 13 || c == 9)
            {
                // spaces get special handling
                if (m_ignoreWhiteSpace)
                {
                    continue;
                }
                
                if (m_compressWhitespace)
                {
                    if (m_breakWhitespace)
                    {
                        // if we're already in a text segment, break out
                        //	and return ws char to stack
                        if (strBuf.length() > 0)
                        {
                            if (wsBreak == 0)
                            {
                                // in text
                                pushChar(c);
                                break;
                            }
                            // else in a ws sequence
                        }
                        else
                        {
                            wsCnt = 1;
                            strBuf.setLength(0);
                            strBuf.append(" ");	// NOI18N
                            wsBreak = 1;
                        }
                    }
                    else
                    {
                        // simply compress WS
                        if (wsCnt == 0)
                        {
                            strBuf.append(' ');
                            wsCnt = 1;
                        }
                        else
                            continue;
                    }
                }
                else // compressWhitespace == false
                {
                    strBuf.append(c);
                }
            }
            else
            {
                wsCnt = 0;
                if (wsBreak == 1)
                {
                    // ws only tag - push char and bail out
                    pushChar(c);
                    break;
                }
                
                if( c==']' && cdata_flag )
                {
                    // handling ]]> (closure of CDATA expression) in a special way
                    char c1 = getNextChar();
                    char c2 = getNextChar();
                    pushChar(c2);
                    pushChar(c1);
                    if( c1==']' && c2=='>')
                    {
                        cdata_flag = false;
                        end_cdata_flag = true;
                        pushChar(c);
                        break;
                    }
                }
                
                strBuf.append(c);
            }
            
        }
        
        if (c == '<')
            pushChar(c);
        
        blk.setText(strBuf.toString());
        return blk;
    }
    
    private boolean cdata_flag = false;
    private boolean end_cdata_flag = false;
    
    /**
     * Gets the end of CDATA expression - "]]>".
     */
    private XMLBlock getNextTagCDATAEnd()
    {
        end_cdata_flag = false;
        
        XMLBlock blk = new XMLBlock();
        blk.setTypeChar('!');
        blk.setTagName("]]"); // NOI18N
        
        // fetches two chars - ]>
        // one ] is already eaten by getNextBlock()
        getNextChar();
        getNextChar();
        
        return blk;
    }
    
    /**
     * Handles tags defined by &lt;!:
     * <ul>
     * <li>Comment tags &lt;!-- ... --&gt;.
     * <li>CDATA tags &lt;![CDATA[ ... ]]&gt;.
     * </ul>
     * <p>
     * For comments we copy "--" into tagname and '...' into first attribute.
     * For CDATA we eat CDATA prefix, setup a "hack" flag and return.
     *
     * @author Maxym Mykhalchuk
     * @bugfixes http://sourceforge.net/tracker/?func=detail&atid=520347&aid=1109089&group_id=68187
     */
    private XMLBlock getNextTagExclamation() throws TranslationException
    {
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
        
        String name = "";	// NOI18N
        String data = "";	// NOI18N
        int state = state_start;
        int type;
        boolean err = false;
        String msg = "";	// NOI18N
        
        int dashCnt = 0;
        
        char c;
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
                            blk.setTagName("CDATA");	// NOI18N
                            state = state_cdata;
                            
                            break;
                            
                        case type_dash:
                            state = state_commentStart;
                            blk.setComment();
                            break;
                            
                        default:
                            err = true;
                            msg = MessageFormat.format( OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state} );	// NOI18N
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
                        msg = OStrings.getString("XSR_ERROR_CONFUSED");
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
                                //blk.setAttribute(data, "");
                                blk.setText(data);
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
                        case type_opBrac:
                            // the end of CDATA declaration
                            state=state_finish;
                            cdata_flag = true;
                            break;
                            
                        default:
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
                            msg = MessageFormat.format( OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state} );	// NOI18N
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
                            blk.setAttribute(data, "");	// NOI18N
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
            if (err)
            {
                // TODO construct error message with correct state data
                // for now, just throw a parse error
                String str = OStrings.getString("XSR_ERROR_TAG_NAME") +
                        blk.getTagName() + " ";	// NOI18N
                if (blk.isComment())
                    str += OStrings.getString("XSR_ERROR_COMMENT_TAG");
                if (blk.numAttributes() > 0)
                    str += blk.getAttribute(0).name;
                throw new TranslationException(msg + str + "::" + data);	// NOI18N
            }
            else if (state == state_finish)
            {
                break;
            }
        }
        return blk;
    }
    
    private void throwErrorInGetNextTag(XMLBlock blk, String msg) throws TranslationException
    {
        // TODO construct error message with correct state data
        // for now, just throw a parse error
        String data = OStrings.getString("XSR_ERROR_TAG_NAME") +
                blk.getTagName() + " ";	// NOI18N
        if (blk.isEmpty())
            data += OStrings.getString("XSR_ERROR_EMPTY_TAG");
        else if (blk.isClose())
            data += OStrings.getString("XSR_ERROR_CLOSE_TAG");
        if (blk.numAttributes() > 0)
            data += OStrings.getString("XSR_ERROR_LOADED") + 
                    blk.numAttributes() + 
                    OStrings.getString("XSR_ERROR_ATTRIBUTES");
        throw new TranslationException(msg + data);
    }
    
    private XMLBlock getNextTag() throws TranslationException
    {
        char c = getNextChar();
        if (c == 0)
            return null;
        
        // <! encountered - handle it seperately
        if (c == '!')
            return getNextTagExclamation();
        
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
        
        XMLBlock blk = new XMLBlock();
        
        if (c == '?')
        {
            // handle this like a normal tag - let stream class figure
            //  out its importance
            c = getNextChar();
            blk.setTypeChar('?');
        }
        
        int state = state_start;
        String name = "";	// NOI18N
        String attr = "";	// NOI18N
        String val = "";	// NOI18N
        int type;
        int buildValueStartType = 0;
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
                            
                        default:
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
                    }
                    break;
                    
                case state_xmlDeclaration:
                    if (c != '>')
                    {
                        // parse error - got '?' followed by something
                        //  unexpected
                        throwErrorInGetNextTag(blk, 
                                OStrings.getString("XSR_ERROR_FLOATING_QUESTION_MARK"));
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
                    }
                    break;
                    
                case state_transitionFromAttr:
                    switch (type)
                    {
                        case type_quote:
                        case type_apos:
                            // the only valid next character
                            state = state_buildValue;
                            buildValueStartType = type;
                            break;
                            
                        default:
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
                    }
                    break;
                    
                case state_buildValue:
                    switch (type)
                    {
                        case type_quote:
                        case type_apos:
                            // checking if it's the char that opened value
                            if( type==buildValueStartType )
                            {
                                // done recording value
                                // store it and move on
                                blk.setAttribute(attr, val);
                                attr = "";	// NOI18N
                                val = "";	// NOI18N
                                state = state_closeValueQuote;
                            } // else -- an error!
                            else 
                            {
                                // this is a quoted value - be lenient on OK chars
                                val += c;
                            }
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
                            throwErrorInGetNextTag(blk, 
                                    MessageFormat.format( 
                                    OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    new Object[] {""+c, ""+state}));	// NOI18N
                    }
                    break;
                    
                default:
                    StaticUtils.log("INTERNAL ERROR untrapped parse state " + state);	// NOI18N
            }
            
            if (state == state_finish)
            {
                break;
            }
            
            c = getNextChar();
        }
        
        return blk;
    }
    
    private static final int	type_text		= 1;
    private static final int	type_ws			= 2;
    private static final int	type_apos		= 3;
    private static final int	type_quote		= 4;
    private static final int	type_lt			= 5;
    private static final int	type_gt			= 6;
    private static final int	type_amp		= 7;
    
    private static final int	type_equals		= 8;
    private static final int	type_ques		= 9;
    private static final int	type_opBrac		= 10;
    private static final int	type_clBrac		= 11;
    private static final int	type_slash		= 12;
    private static final int	type_backSlash	= 13;
    private static final int	type_dash		= 14;
    
    // used by getNextTag for parsing of tag data
    private int getCharType(char c)
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
    
    /**
     * Converts a single char into valid XML.
     * Output stream must convert stream to UTF-8 when saving to disk.
     */
    public String makeValidXML(char c)
    {
        String res = StaticUtils.makeValidXML(c);
        if( res.length()==1 && entityFilter!=null )
            return entityFilter.convertToEntity(c);
        else
            return res;
    }
    
    /**
     * Converts a stream of plaintext into valid XML.
     * Output stream must convert stream to UTF-8 when saving to disk.
     */
    public String makeValidXML(String plaintext)
    {
        char c;
        StringBuffer out = new StringBuffer();
        for (int i=0; i<plaintext.length(); i++)
        {
            c = plaintext.charAt(i);
            out.append(makeValidXML(c));
        }
        return out.toString();
    }
    
    public ArrayList closeBlock(XMLBlock block) throws TranslationException
    {
        return closeBlock(block, false);
    }
    
    /*
     * Returns the list of blocks between the specified block and
     * its matching close.
     * If the provided block is not an empty tag, or if there are
     * no elements between open and close, a null is returned.
     */
    public ArrayList closeBlock(XMLBlock block, boolean includeTerminationBlock) throws TranslationException
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
        XMLBlock blk;
        while (true)
        {
            blk = getNextBlock();
            if (blk == null)
            {
                // stream ended without finding match
                throw new TranslationException(OStrings.getString("XSR_ERROR_END_OF_STREAM"));
            }
            
            if (blk.isTag() && blk.getTagName().equals(block.getTagName()))
            {
                if (blk.isClose())
                {
                    if (depth == 0)
                    {
                        // found the closing tag
                        if (includeTerminationBlock)
                            lst.add(blk);
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
        
        if( lst.size()==0 )
            return null;
        else
            return lst;
    }
    
    public XMLBlock advanceToTag(String tagname) throws TranslationException
    {
        XMLBlock blk;
        while (true)
        {
            blk = getNextBlock();
            if (blk == null)
            {
                break;
            }
            
            if (blk.isTag() && blk.getTagName().equals(tagname))
            {
                break;
            }
            
        }
        return blk;
    }
    
    private char getEscChar() throws TranslationException
    {
        // look for amp, lt, gt, apos, quot and &#
        clearCache();
        char c = getNextCharCache();
        String val = "";	// NOI18N
        boolean hex = false;
        
        if (c == '#')
        {
            // char code
            c = getNextCharCache();
            if (c == 'x' || c == 'X')
            {
                c = getNextCharCache();
                hex = true;
            }
        }
        else if (c == ' ')
        {
            // an ampersand occured by itself - illegal format, but accept
            //	anyways
            revertToCached();
            return 0;
        }
        
        int ctr=0;
        while (c != ';')
        {
            val += c;
            if (c == 0)
            {
                throw new TranslationException(OStrings.getString("XSR_ERROR_UNTERMINATED_ESCAPE_CHAR"));
            }
            c = getNextCharCache();
            if (ctr++ > 13)
            {
                // appears to be literal char because close for escape
                //	sequence not found
                // be lenient and accept the literal '&'
                // rewind stream
                revertToCached();
                return 0;
            }
        }
        
        
        // didn't detect an error so assume everything is OK
        clearCache();
        
        if (val.equals("amp"))	// NOI18N
            return '&';
        else if (val.equals("lt"))	// NOI18N
            return '<';
        else if (val.equals("gt"))	// NOI18N
            return '>';
        else if (val.equals("apos"))	// NOI18N
            return '\'';
        else if (val.equals("quot"))	// NOI18N
            return '"';
        else if (entityFilter != null)
        {
            return entityFilter.convertToSymbol(val);
        }
        
        // else, binary data
        char b;
        for (int i=0; i<val.length(); i++)
        {
            b = val.charAt(i);
            if (hex)
            {
                c *= 16;
                if (b >= '0' && b <= '9')
                    c += b - '0';
                else if (b >= 'A' && b <= 'F')
                {
                    c += 10;
                    c += b - 'A';
                }
                else if (b >= 'a' && b <= 'f')
                {
                    c += 10;
                    c += b - 'a';
                }
                else
                {
                    throw new TranslationException(MessageFormat.format(OStrings.getString("XSR_ERROR_BAD_BINARY_CHAR"), new Object[] {val}) );
                }
            }
            else
            {
                c *= 10;
                if (b >= '0' && b <= '9')
                    c += b - '0';
                else
                {
                    throw new TranslationException(MessageFormat.format(OStrings.getString("XSR_ERROR_BAD_DECIMAL_CHAR"), new Object[] {val}) );
                }
            }
        }
        
        return c;
    }
    
    public XMLBlock getHeadBlock()
    { 
        return m_headBlock;	
    }
    
    /** Closes the TMX file */
    public void close() throws IOException
    {
        m_bufferedReader.close();
    }
    
    ///////////////////////////////////////////////////////////////
    
    ///////////////////////////////////////////////////////////////
    
    private BufferedReader	m_bufferedReader;
    private String			m_stringStream;
    
    private XMLBlock			m_headBlock;
    
    private int		m_pos;
    private Stack	    m_charStack;
    private ArrayList	m_charCache;
    private boolean	m_killEmptyBlocks;
    private boolean	m_ignoreWhiteSpace;	// don't copy ws to text
    private boolean	m_breakWhitespace;	// put all ws in own block
    private boolean	m_compressWhitespace;	// put ws span in single space
    
}
