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

package org.omegat.filters2.xml.xhtml;

import org.omegat.filters2.html.HTMLFilter;

/**
 * Entity filter for XHTML.
 * <p>
 * Does XML Entity -> Symbol conversion on source file read
 * and Symbol -> XML Entity conversion on translation write.
 *
 * @author Keith Godfrey
 */
public class XHTMLEntityFilter
{
    /**
     * Converts plaintext symbol to XML entity.
     */
	public String convertToEntity(char c)
    {
        return HTMLFilter.convertToEntity(c);
    }
    /**
     * Converts XML entity to plaintext symbol.
     */
	public char convertToSymbol(String escapeSequence)
    {
        return HTMLFilter.convertToChar(escapeSequence);
    }
}
