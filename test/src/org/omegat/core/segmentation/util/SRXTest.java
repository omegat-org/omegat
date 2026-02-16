/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2024 Hiroshi Miura
               2025 Thomas Cordonnier
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

package org.omegat.core.segmentation.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;
import org.omegat.util.LocaleRule;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
@RunWith(Enclosed.class)
public final class SRXTest {

    private static final File SEGMENT_DEFAULT = new File("test/data/segmentation/default/");
    private static final String SEGMENT_CONF_BASE = "test/data/segmentation/migrate/";

    public static boolean checkRules(List<MapRule> mapRuleList, String pattern, String language) {
        return mapRuleList.stream()
                .filter(mapRule -> Objects.equals(language, mapRule.getLanguage()))
                .map(mapRule -> mapRule.getCompiledPattern().matcher(pattern).matches())
                .findFirst()
                .orElse(false);
    }

    public static class DefultSRXTest {

        @Test
        public void testSrxComparison() {
            SRX orig = SRX.getDefault();
            SRX clone = orig.copy();
            assertNotSame(orig, clone);
            assertEquals(orig, clone);
            assertEquals(orig.hashCode(), clone.hashCode());

            // Shallow change
            clone.setIncludeEndingTags(!clone.isIncludeEndingTags());
            assertNotEquals(orig, clone);

            // Deep change
            clone = orig.copy();
            org.omegat.core.segmentation.Rule rule = clone.getMappingRules().get(0).getRules().get(0);
            rule.setAfterbreak(rule.getAfterbreak() + "foo");
            assertNotEquals(orig, clone);
        }

        /**
         * Test SRX#loadFromDir produce SRX object properly.
         * <p>
         * MapRule#getLanguageCode should return Language Code defined in
         * LanguageCode class. MapRule#getLanguage should return a localized
         * name of language. The test here check both values. OmegaT 6.0 and
         * before,
         */
        @Test
        public void testSrxReaderDefault() {
            assertTrue(SEGMENT_DEFAULT.exists());
            SRX srx = SRXUtils.loadFromDir(SEGMENT_DEFAULT);
            assertNotNull(srx);
            assertTrue(srx.isCascade());
            List<MapRule> mapRuleList = srx.getMappingRules();
            assertNotNull(mapRuleList);
            assertEquals(18, mapRuleList.size());
            assertTrue(SRXTest.checkRules(mapRuleList, "JA.*", LanguageCodes.JAPANESE_CODE));
            assertEquals("2.0", srx.getVersion());
            assertTrue(srx.isSegmentSubflows());
        }
    }

    public static class SRXMigrateTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigrationBasic() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_en", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SRXTest.testSrxMigration(segmentConf, configDir,  "JA", LanguageCodes.JAPANESE_CODE);
        }
    }

    public static class SRXMigrateJaTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("ja"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigrationJa() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_ja", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SRXTest.testSrxMigration(segmentConf, configDir, "PL", LanguageCodes.POLISH_CODE);
        }
    }

    public static class SRXMigrateOldDeTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("de"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigrationOldDe() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_de_54", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SRXTest.testSrxMigration(segmentConf, configDir, "JA", LanguageCodes.JAPANESE_CODE);
        }
    }
    
    /** Check compatibilty with a conf file which is not at all based on standard OmegaT rules **/
    public static class SRXMigrateExtDeTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("de"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testSrxMigrationExtDe() throws Exception {
            File segmentConf = Paths.get(SEGMENT_CONF_BASE, "ext", "segmentation.conf").toFile();
            File configDir = folder.newFolder();
            SRXTest.testSrxMigration(segmentConf, configDir, "NB", "NB");
        }
    }

    /**
     * Test SRX writer/reader.
     * <p>
     * Previous versions have a bug when saving segmentation.conf file. It is
     * better to save language property using language code defined in
     * LanguageCode class. Unfortunately, OmegaT 6.0 and before produce a
     * localized language name for the property. The test case here trys reading
     * a segmentation.conf file that is produced by OmegaT in English
     * environment and Japanese environment.
     */
    public static void testSrxMigration(File segmentConf, File configDir, String pattern, String lang) throws Exception {
        // ensures the full test runs in temp directory
        Files.copy(segmentConf.toPath(), Paths.get(configDir.getAbsolutePath(), segmentConf.getName()));
        segmentConf = new File(configDir, segmentConf.getName());
        // load from conf file
        SRX srxOrig = SRXUtils.loadConfFile(segmentConf, configDir);
        assertNotNull(srxOrig);
        List<MapRule> mapRuleList = srxOrig.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(17, mapRuleList.size()); // samples have 17 rules, while default had 18
        assertTrue(checkRules(mapRuleList, pattern, lang));
        // load from srx file
        File segmentSrx = new File(configDir, "segmentation.srx");
        assertTrue(segmentSrx.exists());
        SRX srx1 = SRXUtils.loadFromDir(configDir);
        assertNotNull(srx1);
        mapRuleList = srx1.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(17, mapRuleList.size());
        assertTrue(checkRules(mapRuleList, pattern, lang));
        assertEquals("2.0", srx1.getVersion());
        assertTrue(srx1.isCascade());
        assertTrue(srx1.isSegmentSubflows());

    }

    public static class SRXSecurityTest {

        @org.junit.Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @org.junit.Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();


        @Test
        public void testSRXLoaderSecureCVE_2024_51366() throws IOException {
            File tmpDir = folder.newFolder();
            Path segmentConf = tmpDir.toPath().resolve("segmentation.conf");
            // prepare CVE-2024-51366 exploit code
            String xmlContent = "<java>\n" +
                    "    <object\nclass=\"java.lang.ProcessBuilder\">\n" +
                    "        <array class=\n\"java.lang.String\" length=\"2\" >\n" +
                    "            <void index=\"0\">\n" +
                    "                <string>touch</string>\n" +
                    "            </void>\n" +
                    "            <void index=\"1\">\n" +
                    "                <string>" + tmpDir + "/test-file</string>\n" +
                    "            </void>\n" +
                    "        </array>\n" +
                    "        <void method=\"start\"/>\n" +
                    "    </object>\n" +
                    "</java>";
            Files.writeString(segmentConf, xmlContent);
            SRX srx = SRXUtils.loadFromDir(segmentConf.getParent().toFile());
            assertNotNull(srx);
            assertFalse(new File(tmpDir, "test-file").exists()); // true would mean that the vulnerability is still here!
        }
    }
    
    private SRXTest() {
    }
}
