package org.omegat.tokenizer;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;
import org.mockito.Mockito;
import org.omegat.util.Token;

import static org.junit.Assert.assertEquals;

public class BaseTokenizerTest {

    /**
     * A mock implementation of BaseTokenizer to test the tokenizeVerbatim method.
     */
    private class MockTokenizer extends BaseTokenizer {
        @Override
        protected TokenStream getTokenStream(String strOrig, boolean stemsAllowed, boolean stopWordsAllowed) {
            return null; // Unused in tokenizeVerbatim tests
        }
    }

    /**
     * Tests tokenizeVerbatim with a normal string containing multiple words and spaces.
     */
    @Test
    public void testTokenizeVerbatimWithMultipleWords() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "Hello, world! This is a test.";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(9, result.length);
        assertEquals("Hello", result[0].getTextFromString(input));
        assertEquals(",", result[1].getTextFromString(input));
        assertEquals("world", result[2].getTextFromString(input));
        assertEquals("!", result[3].getTextFromString(input));
        assertEquals("This", result[4].getTextFromString(input));
        assertEquals("is", result[5].getTextFromString(input));
        assertEquals("a", result[6].getTextFromString(input));
        assertEquals("test", result[7].getTextFromString(input));
        assertEquals(".", result[8].getTextFromString(input));
    }

    /**
     * Tests tokenizeVerbatim with an empty string.
     */
    @Test
    public void testTokenizeVerbatimWithEmptyString() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(0, result.length);
    }

    /**
     * Tests tokenizeVerbatim with a string containing only whitespace.
     */
    @Test
    public void testTokenizeVerbatimWithWhitespace() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "     ";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(0, result.length);
    }

    /**
     * Tests tokenizeVerbatim with a string containing special characters.
     */
    @Test
    public void testTokenizeVerbatimWithSpecialCharacters() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "!@#$%^&*()-_=+[]{}|;:',.<>?";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(27, result.length);
        for (int i = 0; i < 27; i++) {
            assertEquals(String.valueOf(input.charAt(i)), result[i].getTextFromString(input));
        }
    }

    /**
     * Tests tokenizeVerbatim with a string containing mixed alphanumeric characters.
     */
    @Test
    public void testTokenizeVerbatimWithMixedAlphanumeric() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "abc123 def456 ghi789";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(3, result.length);
        assertEquals("abc123", result[0].getTextFromString(input));
        assertEquals("def456", result[1].getTextFromString(input));
        assertEquals("ghi789", result[2].getTextFromString(input));
    }

    /**
     * Tests tokenizeVerbatim with a string containing Unicode characters.
     */
    @Test
    public void testTokenizeVerbatimWithUnicode() {
        MockTokenizer tokenizer = new MockTokenizer();
        String input = "こんにちは 世界 🌏";
        Token[] result = tokenizer.tokenizeVerbatim(input);

        assertEquals(3, result.length);
        assertEquals("こんにちは", result[0].getTextFromString(input));
        assertEquals("世界", result[1].getTextFromString(input));
        assertEquals("🌏", result[2].getTextFromString(input));
    }


}