/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * A reader for XML stream.
 * 
 * @author Keith Godfrey
 */
public class XMLStreamReader {
    private DefaultEntityFilter entityFilter;

    public XMLStreamReader() {
        m_pos = -1;
        m_stringStream = "";
        m_charStack = new Stack<Integer>();
        m_charCache = new ArrayList<Integer>();
        m_killEmptyBlocks = false;
        m_ignoreWhiteSpace = false;
        m_breakWhitespace = false;
        m_compressWhitespace = false;
        m_headBlock = null;
    }

    public void setStream(File name) throws FileNotFoundException, UnsupportedEncodingException, IOException,
            TranslationException {
        setStream(name, "UTF-8");
    }

    public void setStream(String name, String encoding) throws FileNotFoundException,
            UnsupportedEncodingException, IOException, TranslationException {
        setStream(new File(name), encoding);
    }

    /**
     * Opens and reads the XML file according to the specified encoding. You may
     * pass <code>null</code> as encoding, then we'll try to auto-sense the
     * encoding.
     */
    private void setStream(File file, String encoding) throws FileNotFoundException,
            UnsupportedEncodingException, IOException, TranslationException {
        XMLReader ear = new XMLReader(file.getAbsolutePath(), encoding);
        m_bufferedReader = new BufferedReader(ear);
        _setStream();
    }

    /**
     * Provide an interface where stream can be opened elsewhere.
     */
    public void setStream(BufferedReader rdr) throws IOException, TranslationException {
        m_bufferedReader = rdr;
        _setStream();
    }

    // do the work here
    private void _setStream() throws IOException, TranslationException {
        m_pos = -1;
        // make sure XML file is proper
        XMLBlock blk = getNextBlock();
        if (blk == null) {
            throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML") + "\n"
                    + OStrings.getString("XSR_ERROR_UNABLE_INIT_READ_XML"));
        }
        if (blk.getTagName().equals("xml")) {
            String ver = blk.getAttribute("version");
            // String enc = blk.getAttribute("encoding");
            if (ver == null || ver.equals("")) {
                // no version declared - assume it's readable
            } else if (!ver.equals("1.0")) {
                throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML")
                        + "\n"
                        + StaticUtils.format(OStrings.getString("XSR_ERROR_UNSUPPORTED_XML_VERSION"), ver));
            }
            m_headBlock = blk;
        } else {
            // not a valid XML file
            throw new IOException(OStrings.getString("XSR_ERROR_NONVALID_XML") + "\n"
                    + OStrings.getString("XSR_ERROR_NONVALID_XML"));
        }
    }

    /**
     * Returns next object in stream - either a tag or a string.
     */
    public XMLBlock getNextBlock() throws TranslationException {
        // begin reading text stream
        // if first char a '<' then we've got a tag
        // otherwise it's text
        // strip out any newline and multiple spaces (not valid xml)
        int cp = getNextChar();

        if (cp == 0) {
            return null;
        } else if (cp == '<') {
            // be lenient on incorrectly formatted XML - if a space
            // follows the < then treat it as a literal character
            cp = getNextChar();
            pushChar(cp);
            if (cp != ' ') {
                XMLBlock b = getNextTag();
                return b;
            }
        } else if (cp == ']' && end_cdata_flag) {
            // very, very special case -- the end of CDATA block
            // is handled completely separately
            XMLBlock b = getNextTagCDATAEnd();
            return b;
        }

        pushChar(cp);
        XMLBlock blk = getNextText();
        if (blk != null && m_killEmptyBlocks) {
            String str = blk.getText();
            str = str.trim();
            if (str.isEmpty()) {
                blk = getNextBlock();
            }
        }
        return blk;
    }

    public void killEmptyBlocks() {
        m_killEmptyBlocks = true;
    }

    public void breakOnWhitespace(boolean brk) {
        m_breakWhitespace = brk;
    }

    public void compressWhitespace(boolean tof) {
        m_compressWhitespace = tof;
    }

    public void setEntityFilter(DefaultEntityFilter filter) {
        entityFilter = filter;
    }

    // ////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////
    // protected routines

    /*
     * Pushing chars and marking stream is to allow rewind. Mark is to try to
     * back up to correct for incorrectly formatted document
     */
    private void pushChar(int cp) {
        m_charStack.push(cp);
    }

    /**
     * Caches the current character in case rewind later desired.
     */
    private int getNextCharCache() {
        int c = getNextChar();
        m_charCache.add(c);
        return c;
    }

    /**
     * Clears the character cache.
     */
    private void clearCache() {
        m_charCache.clear();
    }

    /**
     * Pushes cached chars onto the stack, in effect rewinding stream.
     */
    private void revertToCached() {
        for (int i = m_charCache.size() - 1; i >= 0; i--)
            m_charStack.push(m_charCache.get(i));
    }

    /**
     * Returns the next character, either from cache (if the cache is non-empty)
     * or from the underlying file reader.
     */
    private int getNextChar() {
        if (!m_charStack.empty()) {
            Integer ch = m_charStack.pop();
            return ch;
        } else {
            if (m_pos >= 0) {
                // string
                if (m_pos < m_stringStream.length()) {
                    int cp = m_stringStream.codePointAt(m_pos);
                    m_pos += Character.charCount(cp);
                    if (cp == 13) {
                        // convert 13 to 10 - or just omit 13
                        // (XML specs instruct this)
                        cp = m_stringStream.codePointAt(m_pos);
                        if (cp == '\n') {
                            // simply drop 13
                            m_pos += Character.charCount(cp);
                        } else {
                            cp = '\n';
                        }
                    }
                    return cp;
                } else {
                    return 0;
                }
            } else {
                // regular call to read returns int which can't be cast
                // ... so, get the next character in this roundabout fashion
                char[] c = new char[2];
                try {
                    char b;
                    int res = m_bufferedReader.read(c, 0, 1);
                    if (res > 0) {
                        b = c[0];
                        if (b == 13) {
                            // convert 13 10 to 10 and 13 to 10
                            res = m_bufferedReader.read(c, 0, 1);
                            if (res > 0) {
                                b = c[0];
                                if (b != '\n') {
                                    // not a cr/lf pair - make sure not
                                    // another cr and then push char
                                    if (b == 13)
                                        pushChar('\n');
                                }
                                // else - do nothing; swallow the 13
                            } else {
                                b = 0;
                            }
                        }
                    } else {
                        return 0;
                    }
                    return b;
                } catch (IOException e) {
                    Log.logErrorRB("XSR_ERROR_IOEXCEPTION");
                    Log.log(e);
                }
                return 0;
            }
        }
    }

    private XMLBlock getNextText() throws TranslationException {
        XMLBlock blk = new XMLBlock();
        StringBuilder strBuf = new StringBuilder();
        int cp;
        int wsCnt = 0;
        int wsBreak = 0;
        while ((cp = getNextChar()) != '<' && cp != 0) {
            if (cp == '&') {
                wsCnt = 0;
                if (wsBreak == 1) {
                    // ws only tag - push char and bail out
                    pushChar(cp);
                    break;
                }
                int cp2 = getEscChar();
                if (cp2 == 0) {
                    strBuf.append('&');
                } else {
                    strBuf.appendCodePoint(cp2);
                }
            } else if (cp == ' ' || cp == '\n' || cp == 13 || cp == 9) {
                // spaces get special handling
                if (m_ignoreWhiteSpace) {
                    continue;
                }

                if (m_compressWhitespace) {
                    if (m_breakWhitespace) {
                        // if we're already in a text segment, break out
                        // and return ws char to stack
                        if (strBuf.length() > 0) {
                            if (wsBreak == 0) {
                                // in text
                                pushChar(cp);
                                break;
                            }
                            // else in a ws sequence
                        } else {
                            wsCnt = 1;
                            strBuf.setLength(0);
                            strBuf.append(" ");
                            wsBreak = 1;
                        }
                    } else {
                        // simply compress WS
                        if (wsCnt == 0) {
                            strBuf.append(' ');
                            wsCnt = 1;
                        } else
                            continue;
                    }
                } else // compressWhitespace == false
                {
                    strBuf.appendCodePoint(cp);
                }
            } else {
                wsCnt = 0;
                if (wsBreak == 1) {
                    // ws only tag - push char and bail out
                    pushChar(cp);
                    break;
                }

                if (cp == ']' && cdata_flag) {
                    // handling ]]> (closure of CDATA expression) in a special
                    // way
                    int cp1 = getNextChar();
                    int cp2 = getNextChar();
                    pushChar(cp2);
                    pushChar(cp1);
                    if (cp1 == ']' && cp2 == '>') {
                        cdata_flag = false;
                        end_cdata_flag = true;
                        pushChar(cp);
                        break;
                    }
                }

                strBuf.appendCodePoint(cp);
            }

        }

        if (cp == '<') {
            pushChar(cp);
        }

        blk.setText(strBuf.toString());
        return blk;
    }

    private boolean cdata_flag = false;
    private boolean end_cdata_flag = false;

    /**
     * Gets the end of CDATA expression - "]]>".
     */
    private XMLBlock getNextTagCDATAEnd() {
        end_cdata_flag = false;

        XMLBlock blk = new XMLBlock();
        blk.setTypeChar('!');
        blk.setTagName("]]");

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
     * @bugfixes 
     *           http://sourceforge.net/tracker/?func=detail&atid=520347&aid=1109089
     *           &group_id=68187
     */
    private XMLBlock getNextTagExclamation() throws TranslationException {
        final int state_start = 1;
        final int state_name = 2;
        final int state_finish = 3;
        final int state_record = 4;
        final int state_recordSingle = 5;
        final int state_recordDouble = 6;
        final int state_escSingle = 7;
        final int state_escDouble = 8;
        final int state_cdata = 9;
        final int state_commentStart = 10;
        final int state_comment = 11;

        XMLBlock blk = new XMLBlock();
        blk.setTypeChar('!');

        StringBuilder name = new StringBuilder();
        StringBuilder data = new StringBuilder();
        int state = state_start;
        int type;
        boolean err = false;
        String msg = "";

        int dashCnt = 0;

        int cp;
        while ((cp = getNextChar()) != 0) {
            type = getCharType(cp);
            switch (state) {
            case state_start:
                switch (type) {
                case type_ws:
                    // this is OK - do nothing
                    break;

                case type_text:
                    // name - start copying
                    state = state_name;
                    name.appendCodePoint(cp);
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
                    msg = StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                            String.valueOf(Character.toChars(cp)), state);
                }
                break;

            case state_commentStart:
                // verify start of comment string
                if (cp == '-') {
                    state = state_comment;
                } else {
                    err = true;
                    msg = OStrings.getString("XSR_ERROR_CONFUSED");
                }
                break;

            case state_comment:
                // verify comment string - copy until -->
                switch (type) {
                case type_dash:
                    if (dashCnt >= 2)
                        data.appendCodePoint(cp);
                    else
                        dashCnt++;
                    break;

                case type_gt:
                    if (dashCnt >= 2) {
                        // all done
                        // blk.setAttribute(data, "");
                        blk.setText(data.toString());
                        state = state_finish;
                    }
                    break;

                default:
                    if (dashCnt > 0) {
                        // false signal for comment end - return '-'
                        // to stream
                        while (dashCnt > 0) {
                            data.append('-');
                            dashCnt--;
                        }
                    }
                    data.appendCodePoint(cp);
                }
                break;

            case state_cdata:
                // copy until ]]> encountered
                switch (type) {
                case type_opBrac:
                    // the end of CDATA declaration
                    state = state_finish;
                    cdata_flag = true;
                    break;

                default:
                }
                break;

            case state_name:
                switch (type) {
                case type_text:
                    // continue copying name
                    name.appendCodePoint(cp);
                    break;

                case type_ws:
                    // name done - store it and move on
                    blk.setTagName(name.toString());
                    state = state_record;
                    break;

                case type_gt:
                    // no declared data - strange, but allow it
                    state = state_finish;
                    break;

                default:
                    err = true;
                    msg = StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                            String.valueOf(Character.toChars(cp)), state);
                }
                break;

            case state_record:
                switch (type) {
                case type_apos:
                    // continue copying in 'safe' mode
                    state = state_recordSingle;
                    data.appendCodePoint(cp);
                    break;

                case type_quote:
                    // continue copying in 'safe' mode
                    state = state_recordDouble;
                    data.appendCodePoint(cp);
                    break;

                case type_gt:
                    // tag done - record data and close
                    state = state_finish;
                    blk.setAttribute(data.toString(), "");
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case state_recordSingle:
                switch (type) {
                case type_apos:
                    // continue copying normally
                    state = state_record;
                    data.appendCodePoint(cp);
                    break;

                case type_backSlash:
                    // ignore meaning of next char
                    state = state_escSingle;
                    data.appendCodePoint(cp);
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case state_escSingle:
                // whatever happens, just remember character
                data.appendCodePoint(cp);
                state = state_recordSingle;
                break;

            case state_recordDouble:
                switch (type) {
                case type_quote:
                    // continue copying normally
                    state = state_record;
                    data.appendCodePoint(cp);
                    break;

                case type_backSlash:
                    // ignore meaning of next char
                    state = state_escDouble;
                    data.appendCodePoint(cp);
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case state_escDouble:
                // whatever happens, just remember character
                data.appendCodePoint(cp);
                state = state_recordDouble;
                break;

            }
            if (err) {
                // TODO construct error message with correct state data
                // for now, just throw a parse error
                String str = OStrings.getString("XSR_ERROR_TAG_NAME") + blk.getTagName() + " ";
                if (blk.isComment()) {
                    str += OStrings.getString("XSR_ERROR_COMMENT_TAG");
                }
                if (blk.numAttributes() > 0) {
                    str += blk.getAttribute(0).name;
                }
                throw new TranslationException(msg + str + "::" + data);
            } else if (state == state_finish) {
                break;
            }
        }
        return blk;
    }

    private void throwErrorInGetNextTag(XMLBlock blk, String msg) throws TranslationException {
        // TODO construct error message with correct state data
        // for now, just throw a parse error
        String data = OStrings.getString("XSR_ERROR_TAG_NAME") + blk.getTagName() + " ";
        if (blk.isStandalone())
            data += OStrings.getString("XSR_ERROR_EMPTY_TAG");
        else if (blk.isClose())
            data += OStrings.getString("XSR_ERROR_CLOSE_TAG");
        if (blk.numAttributes() > 0)
            data += OStrings.getString("XSR_ERROR_LOADED") + blk.numAttributes()
                    + OStrings.getString("XSR_ERROR_ATTRIBUTES");
        throw new TranslationException(msg + data);
    }

    private XMLBlock getNextTag() throws TranslationException {
        int cp = getNextChar();
        if (cp == 0) {
            return null;
        }

        // <! encountered - handle it seperately
        if (cp == '!') {
            return getNextTagExclamation();
        }

        final int state_start = 1;
        final int state_buildName = 2;
        final int state_setCloseFlag = 3;
        final int state_setStandaloneFlag = 4;
        final int state_attrStandby = 5;
        final int state_buildAttr = 6;
        final int state_transitionFromAttr = 7;
        final int state_buildValue = 8;
        final int state_closeValueQuote = 9;
        final int state_finish = 10;
        final int state_xmlDeclaration = 11;

        XMLBlock blk = new XMLBlock();

        if (cp == '?') {
            // handle this like a normal tag - let stream class figure
            // out its importance
            cp = getNextChar();
            blk.setTypeChar('?');
        }

        int state = state_start;
        StringBuilder name = new StringBuilder();
        StringBuilder attr = new StringBuilder();
        StringBuilder val = new StringBuilder();
        int type;
        int buildValueStartType = 0;
        while (cp != 0) {
            type = getCharType(cp);
            switch (state) {
            case state_start:
                switch (type) {
                case type_slash:
                    blk.setCloseFlag();
                    state = state_setCloseFlag;
                    break;

                case type_text:
                    name.appendCodePoint(cp);
                    state = state_buildName;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_buildName:
                switch (type) {
                case type_dash:
                case type_text:
                    // more name text
                    name.appendCodePoint(cp);
                    break;

                case type_ws:
                    // name is done - move on
                    state = state_attrStandby;
                    blk.setTagName(name.toString());
                    break;

                case type_slash:
                    // name done - standalone tag slash encountered
                    blk.setTagName(name.toString());
                    blk.setStandaloneFlag();
                    state = state_setStandaloneFlag;
                    break;

                case type_gt:
                    // all done
                    blk.setTagName(name.toString());
                    state = state_finish;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_setCloseFlag:
                switch (type) {
                case type_text:
                    // close flag marked not text - start copy
                    name.appendCodePoint(cp);
                    state = state_buildName;
                    break;

                case type_ws:
                    // space after close flag - ignore and continue
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_setStandaloneFlag:
                switch (type) {
                case type_ws:
                    // allow white space to be lenient
                    break;

                case type_gt:
                    // all done with standalone tag
                    state = state_finish;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_attrStandby:
                switch (type) {
                case type_text:
                    // start of attribute name - start recording
                    attr.appendCodePoint(cp);
                    state = state_buildAttr;
                    break;

                case type_ques:
                    // allow question mark so <? ?> tags can
                    // be read by standard parser
                    state = state_xmlDeclaration;
                    break;

                case type_ws:
                    // unexpected space - allow for now because
                    // it isn't ambiguous (be lenient)
                    break;

                case type_slash:
                    blk.setStandaloneFlag();
                    state = state_setStandaloneFlag;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_xmlDeclaration:
                if (cp != '>') {
                    // parse error - got '?' followed by something
                    // unexpected
                    throwErrorInGetNextTag(blk, OStrings.getString("XSR_ERROR_FLOATING_QUESTION_MARK"));
                } else
                    state = state_finish;
                break;

            case state_buildAttr:
                switch (type) {
                case type_dash:
                case type_text:
                    // more name - keep recording
                    attr.appendCodePoint(cp);
                    break;

                case type_equals:
                    // attr done - begin move to value
                    state = state_transitionFromAttr;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_transitionFromAttr:
                switch (type) {
                case type_quote:
                case type_apos:
                    // the only valid next character
                    state = state_buildValue;
                    buildValueStartType = type;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case state_buildValue:
                switch (type) {
                case type_quote:
                case type_apos:
                    // checking if it's the char that opened value
                    if (type == buildValueStartType) {
                        // done recording value
                        // store it and move on
                        blk.setAttribute(attr.toString(), val.toString());
                        attr = new StringBuilder();
                        val = new StringBuilder();
                        state = state_closeValueQuote;
                    } // else -- an error!
                    else {
                        // this is a quoted value - be lenient on OK chars
                        val.appendCodePoint(cp);
                    }
                    break;

                default:
                    // this is a quoted value - be lenient on OK chars
                    val.appendCodePoint(cp);
                    break;
                }
                break;

            case state_closeValueQuote:
                switch (type) {
                case type_text:
                    // new attribute - start recording
                    attr.appendCodePoint(cp);
                    state = state_buildAttr;
                    break;

                case type_ws:
                    // allow this for now
                    break;

                case type_slash:
                    // standalone tag with attributes
                    blk.setStandaloneFlag();
                    state = state_setStandaloneFlag;
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
                    throwErrorInGetNextTag(
                            blk,
                            StaticUtils.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            default:
                Log.log("INTERNAL ERROR untrapped parse state " + state);
            }

            if (state == state_finish) {
                break;
            }

            cp = getNextChar();
        }

        return blk;
    }

    private static final int type_text = 1;
    private static final int type_ws = 2;
    private static final int type_apos = 3;
    private static final int type_quote = 4;
    private static final int type_lt = 5;
    private static final int type_gt = 6;
    private static final int type_amp = 7;

    private static final int type_equals = 8;
    private static final int type_ques = 9;
    private static final int type_opBrac = 10;
    private static final int type_clBrac = 11;
    private static final int type_slash = 12;
    private static final int type_backSlash = 13;
    private static final int type_dash = 14;

    // used by getNextTag for parsing of tag data
    private int getCharType(int cp) {
        int type = type_text;
        switch (cp) {
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
     * Converts a single code point into valid XML. Output stream must convert stream
     * to UTF-8 when saving to disk.
     */
    public String makeValidXML(int cp) {
        String res = StringUtil.escapeXMLChars(cp);
        if (res.codePointCount(0, res.length()) == 1 && entityFilter != null) {
            return entityFilter.convertToEntity(cp);
        } else {
            return res;
        }
    }

    /**
     * Converts a stream of plaintext into valid XML. Output stream must convert
     * stream to UTF-8 when saving to disk.
     */
    public String makeValidXML(String plaintext) {
        StringBuilder out = new StringBuilder();
        for (int cp, i = 0; i < plaintext.length(); i += Character.charCount(cp)) {
            cp = plaintext.codePointAt(i);
            out.append(makeValidXML(cp));
        }
        return out.toString();
    }

    public List<XMLBlock> closeBlock(XMLBlock block) throws TranslationException {
        return closeBlock(block, false);
    }

    /*
     * Returns the list of blocks between the specified block and its matching
     * close. If the provided block is not a standalone tag, or if there are no
     * elements between open and close, a null is returned.
     */
    public List<XMLBlock> closeBlock(XMLBlock block, boolean includeTerminationBlock)
            throws TranslationException {
        List<XMLBlock> lst = new ArrayList<XMLBlock>();

        // sanity check
        if (block == null)
            return lst;

        // if block is a standalone tag, return straight away
        if (block.isStandalone())
            return lst;

        // start search
        int depth = 0;
        XMLBlock blk;
        while (true) {
            blk = getNextBlock();
            if (blk == null) {
                // stream ended without finding match
                throw new TranslationException(OStrings.getString("XSR_ERROR_END_OF_STREAM"));
            }

            if (blk.isTag() && blk.getTagName().equals(block.getTagName())) {
                if (blk.isClose()) {
                    if (depth == 0) {
                        // found the closing tag
                        if (includeTerminationBlock)
                            lst.add(blk);
                        break;
                    } else
                        depth--;
                } else {
                    // imbedded tag of same name - increase stack count
                    depth++;
                }
                lst.add(blk);
            } else {
                lst.add(blk);
            }
        }

        return lst.isEmpty() ? null : lst;
    }

    public XMLBlock advanceToTag(String tagname) throws TranslationException {
        XMLBlock blk;
        while (true) {
            blk = getNextBlock();
            if (blk == null) {
                break;
            }

            if (blk.isTag() && blk.getTagName().equals(tagname)) {
                break;
            }

        }
        return blk;
    }

    private int getEscChar() throws TranslationException {
        // look for amp, lt, gt, apos, quot and &#
        clearCache();
        int cp = getNextCharCache();
        StringBuilder val = new StringBuilder();
        boolean hex = false;

        if (cp == '#') {
            // char code
            cp = getNextCharCache();
            if (cp == 'x' || cp == 'X') {
                cp = getNextCharCache();
                hex = true;
            }
        } else if (cp == ' ') {
            // an ampersand occured by itself - illegal format, but accept
            // anyways
            revertToCached();
            return 0;
        }

        int ctr = 0;
        while (cp != ';') {
            val.appendCodePoint(cp);
            if (cp == 0) {
                throw new TranslationException(OStrings.getString("XSR_ERROR_UNTERMINATED_ESCAPE_CHAR"));
            }
            cp = getNextCharCache();
            if (ctr++ > 13) {
                // appears to be literal char because close for escape
                // sequence not found
                // be lenient and accept the literal '&'
                // rewind stream
                revertToCached();
                return 0;
            }
        }

        // didn't detect an error so assume everything is OK
        clearCache();

        String valString = val.toString();
        if (valString.equals("amp")) {
            return '&';
        } else if (valString.equals("lt")) {
            return '<';
        } else if (valString.equals("gt")) {
            return '>';
        } else if (valString.equals("apos")) {
            return '\'';
        } else if (valString.equals("quot")) {
            return '"';
        } else if (entityFilter != null) {
            return entityFilter.convertToSymbol(val.toString());
        }

        // else, binary data
        if (hex) {
            try {
                cp = Integer.valueOf(valString, 16);
            } catch (NumberFormatException ex) {
                throw new TranslationException(StaticUtils.format(
                            OStrings.getString("XSR_ERROR_BAD_BINARY_CHAR"), val), ex);
            }
        } else {
            try {
                cp = Integer.valueOf(valString, 10);
            } catch (NumberFormatException ex) {
                throw new TranslationException(StaticUtils.format(
                        OStrings.getString("XSR_ERROR_BAD_DECIMAL_CHAR"), val), ex);
            }
        }

        return cp;
    }

    public XMLBlock getHeadBlock() {
        return m_headBlock;
    }

    /** Closes the TMX file */
    public void close() throws IOException {
        if (m_bufferedReader != null) {
            m_bufferedReader.close();
        }
    }

    // /////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////

    private BufferedReader m_bufferedReader;
    private String m_stringStream;

    private XMLBlock m_headBlock;

    private int m_pos;
    private Stack<Integer> m_charStack;
    private List<Integer> m_charCache;
    private boolean m_killEmptyBlocks;
    private boolean m_ignoreWhiteSpace; // don't copy ws to text
    private boolean m_breakWhitespace; // put all ws in own block
    private boolean m_compressWhitespace; // put ws span in single space

}
