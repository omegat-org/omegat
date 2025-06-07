/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2015 Didier Briel
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

package org.omegat.filters2.html2;

import org.omegat.util.BufferedFileReader;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.omegat.util.EncodingDetector.detectHtmlEncoding;

/**
 * This class automatically detects encoding of an inner HTML file and
 * constructs a Reader with appropriate encoding. Detecting of encoding is done
 * by reading a possible
 * <code>&lt;META http-equiv="content-type" content="text/html; charset=..."&gt;</code>
 * and a value from XML header (in case there is one)
 * <code>&lt;?xml version="1.0" encoding="..."?&gt;</code>. If encoding isn't
 * specified, or it is not supported by Java platform, the file is opened in
 * encoding passed to constructor or default system encoding (ISO-8859-2 in USA,
 * Windows-1251 on my OS).
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class HTMLReader extends BufferedFileReader implements AutoCloseable {

    /**
     * Creates a new instance of HTMLReader. If encoding cannot be detected,
     * falls back to supplied <code>encoding</code>, or (if supplied null, or
     * supplied encoding is not supported by JVM) falls back to default encoding
     * of Operating System.
     *
     * @param fileName
     *            The file to read.
     * @param defaultEncoding
     *            The encoding to use if we can't autodetect.
     */
    public HTMLReader(String fileName, String defaultEncoding) throws FileNotFoundException {
        super(fileName, detectHtmlEncoding(fileName, defaultEncoding));
    }

    boolean readFirstTime = true;

    /**
     * Reads characters into a portion of an array. If this is the first time
     * the method is invoked, it ensures that the Byte Order Mark (BOM) is
     * handled correctly by resetting the stream if a BOM is not present.
     * Subsequent reads proceed as normal.
     *
     * @param cbuf
     *            the destination buffer to hold the characters read from the
     *            stream
     * @param off
     *            the start offset in the buffer where characters are written
     * @param len
     *            the maximum number of characters to read
     * @return the number of characters read into the buffer or -1 if the end of
     *         the stream has been reached
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        // BOM (byte order mark) bugfix
        if (readFirstTime) {
            readFirstTime = false;
            super.mark(1);
            int ch = super.read();
            if (ch != 0xFEFF) {
                super.reset();
            }
        }
        return super.read(cbuf, off, len);
    }

}
