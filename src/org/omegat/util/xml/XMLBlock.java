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

import java.util.ArrayList;
import java.util.List;

import org.omegat.util.OConsts;

/*
 * XML Block is either a tag (with optional attributes), or a string
 *
 * @author Keith Godfrey
 */
public class XMLBlock {
    public XMLBlock() {
        reset();
    }

    private void reset() {
        mText = "";
        mIsClose = false;
        mIsStandalone = false;
        mIsComment = false;
        mIsTag = false;
        mTypeChar = 0;
        mHasText = false;
        mShortcut = "";

        if (mAttrList != null) {
            mAttrList.clear();
        }
    }

    // ////////////////////////////////////////////////
    // initialization methods

    public void setAttribute(String attribute, String value) {
        XMLAttribute attr = new XMLAttribute(attribute, value);
        setAttribute(attr);
    }

    private void setAttribute(XMLAttribute attr) {
        if (mAttrList == null) {
            mAttrList = new ArrayList<XMLAttribute>(8);
        }
        // assume that this attribute doesn't exist already
        mAttrList.add(attr);
    }

    public void setText(String text) {
        setTag(false);
        mText = text;

        // block considered text if it has length=1 and includes non ws
        mHasText = false;
        if (text.codePointCount(0, text.length()) == 1) {
            int cp = text.codePointAt(0);
            if (cp != 9 && cp != 10 && cp != 13 && cp != ' ') {
                mHasText = true;
            }
        } else {
            mHasText = true;
        }
    }

    public void setTypeChar(char c) {
        mTypeChar = c;
    }

    public void setShortcut(String shortcut) {
        mShortcut = shortcut;
    }

    public String getShortcut() {
        if (mShortcut != null && !mShortcut.equals("")) {
            if (mIsClose) {
                return "/" + mShortcut;
            } else if (mIsComment) {
                return OConsts.XB_COMMENT_SHORTCUT;
            }
        }
        return mShortcut;
    }

    /** Sets that this block is a closing tag. */
    public void setCloseFlag() {
        mIsClose = true;
        mIsStandalone = false;
    }

    /** Sets that this block is a stand-alone tag. */
    public void setStandaloneFlag() {
        mIsStandalone = true;
        mIsClose = false;
    }

    public void setComment() {
        mIsTag = true;
        setTypeChar('!');
        mIsComment = true;
        mIsClose = false;
        mIsStandalone = false;
    }

    public void setTagName(String name) {
        setTag(true);
        mText = name;
    }

    private void setTag(boolean isTag) {
        mIsTag = isTag;
    }

    // ///////////////////////////////////////////////
    // data retrieval functions

    /** Whether this block is a chunk of text (not a tag). */
    public boolean hasText() {
        return mHasText;
    }

    /** Whether this block is a tag. */
    public boolean isTag() {
        return mIsTag;
    }

    /** Whether this block is a standalone tag. */
    public boolean isStandalone() {
        return mIsStandalone;
    }

    /** Whether this is a closing tag. */
    public boolean isClose() {
        return mIsClose;
    }

    /** Whether this block is a comment. */
    public boolean isComment() {
        return mIsComment;
    }

    /**
     * Returns the block as text - either raw text if not a tag, or the tag and
     * attributes in text form if it is
     */
    public String getText() {
        if (mTypeChar == '?') {
            // write < + [/ +] tagname + attributes + [/ +] >
            StringBuilder tag = new StringBuilder("<?").append(mText);
            if (mAttrList != null) {
                for (XMLAttribute attr : mAttrList) {
                    // add attribute/value pair
                    tag.append(' ').append(attr.name).append("=\"").append(attr.value).append('"');
                }
            }

            tag.append("?>");
            return tag.toString();
        } else if (mTypeChar == '!') {
            StringBuilder tag = new StringBuilder("<!");
            if (mText.equals("CDATA")) {
                tag.append('[').append(mText).append('[');
            } else if (mText.equals("]]")) {
                tag.append("]]>");
            } else if (mIsComment) {
                tag.append("-- ").append(mText).append(" -->");
            } else {
                tag.append(mText).append(' ');
                if (mAttrList != null) {
                    if (!mAttrList.isEmpty()) {
                        tag.append(mAttrList.get(0).name);
                    }
                }
                tag.append('>');
            }
            return tag.toString();
        } else if (isTag()) {
            // write < + [/ +] tagname + attributes + [/ +] >
            StringBuilder tag = new StringBuilder("<");
            if (mIsClose) {
                tag.append('/');
            }
            tag.append(mText);
            if (mAttrList != null) {
                for (XMLAttribute attr : mAttrList) {
                    // add attribute/value pair
                    tag.append(' ').append(attr.name).append("=\"").append(attr.value).append('"');
                }
            }

            if (mIsStandalone) {
                tag.append(" /");
            }
            tag.append('>');

            return tag.toString();
        } else {
            return mText;
        }
    }

    public String getTagName() {
        if (isTag()) {
            return mText;
        } else {
            return "";
        }
    }

    public int numAttributes() {
        if (mAttrList == null) {
            return 0;
        } else {
            return mAttrList.size();
        }
    }

    public XMLAttribute getAttribute(int n) {
        if (n < 0 || !isTag() || mAttrList == null || n > mAttrList.size()) {
            return null;
        } else {
            return mAttrList.get(n);
        }
    }

    public String getAttribute(String name) {
        if (!isTag() || mAttrList == null) {
            return null;
        }
        XMLAttribute attr = null;

        for (int i = 0; i < mAttrList.size(); i++) {
            attr = mAttrList.get(i);
            if (attr.name.equals(name)) {
                break;
            } else {
                attr = null;
            }
        }
        if (attr == null) {
            return null;
        } else {
            return attr.value;
        }
    }

    private String mText; // tagname if tag; text if not
    private String mShortcut; // user display for tag
    private boolean mIsClose;
    private boolean mIsComment;
    private boolean mIsStandalone;
    private boolean mIsTag;
    private boolean mHasText;
    private char mTypeChar;
    private List<XMLAttribute> mAttrList;

    /** Returns a string representation for debugging purposes mainly. */
    public String toString() {
        return getText();
    }

    /** holds the shortcut number of this tag. */
    private int shortcutNumber;

    /** What's the shortcut number of this tag. */
    public int getShortcutNumber() {
        return this.shortcutNumber;
    }

    /** Sets the shortcut number of this tag. */
    public void setShortcutNumber(int shortcutNumber) {
        this.shortcutNumber = shortcutNumber;
    }
}
