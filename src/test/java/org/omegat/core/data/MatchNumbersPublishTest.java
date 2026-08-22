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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.util.OConsts;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves that sharing the match_numbers project setting with the team
 * commits the sidecar settings file and leaves omegat.project untouched, so
 * team members on older OmegaT versions can still open the project (feature
 * request #465).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchNumbersPublishTest {

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
        commitAll("initial team state");
        projectDir = folder.newFolder("project");
        config = new ProjectProperties(projectDir);
        provider = newProvider(projectDir, remoteDir);
    }

    @Test
    public void testPublishDeliversTheSettingsFileToTheTeam() throws Exception {
        ProjectSettingsStorage.saveMatchNumbers(config, true);

        RealProject.commitProjectSettings(provider, config);

        assertEquals("the team must receive the enabled option", Boolean.TRUE,
                loadDeliveredValue("member2"));
        assertFalse("omegat.project must stay untouched for older versions",
                new File(projectDir, OConsts.FILE_PROJECT).exists());
    }

    @Test
    public void testPublishCanAlsoDisableTheOption() throws Exception {
        Files.createDirectories(new File(remoteDir, OConsts.DEFAULT_INTERNAL).toPath());
        Files.writeString(remoteSettingsFile().toPath(), "match_numbers=true\n", StandardCharsets.UTF_8);
        commitAll("team with the option");

        ProjectSettingsStorage.saveMatchNumbers(config, false);
        RealProject.commitProjectSettings(provider, config);

        assertEquals("the team must receive the disabled option", Boolean.FALSE,
                loadDeliveredValue("member2"));
    }

    @Test
    public void testPublishingAnAlreadySharedValueIsNotAnError() throws Exception {
        // Another team member shared the same value in the meantime: the git
        // commit would be a no-op, which reports the same way as a rejected
        // push - so publishing must recognise the identical file and skip.
        Files.createDirectories(new File(remoteDir, OConsts.DEFAULT_INTERNAL).toPath());
        Files.writeString(remoteSettingsFile().toPath(), "match_numbers=true\n", StandardCharsets.UTF_8);
        commitAll("team already carries the value");

        ProjectSettingsStorage.saveMatchNumbers(config, true);
        RealProject.commitProjectSettings(provider, config);

        assertEquals(Boolean.TRUE, loadDeliveredValue("member2"));
    }

    @Test
    public void testStorageRoundTripAndDefaults() throws Exception {
        assertNull("unconfigured project reads as null", ProjectSettingsStorage.loadMatchNumbers(config));
        ProjectSettingsStorage.saveMatchNumbers(config, true);
        assertEquals(Boolean.TRUE, ProjectSettingsStorage.loadMatchNumbers(config));
        // deterministic content, so team no-op detection can compare bytes
        assertEquals("match_numbers=true\n",
                Files.readString(ProjectSettingsStorage.getFile(config).toPath(), StandardCharsets.UTF_8));
        ProjectSettingsStorage.saveMatchNumbers(config, false);
        assertEquals(Boolean.FALSE, ProjectSettingsStorage.loadMatchNumbers(config));
        // unknown keys of future settings survive a save
        Files.writeString(ProjectSettingsStorage.getFile(config).toPath(),
                "future_setting=x\nmatch_numbers=false\n", StandardCharsets.UTF_8);
        ProjectSettingsStorage.saveMatchNumbers(config, true);
        assertTrue(Files.readString(ProjectSettingsStorage.getFile(config).toPath(),
                StandardCharsets.UTF_8).contains("future_setting=x"));
    }

    @Test
    public void testResolveMatchNumbersTruthTable() {
        // non-team / offline: the local file rules, never a question
        assertResolved(false, null, RealProject.resolveMatchNumbers(null, null, false, false));
        assertResolved(true, null, RealProject.resolveMatchNumbers(null, true, false, false));
        // team: remote delivered true over an absent local file - first
        // arrival activates and asks once
        assertResolved(true, Boolean.FALSE, RealProject.resolveMatchNumbers(null, true, true, true));
        // team: remote delivered false over a local true - team wins, asks
        assertResolved(false, Boolean.TRUE, RealProject.resolveMatchNumbers(true, false, true, true));
        // team: local-only survivor - team default stays active, asks
        assertResolved(false, Boolean.TRUE, RealProject.resolveMatchNumbers(true, true, false, true));
        // team: both agree (shared value) - quiet
        assertResolved(true, null, RealProject.resolveMatchNumbers(true, true, true, true));
        // team: nothing anywhere - quiet
        assertResolved(false, null, RealProject.resolveMatchNumbers(null, null, false, true));
        // team: remote carries only foreign future keys - like absent, quiet
        assertResolved(false, null, RealProject.resolveMatchNumbers(null, null, true, true));
    }

    private static void assertResolved(boolean effective, Boolean superseded,
            RealProject.MatchNumbersState state) {
        assertEquals(effective, state.effective);
        assertEquals(superseded, state.supersededLocal);
    }

    @Test
    public void testEscapingWriterRoundTripsForeignKeys() throws Exception {
        Files.createDirectories(ProjectSettingsStorage.getFile(config).getParentFile().toPath());
        Files.writeString(ProjectSettingsStorage.getFile(config).toPath(),
                "future\\ key\\=x=va\\\\lue\\=1\nmatch_numbers=false\n", StandardCharsets.UTF_8);
        Properties before = loadRaw();
        ProjectSettingsStorage.saveMatchNumbers(config, true);
        Properties after = loadRaw();
        assertEquals("foreign key must survive the rewrite unchanged", before.getProperty("future key=x"),
                after.getProperty("future key=x"));
        assertEquals(Boolean.TRUE, ProjectSettingsStorage.loadMatchNumbers(config));
    }

    private Properties loadRaw() throws Exception {
        Properties p = new Properties();
        try (java.io.Reader in = Files.newBufferedReader(
                ProjectSettingsStorage.getFile(config).toPath(), StandardCharsets.UTF_8)) {
            p.load(in);
        }
        return p;
    }

    private File remoteSettingsFile() {
        return new File(remoteDir,
                OConsts.DEFAULT_INTERNAL + "/" + ProjectSettingsStorage.FILE_PROJECT_SETTINGS);
    }

    private void commitAll(String message) throws Exception {
        try (Git git = new File(remoteDir, ".git").exists() ? Git.open(remoteDir)
                : Git.init().setDirectory(remoteDir).call()) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).setAuthor("OmegaT unit test", "test@test.nl").setSign(false)
                    .call();
        }
    }

    /** Fetch the settings file the way a second member's open does. */
    private Boolean loadDeliveredValue(String memberDirName) throws Exception {
        File memberDir = folder.newFolder(memberDirName);
        RemoteRepositoryProvider member = newProvider(memberDir, remoteDir);
        member.switchAllToLatest();
        member.copyFilesFromReposToProject("");
        return ProjectSettingsStorage.loadMatchNumbers(new ProjectProperties(memberDir));
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
