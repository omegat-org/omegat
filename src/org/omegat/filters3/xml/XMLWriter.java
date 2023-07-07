/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel
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

package org.omegat.filters3.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import org.omegat.util.Log;
import org.omegat.util.PatternConsts;

/**
 * This class writes out the XML files, intercepting the output. First it
 * collects all the output inside itself in a string. Then it adds the specified
 * encoding declaration (or replaces the encoding) and writes out the file in
 * the encoding.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class XMLWriter extends Writer {
    /** Internal Buffer to collect the output */
    private StringWriter writer;

    /** real writer to a file */
    private BufferedWriter realWriter;

    /** Replacement string for XML header */
    private String xmlHeader;

    /** Detected EOL chars. */
    private String eol;

    /**
     * Creates new XMLWriter.
     *
     * @param file
     *            file name to write to
     * @param encoding
     *            encoding to write a file in
     * @param eol
     *            End-Of-Line character
     */
    public XMLWriter(File file, String encoding, String eol)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (encoding == null) {
            xmlHeader = "<?xml version=\"1.0\"?>";
        } else {
            xmlHeader = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
        }

        writer = new StringWriter();
        FileOutputStream fos = new FileOutputStream(file);

        OutputStreamWriter osw;
        if (encoding == null) {
            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        } else {
            osw = new OutputStreamWriter(fos, encoding);
        }

        realWriter = new BufferedWriter(osw);
        this.eol = eol;
    }

    /**
     * The minimal size of already written HTML that will be appended headers
     */
    private static final int MIN_HEADERED_BUFFER_SIZE = 4096;

    /** The maximal size of a buffer before flush */
    private static final int MAX_BUFFERED_SIZE = 65536;

    /**
     * Signals that the writer is being closed, hence it needs to write any
     * (little) buffer out.
     */
    private boolean signalClosing = false;

    /**
     * Signals that the writer was already flushed, i.e. already wrote out the
     * headers stuff.
     */
    private boolean signalAlreadyFlushed = false;

    /**
     * Flushes the writer (which does the real write-out of data) and closes the
     * real writer.
     */
    public void close() throws IOException {
        signalClosing = true;
        flush();
        realWriter.close();
    }

    /**
     * Does the real write-out of the data, first adding/replacing encoding
     * statement.
     */
    public void flush() throws IOException {
        StringBuffer buffer = writer.getBuffer();
        if (signalAlreadyFlushed) {
            // already flushed, i.e. already wrote out the headers stuff

            realWriter.write(fixEOL(buffer).toString());
            buffer.setLength(0);
        } else if (signalClosing || buffer.length() >= MIN_HEADERED_BUFFER_SIZE) {
            // else if we're closing or the buffer is big enough
            // to (hopefully) contain all the existing headers

            signalAlreadyFlushed = true;
            String contents;
            Matcher matcherHeader = PatternConsts.XML_HEADER.matcher(buffer);
            if (matcherHeader.find()) {
                contents = fixEOL(new StringBuffer(matcherHeader.replaceFirst(xmlHeader))).toString();
            } else {
                Log.log("Shouldn't happen! " + "XMLWriter: XML File does not contain XML header:\n"
                        + buffer.substring(0, Math.min(buffer.length(), 80)));
                realWriter.write(xmlHeader);
                contents = fixEOL(buffer).toString();
            }

            realWriter.write(contents);
            buffer.setLength(0);
        }
    }

    /**
     * We assume that EOL is always '\n' after XML processing.
     */
    StringBuffer fixEOL(StringBuffer out) {
        for (int cp, i = 0; i < out.length(); i += Character.charCount(cp)) {
            cp = out.codePointAt(i);
            if (cp == '\n') {
                // EOL
                out.setCharAt(i, eol.charAt(0));
                if (eol.length() > 1) {
                    // EOL is more than 1 char - need to insert remaining.
                    // Intentionally iterating by chars instead of code points.
                    for (int j = 1; j < eol.length(); j++) {
                        out.insert(i + 1, eol.charAt(j));
                        i++;
                    }
                }
            }
        }
        return out;
    }

    /**
     * Write a portion of an array of characters. Simply calls
     * <code>write(char[], int, int)</code> of the internal
     * <code>StringWriter</code>.
     *
     * @param cbuf
     *            - Array of characters
     * @param off
     *            - Offset from which to start writing characters
     * @param len
     *            - Number of characters to write
     * @throws IOException
     *             - If an I/O error occurs
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
        if (writer.getBuffer().length() >= MAX_BUFFERED_SIZE) {
            flush();
        }
    }
}
