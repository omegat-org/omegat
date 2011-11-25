/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * Reader class that preserves line breaks when using readLine(), that can be
 * retrieved through getLastLinebreak()
 * 
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class LinebreakPreservingReader extends Reader {

    /** Reader for input, must be able to re-insert non-linebreak characters. */
    private PushbackReader in;

    /** Default constructor, hidden. */
    private LinebreakPreservingReader() {
    }

    /** Buffer that contains the last linebreak */
    private StringBuffer linebreak = new StringBuffer(2);;

    /**
     * Creates a new reader.
     */
    public LinebreakPreservingReader(Reader in) {
        this.in = new PushbackReader(in);
    }

    public void close() throws IOException {
        in.close();
    }

    /**
     * Returns the linebreak after the last line read by readLine(). If any
     * other read methods are called after readLine(), the linebreak returned by
     * this method may be incorrect.
     */
    public String getLinebreak() {
        return linebreak.toString();
    }

    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(char[] cbuf) throws IOException {
        return in.read(cbuf);
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return in.read(cbuf, off, len);
    }

    public String readLine() throws IOException {
        // Clear linebreak buffer
        linebreak.setLength(0);

        // Create a buffer to contain the result
        StringBuffer line = new StringBuffer(1024);

        // Get the next character and check if it's not the end of the stream
        int chr = in.read();
        if (chr == -1)
            return null; // end of the stream reached

        // Read and store characters until a linebreak character is encountered
        while ((chr != -1) && !isLinebreakCharacter(chr)) {
            line.append((char) chr);
            chr = in.read();
        }

        // If the last read character is a linebreak character, save it
        if (isLinebreakCharacter(chr)) {
            // Save the linebreak character
            linebreak.append((char) chr);

            // If the linebreak character is \r, check if it's followed by \n
            if (((char) chr) == '\r') {
                // Get the next character
                chr = in.read();

                // If the next character is \n, add it to the current linebreak,
                // otherwise push it back into the input reader
                if (((char) chr) == '\n')
                    linebreak.append((char) chr);
                else
                    in.unread(chr);
            }
        }

        // We're all done, give the caller what he bargained for
        return line.toString();
    }

    public boolean ready() throws IOException {
        return in.ready();
    }

    public void reset() throws IOException {
        in.reset();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    private final boolean isLinebreakCharacter(int chr) {
        return (((char) chr) == '\n') || (((char) chr) == '\r');
    }

    /* FOR TESTING ONLY */
    public void printLinebreak(java.io.PrintStream out) {
        out.print("Break: ");
        for (int i = 0; i < linebreak.length(); i++) {
            char c = linebreak.charAt(i);
            if (c == '\r')
                out.print("\\r");
            else if (c == '\n')
                out.print("\\n");
            else
                out.print(c);
        }
        out.println();
    }

}
