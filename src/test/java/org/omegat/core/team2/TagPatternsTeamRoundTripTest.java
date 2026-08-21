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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.util.PatternConsts;
import org.omegat.util.TagPatternsStorage;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves that the project-specific tag expressions in omegat/tag_patterns.xml
 * survive the team project round trip: the file arrives with the team
 * checkout and takes effect, an upstream edit wins on the next
 * synchronisation, and a locally saved change committed through the
 * repository provider reaches the next checkout. The tests drive the same
 * {@link RemoteRepositoryProvider} calls that RealProject.loadProject uses
 * for a team project, against a local git repository.
 *
 * @author Stephan Pakebusch
 */
public class TagPatternsTeamRoundTripTest {

    private static final String TAG_PATTERNS_UNDER_ROOT = "omegat/" + TagPatternsStorage.FILE_TAG_PATTERNS;
    private static final String CUSTOM = "%\\d+\\$@";
    private static final String CUSTOM_UPDATED = "%\\d+\\$[@a-z]";
    private static final String REMOVE = "\\[DNT:[^\\]]+\\]";

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
        try (Git git = Git.init().setDirectory(remoteDir).call()) {
            writeTagPatterns(remoteDir, CUSTOM, REMOVE);
            commitAll(git, "team project with tag_patterns.xml");
        }
        projectDir = folder.newFolder("project");
        provider = newProvider(projectDir);
    }

    @After
    public void tearDown() {
        // The applied expressions are global state; the next test or suite
        // member must start from the plain preferences again.
        PatternConsts.clearProjectPatterns();
    }

    @Test
    public void testTeamCheckoutDeliversAndAppliesTagPatterns() throws Exception {
        syncFromRemote(provider);

        ProjectProperties props = new ProjectProperties(projectDir);
        File localFile = new File(props.getProjectInternal(), TagPatternsStorage.FILE_TAG_PATTERNS);
        assertTrue("team checkout must deliver omegat/tag_patterns.xml", localFile.exists());

        props.loadProjectTagPatterns();
        assertEquals(CUSTOM, props.getCustomTagPattern());
        assertEquals(REMOVE, props.getRemoveTextPattern());

        // RealProject.loadProject applies the expressions right after the
        // team sync; the placeholder pipeline must see them.
        PatternConsts.applyProjectPatterns(props.getCustomTagPattern(), props.getRemoveTextPattern());
        assertTrue(PatternConsts.getCustomTagPattern().matcher("%3$@").find());
        assertTrue(PatternConsts.getPlaceholderPattern().matcher("%3$@").find());
        assertTrue(PatternConsts.getRemovePattern().matcher("[DNT:internal]").find());
    }

    @Test
    public void testUpstreamEditWinsOnNextSync() throws Exception {
        syncFromRemote(provider);

        // The project admin publishes changed expressions.
        try (Git git = Git.open(remoteDir)) {
            writeTagPatterns(remoteDir, CUSTOM_UPDATED, REMOVE);
            commitAll(git, "admin updates the tag patterns");
        }

        syncFromRemote(provider);
        ProjectProperties props = new ProjectProperties(projectDir);
        props.loadProjectTagPatterns();
        assertEquals(CUSTOM_UPDATED, props.getCustomTagPattern());

        PatternConsts.applyProjectPatterns(props.getCustomTagPattern(), props.getRemoveTextPattern());
        assertTrue(PatternConsts.getCustomTagPattern().matcher("%1$s").find());
    }

    @Test
    public void testLocallySavedChangeReachesTheNextCheckout() throws Exception {
        syncFromRemote(provider);

        // The local user changes the expressions in the project properties;
        // RealProject.saveProject persists them exactly like this.
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern(CUSTOM_UPDATED);
        TagPatternsStorage.save(patterns, new File(new ProjectProperties(projectDir).getProjectInternal(),
                TagPatternsStorage.FILE_TAG_PATTERNS));

        provider.copyFilesFromProjectToRepos(TAG_PATTERNS_UNDER_ROOT, null);
        provider.commitFiles(TAG_PATTERNS_UNDER_ROOT, "Update project tag patterns");

        // A second team member checks the project out afresh.
        File projectDir2 = folder.newFolder("project2");
        RemoteRepositoryProvider provider2 = newProvider(projectDir2);
        syncFromRemote(provider2);

        ProjectProperties props2 = new ProjectProperties(projectDir2);
        props2.loadProjectTagPatterns();
        assertEquals(CUSTOM_UPDATED, props2.getCustomTagPattern());
        assertNull("the removed expression must not resurrect", props2.getRemoveTextPattern());

        PatternConsts.applyProjectPatterns(props2.getCustomTagPattern(), props2.getRemoveTextPattern());
        assertTrue(PatternConsts.getCustomTagPattern().matcher("%1$s").find());
    }

    @Test
    public void testExistsInRepositoriesSeparatesTeamFromLocalOnlyFile() throws Exception {
        // The seeded team project carries the file.
        syncFromRemote(provider);
        assertTrue(provider.isUnderMapping(TAG_PATTERNS_UNDER_ROOT));
        assertTrue(provider.existsInRepositories(TAG_PATTERNS_UNDER_ROOT));

        // A team project without the file: a locally created
        // tag_patterns.xml is recognised as unknown to the repository...
        File bareRemote = folder.newFolder("remote-without-patterns");
        try (Git git = Git.init().setDirectory(bareRemote).call()) {
            Files.writeString(new File(bareRemote, "readme.txt").toPath(),
                    "team project without local tag definitions");
            commitAll(git, "team project without tag_patterns.xml");
        }
        File projectDir3 = folder.newFolder("project3");
        RemoteRepositoryProvider provider3 = newProvider(projectDir3, bareRemote);
        syncFromRemote(provider3);
        writeTagPatterns(projectDir3, CUSTOM, REMOVE);
        assertTrue(provider3.isUnderMapping(TAG_PATTERNS_UNDER_ROOT));
        assertFalse(provider3.existsInRepositories(TAG_PATTERNS_UNDER_ROOT));

        // ...and publishing it through the provider ends that state.
        provider3.copyFilesFromProjectToRepos(TAG_PATTERNS_UNDER_ROOT, null);
        provider3.commitFiles(TAG_PATTERNS_UNDER_ROOT, "Share the tag patterns");
        assertTrue(provider3.existsInRepositories(TAG_PATTERNS_UNDER_ROOT));
    }

    @Test
    public void testRepublishingUnchangedPatternsIsNotAnError() throws Exception {
        // Sharing expressions the team repository already carries must be a
        // no-op, not a failure: the git commit of an unchanged file reports
        // the same way as a rejected push, so publishTagPatterns skips the
        // commit when the repository copy is already identical.
        syncFromRemote(provider);
        assertTrue(provider.isIdenticalInRepositories(TAG_PATTERNS_UNDER_ROOT));
        // The premise of the skip: git reports the no-op commit of an
        // unchanged file the same way as a rejected push.
        provider.copyFilesFromProjectToRepos(TAG_PATTERNS_UNDER_ROOT, null);
        assertFalse(provider.commitFilesChecked(TAG_PATTERNS_UNDER_ROOT, "no-op"));

        // A changed local file must still go through the commit.
        writeTagPatterns(projectDir, CUSTOM_UPDATED, REMOVE);
        assertFalse(provider.isIdenticalInRepositories(TAG_PATTERNS_UNDER_ROOT));
        provider.copyFilesFromProjectToRepos(TAG_PATTERNS_UNDER_ROOT, null);
        provider.commitFiles(TAG_PATTERNS_UNDER_ROOT, "Update project tag patterns");
        assertTrue(provider.isIdenticalInRepositories(TAG_PATTERNS_UNDER_ROOT));
    }

    @Test
    public void testKeepLocalMarkerSurvivesProviderRecreation() {
        // The 'keep locally' decision is stored in the checkout's team
        // settings, so reopening the project must still see it.
        provider.getTeamSettings().set("tagPatternsKeepLocal", "true");
        RemoteRepositoryProvider reopened = newProvider(projectDir);
        assertEquals("true", reopened.getTeamSettings().get("tagPatternsKeepLocal"));
    }

    /**
     * The same provider calls RealProject.loadProject issues for a managed
     * project before reading the configuration files.
     */
    private static void syncFromRemote(RemoteRepositoryProvider p) throws Exception {
        p.switchAllToLatest();
        p.copyFilesFromReposToProject("");
    }

    private RemoteRepositoryProvider newProvider(File projectRoot) {
        return newProvider(projectRoot, remoteDir);
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

    private static void writeTagPatterns(File root, String custom, String remove) throws IOException {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern(custom);
        patterns.setRemoveTextPattern(remove);
        File omegatDir = new File(root, "omegat");
        if (!omegatDir.isDirectory() && !omegatDir.mkdirs()) {
            throw new IOException("cannot create " + omegatDir);
        }
        TagPatternsStorage.save(patterns, new File(omegatDir, TagPatternsStorage.FILE_TAG_PATTERNS));
    }

    private static void commitAll(Git git, String message) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).setAuthor("OmegaT unit test", "test@test.nl").setSign(false).call();
    }
}
