/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.omegat.util.PatternConsts;

/**
 * This class acts as an interceptor of output: First it collects all the output
 * inside itself in a string. Then it adds a META with a given charset (or
 * replaces the existing charset with a new one) and (if the file is XHTML and
 * has XML header) adds encoding declaration (or replaces the encoding in the
 * XML header). Next it writes out to the file.
 * <p>
 * Note that if <code>encoding</code> parameter of the
 * {@link #HTMLWriter(String, String, HTMLOptions) constructor} is null, no
 * encoding declaration is added, and the file is written in OS-default
 * encoding. This is done to fix a bug
 * <a href="https://sourceforge.net/p/omegat/bugs/101/">[1.6 RC2] Bug with
 * Target Encoding set to &lt;auto&gt; for (x)HTML</a>.
 *
 * @author Maxym Mykhalchuk
 */
public class HTMLWriter extends Writer {
    /** Internal Buffer to collect the output */
    private StringWriter writer;

    /** real writer to a file */
    private BufferedWriter realWriter;

    /** Replacement string for HTML content-type META */
    private String htmlMeta;
    /** Replacement string for XML (XHTML) header */
    private String xmlHeader;

    /**
     * Encoding to write this file in. null value means no encoding declaration.
     */
    private String encoding;

    /** HTML filter options. */
    private HTMLOptions options;

    /**
     * Creates new HTMLWriter.
     *
     * @param fileName
     *            - file name to write to
     * @param encoding
     *            - the encoding to write HTML file in (null means OS-default
     *            encoding)
     */
    public HTMLWriter(String fileName, String encoding, HTMLOptions options)
            throws FileNotFoundException, UnsupportedEncodingException {
        this.encoding = encoding;

        this.options = options;

        writer = new StringWriter();
        FileOutputStream fos = new FileOutputStream(fileName);

        OutputStreamWriter osw;
        if (encoding != null) {
            osw = new OutputStreamWriter(fos, encoding);
        } else {
            osw = new OutputStreamWriter(fos, Charset.defaultCharset());
        }
        realWriter = new BufferedWriter(osw);
    }

    /**
     * The minimal size of already written HTML that will be appended headers
     */
    private static final int MIN_HEADERED_BUFFER_SIZE = 4096;

    /** The maximal size of a buffer before flush */
    private static final int MAX_BUFFER_SIZE = 65536;

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
        if (signalAlreadyFlushed || encoding == null) {
            // already flushed, i.e. already wrote out the headers stuff
            // or we don't add any metas (encoding is null)

            realWriter.write(buffer.toString());
            buffer.setLength(0);
        } else if (signalClosing || buffer.length() >= MIN_HEADERED_BUFFER_SIZE) {
            // else if we're closing or the buffer is big enough
            // to (hopefully) contain all the existing headers

            signalAlreadyFlushed = true;

            String contents = buffer.toString();

            if (options.getRewriteEncoding() != HTMLOptions.REWRITE_MODE.NEVER) {
                String eol = "";
                Matcher matcherLineending = PatternConsts.LINE_ENDING.matcher(contents);
                if (matcherLineending.find()) {
                    eol = matcherLineending.group();
                }

                Matcher matcherHeader = PatternConsts.XML_HEADER.matcher(contents);
                boolean xhtml = false;
                if (matcherHeader.find()) {
                    xmlHeader = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
                    contents = matcherHeader.replaceFirst(xmlHeader);
                    xhtml = true;
                }

                htmlMeta = "<meta http-equiv=\"content-type\" content=\"text/html; charset=" + encoding
                        + "\"";
                if (xhtml) {
                    htmlMeta += " />";
                } else {
                    htmlMeta += ">";
                }
                Matcher matcherEnc = PatternConsts.HTML_ENCODING.matcher(contents);
                Matcher matcherEncHtml5 = PatternConsts.HTML5_ENCODING.matcher(contents);
                if (matcherEnc.find()) {
                    contents = matcherEnc.replaceFirst(htmlMeta);
                } else if (matcherEncHtml5.find()) {
                    contents = matcherEncHtml5.replaceFirst("<meta charset=\"" + encoding + "\">");
                } else if (options.getRewriteEncoding() != HTMLOptions.REWRITE_MODE.IFMETA) {
                    Matcher matcherHead = PatternConsts.HTML_HEAD.matcher(contents);
                    if (matcherHead.find()) {
                        contents = matcherHead.replaceFirst("$0" + eol + "    " + htmlMeta);
                    } else if (options.getRewriteEncoding() != HTMLOptions.REWRITE_MODE.IFHEADER) {
                        Matcher matcherHtml = PatternConsts.HTML_HTML.matcher(contents);
                        if (matcherHtml.find()) {
                            contents = matcherHtml.replaceFirst(
                                    "$0" + eol + "<head>" + eol + "    " + htmlMeta + "" + eol + "</head>");
                        } else {
                            contents = "<html>" + eol + "<head>" + eol + "    " + htmlMeta + eol + "</head>"
                                    + eol + contents;
                        }
                    }
                }
            }

            realWriter.write(contents);
            buffer.setLength(0);
        }
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
    public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
        if (writer.getBuffer().length() >= MAX_BUFFER_SIZE) {
            flush();
        }
    }
}
