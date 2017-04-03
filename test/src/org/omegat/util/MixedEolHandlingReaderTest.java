/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import junit.framework.TestCase;

public class MixedEolHandlingReaderTest extends TestCase {

    public void testDetection() throws Exception {
        String none = "a";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(none))) {
            assertEquals(System.lineSeparator(), reader.getDetectedEol());
            assertFalse(reader.hasMixedEol());
        }

        String lf = "a\nb\nc\n";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(lf))) {
            assertEquals("\n", reader.getDetectedEol());
            assertFalse(reader.hasMixedEol());
        }

        String cr = "a\rb\rc\r";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(cr))) {
            assertEquals("\r", reader.getDetectedEol());
            assertFalse(reader.hasMixedEol());
        }

        String crlf = "a\r\nb\r\nc\r\n";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(crlf))) {
            assertEquals("\r\n", reader.getDetectedEol());
            assertFalse(reader.hasMixedEol());
        }

        // Mostly \r\n
        String mixed1 = "a\r\r\nb\r\nc\r\n";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed1))) {
            assertEquals("\r\n", reader.getDetectedEol());
            assertTrue(reader.hasMixedEol());
        }

        // Mostly \r
        String mixed2 = "a\r\r\nb\rc\r";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed2))) {
            assertEquals("\r", reader.getDetectedEol());
            assertTrue(reader.hasMixedEol());
        }

        // Mostly \n
        String mixed3 = "a\n\r\nb\nc\n";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed3))) {
            assertEquals("\n", reader.getDetectedEol());
            assertTrue(reader.hasMixedEol());
        }

        // Equal numbers of each: \r\n wins
        String mixed4 = "a\r\r\nb\nc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed4))) {
            assertEquals("\r\n", reader.getDetectedEol());
            assertTrue(reader.hasMixedEol());
        }

        // No \r\n, \r and \n tied: system wins
        String mixed5 = "a\rb\nc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed5))) {
            assertEquals(System.lineSeparator(), reader.getDetectedEol());
            assertTrue(reader.hasMixedEol());
        }
    }

    public void testReadLine() throws Exception {
        String cr = "a\rb\rc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(cr))) {
            for (String line : cr.split("\r")) {
                assertEquals(line, reader.readLine());
            }
        }

        String lf = "a\nb\nc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(lf))) {
            for (String line : lf.split("\n")) {
                assertEquals(line, reader.readLine());
            }
        }

        String crlf = "a\r\nb\r\nc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(crlf))) {
            for (String line : crlf.split("\r\n")) {
                assertEquals(line, reader.readLine());
            }
        }

        String mixed = "a\r\r\nb\r\nc";
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(new StringReader(mixed))) {
            for (String line : mixed.split("\r\n")) {
                assertEquals(line, reader.readLine());
            }
        }
    }

    public void testFile() throws Exception {
        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(
                Files.newBufferedReader(Paths.get("test/data/filters/text/file-TextFilter.txt"),
                        StandardCharsets.UTF_8))) {
            assertEquals("This test file for test TextFilter.", reader.readLine());
            assertEquals("", reader.readLine());
            assertEquals("It should be second entry for empty-line separator or third line for line-break separator.",
                    reader.readLine());
            assertEquals("", reader.readLine());
            assertEquals("It should be third entry for empty-line separator or fifth line for line-break separator.",
                    reader.readLine());
            assertNull(reader.readLine());
            assertFalse(reader.hasMixedEol());
            assertEquals("\r\n", reader.getDetectedEol());
        }
    }
}
