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

package org.omegat.util.xml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;

/**
 * This class automatically detects encoding of an inner XML file and constructs
 * a Reader with appropriate encoding.
 * <p>
 * Detecting of encoding is done by reading a value from XML header
 * <code>&lt;?xml version="1.0" encoding="..."?&gt;</code>
 * <p>
 * If encoding isn't specified, or it is not supported by Java platform, the
 * file is opened in default system encoding (ISO-8859-2 in USA, Windows-1251 on
 * my OS).
 *
 * @author Maxym Mykhalchuk
 */
@Deprecated
public class XMLReader extends Reader {
    /** Inner reader */
    private BufferedReader reader;

    /**
     * Creates a new instance of XMLReader. If encoding cannot be detected,
     * falls back to default encoding of Operating System.
     *
     * @param fileName
     *            - the file to read
     */
    public XMLReader(String fileName) throws IOException {
        this(fileName, null);
    }

    /**
     * Creates a new instance of XMLReader. If encoding cannot be detected,
     * falls back to supplied <code>encoding</code>, or (if supplied null, or
     * supplied encoding is not supported by JVM) falls back to default encoding
     * of Operating System.
     *
     * @param fileName
     *            The file to read.
     * @param encoding
     *            The encoding to use if we can't autodetect.
     */
    public XMLReader(String fileName, String encoding) throws IOException {
        reader = new BufferedReader(createReader(new FileInputStream(fileName), encoding));
    }

    public XMLReader(InputStream inputStream, String encoding) throws IOException {
        reader = new BufferedReader(createReader(inputStream, encoding));
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
     *
     * <p>
     * Note that we cannot detect UTF-16 encoding, if there's no BOM!
     */
    private Reader createReader(InputStream inputStream, String defaultEncoding) throws IOException {
        // BOM detection
        BufferedInputStream is = new BufferedInputStream(inputStream);

        is.mark(OConsts.READ_AHEAD_LIMIT);

        int char1 = is.read();
        int char2 = is.read();
        int char3 = is.read();
        String encoding = null;
        if (char1 == 0xFE && char2 == 0xFF) {
            encoding = "UTF-16BE";
        } else if (char1 == 0xFF && char2 == 0xFE) {
            encoding = "UTF-16LE";
        } else if (char1 == 0xEF && char2 == 0xBB && char3 == 0xBF) {
            encoding = "UTF-8";
        }

        is.reset();
        if (encoding != null) {
            return new InputStreamReader(is, encoding);
        }

        is.mark(OConsts.READ_AHEAD_LIMIT);
        byte[] buf = new byte[OConsts.READ_AHEAD_LIMIT];
        int len = is.read(buf);
        if (len > 0) {
            String buffer = defaultEncoding == null ? new String(buf, 0, len, Charset.defaultCharset())
                    : new String(buf, 0, len, defaultEncoding);

            Matcher matcherXml = PatternConsts.XML_ENCODING.matcher(buffer);
            if (matcherXml.find()) {
                encoding = matcherXml.group(1);
            }
        }

        is.reset();
        if (encoding != null) {
            return new InputStreamReader(is, encoding);
        }

        // default encoding if we couldn't detect it ourselves
        try {
            return new InputStreamReader(is, defaultEncoding);
        } catch (Exception e) {
            return new InputStreamReader(is, Charset.defaultCharset());
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    boolean readFirstTime = true;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        // BOM (byte order mark) bugfix
        if (readFirstTime) {
            readFirstTime = false;
            reader.mark(1);
            int ch = reader.read();
            if (ch != 0xFEFF) {
                reader.reset();
            }
        }
        return reader.read(cbuf, off, len);
    }
}
