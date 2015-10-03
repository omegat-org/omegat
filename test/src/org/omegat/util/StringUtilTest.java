/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for (some) static utility methods.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StringUtilTest extends TestCase {
    public void testIsSubstringAfter() {
        assertFalse(StringUtil.isSubstringAfter("123456", 5, "67"));
        assertTrue(StringUtil.isSubstringAfter("123456", 5, "6"));
        assertTrue(StringUtil.isSubstringAfter("123456", 4, "56"));
        assertTrue(StringUtil.isSubstringAfter("123456", 0, "12"));
        assertTrue(StringUtil.isSubstringAfter("123456", 1, "23"));
    }

    public void testIsTitleCase() {
        assertFalse(StringUtil.isTitleCase("foobar"));
        assertFalse(StringUtil.isTitleCase("fooBar"));
        assertFalse(StringUtil.isTitleCase("f1obar"));
        assertFalse(StringUtil.isTitleCase("FooBar"));
        assertTrue(StringUtil.isTitleCase("Fo1bar"));
        assertTrue(StringUtil.isTitleCase("Foobar"));
        // LATIN CAPITAL LETTER L WITH SMALL LETTER J (U+01C8)
        assertTrue(StringUtil.isTitleCase("\u01C8bcd"));
        assertFalse(StringUtil.isTitleCase("a\u01C8bcd"));
        
        // LATIN CAPITAL LETTER L WITH SMALL LETTER J (U+01C8)
        assertTrue(StringUtil.isTitleCase("\u01c8"));
        // LATIN CAPITAL LETTER LJ (U+01C7)
        assertFalse(StringUtil.isTitleCase("\u01c7"));
        // LATIN SMALL LETTER LJ (U+01C9)
        assertFalse(StringUtil.isTitleCase("\u01c9"));
    }
    
    public void testIsSubstringBefore() {
        assertFalse(StringUtil.isSubstringBefore("123456", 1, "01"));
        assertTrue(StringUtil.isSubstringBefore("123456", 1, "1"));
        assertTrue(StringUtil.isSubstringBefore("123456", 2, "12"));
        assertTrue(StringUtil.isSubstringBefore("123456", 6, "56"));
        assertTrue(StringUtil.isSubstringBefore("123456", 5, "45"));
    }
    
    public void testUnicodeNonBMP() {
        // MATHEMATICAL BOLD CAPITAL A (U+1D400)
        String test = "\uD835\uDC00";
        assertTrue(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertTrue(StringUtil.isTitleCase(test));
        
        // MATHEMATICAL BOLD CAPITAL A (U+1D400) x2
        test = "\uD835\uDC00\uD835\uDC00";
        assertTrue(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        
        // MATHEMATICAL BOLD SMALL A (U+1D41A)
        test = "\uD835\uDC1A";
        assertFalse(StringUtil.isUpperCase(test));
        assertTrue(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        
        // MATHEMATICAL BOLD CAPITAL A + MATHEMATICAL BOLD SMALL A
        test = "\uD835\uDC00\uD835\uDC1A";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertTrue(StringUtil.isTitleCase(test));
        
        // MATHEMATICAL BOLD SMALL A + MATHEMATICAL BOLD CAPITAL A
        test = "\uD835\uDC1A\uD835\uDC00";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
    }
    
    public void testEmptyStringCase() {
        String test = null;
        try {
            assertFalse(StringUtil.isUpperCase(test));
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            assertFalse(StringUtil.isLowerCase(test));
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            assertFalse(StringUtil.isTitleCase(test));
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            StringUtil.toTitleCase(test, Locale.ENGLISH);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        
        test = "";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertEquals("", StringUtil.toTitleCase("", Locale.ENGLISH));
    }
    
    public void testIsWhiteSpace() {
        try {
            assertFalse(StringUtil.isWhiteSpace(null));
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        assertFalse(StringUtil.isWhiteSpace(""));
        assertTrue(StringUtil.isWhiteSpace(" "));
        assertFalse(StringUtil.isWhiteSpace(" a "));
        // SPACE (U+0020) + IDEOGRAPHIC SPACE (U+3000)
        assertTrue(StringUtil.isWhiteSpace(" \u3000"));
        // We consider whitespace but Character.isWhiteSpace(int) doesn't:
        // NO-BREAK SPACE (U+00A0) + FIGURE SPACE (U+2007) + NARROW NO-BREAK SPACE (U+202F)
        assertTrue(StringUtil.isWhiteSpace("\u00a0\u2007\u202f"));
    }
    
    public void testIsMixedCase() {
        assertTrue(StringUtil.isMixedCase("ABc"));
        assertTrue(StringUtil.isMixedCase("aBc"));
        // This is title case, not mixed:
        assertFalse(StringUtil.isMixedCase("Abc"));
        // Non-letter characters should not affect the result:
        assertTrue(StringUtil.isMixedCase(" {ABc"));
    }
    
    public void testNonWordCase() {
        String test = "{";
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertFalse(StringUtil.isMixedCase(test));
    }
    
    public void testToTitleCase() {
        Locale locale = Locale.ENGLISH;
        assertEquals("Abc", StringUtil.toTitleCase("abc", locale));
        assertEquals("Abc", StringUtil.toTitleCase("ABC", locale));
        assertEquals("Abc", StringUtil.toTitleCase("Abc", locale));
        assertEquals("Abc", StringUtil.toTitleCase("abc", locale));
        assertEquals("A", StringUtil.toTitleCase("a", locale));
        // LATIN SMALL LETTER NJ (U+01CC) -> LATIN CAPITAL LETTER N WITH SMALL LETTER J (U+01CB)
        assertEquals("\u01CB", StringUtil.toTitleCase("\u01CC", locale));
        // LATIN SMALL LETTER I (U+0069) -> LATIN CAPITAL LETTER I WITH DOT ABOVE (U+0130) in Turkish
        assertEquals("\u0130jk", StringUtil.toTitleCase("ijk", new Locale("tr")));
    }
    
    public void testCompressSpace() {
        assertEquals("One Two Three Four Five", StringUtil.compressSpaces(" One Two\nThree   Four\r\nFive "));
        assertEquals("Six seven", StringUtil.compressSpaces("Six\tseven"));
    }
}
