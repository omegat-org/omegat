/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2024 Hiroshi Miura
 *          Home page: https://www.omegat.org/
 *          Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

public class TmxEscapingWriterTest {

    private OutputStream outputStream;
    private Writer writer;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        outputStream = new ByteArrayOutputStream();
        var factory = new TmxEscapingWriterFactory();
        writer = factory.createEscapingWriterFor(outputStream, null);
    }

    /**
     * Test basic characters, as-is.
     *
     * @throws IOException I/O error happened.
     */
    @Test
    public void escapeBasic() throws IOException {
        writer.write("Hello world!\n");
        writer.flush();
        assertThat(outputStream.toString()).isEqualTo("Hello world!\n");
    }

    /**
     * Test signs to be escaped.
     * @throws IOException I/O error happened.
     */
    @Test
    public void escapeToEntities() throws IOException {
        writer.write("'<escape>\"");
        writer.flush();
        assertThat(outputStream.toString())
                .as("Check escape of < & and > signs and as-is for single/double quote")
                .isEqualTo("'&lt;escape&gt;\"");
    }

    /**
     * Test escape of NBSP, no-escape.
     * @throws IOException I/O error happened.
     */
    @Test
    public void testNBSP() throws IOException {
        writer.write("[\u00A0]");
        writer.flush();
        assertThat(outputStream.toString())
                .as("Check NBSP is not escaped.")
                .isEqualTo("[\u00A0]");
    }

    /**
     * Test Control character No-Break-Here, escape.
     *
     * @throws IOException I/O error happened.
     */
    @Test
    public void testNBH() throws IOException {
        writer.write("\u0083");
        writer.flush();
        assertThat(outputStream.toString())
                .as("Check NO_BREAK_HERE control character to be escaped")
                .isEqualTo("&#x83;");
    }

    /**
     * Test emoji and flag, surrogate pair, escape.
     * @throws IOException I/O error happened.
     */
    @Test
    public void testSurrogatePair() throws IOException {
        writer.write("[😀]");
        writer.flush();
        assertThat(outputStream.toString())
                .as("Check emoji and flag that requires surrogate pair for encode.")
                .isEqualTo("[😀]");
    }

    /**
     * Test Invalid character, BOM flag, escape.
     * @throws IOException I/O error happened.
     */
    @Test
    public void testInvalidChar() throws IOException {
        writer.write((char) 0xFFFE);
        writer.flush();
        assertThat(outputStream.toString())
                .as("check BOM mark to be escaped when appeared in TEXT.")
                .isEqualToIgnoringCase("&#xfffe;");
    }
}
