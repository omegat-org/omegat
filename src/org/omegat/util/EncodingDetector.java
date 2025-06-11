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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.mozilla.universalchardet.UniversalDetector;

public final class EncodingDetector {

    private EncodingDetector() {
    }

    /**
     * Detect the encoding of the supplied file. Convenience method for
     * {@link #detectEncoding(java.io.InputStream)}.
     */
    public static String detectEncoding(File inFile) throws IOException {
        try (FileInputStream stream = new FileInputStream(inFile)) {
            return detectEncoding(stream);
        }
    }

    /**
     * Detect the encoding of the supplied file. The caller is responsible for
     * closing the stream.
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
     * Detect the encoding of the supplied file. If detection fails, return the
     * supplied default encoding.
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
                encoding = detectEncodingFromHtmlContent(inputStream);
            }
        } catch (IOException ignored) {
            // ignore exceptions
        }
        if (encoding != null) {
            return encoding;
        }
        if (defaultEncoding != null) {
            encoding = EncodingSniffer.toCharset(defaultEncoding);
        }
        return checkEncodingOrDefault(fileName, encoding);
    }

    private static final int READ_LIMIT = 8192;
    // Represents a failed character decoding
    private static final char REPLACEMENT_CHARACTER = 65533;
    // Signals end of stream
    private static final char END_OF_STREAM_CHARACTER = 0;

    private static Charset checkEncodingOrDefault(String fileName, Charset encoding) {
        Charset selectedEncoding = (encoding == null) ? StandardCharsets.UTF_8 : encoding;
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), selectedEncoding))) {
            if (containsInvalidCharacters(bufferedReader)) {
                // Fallback to default if invalid characters are found
                return Charset.defaultCharset();
            }
            return selectedEncoding;
        } catch (IOException e) {
            // Fallback to the default charset in case of any exceptions while
            // reading
            return Charset.defaultCharset();
        }
    }

    private static boolean containsInvalidCharacters(BufferedReader reader) throws IOException {
        char[] buffer = new char[READ_LIMIT];
        reader.read(buffer, 0, READ_LIMIT);
        for (char c : buffer) {
            if (c == REPLACEMENT_CHARACTER) {
                return true; // Invalid character detected
            }
            if (c == END_OF_STREAM_CHARACTER) {
                break; // Reached end of stream
            }
        }
        return false;
    }

    /**
     * Detects the Byte Order Mark (BOM) to determine the corresponding
     * character encoding.
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
        byte[] cbuf = new byte[3];
        cbuf[0] = (byte) inputStream.read();
        cbuf[1] = (byte) inputStream.read();
        cbuf[2] = (byte) inputStream.read();
        inputStream.reset();
        return EncodingSniffer.sniffEncodingFromUnicodeBom(cbuf);
    }

    /**
     * Detects the encoding of the HTML file by reading the first 1024 bytes.
     *
     * @param inputStream
     *            the BufferedInputStream to read and detect the encoding from.
     *            The stream's position will be reset after detection.
     * @return the Charset corresponding to the detected encoding, or null if no
     *         encoding is found.
     * @throws IOException
     *             if an I/O error occurs while reading from the stream.
     */
    private static Charset detectEncodingFromHtmlContent(BufferedInputStream inputStream) throws IOException {
        Charset detectedEncoding;
        inputStream.mark(OConsts.READ_AHEAD_LIMIT);
        detectedEncoding = EncodingSniffer.sniffEncodingFromXmlDeclaration(inputStream);
        inputStream.reset();

        if (detectedEncoding != null) {
            return detectedEncoding;
        }
        inputStream.mark(OConsts.READ_AHEAD_LIMIT);
        detectedEncoding = EncodingSniffer.sniffEncodingFromMetaTag(inputStream);
        inputStream.reset();
        return detectedEncoding;
    }
}
