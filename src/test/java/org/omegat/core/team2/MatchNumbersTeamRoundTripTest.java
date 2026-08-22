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

package org.omegat.core.team2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectSettingsStorage;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.util.OConsts;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves that the per-project fuzzy number matching option (feature request
 * #465) survives the team project round trip in its sidecar file
 * omegat/project_settings.properties: the routine full sync distributes and
 * supersedes it like any other configuration file, a purely local file
 * survives the sync and is recognisable as local-only divergence through
 * the repository comparison.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchNumbersTeamRoundTripTest {

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
        Files.writeString(new File(remoteDir, "readme.txt").toPath(), "team project",
                StandardCharsets.UTF_8);
        // the provider detects the repository type at construction time, so
        // the remote must be a git repository before newProvider runs
        commitRemote("initial team state");
        projectDir = folder.newFolder("project");
        config = new ProjectProperties(projectDir);
        provider = newProvider(projectDir, remoteDir);
    }

    @Test
    public void testTeamOpenDeliversTheRemoteSettingsFile() throws Exception {
        writeRemoteSettings("match_numbers=true\n");
        commitRemote("team with the option");

        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");

        assertEquals("the full sync must deliver the settings file", Boolean.TRUE,
                ProjectSettingsStorage.loadMatchNumbers(config));
        assertTrue("a delivered file is identical to the repository copy",
                provider.isIdenticalInRepositories(settingsPathUnderRoot()));
    }

    @Test
    public void testRemoteFileSupersedesADivergingLocalFile() throws Exception {
        writeRemoteSettings("match_numbers=false\n");
        commitRemote("team without the option");
        ProjectSettingsStorage.saveMatchNumbers(config, true);

        // the pre-sync snapshot is what the open uses to notice and report
        // the superseded local value
        assertEquals(Boolean.TRUE, ProjectSettingsStorage.loadMatchNumbers(config));
        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");

        assertEquals("on open, the team's file wins", Boolean.FALSE,
                ProjectSettingsStorage.loadMatchNumbers(config));
    }

    @Test
    public void testLocalOnlyFileSurvivesTheSyncAndReadsAsDivergence() throws Exception {
        commitRemote("team without a settings file");
        ProjectSettingsStorage.saveMatchNumbers(config, true);

        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");

        assertEquals("a file the team never had survives the full sync", Boolean.TRUE,
                ProjectSettingsStorage.loadMatchNumbers(config));
        assertFalse("the survivor is recognisable as local-only, so the open "
                + "keeps the team default active and asks",
                provider.isIdenticalInRepositories(settingsPathUnderRoot()));
    }

    private String settingsPathUnderRoot() {
        return OConsts.DEFAULT_INTERNAL + "/" + ProjectSettingsStorage.FILE_PROJECT_SETTINGS;
    }

    private void writeRemoteSettings(String content) throws Exception {
        File f = new File(remoteDir,
                OConsts.DEFAULT_INTERNAL + "/" + ProjectSettingsStorage.FILE_PROJECT_SETTINGS);
        Files.createDirectories(f.getParentFile().toPath());
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
    }

    private void commitRemote(String message) throws Exception {
        try (Git git = new File(remoteDir, ".git").exists() ? Git.open(remoteDir)
                : Git.init().setDirectory(remoteDir).call()) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).setAuthor("OmegaT unit test", "test@test.nl").setSign(false)
                    .call();
        }
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
