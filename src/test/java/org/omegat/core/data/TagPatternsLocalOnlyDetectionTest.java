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

import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.util.TagPatternsStorage;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Proves the decision logic behind the one-time question on opening a team
 * project: a local omegat/tag_patterns.xml counts as local-only exactly when
 * it exists and is readable, the team repository does not carry it, and the
 * user has not chosen to keep it local.
 *
 * @author Stephan Pakebusch
 */
public class TagPatternsLocalOnlyDetectionTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File projectDir;
    private ProjectProperties props;
    private RemoteRepositoryProvider provider;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestPreferencesInitializer.init();
        GITRemoteRepository2.loadPlugins();
    }

    @Before
    public void setUp() throws Exception {
        File remoteDir = folder.newFolder("remote");
        try (Git git = Git.init().setDirectory(remoteDir).call()) {
            Files.writeString(new File(remoteDir, "readme.txt").toPath(), "team project");
            git.add().addFilepattern(".").call();
            git.commit().setMessage("init").setAuthor("OmegaT unit test", "test@test.nl").setSign(false)
                    .call();
        }
        projectDir = folder.newFolder("project");
        props = new ProjectProperties(projectDir);
        RepositoryDefinition def = new RepositoryDefinition();
        def.setType("git");
        def.setUrl("file://" + remoteDir.getAbsolutePath());
        RepositoryMapping mapping = new RepositoryMapping();
        mapping.setLocal("");
        mapping.setRepository("");
        def.getMapping().add(mapping);
        provider = new RemoteRepositoryProvider(projectDir, Collections.singletonList(def), props);
        provider.switchAllToLatest();
        provider.copyFilesFromReposToProject("");
        props.loadProjectTagPatterns();
    }

    @Test
    public void testNoLocalFileMeansNothingToAsk() {
        assertFalse(RealProject.detectLocalOnlyTagPatterns(props, provider));
    }

    @Test
    public void testLocalFileUnknownToTheRepositoryIsDetected() throws Exception {
        writeLocalPatterns();
        assertTrue(RealProject.detectLocalOnlyTagPatterns(props, provider));
    }

    @Test
    public void testFileCarriedByTheRepositoryIsNotLocalOnly() throws Exception {
        writeLocalPatterns();
        // The team gets the file (e.g. someone shared it); afterwards the
        // local copy matches the repository state.
        String underRoot = "omegat/" + TagPatternsStorage.FILE_TAG_PATTERNS;
        provider.copyFilesFromProjectToRepos(underRoot, null);
        provider.commitFiles(underRoot, "share");
        assertFalse(RealProject.detectLocalOnlyTagPatterns(props, provider));
    }

    @Test
    public void testKeepLocalDecisionSuppressesTheQuestion() throws Exception {
        writeLocalPatterns();
        provider.getTeamSettings().set(RealProject.TAG_PATTERNS_KEEP_LOCAL, "true");
        assertFalse(RealProject.detectLocalOnlyTagPatterns(props, provider));
    }

    @Test
    public void testUnreadableFileIsLeftAloneForRepair() throws Exception {
        File configFile = new File(props.getProjectInternal(), TagPatternsStorage.FILE_TAG_PATTERNS);
        Files.createDirectories(configFile.getParentFile().toPath());
        Files.writeString(configFile.toPath(), "<tag_patterns><custom_tag_pattern>broken");
        props.loadProjectTagPatterns();
        assertTrue(props.isTagPatternsLoadFailed());
        assertFalse(RealProject.detectLocalOnlyTagPatterns(props, provider));
    }

    private void writeLocalPatterns() throws Exception {
        TagPatternsStorage.TagPatterns patterns = new TagPatternsStorage.TagPatterns();
        patterns.setCustomTagPattern("%\\d+\\$@");
        File configFile = new File(props.getProjectInternal(), TagPatternsStorage.FILE_TAG_PATTERNS);
        Files.createDirectories(configFile.getParentFile().toPath());
        TagPatternsStorage.save(patterns, configFile);
        props.loadProjectTagPatterns();
    }
}
