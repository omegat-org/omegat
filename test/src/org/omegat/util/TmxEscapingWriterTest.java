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

    @Test
    public void escapeBasic() throws IOException {
        writer.write("Hello world!\n");
        writer.flush();
        assertThat(outputStream.toString()).isEqualTo("Hello world!\n");
    }

    @Test
    public void escapeToEntities() throws IOException {
        writer.write("\"<escape>\"");
        writer.flush();
        assertThat(outputStream.toString()).isEqualTo("\"&lt;escape&gt;\"");
    }

    @Test
    public void testSurrogatePair() throws IOException {
        writer.write("\uD83D\uDE00");
        writer.flush();
        assertThat(outputStream.toString()).isEqualTo("\uD83D\uDE00");
    }

    @Test
    public void testInvalidChar() throws IOException {
        writer.write((char) 0xFFFE);
        writer.flush();
        assertThat(outputStream.toString()).isEqualToIgnoringCase("&#xfffe;");
    }
}
