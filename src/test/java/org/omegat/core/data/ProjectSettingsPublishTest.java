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
import java.util.concurrent.atomic.AtomicBoolean;

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
 * Proves that sharing a team project setting commits the sidecar settings
 * file and leaves omegat.project untouched, so team members on older OmegaT
 * versions can still open the project.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ProjectSettingsPublishTest {

    private static final String KEY = "test_option";

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
        ProjectSettingsStorage.save(config, KEY, "true");

        RealProject.commitProjectSettings(provider, config, sidecarSetting());

        assertEquals("the team must receive the value", "true", loadDeliveredValue("member2"));
        assertFalse("omegat.project must stay untouched for older versions",
                new File(projectDir, OConsts.FILE_PROJECT).exists());
    }

    @Test
    public void testPublishCanAlsoOverwriteAValue() throws Exception {
        Files.createDirectories(new File(remoteDir, OConsts.DEFAULT_INTERNAL).toPath());
        Files.writeString(remoteSettingsFile().toPath(), KEY + "=true\n", StandardCharsets.UTF_8);
        commitAll("team with the option");

        ProjectSettingsStorage.save(config, KEY, "false");
        RealProject.commitProjectSettings(provider, config, sidecarSetting());

        assertEquals("the team must receive the new value", "false", loadDeliveredValue("member2"));
    }

    @Test
    public void testPublishingAnAlreadySharedValueIsNotAnError() throws Exception {
        // Another team member shared the same value in the meantime: the git
        // commit would be a no-op, which reports the same way as a rejected
        // push - so publishing must recognise the identical file and skip.
        Files.createDirectories(new File(remoteDir, OConsts.DEFAULT_INTERNAL).toPath());
        Files.writeString(remoteSettingsFile().toPath(), KEY + "=true\n", StandardCharsets.UTF_8);
        commitAll("team already carries the value");

        ProjectSettingsStorage.save(config, KEY, "true");
        RealProject.commitProjectSettings(provider, config, sidecarSetting());

        assertEquals("true", loadDeliveredValue("member2"));
    }

    @Test
    public void testStorageRoundTripAndDefaults() throws Exception {
        assertNull("unconfigured project reads as null", ProjectSettingsStorage.load(config, KEY));
        // removing an unset key must not materialise the file
        ProjectSettingsStorage.save(config, KEY, null);
        assertFalse("removing from an absent file must stay a no-op",
                ProjectSettingsStorage.getFile(config).isFile());
        ProjectSettingsStorage.save(config, KEY, "true");
        assertEquals("true", ProjectSettingsStorage.load(config, KEY));
        // deterministic content, so team no-op detection can compare bytes
        assertEquals(KEY + "=true\n",
                Files.readString(ProjectSettingsStorage.getFile(config).toPath(), StandardCharsets.UTF_8));
        ProjectSettingsStorage.save(config, KEY, "false");
        assertEquals("false", ProjectSettingsStorage.load(config, KEY));
        // null removes the key but keeps the file and foreign keys
        Files.writeString(ProjectSettingsStorage.getFile(config).toPath(),
                "future_setting=x\n" + KEY + "=false\n", StandardCharsets.UTF_8);
        ProjectSettingsStorage.save(config, KEY, null);
        assertNull(ProjectSettingsStorage.load(config, KEY));
        assertTrue(Files.readString(ProjectSettingsStorage.getFile(config).toPath(),
                StandardCharsets.UTF_8).contains("future_setting=x"));
    }

    @Test
    public void testResolveSettingTruthTable() {
        // non-team / offline: the local file rules, never a question
        assertResolved(null, null, RealProject.resolveSetting(null, null, false, false));
        assertResolved("true", null, RealProject.resolveSetting(null, "true", false, false));
        // team: remote delivered a value over an absent local file - first
        // arrival activates quietly, there is no local value to rescue and
        // nothing a fresh checkout should question
        assertResolved("true", null, RealProject.resolveSetting(null, "true", true, true));
        // team: remote delivered a differing value - team wins, asks
        assertAsks("false", "true", RealProject.resolveSetting("true", "false", true, true));
        // team: local-only survivor - team default stays active, asks
        assertAsks(null, "true", RealProject.resolveSetting("true", "true", false, true));
        // team: both agree (shared value) - quiet
        assertResolved("true", null, RealProject.resolveSetting("true", "true", true, true));
        // team: nothing anywhere - quiet
        assertResolved(null, null, RealProject.resolveSetting(null, null, false, true));
        // team: remote carries only foreign future keys - like absent, quiet
        assertResolved(null, null, RealProject.resolveSetting(null, null, true, true));
        // team: the sync deleted the key - team default wins, asks
        assertAsks(null, "true", RealProject.resolveSetting("true", null, true, true));
        // team: diverged and not identical - local-only, team default, asks
        assertAsks(null, "true", RealProject.resolveSetting("true", "false", false, true));
    }

    private static void assertResolved(String effective, String superseded,
            RealProject.SettingResolution state) {
        assertEquals(effective, state.effective);
        assertFalse("expected quiet resolution", state.asks);
        assertEquals(superseded, state.supersededLocal);
    }

    private static void assertAsks(String effective, String superseded,
            RealProject.SettingResolution state) {
        assertEquals(effective, state.effective);
        assertTrue("expected a question", state.asks);
        assertEquals(superseded, state.supersededLocal);
    }

    @Test
    public void testPersistSessionSettingsSkipsSupersededAndDefaults() throws Exception {
        AtomicBoolean first = new AtomicBoolean(true);
        AtomicBoolean second = new AtomicBoolean(true);
        TeamSettingsRegistry.register(TeamSetting.ofBoolean("first_option",
                "TEAM_SETTING_DIVERGED_TITLE", false, p -> first.get(), (p, v) -> first.set(v)));
        TeamSettingsRegistry.register(TeamSetting.ofBoolean("second_option",
                "TEAM_SETTING_DIVERGED_TITLE", false, p -> second.get(), (p, v) -> second.set(v)));
        try {
            // superseded key keeps the file untouched for its setting, the
            // other one still persists
            RealProject.persistSessionSettings(config, Collections.singleton("first_option"));
            assertNull(ProjectSettingsStorage.load(config, "first_option"));
            assertEquals("true", ProjectSettingsStorage.load(config, "second_option"));

            // default values stay unmaterialised unless the file exists
            Files.delete(ProjectSettingsStorage.getFile(config).toPath());
            first.set(false);
            second.set(false);
            RealProject.persistSessionSettings(config, Collections.emptySet());
            assertFalse("defaults must not materialise the file",
                    ProjectSettingsStorage.getFile(config).isFile());
        } finally {
            TeamSettingsRegistry.unregister("first_option");
            TeamSettingsRegistry.unregister("second_option");
        }
    }

    @Test
    public void testEscapingWriterRoundTripsForeignKeys() throws Exception {
        Files.createDirectories(ProjectSettingsStorage.getFile(config).getParentFile().toPath());
        Files.writeString(ProjectSettingsStorage.getFile(config).toPath(),
                "future\\ key\\=x=va\\\\lue\\=1\n" + KEY + "=false\n", StandardCharsets.UTF_8);
        Properties before = loadRaw();
        ProjectSettingsStorage.save(config, KEY, "true");
        Properties after = loadRaw();
        assertEquals("foreign key must survive the rewrite unchanged", before.getProperty("future key=x"),
                after.getProperty("future key=x"));
        assertEquals("true", ProjectSettingsStorage.load(config, KEY));
    }

    /** Sidecar-backed setting under KEY, for the commit calls. */
    private static TeamSetting sidecarSetting() {
        return TeamSetting.ofBoolean(KEY, "TEAM_SETTING_DIVERGED_TITLE", false, p -> true,
                (p, v) -> {
                });
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
    private String loadDeliveredValue(String memberDirName) throws Exception {
        File memberDir = folder.newFolder(memberDirName);
        RemoteRepositoryProvider member = newProvider(memberDir, remoteDir);
        member.switchAllToLatest();
        member.copyFilesFromReposToProject("");
        return ProjectSettingsStorage.load(new ProjectProperties(memberDir), KEY);
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
