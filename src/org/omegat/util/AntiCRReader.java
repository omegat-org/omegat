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

import java.io.IOException;
import java.io.Reader;

/**
 * A reader for XML readers, that skips 
 * Windows-like \r symbols.
 *
 * @author Maxym Mykhalchuk
 */
public class AntiCRReader extends Reader
{
    private Reader reader;

    /**
     * Creates a new reader that would skip \r-s,
     * possibly coming from an inner reader.
     *
     * @param reader The inner reader
     */
    public AntiCRReader(Reader reader)
    {
        this.reader = reader;
    }

    /**
     * Reads a single character.
     * Returns a character (which is guaranteed to be non-\r),
     * or -1 if EOF.
     */
    public int read() throws IOException
    {
        int res = reader.read();
        while( res=='\r' )
            res = reader.read();
        return res;
    }

    /** Closes the inner reader. */
    public void close() throws IOException
    {
        reader.close();
    }

    /**
     * Reads a number of characters, no more than <code>len</code>.
     * All these symbols are guaranteed to be non-\r.
     *
     * <p>
     * Returns -1 on EOF.
     */
    public int read(char cbuf[], int off, int len) throws IOException
    {
        if( (off<0) || (off>cbuf.length) || (len<0) || ((off+len)>cbuf.length) 
                || ((off+len)<0) ) 
        {
            throw new IndexOutOfBoundsException();
        } 
        else if( len==0 ) 
        {
            return 0;
        }
        
        int retValue = 0;
        for(int i=0; i<len; i++)
        {
            int ch = read();
            if( ch==-1 )
            {
                if( retValue==0 )
                    retValue=-1;
                return retValue;
            }
            else
                cbuf[off+i] = (char)ch;
            retValue++;
        }
        return retValue;
    }

    /** Whether the inner reader is ready. */
    public boolean ready() throws IOException
    {
        return reader.ready();
    }

    /** 
     * Skips at mose <code>n</code> characters.
     * Note that the actual number of skipped characters may be bigger
     * than requested, as \r-s are skipped anyway.
     */
    public long skip(long n) throws IOException
    {
        long retValue = 0;
        for(int i=0; i<n; i++)
        {
            if( read()==-1 )
                return retValue;
            retValue++;
        }
        return retValue;
    }

}
