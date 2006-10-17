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
     * Two tokens are thought equal if their text is equal.
     */
    public boolean equals(Object obj)
    {
        if( this==obj )
            return true;
        if( obj instanceof Token )
        {
            Token tok = (Token)obj;
            if( text==null )
                return tok.text==null;
            else
                return text.equals(tok.text);
        }
        return false;
    }

    /**
     * -1 if text is null,
     * text's hashcode otherwise.
     */
    public int hashCode()
    {
        if( text==null )
            return -1;
        return text.hashCode();
    }


    private static Pattern AMP = Pattern.compile("\\&");                        // NOI18N

    private String stripAmpersand(String s)
    {
        return AMP.matcher(s).replaceAll("");                                   // NOI18N
    }

    /**
     * Creates a new token.
     * @param _text the text of the token
     * @param _offset the starting position of this token in parent string
     */
    public Token(String _text, int _offset)
    {
        length = _text.length();
        text = stripAmpersand(_text);
        offset = _offset;
    }

    private int length;
    /** Text without '&' */
    private String text;
    private int offset;

    /** Returns the length of a token. */
    public int getLength()
    {
        return length;
    }

    /** Returns token's offset in a source string. */
    public int getOffset()
    {
        return offset;
    }

    public String toString()
    {
        return text+"@"+offset;                                                 // NOI18N
    }

}

