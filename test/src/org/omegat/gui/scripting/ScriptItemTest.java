/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.scripting;

import org.junit.Test;
import org.mockito.Mockito;
import org.omegat.util.LinebreakPreservingReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.omegat.gui.scripting.ScriptItem.SCAN_PATTERN;

public class ScriptItemTest {

    /**
     * Test for getText() method when the script source is provided directly.
     */
    @Test
    public void testGetTextWithScriptSource() throws IOException {
        // Arrange
        String scriptSource = "print('Hello, world!')";
        ScriptItem scriptItem = new ScriptItem(scriptSource);

        // Act
        String result = scriptItem.getText();

        // Assert
        assertEquals(scriptSource, result);
    }

    /**
     * Test for scanFileForDescription method with valid content.
     */
    @Test
    public void testScanFileForDescriptionWithValidContent() throws IOException {
        // Arrange
        File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mockFile.getParentFile()).thenReturn(new File("/foo/"));

        Scanner mockedScanner = Mockito.mock(Scanner.class);
        String fileContent = ":name = Test Script :description = This is a test script";
        Pattern pattern = Pattern.compile(SCAN_PATTERN);
        Matcher m = pattern.matcher(fileContent);
        assertTrue(m.find());

        Mockito.when(mockedScanner.findInLine(SCAN_PATTERN)).thenReturn("");
        Mockito.when(mockedScanner.match()).thenReturn(m);

        ScriptItem scriptItem = new ScriptItem(mockFile) {
            @Override
            void initResourceBundle(File mockFile) {
            }

            @Override
            Scanner getScanner(File file) {
                return mockedScanner;
            }
        };

        // Act
        scriptItem.scanFileForDescription(mockFile);

        // Assert
        assertEquals("Test Script", scriptItem.getScriptName());
        assertEquals("This is a test script", scriptItem.getDescription());
    }

    /**
     * Test for scanFileForDescription method with invalid content.
     */
    @Test
    public void testScanFileForDescriptionWithInvalidContent() throws IOException {
        // Arrange
        File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mockFile.getParentFile()).thenReturn(new File("/foo/"));

        Scanner mockedScanner = Mockito.mock(Scanner.class);
        String fileContent = "some random content without metadata";
        Pattern pattern = Pattern.compile(SCAN_PATTERN);
        Matcher m = pattern.matcher(fileContent);
        assertFalse(m.find());

        Mockito.when(mockedScanner.findInLine(SCAN_PATTERN)).thenReturn("");
        Mockito.when(mockedScanner.match()).thenReturn(m);

        ScriptItem scriptItem = new ScriptItem(mockFile) {
            @Override
            void initResourceBundle(File mockFile) {
            }

            @Override
            Scanner getScanner(File file) {
                return mockedScanner;
            }
        };

        // Act
        scriptItem.scanFileForDescription(mockFile);

        // Assert
        assertEquals(null, scriptItem.getScriptName());
        assertEquals("", scriptItem.getDescription());
    }

    /**
     * Test for getText() method when the script source is in a valid file.
     */
    @Test
    public void testGetTextWithValidFile() throws IOException {
        // Arrange
        File mockFile = Mockito.mock(File.class);
        String expectedContent = "print('Hello from file!')";

        Mockito.when(mockFile.getName()).thenReturn("testScript.txt");
        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mockFile.getPath()).thenReturn("testScript.txt");
        Mockito.when(mockFile.getParentFile()).thenReturn(new File("/foo/"));

        LinebreakPreservingReader mockedReader = Mockito.mock(LinebreakPreservingReader.class);
        Mockito.when(mockedReader.readLine())
                .thenReturn(expectedContent)
                .thenReturn(null);
        Mockito.when(mockedReader.getLinebreak()).thenReturn("");

        ScriptItem scriptItem = new ScriptItem(mockFile) {
            @Override
            LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) {
                return mockedReader;
            }
        };

        // Act
        String result = scriptItem.getText();

        // Assert
        assertEquals(expectedContent, result);
    }

    /**
     * Test for getText() method when the file does not exist.
     */
    @Test
    public void testGetTextWithNonexistentFile() {
        // Arrange
        File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.exists()).thenReturn(false);
        Mockito.when(mockFile.getParentFile()).thenReturn(new File("/foo/"));

        ScriptItem scriptItem = new ScriptItem(mockFile) {
            @Override
            void initResourceBundle(File mockFile) {
            }
        };

        // Act & Assert
        assertThrows(FileNotFoundException.class, scriptItem::getText);
    }

    /**
     * Test for getText() method when the file cannot be read due to an IO exception.
     */
    @Test
    public void testGetTextWithIOException() {
        // Arrange
        File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.getParentFile()).thenReturn(new File("/foo/"));

        ScriptItem scriptItem = new ScriptItem(mockFile) {
            @Override
            void initResourceBundle(File mockFile) {
            }

            @Override
            LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) throws IOException {
                throw new IOException("Simulated IOException");
            }
        };

        // Act & Assert
        assertThrows(IOException.class, scriptItem::getText);
    }
}
