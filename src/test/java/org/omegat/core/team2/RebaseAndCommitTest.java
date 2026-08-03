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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.KnownException;
import org.omegat.core.team2.operation.IRebaseOperation;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Tests for the prepare/rebase/commit cycle of team projects, in particular
 * the robustness of the asynchronous "prepare now, commit later" path and the
 * recovery from a stored base version that no longer exists in the repository
 * (for example after the repository or its local copy was rebuilt).
 *
 * The repository backend is replaced by an in-memory fake, so the tests cover
 * the orchestration logic of {@link RebaseAndCommit} and
 * {@link RemoteRepositoryProvider} without git/svn.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RebaseAndCommitTest {

    private static final String PATH = "omegat/project_save.tmx";
    private static final String REPO_URL = "url";

    private File projectDir;
    private FakeRepo repo;
    private RemoteRepositoryProvider provider;
    private RecordingRebaser rebaser;

    @Before
    public void setUp() throws Exception {
        projectDir = new File("build/testdata/rebaseandcommit");
        FileUtils.deleteDirectory(projectDir);
        assertTrue(projectDir.mkdirs());
        provider = new FakeBackendProvider(projectDir, List.of(definition(REPO_URL)));
        repo = new FakeRepo(new File(projectDir, RemoteRepositoryProvider.REPO_SUBDIR + REPO_URL));
        provider.repositories.add(repo);
        rebaser = new RecordingRebaser();
    }

    private static RepositoryDefinition definition(String url) {
        RepositoryDefinition def = new RepositoryDefinition();
        def.setType("fake");
        def.setUrl(url);
        RepositoryMapping mapping = new RepositoryMapping();
        mapping.setLocal("");
        mapping.setRepository("");
        def.getMapping().add(mapping);
        return def;
    }

    private void writeLocal(String content) throws IOException {
        File f = new File(projectDir, PATH);
        assertTrue(f.getParentFile().isDirectory() || f.getParentFile().mkdirs());
        Files.writeString(f.toPath(), content);
    }

    private void setMarker(String version) {
        provider.getTeamSettings().set(RebaseAndCommit.VERSION_PREFIX + PATH, version);
    }

    private String getMarker() {
        return provider.getTeamSettings().get(RebaseAndCommit.VERSION_PREFIX + PATH);
    }

    // ---------------------------------------------------------------------
    // The prepared info returned for the asynchronous commit must be complete
    // ---------------------------------------------------------------------

    @Test
    public void preparedInfoCarriesPathAndVersions() throws Exception {
        String v1 = repo.addVersion("one");
        setMarker(v1);
        writeLocal("one with a local change");

        PreparedFileInfo prep = RebaseAndCommit.prepare(provider, projectDir, PATH);
        assertNotNull(prep);
        assertEquals(PATH, prep.getPath());
        assertEquals(v1, prep.getVersionBase());
        assertEquals(v1, prep.getVersionHead());

        PreparedFileInfo result = RebaseAndCommit.rebaseAndCommit(prep, provider, projectDir, PATH, rebaser);
        assertNotNull(result);
        assertEquals(PATH, result.getPath());
        assertEquals(v1, result.getVersionHead());
        assertTrue(result.needToCommit());
    }

    @Test
    public void commitPreparedCommitsWithoutError() throws Exception {
        String v1 = repo.addVersion("one");
        setMarker(v1);
        writeLocal("one with a local change");

        PreparedFileInfo prep = RebaseAndCommit.prepare(provider, projectDir, PATH);
        PreparedFileInfo result = RebaseAndCommit.rebaseAndCommit(prep, provider, projectDir, PATH, rebaser);

        String newVersion = RebaseAndCommit.commitPrepared(result, provider, null);
        assertNotNull(newVersion);
        assertEquals("one with a local change", repo.versions.get(newVersion));
        assertEquals(newVersion, getMarker());
    }

    @Test
    public void commitPreparedWithoutLocalChangesIsNoop() throws Exception {
        String v1 = repo.addVersion("one");
        setMarker(v1);
        writeLocal("one");

        PreparedFileInfo prep = RebaseAndCommit.prepare(provider, projectDir, PATH);
        PreparedFileInfo result = RebaseAndCommit.rebaseAndCommit(prep, provider, projectDir, PATH, rebaser);
        assertNotNull(result);
        assertFalse(result.needToCommit());

        assertNull(RebaseAndCommit.commitPrepared(result, provider, null));
        assertEquals("no new version must be committed", 1, repo.versions.size());
    }

    @Test
    public void rebaseReusesPreparedFilesWithoutNewCheckouts() throws Exception {
        String v1 = repo.addVersion("one");
        setMarker(v1);
        writeLocal("one");

        PreparedFileInfo prep = RebaseAndCommit.prepare(provider, projectDir, PATH);
        int switches = repo.switchCount;
        RebaseAndCommit.rebaseAndCommit(prep, provider, projectDir, PATH, rebaser);
        assertEquals("base and head were prepared, no further checkout is needed", switches,
                repo.switchCount);
    }

    @Test
    public void rebaseMergesLocalAndRemoteChanges() throws Exception {
        String v1 = repo.addVersion("base");
        setMarker(v1);
        repo.addVersion("base plus remote change");
        writeLocal("base plus local change");

        PreparedFileInfo result = RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH,
                rebaser);
        assertNull(result);
        assertTrue("both sides changed, so a rebase must happen", rebaser.rebased);
        assertEquals("base", rebaser.baseContent);
        assertEquals("base plus remote change", rebaser.headContent);
        assertEquals("merged", repo.versions.get(getMarker()));
    }

    // ---------------------------------------------------------------------
    // Repository initialization failures must fail fast and clear
    // ---------------------------------------------------------------------

    @Test
    public void initFailureThrowsLocalizedError() {
        RemoteRepositoryFactory.addRepositoryConnector("failing", FailingRepo.class);
        RepositoryDefinition def = definition("https://example.com/broken.git");
        def.setType("failing");
        try {
            new RemoteRepositoryProvider(projectDir, List.of(def));
            fail("provider construction must fail when a repository cannot be initialized");
        } catch (KnownException ex) {
            assertEquals("TEAM_REPOSITORY_INIT_ERROR", ex.getMessage());
            assertEquals("https://example.com/broken.git", ex.getParams()[0]);
            assertTrue(ex.getCause() instanceof IOException);
        }
    }

    @Test
    public void initFailureOnUnknownConnectorTypeThrowsLocalizedError() {
        RepositoryDefinition def = definition("https://example.com/other.git");
        def.setType("no-such-connector-type");
        try {
            new RemoteRepositoryProvider(projectDir, List.of(def));
            fail("provider construction must fail for an unknown connector type");
        } catch (KnownException ex) {
            assertEquals("TEAM_REPOSITORY_INIT_ERROR", ex.getMessage());
            assertEquals("https://example.com/other.git", ex.getParams()[0]);
        }
    }

    // ---------------------------------------------------------------------
    // Version checkout failures must name the file, version and repository
    // ---------------------------------------------------------------------

    @Test
    public void switchToUnknownVersionExplainsWhatWasLookedUp() throws Exception {
        repo.addVersion("one");
        try {
            provider.switchToVersion(PATH, "deadbeef");
            fail("switching to an unknown version must fail");
        } catch (KnownException ex) {
            assertEquals("TEAM_SWITCH_VERSION_ERROR", ex.getMessage());
            assertEquals(PATH, ex.getParams()[0]);
            assertEquals("deadbeef", ex.getParams()[1]);
            assertEquals(REPO_URL, ex.getParams()[2]);
            assertTrue(ex.getCause().getMessage().contains("Missing unknown"));
            assertTrue("localized text must name the looked-up file",
                    ex.getLocalizedMessage().contains(PATH));
            assertTrue("localized text must name the missing version",
                    ex.getLocalizedMessage().contains("deadbeef"));
        }
    }

    @Test
    public void switchToLatestVersionFailurePassesThrough() {
        // an empty repository cannot even provide a latest version; that is no
        // stale-marker situation, so the raw backend error must survive
        try {
            provider.switchToVersion(PATH, null);
            fail("switching in an empty repository must fail");
        } catch (Exception ex) {
            assertFalse(ex instanceof KnownException);
        }
    }

    // ---------------------------------------------------------------------
    // A stored base version that no longer exists must not break the project
    // ---------------------------------------------------------------------

    @Test
    public void prepareWithoutMarkerReturnsNull() throws Exception {
        repo.addVersion("one");
        assertNull(RebaseAndCommit.prepare(provider, projectDir, PATH));
    }

    @Test
    public void prepareWithStaleMarkerReturnsNullInsteadOfFailing() throws Exception {
        repo.addVersion("one");
        setMarker("gone1234");
        assertNull(RebaseAndCommit.prepare(provider, projectDir, PATH));
    }

    @Test
    public void rebaseWithStaleMarkerMergesAgainstEmptyBaseAndHealsTheMarker() throws Exception {
        repo.addVersion("content");
        setMarker("gone1234");
        writeLocal("content");

        RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
        assertNotNull("project loading must survive a stale marker", rebaser.reloaded);
        assertTrue("without a trustworthy base, local and remote must be merged", rebaser.rebased);
        assertNull("the base must be treated as empty data", rebaser.baseContent);
        assertEquals("content", rebaser.headContent);
        assertEquals("the marker must be healed to the merged commit", "merged",
                repo.versions.get(getMarker()));
    }

    @Test
    public void rebaseWithStaleMarkerNeverOverwritesRemoteChanges() throws Exception {
        // remote moved on after the history rewrite; the local file is based
        // on a lost version. Neither side may silently win.
        repo.addVersion("remote work by a colleague");
        setMarker("gone1234");
        writeLocal("local work based on a lost version");

        RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
        assertTrue("a three-way merge with an empty base is required", rebaser.rebased);
        assertEquals("remote work by a colleague", rebaser.headContent);
        assertEquals("the merge result must be committed, not the plain local file", "merged",
                repo.versions.get(getMarker()));
    }

    @Test
    public void staleMarkerWithUnreachableLatestVersionStillFails() throws Exception {
        // no versions in the repository at all: the fallback cannot determine
        // a latest version either, and that failure must surface
        setMarker("gone1234");
        writeLocal("content");
        try {
            RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
            fail("an unreachable latest version must still be an error");
        } catch (Exception ex) {
            assertTrue("no commit must have happened", repo.versions.isEmpty());
            assertEquals("the marker must not be healed on failure", "gone1234", getMarker());
        }
    }

    @Test
    public void transientCheckoutFailureFailsLoudAndKeepsTheMarker() throws Exception {
        // a lock file or a network outage is not a lost version: recovering
        // would replace a still-valid base and merge against the wrong state
        String v1 = repo.addVersion("content");
        setMarker(v1);
        writeLocal("content with local change");
        repo.nextSwitchFailure = new IOException("cannot lock index.lock");
        try {
            RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
            fail("a transient checkout failure must not be swallowed");
        } catch (Exception ex) {
            assertEquals("the valid marker must survive a transient failure", v1, getMarker());
            assertEquals("nothing must have been committed", 1, repo.versions.size());
        }
    }

    @Test
    public void transientCheckoutFailureInPrepareIsNotSwallowed() throws Exception {
        String v1 = repo.addVersion("content");
        setMarker(v1);
        repo.nextSwitchFailure = new IOException("connection refused");
        try {
            RebaseAndCommit.prepare(provider, projectDir, PATH);
            fail("a transient checkout failure must not be reported as a lost version");
        } catch (Exception ex) {
            assertEquals(v1, getMarker());
        }
    }

    @Test
    public void reusingTheReturnedInfoRechecksOutInsteadOfCrashing() throws Exception {
        // the returned info carries versions but no prepared files; a second
        // rebase with it must fall back to fresh checkouts, not NPE
        String v1 = repo.addVersion("one");
        setMarker(v1);
        writeLocal("one");

        PreparedFileInfo first = RebaseAndCommit.prepare(provider, projectDir, PATH);
        PreparedFileInfo result = RebaseAndCommit.rebaseAndCommit(first, provider, projectDir, PATH,
                rebaser);
        PreparedFileInfo again = RebaseAndCommit.rebaseAndCommit(result, provider, projectDir, PATH,
                rebaser);
        assertNotNull(again);
        assertFalse(again.needToCommit());
        assertEquals(v1, getMarker());
    }

    @Test
    public void validMarkerKeepsTheNormalPath() throws Exception {
        String v1 = repo.addVersion("content");
        setMarker(v1);
        writeLocal("content");

        RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
        assertNotNull(rebaser.reloaded);
        assertEquals("an intact marker must stay untouched", v1, getMarker());
        assertEquals(1, repo.versions.size());
    }

    @Test
    public void missingLocalFileTakesRemoteWithStaleMarker() throws Exception {
        String v1 = repo.addVersion("remote content");
        setMarker("gone1234");
        // no local file at all

        RebaseAndCommit.rebaseAndCommit(null, provider, projectDir, PATH, rebaser);
        File localFile = new File(projectDir, PATH);
        assertTrue("the remote content must have been taken over", localFile.isFile());
        assertEquals("remote content", Files.readString(localFile.toPath()));
        assertEquals(v1, getMarker());
    }

    // ---------------------------------------------------------------------
    // Test doubles
    // ---------------------------------------------------------------------

    /** Provider whose backend is injected by the test instead of the factory. */
    private static final class FakeBackendProvider extends RemoteRepositoryProvider {
        FakeBackendProvider(File projectRoot, List<RepositoryDefinition> definitions) {
            super(projectRoot, definitions);
        }

        @Override
        protected void initializeRepositories() {
            // the test adds its FakeRepo directly
        }
    }

    /**
     * In-memory versioned repository: a map of version id to the content of
     * {@link #PATH}. switchToVersion materializes the content in the working
     * directory, like a git checkout; an unknown version fails with the same
     * kind of message JGit produces.
     */
    private static final class FakeRepo implements IRemoteRepository2 {
        final File workDir;
        final Map<String, String> versions = new HashMap<>();
        String head;
        String checkedOut;
        int seq;
        int switchCount;
        Exception nextSwitchFailure;

        FakeRepo(File workDir) {
            this.workDir = workDir;
        }

        String addVersion(String content) {
            String id = "v" + (++seq);
            versions.put(id, content);
            head = id;
            return id;
        }

        @Override
        public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) {
        }

        @Override
        public String getFileVersion(String file) {
            return checkedOut;
        }

        @Override
        public void switchToVersion(String version) throws Exception {
            switchCount++;
            if (nextSwitchFailure != null) {
                Exception ex = nextSwitchFailure;
                nextSwitchFailure = null;
                throw ex;
            }
            String v = version == null ? head : version;
            String content = v == null ? null : versions.get(v);
            if (content == null) {
                // same shape as a real JGit failure for an unknown object
                throw new MissingObjectException(ObjectId.zeroId(), "unknown");
            }
            File f = new File(workDir, PATH);
            assertTrue(f.getParentFile().isDirectory() || f.getParentFile().mkdirs());
            Files.writeString(f.toPath(), content);
            checkedOut = v;
        }

        @Override
        public void addForCommit(String path) {
        }

        @Override
        public void addForDeletion(String path) {
        }

        @Override
        public File getLocalDirectory() {
            return workDir;
        }

        @Override
        public String[] getRecentlyDeletedFiles() {
            return new String[0];
        }

        @Override
        public String commit(String[] onVersions, String comment) throws Exception {
            return addVersion(Files.readString(new File(workDir, PATH).toPath()));
        }
    }

    /** Repository connector whose initialization always fails. */
    public static final class FailingRepo implements IRemoteRepository2 {
        @Override
        public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings)
                throws Exception {
            throw new IOException("boom");
        }

        @Override
        public String getFileVersion(String file) {
            return null;
        }

        @Override
        public void switchToVersion(String version) {
        }

        @Override
        public void addForCommit(String path) {
        }

        @Override
        public void addForDeletion(String path) {
        }

        @Override
        public File getLocalDirectory() {
            return null;
        }

        @Override
        public String[] getRecentlyDeletedFiles() {
            return new String[0];
        }

        @Override
        public String commit(String[] onVersions, String comment) {
            return null;
        }
    }

    /** Rebase operation that records what it was fed and writes a fixed merge result. */
    private static final class RecordingRebaser implements IRebaseOperation {
        String baseContent;
        String headContent;
        File reloaded;
        boolean rebased;

        @Override
        public void parseBaseFile(File file) throws Exception {
            baseContent = file.exists() ? Files.readString(file.toPath()) : null;
        }

        @Override
        public void parseHeadFile(File file) throws Exception {
            headContent = file.exists() ? Files.readString(file.toPath()) : null;
        }

        @Override
        public void rebaseAndSave(File out) throws Exception {
            rebased = true;
            Files.writeString(out.toPath(), "merged");
        }

        @Override
        public void reload(File file) {
            reloaded = file;
        }

        @Override
        public String getCommentForCommit() {
            return "test comment";
        }

        @Override
        public String getFileCharset(File file) {
            return null;
        }
    }
}
