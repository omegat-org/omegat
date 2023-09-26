/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2015 Aaron Madlon-Kay
 *                2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util;

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
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.omegat.Main;

public class CommonVerifications {

    /**
     * Test method to ensure that all resource bundles have either US-ASCII
     * encoding or ISO-8859-1 encoding. The spec requires the latter,
     * but ISO-8859-1 is a superset of ASCII, so ASCII is also acceptable
     * (and is widely used in practice).
     */
    protected void assertBundle(String basename) throws IOException {
        // Test English bundle separately as its name corresponds to the
        // empty locale, and will not be resolved otherwise.
        assertEncoding(basename + ".properties");
        for (Language lang : Language.getLanguages()) {
            String bundle = basename + "_" + lang.getLocaleCode() + ".properties";
            assertEncoding(bundle);
        }
    }

    protected void assertEncoding(String bundle) throws IOException {
        try (InputStream stream = Main.class.getResourceAsStream(bundle)) {
            if (stream == null) {
                return;
            }
            String encoding = EncodingDetector.detectEncoding(stream);
            System.out.println(bundle + ": " + encoding);
            // The detector will give Windows-1252 for ISO-8859-1; yes, this is
            // not technically correct, but it's close enough. See:
            // http://www.i18nqa.com/debug/table-iso8859-1-vs-windows-1252.html
            assertTrue("US-ASCII".equals(encoding) || "WINDOWS-1252".equals(encoding));
        }
    }

    protected void assertBundleLoading(String basename) {
        // We must set the default locale to English first because we provide
        // our English bundle as the empty-locale default. If we don't do so,
        // the English bundle will never be tested in the case that the
        // "default" is a language we provide a bundle for.
        Locale.setDefault(Locale.ENGLISH);

        for (Language lang : Language.getLanguages()) {
            ResourceBundle bundle = ResourceBundle.getBundle(basename, lang.getLocale());
            assertTrue(bundle.getKeys().hasMoreElements());
        }
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
    protected void assertBundleHasAllKeys(String[] targets, ResourceBundle bundle) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        Pattern pattern = Pattern.compile("(OStrings|BUNDLE)\\.getString\\(\\s*\"([^\"]+)\"\\s*[,\\)]");
        processSourceContent(targets, (path, chars) -> {
            Matcher m = pattern.matcher(chars);
            while (m.find()) {
                if (m.group(1).equals("OStrings")) {
                    OStrings.getString(m.group(2));
                } else {
                    bundle.getString(m.group(2));
                }
            }
        });
    }

    /**
     * Process the text content of all .java files under /src. Also check
     * DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE for security reasons.
     * And check invisible space character.
     *
     * @param consumer
     *            A function that accepts the file path and content
     * @throws IOException
     *             from Files.find()
     */
    public static void processSourceContent(String[] targets, BiConsumer<Path, CharSequence> consumer) throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        for (String root : targets) {
            Path rootPath = Paths.get(".", root);
            assertTrue(rootPath.toFile().isDirectory());
            try (Stream<Path> files = Files.find(rootPath, 100,
                    (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))) {
                files.forEach(p -> {
                    try {
                        checkFileContent(p, decoder, consumer);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    public static void checkFileContent(Path p, CharsetDecoder decoder,
                                        BiConsumer<Path, CharSequence> consumer) throws Exception {
        try {
            byte[] bytes = Files.readAllBytes(p);
            CharBuffer chars = decoder.decode(ByteBuffer.wrap(bytes));
            for (int i = 0; i < chars.limit(); i++) {
                int c = chars.charAt(i);
                if (c == 0x202e) {
                    // found Right-to-left-override: unicode 202e
                    throw new Exception("File contains Right-to-Left-Override (RLTO) character: " + p);
                }
                if (c == 0x0009 || c == 0x00a0 || c == 0x00ad || c == 0x034f || c == 0x061c
                        || c >= 0x2000 && c < 0x200b || c == 0x2028 || c >= 0x205f && c <= 0x206f
                        || c == 0x2800 || c == 0x3000 || c == 0x3164) {
                    throw new Exception("File contains invisible character: " + p);
                }
            }
            chars.clear();
            consumer.accept(p, chars);
        } catch (MalformedInputException ex) {
            throw new Exception("File contains a bad character sequence for UTF-8: " + p, ex);
        } catch (IOException ex) {
            throw new Exception(p.toString(), ex);
        }
    }
}
