package org.omegat.filters2.text;

import org.junit.Before;
import org.junit.Test;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LineLengthLimitWriterTest {

    @Test
    public void testIsSpaces() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bufferedWriter, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Insert a test string into the writer's internal buffer
            writer.str.append("  abc   def\n");

            // Define test tokens
            Token spacesToken = new Token("  ", 0, 2);
            Token mixedToken = new Token("abc ", 2, 4);
            Token nonSpacesToken = new Token("def", 8, 3);

            // Assert behavior of isSpaces
            assertTrue(writer.isSpaces(spacesToken)); // All spaces
            assertFalse(writer.isSpaces(mixedToken)); // Contains both spaces and non-spaces
            assertFalse(writer.isSpaces(nonSpacesToken)); // Non-spaces only
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testIsPossibleBreakBefore() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bufferedWriter, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Insert a test string into the writer's internal buffer
            writer.str.append("Example:Test,Special«A");

            // Test cases for breaking position
            assertTrue(writer.isPossibleBreakBefore(3)); // normal alphabet
            assertFalse(writer.isPossibleBreakBefore(7)); // Colon ":"
            assertFalse(writer.isPossibleBreakBefore(12)); // Comma ","
            assertFalse(writer.isPossibleBreakBefore(21)); // Double arrow
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File outFile;
    private ITokenizer tokenizer;
    private static final int MAX_LENGTH = 100;
    private static final int LINE_LENGTH = 80;

    @Before
    public void setup() throws IOException {
        File tmpDir = Files.createTempDirectory("omegat").toFile();
        outFile = new File(tmpDir, "out");
        tokenizer = new DefaultTokenizer();
    }

    @Test
    public void testWrite() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             Writer output = new LineLengthLimitWriter(bufferedWriter, LINE_LENGTH, MAX_LENGTH, tokenizer)) {
            for (int i = 0; i < 1000; i++) {
                output.write("The «quick brown» fox jumps over the lazy dog");
            }
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (bufferedReader.ready()) { // Not the last line
                    // When last character is white space, it will be removed.
                    assertTrue(line.length() >= LINE_LENGTH - 1 && line.length() <= MAX_LENGTH);
                } else { // Last line can be less than LINE_LENGTH
                    assertTrue(line.length() <= MAX_LENGTH);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOutLine() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bufferedWriter, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Add test string to the buffer
            writer.str.append("This is a test line of text");

            // Call outLine and verify the buffer writes the line correctly
            writer.outLine();

            // Assert that the buffer is now empty
            assertEquals(0, writer.str.length());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Check the written file contents
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
            String line = reader.readLine();
            assertEquals("This is a test line of text", line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOutLineWithEmptyBuffer() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Ensure the buffer is empty
            writer.str.setLength(0);

            // Call outLine
            writer.outLine();

            // Assert no content was added to the output file
            assertEquals(0, outFile.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOutLineWithEOLCharacters() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Add a string containing EOL characters
            writer.str.append("Line with EOL\n");

            // Call outLine
            writer.outLine();

            // Assert that the buffer is cleared
            assertEquals(0, writer.str.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Check the file contents for proper line break
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
            String line = reader.readLine();
            assertEquals("Line with EOL", line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetBreakPosSimpleCase() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Set buffer to a simple string needing a break point
            writer.str.append(
                    "This is a very very simple test case for getBreakPos with enough length to require a break");

            // Tokenize the buffer
            Token[] tokens = tokenizer.tokenizeVerbatim(writer.str.toString());

            // Assert that break occurs at or near line length
            int breakPos = writer.getBreakPos(tokens);
            assertTrue(breakPos >= LINE_LENGTH - 10 && breakPos <= LINE_LENGTH + 10);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetBreakPosHandlesSpaces() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Set buffer to a string with spaces near the line length
            writer.str.append(
            "This is a test with spaces in strategic locations with enough length to require a break to be inserted.");

            // Tokenize the buffer
            Token[] tokens = tokenizer.tokenizeVerbatim(writer.str.toString());

            // Assert that break position falls on a space for optimal breaking
            int breakPos = writer.getBreakPos(tokens);
            int c = writer.str.charAt(breakPos - 1);
            assertTrue(Character.isWhitespace(c));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetBreakPosNoBreakPossible() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Set buffer to a single long word exceeding line limits
            writer.str.append("Supercalifragilisticexpialidocious");

            // Tokenize the buffer
            Token[] tokens = tokenizer.tokenizeVerbatim(writer.str.toString());

            // Assert that no break is possible and the full word is returned
            int breakPos = writer.getBreakPos(tokens);
            assertEquals(writer.str.length(), breakPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetBreakPosBeyondMaxLength() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
             LineLengthLimitWriter writer = new LineLengthLimitWriter(bw, LINE_LENGTH, MAX_LENGTH, tokenizer)) {

            // Set buffer to a very long string
            writer.str.append("This line contains more characters than allowed by max length restrictions");

            // Tokenize the buffer
            Token[] tokens = tokenizer.tokenizeVerbatim(writer.str.toString());

            // Assert that a break occurs before or at max line length
            int breakPos = writer.getBreakPos(tokens);
            assertTrue(breakPos <= MAX_LENGTH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
