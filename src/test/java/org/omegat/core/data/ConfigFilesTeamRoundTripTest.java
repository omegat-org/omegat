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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.segmentation.SRXManager;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.OConsts;
import org.omegat.util.TagPatternsStorage;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.filters.Filter;
import gen.core.filters.Filters;
import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves that the file-backed team settings distribute the project's
 * filters.xml, segmentation and tag_patterns.xml files through the team
 * repository: sharing a
 * configuration delivers the file to the next member, sharing "no
 * project-specific configuration" deletes it there, and sharing migrated
 * segmentation rules replaces the legacy conf file with the srx file.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ConfigFilesTeamRoundTripTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File remoteDir;
    private File projectDir;
    private ProjectProperties config;
    private RemoteRepositoryProvider provider;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestPreferencesInitializer.init();
        GITRemoteRepository2.loadPlugins();
    }

    @Before
    public void setUp() throws Exception {
        remoteDir = folder.newFolder("remote");
        Files.writeString(new File(remoteDir, "README").toPath(), "team project\n",
                StandardCharsets.UTF_8);
        commitRemote("initial team state");
        projectDir = folder.newFolder("project");
        config = new ProjectProperties(projectDir);
        Files.createDirectories(new File(config.getProjectInternal()).toPath());
        provider = newProvider(projectDir, remoteDir);
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
    public void testShareDeliversTheFiltersFileToTheTeam() throws Exception {
        String raw = sampleFiltersRaw();
        TeamSettingFiles.FILTERS.saveStored(config, raw);

        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.FILTERS);

        ProjectProperties member = syncNewMember("member2");
        assertEquals("the team must receive the configuration", raw,
                TeamSettingFiles.FILTERS.loadStored(member));
    }

    @Test
    public void testShareDistributesTheRemovalOfTheFiltersFile() throws Exception {
        Files.writeString(remoteFile(FilterMaster.FILE_FILTERS).toPath(), sampleFiltersRaw(),
                StandardCharsets.UTF_8);
        commitRemote("team with a filters file");

        // the checkout carries no filters.xml: sharing distributes exactly
        // that state, so the repository copy has to go
        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.FILTERS);

        RemoteRepositoryProvider member = newProvider(folder.newFolder("member2"), remoteDir);
        member.switchAllToLatest();
        assertFalse("the removal must reach the team",
                member.existsInRepositories(filtersPathUnderRoot()));
    }

    @Test
    public void testSharingAnIdenticalFileSkipsTheCommit() throws Exception {
        String raw = sampleFiltersRaw();
        TeamSettingFiles.FILTERS.saveStored(config, raw);
        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.FILTERS);

        // another member shared the same content in the meantime: the no-op
        // commit would report like a rejected push, so it must be skipped
        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.FILTERS);

        ProjectProperties member = syncNewMember("member2");
        assertEquals(raw, TeamSettingFiles.FILTERS.loadStored(member));
    }

    @Test
    public void testShareDeliversTheTagPatternsFileToTheTeam() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("<x\\d+>");
        String raw = TagPatternsStorage.writeToString(patterns);
        TeamSettingFiles.TAG_PATTERNS.saveStored(config, raw);

        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.TAG_PATTERNS);

        ProjectProperties member = syncNewMember("member2");
        assertEquals("the team must receive the tag definitions", raw,
                TeamSettingFiles.TAG_PATTERNS.loadStored(member));
    }

    @Test
    public void testShareDistributesTheRemovalOfTheTagPatternsFile() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setRemoveTextPattern("\\[note]");
        Files.writeString(remoteFile(TagPatternsStorage.FILE_TAG_PATTERNS).toPath(),
                TagPatternsStorage.writeToString(patterns), StandardCharsets.UTF_8);
        commitRemote("team with a tag patterns file");

        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.TAG_PATTERNS);

        RemoteRepositoryProvider member = newProvider(folder.newFolder("member2"), remoteDir);
        member.switchAllToLatest();
        assertFalse("the removal must reach the team", member.existsInRepositories(
                OConsts.DEFAULT_INTERNAL + "/" + TagPatternsStorage.FILE_TAG_PATTERNS));
    }

    @Test
    public void testShareReplacesTheLegacyConfWithTheSrxFile() throws Exception {
        // the team still carries segmentation.conf from an older OmegaT
        try (InputStream in = ConfigFilesTeamRoundTripTest.class
                .getResourceAsStream("/data/segmentation/migrate/ext/segmentation.conf")) {
            assertNotNull(in);
            Files.copy(in, remoteFile(SRXManager.CONF_SENTSEG).toPath());
        }
        commitRemote("team with legacy segmentation");
        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");
        String raw = TeamSettingFiles.SEGMENTATION.loadStored(config);
        assertNotNull(raw);

        // opening the checkout migrates conf to srx on disk (existing
        // OmegaT behavior), without changing the stored value
        config.loadProjectSRX();
        File internal = new File(config.getProjectInternal());
        assertTrue(new File(internal, SRXManager.SRX_SENTSEG).isFile());
        assertFalse(new File(internal, SRXManager.CONF_SENTSEG).isFile());
        assertEquals("the migration must not change the value", raw,
                TeamSettingFiles.SEGMENTATION.loadStored(config));

        // sharing mirrors the migrated checkout in the team repository
        RealProject.commitProjectSettings(provider, config, TeamSettingFiles.SEGMENTATION);

        ProjectProperties member = syncNewMember("member2");
        File memberInternal = new File(member.getProjectInternal());
        assertTrue("the srx file must reach the team",
                new File(memberInternal, SRXManager.SRX_SENTSEG).isFile());
        assertFalse("the legacy file must be gone",
                new File(memberInternal, SRXManager.CONF_SENTSEG).isFile());
        assertEquals("the rules must survive the trip", raw,
                TeamSettingFiles.SEGMENTATION.loadStored(member));
    }

    private String filtersPathUnderRoot() {
        return OConsts.DEFAULT_INTERNAL + "/" + FilterMaster.FILE_FILTERS;
    }

    private File remoteFile(String nameUnderInternal) throws Exception {
        File f = new File(remoteDir, OConsts.DEFAULT_INTERNAL + "/" + nameUnderInternal);
        Files.createDirectories(f.getParentFile().toPath());
        return f;
    }

    private void commitRemote(String message) throws Exception {
        try (Git git = new File(remoteDir, ".git").exists() ? Git.open(remoteDir)
                : Git.init().setDirectory(remoteDir).call()) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).setAuthor("OmegaT unit test", "test@test.nl").setSign(false)
                    .call();
        }
    }

    /** Sync a fresh member checkout and hand back its configuration view. */
    private ProjectProperties syncNewMember(String name) throws Exception {
        File memberDir = folder.newFolder(name);
        RemoteRepositoryProvider member = newProvider(memberDir, remoteDir);
        member.switchAllToLatest();
        member.copyFilesFromReposToProject("");
        return new ProjectProperties(memberDir);
    }

    private static RemoteRepositoryProvider newProvider(File projectRoot, File remote) {
        RepositoryDefinition def = new RepositoryDefinition();
        def.setType("git");
        def.setUrl("file://" + remote.getAbsolutePath());
        RepositoryMapping mapping = new RepositoryMapping();
        mapping.setLocal("");
        mapping.setRepository("");
        def.getMapping().add(mapping);
        return new RemoteRepositoryProvider(projectRoot, Collections.singletonList(def),
                new ProjectProperties(projectRoot));
    }
}
