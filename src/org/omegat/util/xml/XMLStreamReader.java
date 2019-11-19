/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * A reader for XML stream.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class XMLStreamReader implements Closeable {
    private DefaultEntityFilter entityFilter;

    public XMLStreamReader() {
        mPos = -1;
        mStringStream = "";
        mCharStack = new Stack<Integer>();
        mCharCache = new ArrayList<Integer>();
        mKillEmptyBlocks = false;
        mIgnoreWhiteSpace = false;
        mBreakWhitespace = false;
        mCompressWhitespace = false;
        mHeadBlock = null;
    }

    public final void setStream(File name)
            throws FileNotFoundException, UnsupportedEncodingException, IOException, TranslationException {
        setStream(name, "UTF-8");
    }

    public final void setStream(String name, String encoding)
            throws FileNotFoundException, UnsupportedEncodingException, IOException, TranslationException {
        setStream(new File(name), encoding);
    }

    /**
     * Opens and reads the XML file according to the specified encoding. You may
     * pass <code>null</code> as encoding, then we'll try to auto-sense the
     * encoding.
     */
    private void setStream(File file, String encoding) throws FileNotFoundException,
            UnsupportedEncodingException, IOException, TranslationException {
        mReader = new XMLReader(file.getAbsolutePath(), encoding);
        setStreamImpl();
    }

    /**
     * Provide an interface where stream can be opened elsewhere.
     */
    public void setStream(InputStream stream) throws IOException, TranslationException {
        setStream(stream, "UTF-8");
    }

    /**
     * Provide an interface where stream can be opened elsewhere.
     */
    public void setStream(InputStream stream, String encoding) throws IOException, TranslationException {
        mReader = new XMLReader(stream, encoding);
        setStreamImpl();
    }

    // do the work here
    private void setStreamImpl() throws IOException, TranslationException {
        mPos = -1;
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
                        + StringUtil.format(OStrings.getString("XSR_ERROR_UNSUPPORTED_XML_VERSION"), ver));
            }
            mHeadBlock = blk;
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
        } else if (cp == ']' && endCdataFlag) {
            // very, very special case -- the end of CDATA block
            // is handled completely separately
            XMLBlock b = getNextTagCDATAEnd();
            return b;
        }

        pushChar(cp);
        XMLBlock blk = getNextText();
        if (blk != null && mKillEmptyBlocks) {
            String str = blk.getText();
            str = str.trim();
            if (str.isEmpty()) {
                blk = getNextBlock();
            }
        }
        return blk;
    }

    public final void killEmptyBlocks() {
        mKillEmptyBlocks = true;
    }

    public final void breakOnWhitespace(boolean brk) {
        mBreakWhitespace = brk;
    }

    public final void compressWhitespace(boolean tof) {
        mCompressWhitespace = tof;
    }

    public final void setEntityFilter(DefaultEntityFilter filter) {
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
        mCharStack.push(cp);
    }

    /**
     * Caches the current character in case rewind later desired.
     */
    private int getNextCharCache() {
        int c = getNextChar();
        mCharCache.add(c);
        return c;
    }

    /**
     * Clears the character cache.
     */
    private void clearCache() {
        mCharCache.clear();
    }

    /**
     * Pushes cached chars onto the stack, in effect rewinding stream.
     */
    private void revertToCached() {
        for (int i = mCharCache.size() - 1; i >= 0; i--) {
            mCharStack.push(mCharCache.get(i));
        }
    }

    /**
     * Returns the next character, either from cache (if the cache is non-empty)
     * or from the underlying file reader.
     */
    private int getNextChar() {
        if (!mCharStack.empty()) {
            Integer ch = mCharStack.pop();
            return ch;
        } else {
            if (mPos >= 0) {
                // string
                if (mPos < mStringStream.length()) {
                    int cp = mStringStream.codePointAt(mPos);
                    mPos += Character.charCount(cp);
                    if (cp == 13) {
                        // convert 13 to 10 - or just omit 13
                        // (XML specs instruct this)
                        cp = mStringStream.codePointAt(mPos);
                        if (cp == '\n') {
                            // simply drop 13
                            mPos += Character.charCount(cp);
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
                    int res = mReader.read(c, 0, 1);
                    if (res > 0) {
                        b = c[0];
                        if (b == 13) {
                            // convert 13 10 to 10 and 13 to 10
                            res = mReader.read(c, 0, 1);
                            if (res > 0) {
                                b = c[0];
                                if (b != '\n') {
                                    // not a cr/lf pair - make sure not
                                    // another cr and then push char
                                    if (b == 13) {
                                        pushChar('\n');
                                    }
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
                if (mIgnoreWhiteSpace) {
                    continue;
                }

                if (mCompressWhitespace) {
                    if (mBreakWhitespace) {
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
                        } else {
                            continue;
                        }
                    }
                } else { // compressWhitespace == false
                    strBuf.appendCodePoint(cp);
                }
            } else {
                wsCnt = 0;
                if (wsBreak == 1) {
                    // ws only tag - push char and bail out
                    pushChar(cp);
                    break;
                }

                if (cp == ']' && cdataFlag) {
                    // handling ]]> (closure of CDATA expression) in a special
                    // way
                    int cp1 = getNextChar();
                    int cp2 = getNextChar();
                    pushChar(cp2);
                    pushChar(cp1);
                    if (cp1 == ']' && cp2 == '>') {
                        cdataFlag = false;
                        endCdataFlag = true;
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

    private boolean cdataFlag = false;
    private boolean endCdataFlag = false;

    /**
     * Gets the end of CDATA expression - "]]>".
     */
    private XMLBlock getNextTagCDATAEnd() {
        endCdataFlag = false;

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
     * @see <a href=
     *      "http://sourceforge.net/tracker/?func=detail&atid=520347&aid=1109089&group_id=68187">
     *      bug fixes</a>
     */
    private XMLBlock getNextTagExclamation() throws TranslationException {
        final int stateStart = 1;
        final int stateName = 2;
        final int stateFinish = 3;
        final int stateRecord = 4;
        final int stateRecordSingle = 5;
        final int stateRecordDouble = 6;
        final int stateEscSingle = 7;
        final int stateEscDouble = 8;
        final int stateCdata = 9;
        final int stateCommentStart = 10;
        final int stateComment = 11;

        XMLBlock blk = new XMLBlock();
        blk.setTypeChar('!');

        StringBuilder name = new StringBuilder();
        StringBuilder data = new StringBuilder();
        int state = stateStart;
        int type;
        boolean err = false;
        String msg = "";

        int dashCnt = 0;

        int cp;
        while ((cp = getNextChar()) != 0) {
            type = getCharType(cp);
            switch (state) {
            case stateStart:
                switch (type) {
                case TYPE_WS:
                    // this is OK - do nothing
                    break;

                case TYPE_TEXT:
                    // name - start copying
                    state = stateName;
                    name.appendCodePoint(cp);
                    break;

                case TYPE_OPBRAC:
                    blk.setTagName("CDATA");
                    state = stateCdata;

                    break;

                case TYPE_DASH:
                    state = stateCommentStart;
                    blk.setComment();
                    break;

                default:
                    err = true;
                    msg = StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                            String.valueOf(Character.toChars(cp)), state);
                }
                break;

            case stateCommentStart:
                // verify start of comment string
                if (cp == '-') {
                    state = stateComment;
                } else {
                    err = true;
                    msg = OStrings.getString("XSR_ERROR_CONFUSED");
                }
                break;

            case stateComment:
                // verify comment string - copy until -->
                switch (type) {
                case TYPE_DASH:
                    if (dashCnt >= 2) {
                        data.appendCodePoint(cp);
                    } else {
                        dashCnt++;
                    }
                    break;

                case TYPE_GT:
                    if (dashCnt >= 2) {
                        // all done
                        // blk.setAttribute(data, "");
                        blk.setText(data.toString());
                        state = stateFinish;
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

            case stateCdata:
                // copy until ]]> encountered
                switch (type) {
                case TYPE_OPBRAC:
                    // the end of CDATA declaration
                    state = stateFinish;
                    cdataFlag = true;
                    break;

                default:
                }
                break;

            case stateName:
                switch (type) {
                case TYPE_TEXT:
                    // continue copying name
                    name.appendCodePoint(cp);
                    break;

                case TYPE_WS:
                    // name done - store it and move on
                    blk.setTagName(name.toString());
                    state = stateRecord;
                    break;

                case TYPE_GT:
                    // no declared data - strange, but allow it
                    state = stateFinish;
                    break;

                default:
                    err = true;
                    msg = StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                            String.valueOf(Character.toChars(cp)), state);
                }
                break;

            case stateRecord:
                switch (type) {
                case TYPE_APOS:
                    // continue copying in 'safe' mode
                    state = stateRecordSingle;
                    data.appendCodePoint(cp);
                    break;

                case TYPE_QUOTE:
                    // continue copying in 'safe' mode
                    state = stateRecordDouble;
                    data.appendCodePoint(cp);
                    break;

                case TYPE_GT:
                    // tag done - record data and close
                    state = stateFinish;
                    blk.setAttribute(data.toString(), "");
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case stateRecordSingle:
                switch (type) {
                case TYPE_APOS:
                    // continue copying normally
                    state = stateRecord;
                    data.appendCodePoint(cp);
                    break;

                case TYPE_BACKSLASH:
                    // ignore meaning of next char
                    state = stateEscSingle;
                    data.appendCodePoint(cp);
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case stateEscSingle:
                // whatever happens, just remember character
                data.appendCodePoint(cp);
                state = stateRecordSingle;
                break;

            case stateRecordDouble:
                switch (type) {
                case TYPE_QUOTE:
                    // continue copying normally
                    state = stateRecord;
                    data.appendCodePoint(cp);
                    break;

                case TYPE_BACKSLASH:
                    // ignore meaning of next char
                    state = stateEscDouble;
                    data.appendCodePoint(cp);
                    break;

                default:
                    data.appendCodePoint(cp);
                }
                break;

            case stateEscDouble:
                // whatever happens, just remember character
                data.appendCodePoint(cp);
                state = stateRecordDouble;
                break;
            default:
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
            } else if (state == stateFinish) {
                break;
            }
        }
        return blk;
    }

    private void throwErrorInGetNextTag(XMLBlock blk, String msg) throws TranslationException {
        // TODO construct error message with correct state data
        // for now, just throw a parse error
        String data = OStrings.getString("XSR_ERROR_TAG_NAME") + blk.getTagName() + " ";
        if (blk.isStandalone()) {
            data += OStrings.getString("XSR_ERROR_EMPTY_TAG");
        } else if (blk.isClose()) {
            data += OStrings.getString("XSR_ERROR_CLOSE_TAG");
        }
        if (blk.numAttributes() > 0) {
            data += OStrings.getString("XSR_ERROR_LOADED") + blk.numAttributes()
                    + OStrings.getString("XSR_ERROR_ATTRIBUTES");
        }
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

        final int stateStart = 1;
        final int stateBuildName = 2;
        final int stateSetCloseFlag = 3;
        final int stateSetStandaloneFlag = 4;
        final int stateAttrStandby = 5;
        final int stateBuildAttr = 6;
        final int stateTransitionFromAttr = 7;
        final int stateBuildValue = 8;
        final int stateCloseValueQuote = 9;
        final int stateFinish = 10;
        final int stateXmlDeclaration = 11;

        XMLBlock blk = new XMLBlock();

        if (cp == '?') {
            // handle this like a normal tag - let stream class figure
            // out its importance
            cp = getNextChar();
            blk.setTypeChar('?');
        }

        int state = stateStart;
        StringBuilder name = new StringBuilder();
        StringBuilder attr = new StringBuilder();
        StringBuilder val = new StringBuilder();
        int type;
        int buildValueStartType = 0;
        while (cp != 0) {
            type = getCharType(cp);
            switch (state) {
            case stateStart:
                switch (type) {
                case TYPE_SLASH:
                    blk.setCloseFlag();
                    state = stateSetCloseFlag;
                    break;

                case TYPE_TEXT:
                    name.appendCodePoint(cp);
                    state = stateBuildName;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateBuildName:
                switch (type) {
                case TYPE_DASH:
                case TYPE_TEXT:
                    // more name text
                    name.appendCodePoint(cp);
                    break;

                case TYPE_WS:
                    // name is done - move on
                    state = stateAttrStandby;
                    blk.setTagName(name.toString());
                    break;

                case TYPE_SLASH:
                    // name done - standalone tag slash encountered
                    blk.setTagName(name.toString());
                    blk.setStandaloneFlag();
                    state = stateSetStandaloneFlag;
                    break;

                case TYPE_GT:
                    // all done
                    blk.setTagName(name.toString());
                    state = stateFinish;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateSetCloseFlag:
                switch (type) {
                case TYPE_TEXT:
                    // close flag marked not text - start copy
                    name.appendCodePoint(cp);
                    state = stateBuildName;
                    break;

                case TYPE_WS:
                    // space after close flag - ignore and continue
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateSetStandaloneFlag:
                switch (type) {
                case TYPE_WS:
                    // allow white space to be lenient
                    break;

                case TYPE_GT:
                    // all done with standalone tag
                    state = stateFinish;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateAttrStandby:
                switch (type) {
                case TYPE_TEXT:
                    // start of attribute name - start recording
                    attr.appendCodePoint(cp);
                    state = stateBuildAttr;
                    break;

                case TYPE_QUES:
                    // allow question mark so <? ?> tags can
                    // be read by standard parser
                    state = stateXmlDeclaration;
                    break;

                case TYPE_WS:
                    // unexpected space - allow for now because
                    // it isn't ambiguous (be lenient)
                    break;

                case TYPE_SLASH:
                    blk.setStandaloneFlag();
                    state = stateSetStandaloneFlag;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateXmlDeclaration:
                if (cp != '>') {
                    // parse error - got '?' followed by something
                    // unexpected
                    throwErrorInGetNextTag(blk, OStrings.getString("XSR_ERROR_FLOATING_QUESTION_MARK"));
                } else {
                    state = stateFinish;
                }
                break;

            case stateBuildAttr:
                switch (type) {
                case TYPE_DASH:
                case TYPE_TEXT:
                    // more name - keep recording
                    attr.appendCodePoint(cp);
                    break;

                case TYPE_EQUALS:
                    // attr done - begin move to value
                    state = stateTransitionFromAttr;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateTransitionFromAttr:
                switch (type) {
                case TYPE_QUOTE:
                case TYPE_APOS:
                    // the only valid next character
                    state = stateBuildValue;
                    buildValueStartType = type;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            case stateBuildValue:
                switch (type) {
                case TYPE_QUOTE:
                case TYPE_APOS:
                    // checking if it's the char that opened value
                    if (type == buildValueStartType) {
                        // done recording value
                        // store it and move on
                        blk.setAttribute(attr.toString(), val.toString());
                        attr = new StringBuilder();
                        val = new StringBuilder();
                        state = stateCloseValueQuote;
                    // else -- an error!
                    } else {
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

            case stateCloseValueQuote:
                switch (type) {
                case TYPE_TEXT:
                    // new attribute - start recording
                    attr.appendCodePoint(cp);
                    state = stateBuildAttr;
                    break;

                case TYPE_WS:
                    // allow this for now
                    break;

                case TYPE_SLASH:
                    // standalone tag with attributes
                    blk.setStandaloneFlag();
                    state = stateSetStandaloneFlag;
                    break;

                case TYPE_GT:
                    // finished
                    state = stateFinish;
                    break;

                case TYPE_QUES:
                    // allow question mark so <? ?> tags can
                    // be read by standard parser
                    state = stateXmlDeclaration;
                    break;

                default:
                    throwErrorInGetNextTag(
                            blk,
                            StringUtil.format(OStrings.getString("XSR_ERROR_UNEXPECTED_CHAR"),
                                    String.valueOf(Character.toChars(cp)), state));
                }
                break;

            default:
                Log.log("INTERNAL ERROR untrapped parse state " + state);
            }

            if (state == stateFinish) {
                break;
            }

            cp = getNextChar();
        }

        return blk;
    }

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_WS = 2;
    private static final int TYPE_APOS = 3;
    private static final int TYPE_QUOTE = 4;
    private static final int TYPE_LT = 5;
    private static final int TYPE_GT = 6;
    private static final int TYPE_AMP = 7;

    private static final int TYPE_EQUALS = 8;
    private static final int TYPE_QUES = 9;
    private static final int TYPE_OPBRAC = 10;
    private static final int TYPE_CLBRAC = 11;
    private static final int TYPE_SLASH = 12;
    private static final int TYPE_BACKSLASH = 13;
    private static final int TYPE_DASH = 14;

    // used by getNextTag for parsing of tag data
    private int getCharType(int cp) {
        int type = TYPE_TEXT;
        switch (cp) {
        case 0x20:
        case 0x0a:
        case 0x0d:
        case 0x09:
            type = TYPE_WS;
            break;

        case '"':
            type = TYPE_QUOTE;
            break;

        case '\'':
            type = TYPE_APOS;
            break;

        case '&':
            type = TYPE_AMP;
            break;

        case '<':
            type = TYPE_LT;
            break;

        case '>':
            type = TYPE_GT;
            break;

        case '?':
            type = TYPE_QUES;
            break;

        case '/':
            type = TYPE_SLASH;
            break;

        case '=':
            type = TYPE_EQUALS;
            break;

        case '[':
            type = TYPE_OPBRAC;
            break;

        case ']':
            type = TYPE_CLBRAC;
            break;

        case '-':
            type = TYPE_DASH;
            break;

        case '\\':
            type = TYPE_BACKSLASH;
            break;
        default:
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

    public final List<XMLBlock> closeBlock(XMLBlock block) throws TranslationException {
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
        if (block == null) {
            return lst;
        }
        // if block is a standalone tag, return straight away
        if (block.isStandalone()) {
            return lst;
        }
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
                        if (includeTerminationBlock) {
                            lst.add(blk);
                        }
                        break;
                    } else {
                        depth--;
                    }
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

    public final XMLBlock advanceToTag(String tagname) throws TranslationException {
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
                throw new TranslationException(StringUtil.format(
                            OStrings.getString("XSR_ERROR_BAD_BINARY_CHAR"), val), ex);
            }
            if (!StringUtil.isValidXMLChar(cp)) {
                throw new TranslationException(StringUtil.format(
                        OStrings.getString("XSR_ERROR_BAD_BINARY_CHAR"), val));
            }
        } else {
            try {
                cp = Integer.valueOf(valString, 10);
            } catch (NumberFormatException ex) {
                throw new TranslationException(StringUtil.format(
                        OStrings.getString("XSR_ERROR_BAD_DECIMAL_CHAR"), val), ex);
            }
            if (!StringUtil.isValidXMLChar(cp)) {
                throw new TranslationException(StringUtil.format(
                        OStrings.getString("XSR_ERROR_BAD_DECIMAL_CHAR"), val));
            }
        }

        return cp;
    }

    public final XMLBlock getHeadBlock() {
        return mHeadBlock;
    }

    /** Closes the TMX file */
    @Override
    public void close() throws IOException {
        if (mReader != null) {
            mReader.close();
        }
    }

    // /////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////

    private XMLReader mReader;
    private String mStringStream;

    private XMLBlock mHeadBlock;

    private int mPos;
    private Stack<Integer> mCharStack;
    private List<Integer> mCharCache;
    private boolean mKillEmptyBlocks;
    private boolean mIgnoreWhiteSpace; // don't copy ws to text
    private boolean mBreakWhitespace; // put all ws in own block
    private boolean mCompressWhitespace; // put ws span in single space

}
