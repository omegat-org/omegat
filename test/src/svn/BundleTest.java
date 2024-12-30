/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2023 Hiroshi Miura
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

package svn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

import org.junit.Test;

import org.omegat.util.CommonVerifications;
import org.omegat.util.OStrings;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class BundleTest extends CommonVerifications {

    /**
     * Ensure that all UI string bundles have either US-ASCII encoding or
     * ISO-8859-1 encoding. The spec requires the latter, but ISO-8859-1 is a
     * superset of ASCII, so ASCII is also acceptable (and is widely used in
     * practice).
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/util/PropertyResourceBundle.html">PropertyResourceBundle</a>
     */
    @Test
    public void testBundleEncodings() throws Exception {
        assertBundle("Bundle");
    }

    @Test
    public void testBundleLoading() {
        assertBundleLoading("org/omegat/Bundle");
    }

    @Test
    public void testVersionPropsLoading() {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Version");
        bundle.getString("version");
        bundle.getString("update");
        bundle.getString("revision");
    }

    @Test
    public void testLoggerPropsLoading() {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/logger");
        assertTrue(bundle.getKeys().hasMoreElements());
    }

    @Test
    public void testShortcutPropsLoading() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/gui/main/MainMenuShortcuts");
        assertTrue(bundle.getKeys().hasMoreElements());

        // ResourceBundle.getBundle won't resolve the Mac-specific file's name
        // so we have to load it manually.
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/org/omegat/gui/main/MainMenuShortcuts.mac.properties"));
        assertFalse(props.isEmpty());
    }

    /**
     * Search for UI strings used via OStrings.getString() but not defined in
     * Bundle.properties.
     * <p>
     * This is a brute-force text search over all .java source files and will
     * not catch dynamically computed keys or exotic invocations (such as via
     * method references, e.g. OStrings::getString, etc.).
     * <p>
     * More thorough checks would be possible by actually running the relevant
     * code, but a lot of it requires a GUI environment (our CI is headless, so
     * such tests will rarely get run), or a substantial testing harness, or
     * requires people to manually add tests (which they just won't do).
     *
     * @throws Exception
     */
    @Test
    public void testUndefinedStrings() throws Exception {
        assertBundleHasAllKeys(new String[] { "src", "test", "test-integration", "tipoftheday" },
                OStrings.getResourceBundle());
    }

    /**
     * Test the behavior when a resource key is missing. Various code assumes
     * that we throw a MissingResourceException, not e.g. return null or the
     * empty string.
     */
    @Test(expected = MissingResourceException.class)
    public void testUndefinedString() {
        Locale.setDefault(Locale.ENGLISH);
        // Not sure why we'd ever have a UUID key, but just in case, keep trying
        // new UUIDs until we hit one that's missing (or we give up at
        // Integer.MAX_VALUE and fail).
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            OStrings.getString(UUID.randomUUID().toString());
        }
    }

    @Test(expected = Exception.class)
    public void testDetectRTLO() throws Exception {
        String badChars = "photo_high_re\u202Egnp.js";
        Path p = Files.createTempFile("omegat", ".txt");
        Files.write(p, Collections.singletonList(badChars), StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            checkFileContent(p, decoder, (path, chars) -> {
            });
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

}
