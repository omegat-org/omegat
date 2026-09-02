/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.segmentation.SRXManager;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.TagPatternsStorage;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.filters.Filter;
import gen.core.filters.Filters;

/**
 * Proves that the built-in file-backed team settings expose the project's
 * filters.xml, segmentation and tag_patterns.xml files as canonical raw
 * values: differently formatted files with equal content read as the same
 * value, reading never modifies the checkout, and storing null removes the
 * files.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class TeamSettingFilesTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File projectDir;
    private File internalDir;
    private ProjectProperties config;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestPreferencesInitializer.init();
    }

    @Before
    public void setUp() throws Exception {
        projectDir = folder.newFolder("project");
        config = new ProjectProperties(projectDir);
        internalDir = new File(config.getProjectInternal());
        Files.createDirectories(internalDir.toPath());
    }

    private static String sampleFiltersRaw() throws Exception {
        Filters filters = new Filters();
        Filter filter = new Filter();
        filter.setClassName("org.omegat.filters2.text.TextFilter");
        filter.setEnabled(false);
        filters.getFilters().add(filter);
        // the canonical form completes the configuration with the filters
        // available in this installation, so the baseline must too
        return FilterMaster.writeConfigToString(FilterMaster.completeWithAvailableFilters(filters));
    }

    @Test
    public void testFiltersValueIsCanonicalRegardlessOfFileFormatting() throws Exception {
        String raw = sampleFiltersRaw();
        TeamSettingFiles.FILTERS.saveStored(config, raw);
        assertEquals("stored value must read back unchanged", raw,
                TeamSettingFiles.FILTERS.loadStored(config));

        // a differently formatted file with equal content is the same value
        File file = new File(internalDir, FilterMaster.FILE_FILTERS);
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        // the writer uses the platform line separator
        String sep = content.contains("\r\n") ? "\r\n" : "\n";
        String reformatted = content.replace(">" + sep, ">" + sep + sep);
        assertNotEquals("the reformatting must change the file", reformatted,
                Files.readString(file.toPath(), StandardCharsets.UTF_8));
        Files.writeString(file.toPath(), reformatted, StandardCharsets.UTF_8);
        assertEquals("formatting must not count as divergence", raw,
                TeamSettingFiles.FILTERS.loadStored(config));
    }

    @Test
    public void testFiltersNullRemovesTheFile() throws Exception {
        TeamSettingFiles.FILTERS.saveStored(config, sampleFiltersRaw());
        assertTrue(TeamSettingFiles.FILTERS.storageMaterialized(config));
        TeamSettingFiles.FILTERS.saveStored(config, null);
        assertFalse("null must remove the file",
                new File(internalDir, FilterMaster.FILE_FILTERS).isFile());
        assertNull(TeamSettingFiles.FILTERS.loadStored(config));
    }

    @Test
    public void testSegmentationLegacyConfReadsWithoutMigratingTheCheckout() throws Exception {
        File conf = new File(internalDir, SRXManager.CONF_SENTSEG);
        try (InputStream in = TeamSettingFilesTest.class
                .getResourceAsStream("/data/segmentation/migrate/ext/segmentation.conf")) {
            assertNotNull(in);
            Files.copy(in, conf.toPath());
        }
        byte[] confBytes = Files.readAllBytes(conf.toPath());

        String raw = TeamSettingFiles.SEGMENTATION.loadStored(config);
        assertNotNull("the legacy file must read as a value", raw);
        assertTrue("reading must not migrate the checkout", conf.isFile());
        assertFalse("reading must not create the srx file",
                new File(internalDir, SRXManager.SRX_SENTSEG).isFile());
        assertEquals("reading must not touch the legacy file bytes",
                new String(confBytes, StandardCharsets.UTF_8),
                Files.readString(conf.toPath(), StandardCharsets.UTF_8));

        // once stored as srx, the value stays the same: the conversion is
        // part of the canonical form, not of the storage format
        TeamSettingFiles.SEGMENTATION.saveStored(config, raw);
        assertTrue(new File(internalDir, SRXManager.SRX_SENTSEG).isFile());
        assertEquals(raw, TeamSettingFiles.SEGMENTATION.loadStored(config));

        TeamSettingFiles.SEGMENTATION.saveStored(config, null);
        assertFalse("null must remove both segmentation files",
                new File(internalDir, SRXManager.SRX_SENTSEG).isFile());
        assertFalse(new File(internalDir, SRXManager.CONF_SENTSEG).isFile());
    }

    @Test
    public void testRepositoryViewReadsLegacyConfAsTheSameValue() throws Exception {
        // the checkout was migrated to srx while the repository still
        // carries the legacy conf: same rules must read as the same value,
        // or every open would report a bogus divergence
        File conf = folder.newFile("repo-segmentation.conf");
        try (InputStream in = TeamSettingFilesTest.class
                .getResourceAsStream("/data/segmentation/migrate/ext/segmentation.conf")) {
            assertNotNull(in);
            Files.copy(in, conf.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        String confPath = config.getProjectInternalRelative() + SRXManager.CONF_SENTSEG;
        String fromRepoView = TeamSettingFiles.SEGMENTATION.loadStoredFrom(config,
                path -> path.equals(confPath) ? conf : null);
        assertNotNull(fromRepoView);

        Files.copy(conf.toPath(), new File(internalDir, SRXManager.CONF_SENTSEG).toPath());
        String raw = TeamSettingFiles.SEGMENTATION.loadStored(config);
        TeamSettingFiles.SEGMENTATION.saveStored(config, null);
        TeamSettingFiles.SEGMENTATION.saveStored(config, raw);
        assertTrue(new File(internalDir, SRXManager.SRX_SENTSEG).isFile());
        assertEquals("conf in the repository and migrated srx in the checkout "
                + "must count as the same value", fromRepoView,
                TeamSettingFiles.SEGMENTATION.loadStored(config));
    }

    @Test
    public void testDescribeNamesStateAndFingerprintsContent() throws Exception {
        String noFile = TeamSettingFiles.FILTERS.describe(null);
        String one = TeamSettingFiles.FILTERS.describe("content one");
        String other = TeamSettingFiles.FILTERS.describe("content two");
        assertNotEquals("distinct contents must describe distinctly", one, other);
        assertNotEquals(noFile, one);
        assertEquals("equal content must describe stably", one,
                TeamSettingFiles.FILTERS.describe("content one"));
        assertTrue("the description must carry the fingerprint",
                one.contains(TeamSettingFiles.fingerprint("content one")));
    }

    private static String sampleTagPatternsRaw() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("<x\\d+>");
        patterns.setRemoveTextPattern("\\[remove]");
        return TagPatternsStorage.writeToString(patterns);
    }

    @Test
    public void testTagPatternsValueIsCanonicalRegardlessOfFileFormatting() throws Exception {
        String raw = sampleTagPatternsRaw();
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, raw);
        assertEquals("stored value must read back unchanged", raw,
                TeamSettingFiles.TAG_PATTERNS.loadStored(config));

        File file = new File(internalDir, TagPatternsStorage.FILE_TAG_PATTERNS);
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        String sep = content.contains("\r\n") ? "\r\n" : "\n";
        String reformatted = content.replace(">" + sep, ">" + sep + sep);
        assertNotEquals("the reformatting must change the file", reformatted,
                Files.readString(file.toPath(), StandardCharsets.UTF_8));
        Files.writeString(file.toPath(), reformatted, StandardCharsets.UTF_8);
        assertEquals("formatting must not count as divergence", raw,
                TeamSettingFiles.TAG_PATTERNS.loadStored(config));
    }

    @Test
    public void testTagPatternsEmptyStringOverrideIsARealValue() throws Exception {
        // an empty expression switches the pattern off for the project,
        // unlike an absent one, which means the global preference applies
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("");
        String raw = TagPatternsStorage.writeToString(patterns);
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, raw);
        assertTrue(TeamSettingFiles.TAG_PATTERNS.storageMaterialized(config));
        assertEquals("the switched-off expression must survive the round trip", raw,
                TeamSettingFiles.TAG_PATTERNS.loadStored(config));
    }

    @Test
    public void testTagPatternsNullRemovesTheFile() throws Exception {
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, sampleTagPatternsRaw());
        assertTrue(TeamSettingFiles.TAG_PATTERNS.storageMaterialized(config));
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, null);
        assertFalse("null must remove the file",
                new File(internalDir, TagPatternsStorage.FILE_TAG_PATTERNS).isFile());
        assertNull(TeamSettingFiles.TAG_PATTERNS.loadStored(config));
    }

    @Test
    public void testTagPatternsBrokenFileSurvivesRoutineSaves() throws Exception {
        File file = new File(internalDir, TagPatternsStorage.FILE_TAG_PATTERNS);
        Files.writeString(file.toPath(), "<tag_patterns><unclosed", StandardCharsets.UTF_8);
        config.loadProjectTagPatterns();
        assertTrue(config.isTagPatternsLoadFailed());
        assertNull("a broken file must read as no value",
                TeamSettingFiles.TAG_PATTERNS.loadStored(config));

        // the null session value does not represent the unreadable file, so
        // a routine save must leave it in place for repair
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, null);
        assertTrue("the broken file must survive the routine save", file.isFile());

        // writing real content replaces the broken file and ends the guard
        String raw = sampleTagPatternsRaw();
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, raw);
        assertFalse(config.isTagPatternsLoadFailed());
        assertEquals(raw, TeamSettingFiles.TAG_PATTERNS.loadStored(config));
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, null);
        assertFalse("after the repair null must remove the file again", file.isFile());
    }

    @Test
    public void testTagPatternsSessionRoundTrip() throws Exception {
        config.setCustomTagPattern("<x\\d+>");
        config.setRemoveTextPattern(null);
        String raw = TeamSettingFiles.TAG_PATTERNS.read(config);
        assertNotNull(raw);

        ProjectProperties other = new ProjectProperties(folder.newFolder("other"));
        TeamSettingFiles.TAG_PATTERNS.apply(other, raw);
        assertEquals("<x\\d+>", other.getCustomTagPattern());
        assertNull(other.getRemoveTextPattern());

        TeamSettingFiles.TAG_PATTERNS.apply(other, null);
        assertNull(other.getCustomTagPattern());
        assertNull("a default session must read as no value",
                TeamSettingFiles.TAG_PATTERNS.read(other));
    }

    @Test
    public void testTagPatternsDescribeNamesTheExpressions() throws Exception {
        String noFile = TeamSettingFiles.TAG_PATTERNS.describe(null);
        String named = TeamSettingFiles.TAG_PATTERNS.describe(sampleTagPatternsRaw());
        assertNotEquals(noFile, named);
        assertTrue("the description must show the expressions", named.contains("<x\\d+>"));

        TagPatternsStorage.TagPatterns off = new TagPatternsStorage.TagPatterns();
        off.setCustomTagPattern("");
        assertNotEquals("a switched-off expression must describe differently "
                + "than an inherited one", named,
                TeamSettingFiles.TAG_PATTERNS.describe(TagPatternsStorage.writeToString(off)));
    }

    @Test
    public void testPersistSessionSettingsWritesTheProjectFiles() throws Exception {
        TeamSettingFiles.ensureRegistered();
        String raw = sampleFiltersRaw();
        config.setProjectFilters(FilterMaster.loadConfigFromString(raw));

        RealProject.persistSessionSettings(config, Collections.emptySet());
        assertEquals("the session configuration must reach the file", raw,
                TeamSettingFiles.FILTERS.loadStored(config));

        // a superseded setting keeps the stored local value while the
        // session runs on the team value
        config.setProjectFilters(null);
        RealProject.persistSessionSettings(config,
                Collections.singleton(TeamSettingFiles.FILTERS_KEY));
        assertEquals("a pending question must keep the stored value", raw,
                TeamSettingFiles.FILTERS.loadStored(config));

        RealProject.persistSessionSettings(config, Collections.emptySet());
        assertNull("without the pending question the default deletes the file",
                TeamSettingFiles.FILTERS.loadStored(config));
    }
}
