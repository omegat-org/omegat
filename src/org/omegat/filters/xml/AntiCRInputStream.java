/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

package org.omegat.filters.xml;

import java.io.InputStream;
import java.io.IOException;

/**
 * A stream for XML readers, that skips 
 * Windows-like \r symbols.
 *
 * @author Maxym Mykhalchuk
 */
class AntiCRInputStream extends InputStream
{
    private InputStream is;

    public AntiCRInputStream(InputStream is)
    {
        this.is = is;
    }

    public int available() throws IOException
    {
        return is.available();
    }
    public long skip(long n) throws IOException
    {
        return is.skip(n);
    }
    public void close() throws IOException
    {
        is.close();
    }

    public int read() throws IOException
    {
        int res = is.read();
        while( res=='\r' )
            res = is.read();
        return res;
    }

}
