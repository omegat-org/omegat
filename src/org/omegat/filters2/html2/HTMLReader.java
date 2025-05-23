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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @author Didier Briel
 */
public class HTMLReader extends Reader implements AutoCloseable {
    /** Inner reader */
    private final BufferedReader reader;

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

    private Charset encoding = null;

    /**
     * Returns encoding that was used to read the HTML file.
     */
    public String getEncoding() {
        return encoding.name();
    }

    private Reader createReader(String fileName, String defaultEncoding) throws IOException {

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
            encoding = detectBOM(inputStream);
            if (encoding == null) {
                encoding = detectEncodingFromContent(inputStream, defaultEncoding);
            }
            return new InputStreamReader(inputStream, encoding != null ? encoding : Charset.defaultCharset());
        }
    }

    private Charset detectBOM(BufferedInputStream inputStream) throws IOException {
        inputStream.mark(OConsts.READ_AHEAD_LIMIT);
        int firstByte = inputStream.read();
        int secondByte = inputStream.read();
        int thirdByte = inputStream.read();
        inputStream.reset();

        if (firstByte == 0xFE && secondByte == 0xFF) {
            return StandardCharsets.UTF_16BE;
        } else if (firstByte == 0xFF && secondByte == 0xFE) {
            return StandardCharsets.UTF_16LE;
        } else if (firstByte == 0xEF && secondByte == 0xBB && thirdByte == 0xBF) {
            return StandardCharsets.UTF_8;
        }
        return null;
    }

    private Charset detectEncodingFromContent(BufferedInputStream inputStream, String defaultEncoding) throws IOException {
        inputStream.mark(OConsts.READ_AHEAD_LIMIT);

        byte[] buffer = new byte[OConsts.READ_AHEAD_LIMIT];
        int length = inputStream.read(buffer);
        inputStream.reset();

        if (length <= 0) {
            return null;
        }

        String content = defaultEncoding == null
            ? new String(buffer, 0, length, Charset.defaultCharset())
            : new String(buffer, 0, length, defaultEncoding);

        // Extracted helper method to detect charset
        return detectCharset(content, PatternConsts.HTML_ENCODING, PatternConsts.HTML5_ENCODING, PatternConsts.XML_ENCODING);
    }

    // Helper method to handle repetitive charset detection
    private Charset detectCharset(String content, Pattern... patterns) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group(1));
                } catch (Exception ignored) {
                    // ignore and try next.
                }
            }
        }
        return null;
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
            if (ch != 0xFEFF) {
                reader.reset();
            }
        }
        return reader.read(cbuf, off, len);
    }
}
