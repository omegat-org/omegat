/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               2002-2025 Gargoyle Software Inc.(APACHE-2)
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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Sniffs encoding settings from HTML, XML or other content. The HTML encoding
 * sniffing algorithm is based on the <a href=
 * "http://www.whatwg.org/specs/web-apps/current-work/multipage/parsing.html#determining-the-character-encoding">HTML5
 * encoding sniffing algorithm</a>.
 *
 * Copyright (c) 2002-2025 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Daniel Gredler
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Lai Quang Duong
 */
public final class EncodingSniffer {

    /** Sequence(s) of bytes indicating the beginning of a comment. */
    private static final byte[][] COMMENT_START = { new byte[] { '<' }, new byte[] { '!' },
            new byte[] { '-' }, new byte[] { '-' } };

    /**
     * Sequence(s) of bytes indicating the beginning of a <code>meta</code> HTML
     * tag.
     */
    private static final byte[][] META_START = { new byte[] { '<' }, new byte[] { 'm', 'M' },
            new byte[] { 'e', 'E' }, new byte[] { 't', 'T' }, new byte[] { 'a', 'A' },
            new byte[] { 0x09, 0x0A, 0x0C, 0x0D, 0x20, 0x2F } };

    /**
     * Sequence(s) of bytes indicating the beginning of miscellaneous HTML
     * content.
     */
    private static final byte[][] OTHER_START = { new byte[] { '<' }, new byte[] { '!', '/', '?' } };

    /**
     * Sequence(s) of bytes indicating the beginning of a charset specification.
     */
    private static final byte[][] CHARSET_START = { new byte[] { 'c', 'C' }, new byte[] { 'h', 'H' },
            new byte[] { 'a', 'A' }, new byte[] { 'r', 'R' }, new byte[] { 's', 'S' },
            new byte[] { 'e', 'E' }, new byte[] { 't', 'T' } };

    private static final byte[] WHITESPACE = { 0x09, 0x0A, 0x0C, 0x0D, 0x20, 0x3E };
    private static final byte[] TAG_CLOSE_DELIMITER = { 0x3E };
    private static final byte[] COMMENT_END = { '-', '-', '>' };

    private static final byte[] XML_DECLARATION_PREFIX = "<?xml ".getBytes(US_ASCII);

    private static final byte[] CSS_CHARSET_DECLARATION_PREFIX = "@charset \"".getBytes(US_ASCII);

    /**
     * The number of HTML bytes to sniff for encoding info embedded in
     * <code>meta</code> tags;
     */
    private static final int SIZE_OF_HTML_CONTENT_SNIFFED = 1024;

    /**
     * The number of XML bytes to sniff for encoding info embedded in the XML
     * declaration; relatively small because it's always at the very beginning
     * of the file.
     */
    private static final int SIZE_OF_XML_CONTENT_SNIFFED = 512;

    private static final int SIZE_OF_CSS_CONTENT_SNIFFED = 1024;

    /**
     * Disallow instantiation of this class.
     */
    private EncodingSniffer() {
        // Empty.
    }

    /**
     * Attempts to sniff an encoding from a
     * <a href="http://en.wikipedia.org/wiki/Byte_Order_Mark">Byte Order
     * Mark</a> in the specified byte array.
     *
     * @param bytes
     *            the bytes to check for a Byte Order Mark
     * @return the encoding sniffed from the specified bytes, or {@code null} if
     *         the encoding could not be determined
     */
    static Charset sniffEncodingFromUnicodeBom(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Charset encoding = null;
        if (startsWith(bytes, ByteOrderMark.UTF_8)) {
            encoding = UTF_8;
        } else if (startsWith(bytes, ByteOrderMark.UTF_16BE)) {
            encoding = UTF_16BE;
        } else if (startsWith(bytes, ByteOrderMark.UTF_16LE)) {
            encoding = UTF_16LE;
        }

        if (encoding != null && Log.isDebugEnabled()) {
            Log.logDebug("Encoding found in Unicode Byte Order Mark: '{}'.", encoding);
        }
        return encoding;
    }

    /**
     * Returns whether the specified byte array starts with the given
     * {@link ByteOrderMark}, or not.
     * 
     * @param bytes
     *            the byte array to check
     * @param bom
     *            the {@link ByteOrderMark}
     * @return whether the specified byte array starts with the given
     *         {@link ByteOrderMark}, or not
     */
    private static boolean startsWith(final byte[] bytes, final ByteOrderMark bom) {
        final byte[] bomBytes = bom.getBytes();
        final byte[] firstBytes = Arrays.copyOfRange(bytes, 0, Math.min(bytes.length, bomBytes.length));
        return Arrays.equals(firstBytes, bomBytes);
    }

    /**
     * Attempts to sniff an encoding from an HTML <code>meta</code> tag in the
     * specified byte array.
     *
     * @param is
     *            the content stream to check for an HTML <code>meta</code> tag
     * @return the encoding sniffed from the specified bytes, or {@code null} if
     *         the encoding could not be determined
     * @throws IOException
     *             if an IO error occurs
     */
    public static Charset sniffEncodingFromMetaTag(final InputStream is) throws IOException {
        final byte[] bytes = read(is, SIZE_OF_HTML_CONTENT_SNIFFED);
        int i = 0;

        while (i < bytes.length) {
            if (matches(bytes, i, COMMENT_START)) {
                i = skipToEndOfComment(bytes, i);
            } else if (matches(bytes, i, META_START)) {
                i = processMetaTag(bytes, i);
                Charset charset = extractCharsetFromMeta(bytes, i);
                if (charset != null) {
                    return charset;
                }
            } else if (matchesOpeningTag(bytes, i)) {
                i = skipAttributes(bytes, i, WHITESPACE);
            } else if (matchesClosingTag(bytes, i)) {
                i = skipAttributes(bytes, i, WHITESPACE);
            } else if (matches(bytes, i, OTHER_START)) {
                i = skipToAnyOf(bytes, i, TAG_CLOSE_DELIMITER);
            } else {
                i++;
            }

            if (i == -1) {
                break;
            }
        }
        return null;
    }

    private static int skipToEndOfComment(byte[] bytes, int index) {
        index = indexOfSubArray(bytes, COMMENT_END, index);
        return (index == -1) ? -1 : index + COMMENT_END.length;
    }

    private static int processMetaTag(byte[] bytes, int index) {
        return index + META_START.length;
    }

    private static Charset extractCharsetFromMeta(byte[] bytes, int index) {
        for (Attribute currentAttribute = getAttribute(bytes,
                index); currentAttribute != null; currentAttribute = getAttribute(bytes, index)) {
            index = currentAttribute.getUpdatedIndex();
            Charset charset = processMetaAttributes(currentAttribute);
            if (charset != null) {
                return charset;
            }
        }
        return null;
    }

    private static boolean matchesOpeningTag(byte[] bytes, int index) {
        return index + 1 < bytes.length && bytes[index] == '<' && Character.isLetter(bytes[index + 1]);
    }

    private static boolean matchesClosingTag(byte[] bytes, int index) {
        return index + 2 < bytes.length && bytes[index] == '<' && bytes[index + 1] == '/'
                && Character.isLetter(bytes[index + 2]);
    }

    private static int skipAttributes(byte[] bytes, int index, byte[] delimiters) {
        index = skipToAnyOf(bytes, index, delimiters);
        while (index != -1) {
            Attribute currentAttribute = getAttribute(bytes, index);
            if (currentAttribute == null) {
                break;
            }
            index = currentAttribute.getUpdatedIndex();
        }
        return index;
    }

    private static @Nullable Charset processMetaAttributes(Attribute att) {
        final String name = att.getName().toLowerCase(Locale.ROOT);
        final String value = att.getValue().toLowerCase(Locale.ROOT);
        if ("charset".equals(name) || "content".equals(name)) {
            Charset charset = null;
            if ("charset".equals(name)) {
                charset = toCharset(value);
                // https://html.spec.whatwg.org/multipage/parsing.html#prescan-a-byte-stream-to-determine-its-encoding
                if (charset == null && "x-user-defined".equals(value)) {
                    charset = Charset.forName("windows-1252");
                }
            } else if ("content".equals(name)) {
                charset = extractEncodingFromContentType(value);
                // https://html.spec.whatwg.org/multipage/parsing.html#prescan-a-byte-stream-to-determine-its-encoding
                if (charset == null && value != null && value.contains("x-user-defined")) {
                    charset = Charset.forName("windows-1252");
                }
                if (charset == null) {
                    return null;
                }
            }
            if (UTF_16BE == charset || UTF_16LE == charset) {
                charset = UTF_8;
            }
            if (charset != null) {
                if (Log.isDebugEnabled()) {
                    Log.logDebug("Encoding found in meta tag: '{}'", charset);
                }
                return charset;
            }
        }
        return null;
    }

    /**
     * Extracts an attribute from the specified byte array, starting at the
     * specified index, using the <a href=
     * "http://www.whatwg.org/specs/web-apps/current-work/multipage/parsing.html#concept-get-attributes-when-sniffing">
     * HTML5 attribute algorithm</a>.
     *
     * @param bytes
     *            the byte array to extract an attribute from
     * @param startFrom
     *            the index to start searching from
     * @return the next attribute in the specified byte array, or {@code null}
     *         if one is not available
     */
    static Attribute getAttribute(final byte[] bytes, final int startFrom) {
        if (startFrom >= bytes.length) {
            return null;
        }

        int pos = startFrom;
        while (bytes[pos] == 0x09 || bytes[pos] == 0x0A || bytes[pos] == 0x0C || bytes[pos] == 0x0D
                || bytes[pos] == 0x20 || bytes[pos] == 0x2F) {
            pos++;
            if (pos >= bytes.length) {
                return null;
            }
        }
        if (bytes[pos] == '>') {
            return null;
        }
        final StringBuilder name = new StringBuilder();
        final StringBuilder value = new StringBuilder();
        for (;; pos++) {
            if (pos >= bytes.length) {
                return new Attribute(name.toString(), value.toString(), pos);
            }
            if (bytes[pos] == '=' && name.length() != 0) {
                pos++;
                break;
            }
            if (bytes[pos] == 0x09 || bytes[pos] == 0x0A || bytes[pos] == 0x0C || bytes[pos] == 0x0D
                    || bytes[pos] == 0x20) {
                while (bytes[pos] == 0x09 || bytes[pos] == 0x0A || bytes[pos] == 0x0C || bytes[pos] == 0x0D
                        || bytes[pos] == 0x20) {
                    pos++;
                    if (pos >= bytes.length) {
                        return new Attribute(name.toString(), value.toString(), pos);
                    }
                }
                if (bytes[pos] != '=') {
                    return new Attribute(name.toString(), value.toString(), pos);
                }
                pos++;
                break;
            }
            if (bytes[pos] == '/' || bytes[pos] == '>') {
                return new Attribute(name.toString(), value.toString(), pos);
            }
            name.append((char) bytes[pos]);
        }
        if (pos >= bytes.length) {
            return new Attribute(name.toString(), value.toString(), pos);
        }
        while (bytes[pos] == 0x09 || bytes[pos] == 0x0A || bytes[pos] == 0x0C || bytes[pos] == 0x0D
                || bytes[pos] == 0x20) {
            pos++;
            if (pos >= bytes.length) {
                return new Attribute(name.toString(), value.toString(), pos);
            }
        }
        if (bytes[pos] == '"' || bytes[pos] == '\'') {
            final byte b = bytes[pos];
            for (pos++; pos < bytes.length; pos++) {
                if (bytes[pos] == b) {
                    pos++;
                    return new Attribute(name.toString(), value.toString(), pos);
                } else if (bytes[pos] >= 'A' && bytes[pos] <= 'Z') {
                    final byte b2 = (byte) (bytes[pos] + 0x20);
                    value.append((char) b2);
                } else {
                    value.append((char) bytes[pos]);
                }
            }
            return new Attribute(name.toString(), value.toString(), pos);
        } else if (bytes[pos] == '>') {
            return new Attribute(name.toString(), value.toString(), pos);
        } else if (bytes[pos] >= 'A' && bytes[pos] <= 'Z') {
            final byte b = (byte) (bytes[pos] + 0x20);
            value.append((char) b);
            pos++;
        } else {
            value.append((char) bytes[pos]);
            pos++;
        }
        for (; pos < bytes.length; pos++) {
            if (bytes[pos] == 0x09 || bytes[pos] == 0x0A || bytes[pos] == 0x0C || bytes[pos] == 0x0D
                    || bytes[pos] == 0x20 || bytes[pos] == 0x3E) {
                return new Attribute(name.toString(), value.toString(), pos);
            } else if (bytes[pos] >= 'A' && bytes[pos] <= 'Z') {
                final byte b = (byte) (bytes[pos] + 0x20);
                value.append((char) b);
            } else {
                value.append((char) bytes[pos]);
            }
        }
        return new Attribute(name.toString(), value.toString(), pos);
    }

    /**
     * Extracts an encoding from the specified <code>Content-Type</code> value
     * using
     * <a href="http://ietfreport.isoc.org/idref/draft-abarth-mime-sniff/">the
     * IETF algorithm</a>; if no encoding is found, this method returns
     * {@code null}.
     *
     * @param s
     *            the <code>Content-Type</code> value to search for an encoding
     * @return the encoding found in the specified <code>Content-Type</code>
     *         value, or {@code null} if no encoding was found
     */
    public static Charset extractEncodingFromContentType(final String s) {
        if (s == null) {
            return null;
        }
        final byte[] bytes = s.getBytes(US_ASCII);
        int i;
        for (i = 0; i < bytes.length; i++) {
            if (matches(bytes, i, CHARSET_START)) {
                i += CHARSET_START.length;
                break;
            }
        }
        if (i == bytes.length) {
            return null;
        }
        while (bytes[i] == 0x09 || bytes[i] == 0x0A || bytes[i] == 0x0C || bytes[i] == 0x0D
                || bytes[i] == 0x20) {
            i++;
            if (i == bytes.length) {
                return null;
            }
        }
        if (bytes[i] != '=') {
            return null;
        }
        i++;
        if (i == bytes.length) {
            return null;
        }
        while (bytes[i] == 0x09 || bytes[i] == 0x0A || bytes[i] == 0x0C || bytes[i] == 0x0D
                || bytes[i] == 0x20) {
            i++;
            if (i == bytes.length) {
                return null;
            }
        }
        if (bytes[i] == '"') {
            if (bytes.length <= i + 1) {
                return null;
            }
            final int index = ArrayUtils.indexOf(bytes, (byte) '"', i + 1);
            if (index == -1) {
                return null;
            }
            final String charsetName = new String(ArrayUtils.subarray(bytes, i + 1, index), US_ASCII);
            return toCharset(charsetName);
        }
        if (bytes[i] == '\'') {
            if (bytes.length <= i + 1) {
                return null;
            }
            final int index = ArrayUtils.indexOf(bytes, (byte) '\'', i + 1);
            if (index == -1) {
                return null;
            }
            final String charsetName = new String(ArrayUtils.subarray(bytes, i + 1, index), US_ASCII);
            return toCharset(charsetName);
        }
        int end = skipToAnyOf(bytes, i, new byte[] { 0x09, 0x0A, 0x0C, 0x0D, 0x20, 0x3B });
        if (end == -1) {
            end = bytes.length;
        }
        final String charsetName = new String(ArrayUtils.subarray(bytes, i, end), US_ASCII);
        return toCharset(charsetName);
    }

    private static final String ENCODING_ATTRIBUTE = "encoding";

    /**
     * Searches the specified XML content for an XML declaration and returns the
     * encoding if found, otherwise returns {@code null}.
     *
     * @param is
     *            the content stream to check for the charset declaration
     * @return the encoding of the specified XML content, or {@code null} if it
     *         could not be determined
     * @throws IOException
     *             if an IO error occurs
     */
    public static Charset sniffEncodingFromXmlDeclaration(final InputStream is) throws IOException {
        final byte[] bytes = read(is, SIZE_OF_XML_CONTENT_SNIFFED);
        if (!startsWithXmlDeclaration(bytes)) {
            return null;
        }

        final int declarationEndIndex = ArrayUtils.indexOf(bytes, (byte) '?', 2);
        if (declarationEndIndex + 1 >= bytes.length || bytes[declarationEndIndex + 1] != '>') {
            return null;
        }

        final String declaration = new String(bytes, 0, declarationEndIndex + 2, US_ASCII);
        final Charset charset = extractEncodingFromDeclaration(declaration);

        if (charset != null && Log.isDebugEnabled()) {
            Log.logDebug("Encoding found in XML declaration: '{}'", charset);
        }
        return charset;
    }

    private static boolean startsWithXmlDeclaration(final byte[] bytes) {
        return bytes.length > 5 && XML_DECLARATION_PREFIX[0] == bytes[0]
                && XML_DECLARATION_PREFIX[1] == bytes[1] && XML_DECLARATION_PREFIX[2] == bytes[2]
                && XML_DECLARATION_PREFIX[3] == bytes[3] && XML_DECLARATION_PREFIX[4] == bytes[4]
                && XML_DECLARATION_PREFIX[5] == bytes[5];
    }

    private static Charset extractEncodingFromDeclaration(final String declaration) {
        int encodingStart = declaration.indexOf(ENCODING_ATTRIBUTE);
        if (encodingStart == -1) {
            return null;
        }

        encodingStart += ENCODING_ATTRIBUTE.length();
        while (encodingStart < declaration.length()) {
            char currentChar = declaration.charAt(encodingStart);
            if (currentChar == '"' || currentChar == '\'') {
                break;
            }
            encodingStart++;
        }

        if (encodingStart >= declaration.length()) {
            return null;
        }

        final char delimiter = declaration.charAt(encodingStart);
        encodingStart++;

        final int encodingEnd = declaration.indexOf(delimiter, encodingStart);
        if (encodingEnd == -1) {
            return null;
        }

        return toCharset(declaration.substring(encodingStart, encodingEnd));
    }

    /**
     * Parses and returns the charset declaration at the start of a css file if
     * any, otherwise returns {@code null}.
     * <p>
     * e.g.
     * 
     * <pre>
     * &#64;charset "UTF-8"
     * </pre>
     *
     * @param is
     *            the input stream to parse
     * @return the charset declaration at the start of a css file if any,
     *         otherwise returns {@code null}.
     * @throws IOException
     *             if an IO error occurs
     */
    public static Charset sniffEncodingFromCssDeclaration(final InputStream is) throws IOException {
        final byte[] bytes = read(is, SIZE_OF_CSS_CONTENT_SNIFFED);
        if (bytes.length < CSS_CHARSET_DECLARATION_PREFIX.length) {
            return null;
        }
        for (int i = 0; i < CSS_CHARSET_DECLARATION_PREFIX.length; i++) {
            if (bytes[i] != CSS_CHARSET_DECLARATION_PREFIX[i]) {
                return null;
            }
        }

        Charset encoding = null;
        final int index = ArrayUtils.indexOf(bytes, (byte) '"', CSS_CHARSET_DECLARATION_PREFIX.length);
        if (index + 1 < bytes.length && bytes[index + 1] == ';') {
            encoding = toCharset(new String(bytes, CSS_CHARSET_DECLARATION_PREFIX.length,
                    index - CSS_CHARSET_DECLARATION_PREFIX.length, US_ASCII));
            // https://www.w3.org/TR/css-syntax-3/#input-byte-stream "Why use
            // utf-8 when the declaration says utf-16?"
            if (encoding == UTF_16BE || encoding == UTF_16LE) {
                encoding = UTF_8;
            }
        }
        return encoding;
    }

    /**
     * Returns {@code Charset} if the specified charset name is supported on
     * this platform.
     *
     * @param charsetName
     *            the charset name to check
     * @return {@code Charset} if the specified charset name is supported on
     *         this platform
     */
    public static Charset toCharset(final String charsetName) {
        try {
            return Charset.forName(charsetName);
        } catch (final IllegalCharsetNameException | UnsupportedCharsetException e) {
            return null;
        }
    }

    /**
     * Returns {@code true} if the byte in the specified byte array at the
     * specified index matches one of the specified byte array patterns.
     *
     * @param bytes
     *            the byte array to search in
     * @param i
     *            the index at which to search
     * @param sought
     *            the byte array patterns to search for
     * @return {@code true} if the byte in the specified byte array at the
     *         specified index matches one of the specified byte array patterns
     */
    static boolean matches(final byte[] bytes, final int i, final byte[][] sought) {
        if (i + sought.length > bytes.length) {
            return false;
        }
        for (int x = 0; x < sought.length; x++) {
            final byte[] possibilities = sought[x];
            boolean match = false;
            for (final byte possibility : possibilities) {
                if (bytes[i + x] == possibility) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    /**
     * Skips ahead to the first occurrence of any of the specified targets
     * within the specified array, starting at the specified index. This method
     * returns <code>-1</code> if none of the targets are found.
     *
     * @param bytes
     *            the array to search through
     * @param startFrom
     *            the index to start looking at
     * @param targets
     *            the targets to search for
     * @return the index of the first occurrence of the specified targets within
     *         the specified array
     */
    static int skipToAnyOf(final byte[] bytes, final int startFrom, final byte[] targets) {
        int i = startFrom;
        for (; i < bytes.length; i++) {
            if (ArrayUtils.contains(targets, bytes[i])) {
                break;
            }
        }
        if (i == bytes.length) {
            i = -1;
        }
        return i;
    }

    /**
     * Finds the first index of the specified sub-array inside the specified
     * array, starting at the specified index. This method returns
     * <code>-1</code> if the specified sub-array cannot be found.
     *
     * @param array
     *            the array to traverse for looking for the sub-array
     * @param subarray
     *            the sub-array to find
     * @param startIndex
     *            the start index to traverse forwards from
     * @return the index of the sub-array within the array
     */
    static int indexOfSubArray(final byte[] array, final byte[] subarray, final int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            boolean found = true;
            if (i + subarray.length > array.length) {
                break;
            }
            for (int j = 0; j < subarray.length; j++) {
                final byte a = array[i + j];
                final byte b = subarray[j];
                if (a != b) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Attempts to read <code>size</code> bytes from the specified input stream.
     * Note that this method is not guaranteed to be able to read
     * <code>size</code> bytes; however, the returned byte array will always be
     * the exact length of the number of bytes read.
     *
     * @param content
     *            the input stream to read from
     * @param size
     *            the number of bytes to try to read
     * @return the bytes read from the specified input stream
     * @throws IOException
     *             if an IO error occurs
     */
    static byte[] read(final InputStream content, final int size) throws IOException {
        byte[] bytes = new byte[size];
        // using IOUtils guarantees that it will read as many bytes as possible
        // before giving up;
        // this may not always be the case for subclasses of InputStream} - eg.
        // GZIPInputStream
        final int count = IOUtils.read(content, bytes);
        if (count < size) {
            final byte[] smaller = new byte[count];
            System.arraycopy(bytes, 0, smaller, 0, count);
            bytes = smaller;
        }
        return bytes;
    }

    /**
     * Attempts to read <code>size</code> bytes from the specified input stream
     * and then prepends the specified prefix to the bytes read, returning the
     * resultant byte array. Note that this method is not guaranteed to be able
     * to read <code>size</code> bytes; however, the returned byte array will
     * always be the exact length of the number of bytes read plus the length of
     * the prefix array.
     *
     * @param content
     *            the input stream to read from
     * @param size
     *            the number of bytes to try to read
     * @param prefix
     *            the byte array to prepend to the bytes read from the specified
     *            input stream
     * @return the bytes read from the specified input stream, prefixed by the
     *         specified prefix
     * @throws IOException
     *             if an IO error occurs
     */
    static byte[] readAndPrepend(final InputStream content, final int size, final byte[] prefix)
            throws IOException {
        final int prefixLength = prefix.length;
        final byte[] joined = new byte[prefixLength + size];

        // using the `IOUtils` class guarantees that it will read as many bytes
        // as possible before giving up,
        // this may not always be the case for subclasses of InputStream
        // - eg. GZIPInputStream
        final int count = IOUtils.read(content, joined, prefixLength, joined.length - prefixLength);
        if (count < size) {
            final byte[] smaller = new byte[prefixLength + count];
            System.arraycopy(prefix, 0, smaller, 0, prefix.length);
            System.arraycopy(joined, prefixLength, smaller, prefixLength, count);
            return smaller;
        }

        System.arraycopy(prefix, 0, joined, 0, prefix.length);
        return joined;
    }

    static class Attribute {
        private final String name_;
        private final String value_;
        private final int updatedIndex_;

        Attribute(final String name, final String value, final int updatedIndex) {
            name_ = name;
            value_ = value;
            updatedIndex_ = updatedIndex;
        }

        String getName() {
            return name_;
        }

        String getValue() {
            return value_;
        }

        int getUpdatedIndex() {
            return updatedIndex_;
        }
    }
}
