/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
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

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * /dev/null writer. Used for filters where BufferedWriter required by filter,
 * but not need for application.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class NullBufferedWriter extends BufferedWriter {
    public NullBufferedWriter() {
        super(new NullWriter());
    }

    public void write(int c) throws IOException {
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
    }

    public void write(String s, int off, int len) throws IOException {
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
    }
}
