/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.xtagqxp;

import org.omegat.filters3.Element;
import org.omegat.util.StaticUtils;

/**
 * A Xtag in a CopyFlow Gold for QuarkXPress source text.
 *
 * @author Didier Briel
 */
public class Xtag implements Element {

    public Xtag(String tag, int index) {
        this.tag = tag;
        this.shortcut = makeShortcut(tag);
        this.index = index;
    }

    /**
     * Makes a shortcut from an Xtag. If the tag contains no letter, uses 'x'
     * for the shortcut.
     *
     * @param tag
     *            The full Xtag
     * @return The shortcut
     */
    private String makeShortcut(String tag) {
        int cp = 0;

        for (int i = 0; i < tag.length(); i += Character.charCount(cp)) {
            cp = tag.codePointAt(i);
            if (Character.isLetter(cp)) {
                cp = Character.toLowerCase(cp);
                return String.valueOf(Character.toChars(cp));
            }
        }
        if (cp == '<') {
            return "<";
        } else if (cp == '>') {
            return ">";
        } else {
            return "x";
        }
    }

    private String tag;

    /** Returns this tag. */
    public String getTag() {
        return tag;
    }

    private String shortcut;

    /** Returns the short form of this tag, most often -- the first letter. */
    public String getShortcut() {
        if (shortcut != null) {
            return shortcut;
        } else {
            return String.valueOf(Character.toChars(getTag().codePointAt(0)));
        }
    }

    public String toSafeCalcShortcut() {
        return StaticUtils.TAG_REPLACEMENT_CHAR + getShortcut().replace('<', '_').replace('>', '_')
                + StaticUtils.TAG_REPLACEMENT_CHAR;
    }

    private int index;

    /** Returns the index of this tag in the entry. */
    public int getIndex() {
        return index;
    }

    /**
     * Returns shortcut string representation of the element. If the shortcut is
     * &lt; or &gt;, return the character rather than a tag E.g. for
     * &lt;strong&gt; tag should return &lt;s3&gt;.
     */
    public String toShortcut() {
        StringBuilder buf = new StringBuilder();

        if (getShortcut().equals("<")) {
            return "<";
        } else if (getShortcut().equals(">")) {
            return ">";
        } else {
            buf.append("<");
        }
        buf.append(getShortcut());
        buf.append(getIndex());
        // All Xtags are single tags
        buf.append("/");
        buf.append(">");

        return buf.toString();
    }

    /**
     * Returns the tag in its original form as it was in the original document.
     * E.g. for &lt;strong&gt; tag should return &lt;strong&gt;.
     */
    public String toOriginal() {
        return "<" + getTag() + ">";
    }

    /**
     * Not really implemented
     *
     * @return an empty string
     */
    public String toTMX() {
        return "";
    }
}
