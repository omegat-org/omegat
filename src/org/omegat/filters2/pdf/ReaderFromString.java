/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Arno Peters
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

package org.omegat.filters2.pdf;

import java.io.IOException;
import java.io.Reader;

/**
 * 
 * @author Arno Peters
 */
public class ReaderFromString extends Reader
{
    private String source = "";
    private int mark = 0;
	
    public ReaderFromString(String s) {
	this.source = s;
	this.mark = 0;
    }
	
    @Override
    public int read(char[] c, int off, int len) 
	throws IOException {
	if ( mark + off >= source.length() )
	    return -1;

	int charsRead = 0;
	mark = mark + off;

	while ( mark < source.length() ) {
	    if (charsRead == c.length)
		break;
	    c[charsRead] = source.charAt(mark);
	    charsRead++;
	    mark++;
	}
		
	return charsRead;
    }

    @Override
    public void close() {
    }
}
