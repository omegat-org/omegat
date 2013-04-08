/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.html2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;

import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;

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
 */
public class HTMLReader extends Reader {
    /** Inner reader */
    private BufferedReader reader;

    /**
     * Creates a new instance of HTMLReader. If encoding cannot be detected,
     * falls back to supplied <code>encoding</code>, or (if supplied null, or
     * supplied encoding is not supported by JVM) falls back to default encoding
     * of Operating System.
     * 
     * @param fileName
     *            The file to read.
     * @param encoding
     *            The encoding to use if we can't autodetect.
     */
    public HTMLReader(String fileName, String encoding) throws IOException {
        reader = new BufferedReader(createReader(fileName, encoding));
    }

    private String encoding = null;

    /**
     * Returns encoding that was used to read the HTML file.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the reader of the underlying file in the correct encoding.
     * 
     * <p>
     * We can detect the following:
     * <ul>
     * <li>UTF-16 with BOM (byte order mark)
     * <li>UTF-8 with BOM (byte order mark)
     * <li>Any other encoding with 8-bit Latin symbols (e.g. Windows-1251, UTF-8
     * etc), if it is specified using XML/HTML-style encoding declarations.
     * </ul>
     * <p>
     * Note that we cannot detect UTF-16 encoding, if there's no BOM!
     */
    private Reader createReader(String fileName, String defaultEncoding) throws IOException {
        // BOM detection
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileName));

        is.mark(OConsts.READ_AHEAD_LIMIT);

        int char1 = is.read();
        int char2 = is.read();
        int char3 = is.read();
        if (char1 == 0xFE && char2 == 0xFF)
            encoding = "UTF-16BE";
        if (char1 == 0xFF && char2 == 0xFE)
            encoding = "UTF-16LE";
        if (char1 == 0xEF && char2 == 0xBB && char3 == 0xBF)
            encoding = "UTF-8";

        is.reset();
        if (encoding != null) {
            return new InputStreamReader(is, encoding);
        }

        is.mark(OConsts.READ_AHEAD_LIMIT);
        byte[] buf = new byte[OConsts.READ_AHEAD_LIMIT];
        int len = is.read(buf);
        if (len > 0) {
            String buffer = new String(buf, 0, len);

            Matcher matcher_html = PatternConsts.HTML_ENCODING.matcher(buffer);
            if (matcher_html.find())
                encoding = matcher_html.group(1);

            if (encoding == null) {
                Matcher matcher_xml = PatternConsts.XML_ENCODING.matcher(buffer);
                if (matcher_xml.find())
                    encoding = matcher_xml.group(1);
            }
        }

        // reset the inputstream to its start
        is.reset();

        // create an inputstream reader
        InputStreamReader isr = null;

        // try the encoding specified in the file first
        if (encoding != null)
            try {
                isr = new InputStreamReader(is, encoding);
            } catch (Exception e) {
            }

        // if there's no reader yet, try the default encoding
        if (isr == null)
            try {
                isr = new InputStreamReader(is, defaultEncoding);
                encoding = defaultEncoding;
            } catch (Exception e) {
            }

        // just create one without an encoding and cross fingers
        if (isr == null)
            isr = new InputStreamReader(is);

        return isr;
    }

    public void close() throws IOException {
        reader.close();
    }

    boolean readFirstTime = true;

    public int read(char[] cbuf, int off, int len) throws IOException {
        // BOM (byte order mark) bugfix
        if (readFirstTime) {
            readFirstTime = false;
            reader.mark(1);
            int ch = reader.read();
            if (ch != 0xFEFF)
                reader.reset();
        }
        return reader.read(cbuf, off, len);
    }

}
