/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

public final class EncodingDetector {

    private EncodingDetector() {
    }

    /**
     * Detect the encoding of the supplied file.
     * Convenience method for {@link #detectEncoding(java.io.InputStream)}.
     */
    public static String detectEncoding(File inFile) throws IOException {
        try (FileInputStream stream = new FileInputStream(inFile)) {
            return detectEncoding(stream);
        }
    }

    /**
     * Detect the encoding of the supplied file. The caller is responsible for closing the stream.
     *
     * @see <a href="https://code.google.com/p/juniversalchardet/">Original</a>
     * @see <a href="https://github.com/amake/juniversalchardet">Fork</a>
     */
    public static String detectEncoding(InputStream stream) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) > 0 && !detector.isDone()) {
            detector.handleData(buffer, 0, read);
        }

        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding;
    }

    /**
     * Detect the encoding of the supplied file. If detection fails, return the supplied
     * default encoding.
     */
    public static String detectEncodingDefault(File inFile, String defaultEncoding) {
        String detected = null;
        try {
            detected = detectEncoding(inFile);
        } catch (IOException ex) {
            // Ignore
        }
        return detected == null ? defaultEncoding : detected;
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
    public static Charset detectHtmlEncoding(String fileName, String defaultEncoding) {
        Charset encoding = null;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
            encoding = detectBOM(inputStream);
            if (encoding == null) {
                encoding = detectEncodingFromContent(inputStream, defaultEncoding);
            }
        } catch (IOException ignored) {
            // ignore exceptions
        }
        return encoding != null ? encoding : Charset.defaultCharset();
    }

    /**
     * Detects the Byte Order Mark (BOM) to determine the corresponding character encoding.
     * <p>
     * This method identifies the following BOM types: UTF-16BE (Big Endian),
     * UTF-16LE (Little Endian), and UTF-8. If no BOM is detected, it returns
     * null.
     *
     * @param inputStream
     *            the BufferedInputStream to read and detect the BOM from. The
     *            stream's position will be reset after detection.
     * @return the Charset corresponding to the detected BOM, or null if no BOM
     *         is found.
     * @throws IOException
     *             if an I/O error occurs while reading from the stream.
     */
    private static Charset detectBOM(BufferedInputStream inputStream) throws IOException {
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

    /**
     * Detects the encoding of the HTML file by reading the first 1024 bytes.
     *
     * @param inputStream
     *            the BufferedInputStream to read and detect the encoding from.
     *            The stream's position will be reset after detection.
     * @param defaultEncoding
     *            the default encoding to use if no encoding is specified in the
     *            HTML file.
     * @return the Charset corresponding to the detected encoding, or null if no
     *         encoding is found.
     * @throws IOException
     *             if an I/O error occurs while reading from the stream.
     */
    private static Charset detectEncodingFromContent(BufferedInputStream inputStream, String defaultEncoding)
            throws IOException {
        inputStream.mark(OConsts.READ_AHEAD_LIMIT);

        byte[] buffer = new byte[OConsts.READ_AHEAD_LIMIT];
        int length = inputStream.read(buffer);
        inputStream.reset();

        if (length <= 0) {
            return null;
        }

        String content = defaultEncoding == null ? new String(buffer, 0, length, Charset.defaultCharset())
                : new String(buffer, 0, length, defaultEncoding);

        // Extracted helper method to detect charset
        return detectCharset(content, PatternConsts.HTML_ENCODING, PatternConsts.HTML5_ENCODING,
                PatternConsts.XML_ENCODING);
    }

    /**
     * Detects the character encoding of a given content by matching it against
     * a series of patterns.
     * <p>
     * Each pattern is used to search for an encoding declaration in the
     * content, and if found, the corresponding charset is returned. If none of
     * the patterns match or an invalid charset is encountered, null is
     * returned.
     *
     * @param content
     *            the content in which the character encoding is to be detected
     * @param patterns
     *            an array of regular expression patterns used to identify the
     *            character encoding declaration
     * @return the detected Charset if a matching pattern is found and a valid
     *         encoding is identified, or null if no match or valid encoding is
     *         found
     */
    private static Charset detectCharset(String content, Pattern... patterns) {
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
}
