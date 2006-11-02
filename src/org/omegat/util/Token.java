/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.util;

import java.util.regex.Pattern;

/**
 * Offset marks the display offset of character - this might be
 * different than the characters position in the char array
 * due existence of multi-char characters.
 * <p>
 * Since 1.6 strips '&' in given token text.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class Token
{
    /**
     * Two tokens are thought equal if their hash code is equal.
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public boolean equals(Object other) {
        return (   (this == other)
                || (   (other instanceof Token)
                    && (hash == ((Token)other).hash)));
    }

    /**
     * -1 if text is null,
     * text's hashcode otherwise.
     */
    private int hash;
    public int hashCode() {
        return hash;
    }

    private static Pattern AMP = Pattern.compile("\\&");                        // NOI18N

    private final String stripAmpersand(String s)
    {
        return AMP.matcher(s).replaceAll("");                                   // NOI18N
    }

public String text;
    /**
     * Creates a new token.
     * @param _text the text of the token
     * @param _offset the starting position of this token in parent string
     */
    public Token(String _text, int _offset)
    {
this.text = _text;
        length = _text.length();
        hash = (_text == null) ? -1 : stripAmpersand(_text).hashCode();
        offset = _offset;
    }

    private int length;
    private int offset;

    /** Returns the length of a token. */
    public final int getLength()
    {
        return length;
    }

    /** Returns token's offset in a source string. */
    public final int getOffset()
    {
        return offset;
    }

    public final String toString()
    {
        return hash+"@"+offset;                                                 // NOI18N
    }

}

