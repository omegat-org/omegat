/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package svn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.omegat.Main;
import org.omegat.util.EncodingDetector;
import org.omegat.util.Language;
import org.omegat.util.OStrings;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class BundleTest {

    /**
     * Ensure that all UI string bundles have either US-ASCII encoding or ISO-8859-1 encoding. The spec requires the
     * latter, but ISO-8859-1 is a superset of ASCII so ASCII is also acceptable (and is widely used in practice).
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/util/PropertyResourceBundle.html">PropertyResourceBundle</a>
     */
    @Test
    public void testBundleEncodings() throws Exception {
        // Test English bundle separately as its name corresponds to the
        // empty locale, and will not be resolved otherwise.
        assertEncoding("Bundle.properties");
        for (Language lang : Language.getLanguages()) {
            String bundle = "Bundle_" + lang.getLocaleCode() + ".properties";
            assertEncoding(bundle);
        }
    }

    private void assertEncoding(String bundle) throws IOException {
        try (InputStream stream = Main.class.getResourceAsStream(bundle)) {
            if (stream == null) {
                return;
            }
            String encoding = EncodingDetector.detectEncoding(stream);
            System.out.println(bundle + ": " + encoding);
            // The detector will give null for ASCII and Windows-1252 for ISO-8859-1;
            // yes, this is not technically correct, but it's close enough. See:
            // http://www.i18nqa.com/debug/table-iso8859-1-vs-windows-1252.html
            assertTrue(encoding == null || "WINDOWS-1252".equals(encoding));
        }
    }

    @Test
    public void testBundleLoading() {
        // We must set the default locale to English first because we provide our
        // English bundle as the empty-locale default. If we don't do so, the
        // English bundle will never be tested in the case that the "default default"
        // is a language we provide a bundle for.
        Locale.setDefault(Locale.ENGLISH);

        for (Language lang : Language.getLanguages()) {
            ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Bundle", lang.getLocale());
            assertTrue(bundle.getKeys().hasMoreElements());
        }
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
        Locale.setDefault(Locale.ENGLISH);
        Pattern pattern = Pattern.compile("OStrings\\.getString\\(\\s*\"([^\"]+)\"\\s*[,\\)]");
        processSourceContent((path, chars) -> {
            Matcher m = pattern.matcher(chars);
            while (m.find()) {
                OStrings.getString(m.group(1));
            }
        });
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

    /**
     * Process the text content of all .java files under /src. Will blow up if
     * any are not US-ASCII.
     *
     * @param consumer
     *            A function that accepts the file path and content
     * @throws IOException
     *             from Files.find()
     */
    public static void processSourceContent(BiConsumer<Path, CharSequence> consumer) throws IOException {
        CharsetDecoder decoder = StandardCharsets.US_ASCII.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        for (String root : new String[] { "src", "test", "test-integration" }) {
            Path rootPath = Paths.get(".", root);
            assertTrue(rootPath.toFile().isDirectory());
            Files.find(rootPath, 100,
                    (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java")).forEach(p -> {
                        try {
                            byte[] bytes = Files.readAllBytes(p);
                            CharBuffer chars = decoder.decode(ByteBuffer.wrap(bytes));
                            consumer.accept(p, chars);
                        } catch (MalformedInputException ex) {
                            throw new RuntimeException("File contains non-ASCII characters: " + p, ex);
                        } catch (IOException ex) {
                            throw new RuntimeException(p.toString(), ex);
                        }
                    });
        }
    }
}
