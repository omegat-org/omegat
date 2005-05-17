/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
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

package org.omegat.util;

/**
 * offset marks the display offset of character - this might be
 * different than the characters position in the char array
 * due existence of multi-char characters
 *
 * @author Keith Godfrey
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
            else if( text.equals(tok.text) )
				return true;
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
	
	/**
	 * Creates a new token.
	 * @param _text the text of the token
	 * @param _offset the starting position of this token in parent string
	 */
	public Token(String _text, int _offset)
	{
		text = _text;
        offset = _offset;
	}
	public String text;
    public int offset;
}

