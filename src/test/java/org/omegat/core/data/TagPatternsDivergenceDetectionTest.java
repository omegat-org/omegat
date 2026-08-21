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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.util.TagPatternsStorage;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Proves the decision logic behind the divergence question on opening a team
 * project: the pre-sync snapshot of omegat/tag_patterns.xml counts as
 * diverged exactly when it carried expressions and the team version that
 * replaced it resolves to different ones.
 *
 * @author Stephan Pakebusch
 */
public class TagPatternsDivergenceDetectionTest {

    private static final String LOCAL_CUSTOM = "%\\d+\\$@";
    private static final String TEAM_CUSTOM = "%\\d+\\$[@a-z]";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ProjectProperties props;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestPreferencesInitializer.init();
    }

    @Before
    public void setUp() throws Exception {
        props = new ProjectProperties(folder.newFolder("project"));
    }

    @Test
    public void testNoSnapshotMeansNothingToAsk() throws Exception {
        writeTeamFile(TEAM_CUSTOM);
        assertNull(RealProject.detectDivergedTagPatterns(props, null));
    }

    @Test
    public void testEqualExpressionsAreNotADivergence() throws Exception {
        writeTeamFile(TEAM_CUSTOM);
        assertNull(RealProject.detectDivergedTagPatterns(props, patterns(TEAM_CUSTOM)));
    }

    @Test
    public void testDifferingExpressionsReturnTheLocalVersion() throws Exception {
        writeTeamFile(TEAM_CUSTOM);
        TagPatternsStorage.TagPatterns diverged = RealProject.detectDivergedTagPatterns(props,
                patterns(LOCAL_CUSTOM));
        assertNotNull("the replaced local expressions must be reported", diverged);
        assertEquals(LOCAL_CUSTOM, diverged.getCustomTagPattern());
    }

    @Test
    public void testArmedRestoreWinsOverTheSyncedTeamFileOnce() throws Exception {
        // The user chose 'use the local version this time'; the reload's
        // team sync delivers the team file again, and the load writes the
        // armed local expressions over it.
        writeTeamFile(TEAM_CUSTOM);
        RealProject.armRestoreLocalTagPatterns(patterns(LOCAL_CUSTOM));
        assertTrue(RealProject.consumeRestoredLocalTagPatterns(props));
        assertEquals(LOCAL_CUSTOM, props.getCustomTagPattern());
        // One-shot: the next load synchronises and asks again.
        assertFalse(RealProject.consumeRestoredLocalTagPatterns(props));
    }

    @Test
    public void testEmptySnapshotIsIgnored() throws Exception {
        writeTeamFile(TEAM_CUSTOM);
        assertNull(RealProject.detectDivergedTagPatterns(props, new TagPatternsStorage.TagPatterns()));
    }

    @Test
    public void testDeletedFileMeansTheTeamRemovedIt() {
        // The sync propagated a repository-side deletion; there is no team
        // version to compare against.
        assertNull(RealProject.detectDivergedTagPatterns(props, patterns(LOCAL_CUSTOM)));
    }

    @Test
    public void testUnreadableTeamFileSuppressesTheQuestion() throws Exception {
        File configFile = new File(props.getProjectInternal(), TagPatternsStorage.FILE_TAG_PATTERNS);
        Files.createDirectories(configFile.getParentFile().toPath());
        Files.writeString(configFile.toPath(), "<tag_patterns><custom_tag_pattern>broken");
        props.loadProjectTagPatterns();
        assertTrue(props.isTagPatternsLoadFailed());
        assertNull(RealProject.detectDivergedTagPatterns(props, patterns(LOCAL_CUSTOM)));
    }

    private static TagPatternsStorage.TagPatterns patterns(String custom) {
        TagPatternsStorage.TagPatterns result = new TagPatternsStorage.TagPatterns();
        result.setCustomTagPattern(custom);
        return result;
    }

    /** The state the team sync leaves behind: file present and loaded. */
    private void writeTeamFile(String custom) throws Exception {
        File configFile = new File(props.getProjectInternal(), TagPatternsStorage.FILE_TAG_PATTERNS);
        Files.createDirectories(configFile.getParentFile().toPath());
        TagPatternsStorage.save(patterns(custom), configFile);
        props.loadProjectTagPatterns();
    }
}
