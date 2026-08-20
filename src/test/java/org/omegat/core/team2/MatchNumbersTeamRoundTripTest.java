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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneFrenchTokenizer;
import org.omegat.util.OConsts;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves that the per-project fuzzy number matching option (feature request
 * #465) survives the team project round trip. The option lives in
 * omegat.project, so the relevant team mechanics differ from separate
 * configuration files: opening a team project fetches the remote
 * omegat.project as omegat.project.NEW (the sequence below mirrors
 * ProjectUICommands.projectOpen), while the routine full sync leaves the
 * local omegat.project alone.
 *
 * @author Stephan Pakebusch
 */
public class MatchNumbersTeamRoundTripTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File remoteDir;
    private File projectDir;
    private RemoteRepositoryProvider provider;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestPreferencesInitializer.init();
        GITRemoteRepository2.loadPlugins();
    }

    @Before
    public void setUp() throws Exception {
        remoteDir = folder.newFolder("remote");
        writeProjectFile(remoteDir, true);
        try (Git git = Git.init().setDirectory(remoteDir).call()) {
            Files.writeString(new File(remoteDir, "readme.txt").toPath(), "team project");
            commitAll(git, "team project with match_numbers");
        }
        projectDir = folder.newFolder("project");
        provider = newProvider(projectDir, remoteDir);
    }

    @Test
    public void testTeamOpenDeliversTheRemoteOption() throws Exception {
        ProjectProperties delivered = fetchRemoteProjectFile(provider, projectDir);
        assertTrue("the remote omegat.project must deliver the enabled option",
                delivered.isMatchNumbersEnabled());
    }

    @Test
    public void testRoutineSyncLeavesTheLocalProjectFileAlone() throws Exception {
        // The local checkout enabled the option while the team's
        // omegat.project does not carry it yet.
        writeProjectFile(remoteDir, false);
        commitRemote("team without the option");
        writeProjectFile(projectDir, true);

        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");

        assertTrue("the full sync must deliver the other files",
                new File(projectDir, "readme.txt").exists());
        ProjectProperties local = ProjectFileStorage.loadPropertiesFile(projectDir,
                new File(projectDir, OConsts.FILE_PROJECT));
        assertTrue("omegat.project is excluded from the full sync and must keep the local option",
                local.isMatchNumbersEnabled());
    }

    @Test
    public void testAdminCommitDistributesTheOptionToTheNextCheckout() throws Exception {
        // The team starts without the option; the admin enables it locally
        // and commits omegat.project through the repository provider.
        writeProjectFile(remoteDir, false);
        commitRemote("team without the option");
        provider.switchAllToLatest();
        writeProjectFile(projectDir, true);
        provider.copyFilesFromProjectToRepos(OConsts.FILE_PROJECT, null);
        provider.commitFiles(OConsts.FILE_PROJECT, "Enable fuzzy number matching");

        // The second team member opens their checkout the next time.
        File projectDir2 = folder.newFolder("project2");
        RemoteRepositoryProvider provider2 = newProvider(projectDir2, remoteDir);
        ProjectProperties delivered = fetchRemoteProjectFile(provider2, projectDir2);
        assertTrue("the next open of another checkout must receive the enabled option",
                delivered.isMatchNumbersEnabled());
    }

    @Test
    public void testRemoteProjectFileSupersedesALocalOnlyEnablement() throws Exception {
        // Documented team semantics: on open, the remote omegat.project wins.
        // A purely local enablement therefore does not survive the next open
        // and has to be committed by the project admin to stick.
        writeProjectFile(remoteDir, false);
        commitRemote("team without the option");
        writeProjectFile(projectDir, true);

        ProjectProperties delivered = fetchRemoteProjectFile(provider, projectDir);
        assertFalse("the delivered remote omegat.project does not carry the local-only option",
                delivered.isMatchNumbersEnabled());
        ProjectProperties local = ProjectFileStorage.loadPropertiesFile(projectDir,
                new File(projectDir, OConsts.FILE_PROJECT));
        assertTrue("the fetch itself must not touch the local file", local.isMatchNumbersEnabled());

        // projectOpen then replaces the local file with the delivered
        // properties (after a timestamped backup) - the local-only
        // enablement is gone.
        ProjectFileStorage.writeProjectFile(delivered);
        ProjectProperties reloaded = ProjectFileStorage.loadPropertiesFile(projectDir,
                new File(projectDir, OConsts.FILE_PROJECT));
        assertFalse("after the open, the remote state has superseded the local-only option",
                reloaded.isMatchNumbersEnabled());
    }

    /** The omegat.project fetch sequence of ProjectUICommands.projectOpen. */
    private static ProjectProperties fetchRemoteProjectFile(RemoteRepositoryProvider p, File projectRoot)
            throws Exception {
        p.switchToVersion(OConsts.FILE_PROJECT, null);
        p.copyFilesFromReposToProject(OConsts.FILE_PROJECT, ".NEW", false);
        File fetched = new File(projectRoot, OConsts.FILE_PROJECT + ".NEW");
        assertTrue("the team open must deliver omegat.project.NEW", fetched.exists());
        return ProjectFileStorage.loadPropertiesFile(projectRoot, fetched);
    }

    /**
     * The remote repository is non-bare and its work tree goes stale once a
     * provider pushed to it, so only call this before the first provider
     * commit of a test.
     */
    private void commitRemote(String message) throws Exception {
        try (Git git = Git.open(remoteDir)) {
            commitAll(git, message);
        }
    }

    private static void commitAll(Git git, String message) throws Exception {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).setAuthor("OmegaT unit test", "test@test.nl").setSign(false).call();
    }

    private static void writeProjectFile(File root, boolean matchNumbers) throws Exception {
        ProjectProperties p = new ProjectProperties(root);
        p.setSourceLanguage("en-US");
        p.setTargetLanguage("fr-FR");
        p.setSourceTokenizer(LuceneEnglishTokenizer.class);
        p.setTargetTokenizer(LuceneFrenchTokenizer.class);
        p.setMatchNumbersEnabled(matchNumbers);
        ProjectFileStorage.writeProjectFile(p);
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
