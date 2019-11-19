/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Tests for (some) static utility methods.
 *
 * @author Maxym Mykhalchuk
 * @author Aaron Madlon-Kay
 */
public class StaticUtilsTest {

    @Test
    public void testParseCLICommand() {
        String cmd = " sort  \"/path with/spaces in/it\"    /path\\ with/escaped\\ spaces/"
                + " \"escape\\\"escape\" 'noescape\\'noescape'' \"noescape\\ noescape\""
                + " C:\\windows\\path";
        String[] args = StaticUtils.parseCLICommand(cmd);
        assertEquals("/path with/spaces in/it", args[1]);
        assertEquals("/path with/escaped spaces/", args[2]);
        assertEquals("escape\"escape", args[3]);
        assertEquals("noescape\\noescape", args[4]);
        assertEquals("noescape\\ noescape", args[5]);
        assertEquals("C:\\windows\\path", args[6]);
        assertEquals(args.length, 7);
        args = StaticUtils.parseCLICommand(" ");
        assertEquals(args[0], "");
        assertEquals(args.length, 1);
    }

    @Test
    public void testInstallDir() {
        File installDir = new File(StaticUtils.installDir());

        assertTrue(installDir.isDirectory());

        for (String dir : new String[] { "src", "docs", "lib" }) {
            assertTrue(new File(installDir, dir).isDirectory());
        }
    }

    @Test
    public void testGlobToRegex() {
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab?d", false), "abcd"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("ab?d", false), "abd"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abcccccd"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abd"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("ab*d", false), "abde"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("ab*", false), "abdefg"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("$a[b-c]!?*d{}", false), "$a[b-c]!?1234d{}"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a?", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a ?", false), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a* b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a* b", true), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a*b", false), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a*b", true), "a\u00A0b"));

        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", false), "a b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", true), "a b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a b", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a b", true), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a *", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a *", true), "a\u00A0b"));
        assertFalse(Pattern.matches(StaticUtils.globToRegex("a ?", false), "a\u00A0b"));
        assertTrue(Pattern.matches(StaticUtils.globToRegex("a ?", true), "a\u00A0b"));
    }
}
