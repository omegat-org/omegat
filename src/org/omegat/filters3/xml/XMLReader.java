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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;

/**
 * This class automatically detects encoding of an inner XML file and constructs
 * a Reader with appropriate encoding.
 * <p>
 * Detecting of encoding is done first by reading a possible BOM, to detect
 * UTF-16 or UTF-8 then by reading a value from the XML header
 * <code>&lt;?xml version="1.0" encoding="..."?&gt;</code>
 * <p>
 * If encoding isn't specified, or it is not supported by the Java platform, the
 * file is opened in UTF-8, in compliance with the XML specifications
 *
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class XMLReader extends Reader {
    /** Inner reader */
    private final BufferedReader reader;

    /** Inner encoding. */
    private String encoding;

    /** EOL chars used in source file. */
    private String eol;

    /** Returns detected encoding. */
    public String getEncoding() {
        return encoding;
    }

    /** Returns detected EOL chars. */
    public String getEol() {
        return eol;
    }

    /**
     * Creates a new instance of XMLReader. If encoding cannot be detected,
     * falls back to default UTF-8.
     * 
     * @param is
     *            the InputStream instance to read
     */
    public XMLReader(InputStream is) throws IOException {
        reader = createReader(is, null);
    }

    /**
     * Creates a new instance of XMLReader. If encoding cannot be detected,
     * falls back to default UTF-8.
     *
     * @param file
     *            - the file to read
     */
    public XMLReader(File file) throws IOException {
        this(file, null);
    }

    /**
     * Creates a new instance of XMLReader. If encoding cannot be detected,
     * falls back to supplied <code>encoding</code>, or (if supplied null, or
     * supplied encoding is not supported by JVM) falls back to UTF-8.
     *
     * @param file
     *            The file to read.
     * @param defaultEncoding
     *            The encoding to use if we can't autodetect.
     */
    public XMLReader(File file, String defaultEncoding) throws IOException {
        reader = createReader(new FileInputStream(file), defaultEncoding);
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
    private BufferedReader createReader(InputStream in, String defaultEncoding) throws IOException {
        // BOM detection
        BufferedInputStream is = new BufferedInputStream(in);

        is.mark(OConsts.READ_AHEAD_LIMIT);

        int char1 = is.read();
        int char2 = is.read();
        int char3 = is.read();
        encoding = null;
        if (char1 == 0xFE && char2 == 0xFF) {
            encoding = "UTF-16BE";
        }
        if (char1 == 0xFF && char2 == 0xFE) {
            encoding = "UTF-16LE";
        }
        if (char1 == 0xEF && char2 == 0xBB && char3 == 0xBF) {
            encoding = "UTF-8";
        }
        is.reset();
        if (encoding != null) {
            return createReaderAndDetectEOL(is, Charset.forName(encoding));
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
            return createReaderAndDetectEOL(is, Charset.forName(encoding));
        }

        // UTF-8 if we couldn't detect it ourselves
        try {
            return createReaderAndDetectEOL(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return createReaderAndDetectEOL(is, Charset.defaultCharset());
        }
    }

    private BufferedReader createReaderAndDetectEOL(@NotNull InputStream is, @NotNull Charset encoding)
            throws IOException {
        InputStreamReader isr = new InputStreamReader(is, encoding);
        BufferedReader bufferedReader = new BufferedReader(isr, OConsts.READ_AHEAD_LIMIT);
        bufferedReader.mark(OConsts.READ_AHEAD_LIMIT);
        eol = detectEndOfLine(bufferedReader);
        bufferedReader.reset();
        return bufferedReader;
    }

    private static final char CARRIAGE_RETURN = '\r';
    private static final char LINE_FEED = '\n';

    private String detectEndOfLine(BufferedReader reader) throws IOException {
        StringBuilder endOfLineBuilder = new StringBuilder();

        for (int i = 0; i < OConsts.READ_AHEAD_LIMIT; i++) {
            char currentChar = (char) reader.read();

            if (currentChar == CARRIAGE_RETURN || currentChar == LINE_FEED) {
                if (endOfLineBuilder.length() == 0) {
                    endOfLineBuilder.append(currentChar);
                } else if (endOfLineBuilder.charAt(0) == currentChar) {
                    // Detect duplicate character (second line)
                    return endOfLineBuilder.toString();
                } else {
                    endOfLineBuilder.append(currentChar);
                    if (endOfLineBuilder.length() == 2) {
                        // Both characters (\r\n or \n\r) found
                        return endOfLineBuilder.toString();
                    }
                }
            } else if (endOfLineBuilder.length() > 0) {
                // Reset if a non-EOL character is found after starting EOL
                return endOfLineBuilder.toString();
            }
        }

        // Default to '\n' if no EOL is detected
        return String.valueOf(LINE_FEED);
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
