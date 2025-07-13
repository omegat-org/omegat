/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX.CheckOrphanedCallback;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.core.team2.impl.SVNAuthenticationManager;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.util.Language;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TMXWriter2;
import org.omegat.util.TestPreferencesInitializer;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * This is test for team project concurrent modification. It doesn't simple
 * junit test, but looks like 'integration' test.
 * <p>
 * This test prepare scenario, execute separate JVMs for concurrent updates,
 * then check remote repository data.
 * <p>
 * Each child process updates own segments with source1..5/0/1/2/3 by values
 * from 1 and more. Segment source/0 updated each time, but source/1/2/3 updated
 * once per cycle. After process will be finished, values in tmx should be in
 * right order, i.e. only by increasing order. That means user will not commit
 * previous translation for other user's segments.
 * <p>
 * Segment with 'concurrent' source will be modified by all users by values from
 * 1 and more with user's prefix. Conflicts should be resolved by choose higher
 * value. After process will be finished, values in 'concurrent' segment should
 * be also increased only.
 * <p>
 * Each child saves {@code Integer.MAXVALUE} as last translation, but current
 * OmegaT implementation doesn't require to commit it, see "GIT_CONFLICT=Push
 * failed. Will be synchronized next time."
 * <p>
 * Note that when using a git repository accessed by the {@code file://}
 * protocol, gc can cause concurrency issues that result in an error saving the
 * project and missed sync cycles. When this happens near the end of the test
 * the test will fail (seen with JGit 4.8). This has not been seen when using
 * {@code git+ssh} protocol. This is unlikely to be a problem in real-world
 * scenarios, but a workaround for this test is to disable gc on the repo with
 *
 * <pre>
 * {@code
 * git config gc.auto 0
 * git config gc.autodetach false
 * git config receive.autogc false
 * }
 * </pre>
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 *
 */
public final class TestTeamIntegration {

    private TestTeamIntegration() {
    }

    private static final String PLUGINS_LIST_FILE = "test-integration/plugins.properties";
    private static final Pattern URL_PATTERN = Pattern
            .compile("(http(s)?|svn(\\+ssh)?)" + "://(?<username>.+?)(:(?<password>.+?))?@.+");
    private static final String PROJECT_SAVE_PATH = "omegat/project_save.tmx";

    static final int MAX_DELAY_SECONDS = 15;
    static final int SEG_COUNT = 4;

    static @Nullable String mapRepo;
    static @Nullable String mapRepoType;
    static @Nullable String mapFile;
    static int processSeconds;

    // referenced from TestTeamIntegrationChild class
    static final Language SRC_LANG = new Language("en");
    static final Language TRG_LANG = new Language("be");

    // test with 3 threads.
    static final String[] THREADS = new String[] { "s1", "s2", "s3" };

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTeamIntegration.class);
    private static final String ERROR_CREATE_TEST_DIR = "Impossible to create test dir";

    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("java.util.logging.config.file", null);
        String repositoryProperty = System.getProperty("omegat.test.repo", null);
        if (repositoryProperty == null) {
            LOGGER.atError().log("Property omegat.test.repo is mandatory.");
            System.exit(1);
        }
        Properties props = new Properties();
        try (InputStream fis = Files.newInputStream(Paths.get(PLUGINS_LIST_FILE))) {
            props.load(fis);
            PluginUtils.loadPluginFromProperties(props);
        }
        final List<String> repositoryUrls = new ArrayList<>();
        repositoryUrls.add(repositoryProperty);
        String altRepo = System.getProperty("omegat.test.repo.alt", null);
        if (altRepo != null) {
            repositoryUrls.add(altRepo);
        }
        mapRepo = System.getProperty("omegat.test.map.repo", null);
        mapRepoType = System.getProperty("omegat.test.map.type", "http");
        mapFile = System.getProperty("omegat.test.map.file", null);
        try {
            String propDuration = System.getProperty("omegat.test.duration");
            processSeconds = propDuration != null ? Integer.parseInt(propDuration) : 4 * 60 * 60;
        } catch (NumberFormatException ignored) {
            processSeconds = 4 * 60 * 60;
        }

        System.out.println("Target repository: " + repositoryProperty);
        System.out.println("Process duration: " + processSeconds + " seconds");
        if (mapRepo != null) {
            System.out.println("Map repository: " + mapRepo);
            System.out.println("Map repository type: " + mapRepoType);
            System.out.println("Map file: " + mapFile);
        }

        Path tempDir = Files.createTempDirectory("teamtest");
        LOGGER.atInfo().log("Test runner directory is: " + tempDir);
        String startVersion = prepareRepo(repositoryUrls.get(0), tempDir);

        Run[] runs = new Run[THREADS.length];
        for (int i = 0; i < THREADS.length; i++) {
            Path runnerDir = setupRunnerDirectory(tempDir, i);
            runs[i] = new Run(THREADS[i], runnerDir.toFile(), MAX_DELAY_SECONDS,
                    repositoryUrls.get(i % repositoryUrls.size()), logConfig);
        }
        for (int i = 0; i < THREADS.length; i++) {
            runs[i].start();
        }
        boolean alive;
        do {
            alive = false;
            for (int i = 0; i < THREADS.length; i++) {
                if (runs[i].finished) {
                    if (runs[i].result != 200) {
                        for (Run r : runs) {
                            r.end();
                        }
                        LOGGER.atError().log("==================== EXIT BY ERROR ====================");
                        System.exit(1);
                    }
                } else {
                    alive = true;
                }
            }
            Thread.sleep(500);
        } while (alive);

        final Team teamRepository = createRepo2(repositoryUrls.get(0), tempDir.resolve("repo").toFile());
        teamRepository.update();

        LOGGER.atInfo().log("Check repo");

        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        checkRepo(teamRepository, startVersion);

        LOGGER.atInfo().log("Processed successfully");
    }

    private static @NotNull Path setupRunnerDirectory(Path tempDir, int i) throws IOException {
        Path runnerDir = tempDir.resolve(THREADS[i]);
        if (!runnerDir.toFile().mkdirs()) {
            throw new IOException(ERROR_CREATE_TEST_DIR);
        }
        Files.copy(tempDir.resolve("repo/omegat.project"), runnerDir.resolve("omegat.project"));
        if (!runnerDir.resolve("omegat").toFile().mkdirs()) {
            throw new IOException(ERROR_CREATE_TEST_DIR);
        }
        return runnerDir;
    }

    /**
     * Check repository after children processed.
     */
    static void checkRepo(Team teamRepository, String startVersion) throws Exception {
        List<String> segments = buildSegmentsList();
        Map<String, List<Long>> data = initializeDataMap(segments);

        int tmxCount = processRevisions(teamRepository, startVersion, data);

        logSegmentValues(segments);
        logRevisionData(tmxCount, segments, data);
        checkCommitOrder(data);
    }

    private static List<String> buildSegmentsList() {
        List<String> segments = new ArrayList<>();
        for (String thread : THREADS) {
            for (int segmentIndex = 0; segmentIndex < SEG_COUNT; segmentIndex++) {
                segments.add(thread + "/" + segmentIndex);
            }
        }
        return segments;
    }

    private static Map<String, List<Long>> initializeDataMap(List<String> segments) {
        Map<String, List<Long>> dataMap = new TreeMap<>();
        for (String segment : segments) {
            dataMap.put(segment, List.of(0L));
        }
        dataMap.put(TestTeamIntegrationChild.CONCURRENT_NAME, List.of(0L));
        return dataMap;
    }

    private static int processRevisions(Team teamRepository, String startVersion, Map<String, List<Long>> data) throws Exception {
        int tmxCount = 0;
        for (String revision : teamRepository.listRevisions(startVersion)) {
            teamRepository.checkout(revision);
            ProjectTMX projectTMX = loadProjectTmx(teamRepository);
            for (String segment : data.keySet()) {
                TMXEntry entry = projectTMX.getDefaultTranslation(segment);
                long value = (entry == null) ? 0 : Long.parseLong(entry.translation);
                data.get(segment).add(value);
            }
            tmxCount++;
        }
        return tmxCount;
    }

    private static ProjectTMX loadProjectTmx(Team teamRepository) throws Exception {
        ProjectTMX projectTMX = new ProjectTMX(checkOrphanedCallback);
        File projectFile = new File(teamRepository.getDir(), PROJECT_SAVE_PATH);
        projectTMX.load(SRC_LANG, TRG_LANG, false, projectFile, Core.getSegmenter());
        return projectTMX;
    }

    private static void logSegmentValues(List<String> segments) {
        LOGGER.atInfo().log(() -> {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Values :");
            for (String segment : segments) {
                logBuilder.append(getPadding(segment.length())).append(segment).append(" ");
            }
            logBuilder.append("\n");
            return logBuilder.toString();
        });
    }

    private static void logRevisionData(int tmxCount, List<String> segments, Map<String, List<Long>> data) {
        LOGGER.atInfo().log(() -> {
            StringBuilder logBuilder = new StringBuilder();
            for (int i = 0; i < tmxCount; i++) {
                for (String segment : segments) {
                    String valueString = data.get(segment).get(i).toString();
                    logBuilder.append(getPadding(valueString.length())).append(valueString).append(" ");
                }
                logBuilder.append("\n");
            }
            return logBuilder.toString();
        });
    }

    private static void checkCommitOrder(Map<String, List<Long>> data) {
        boolean allCommitsOrdered = true;
        for (Map.Entry<String, List<Long>> entry : data.entrySet()) {
            long previousValue = 0;
            for (long value : entry.getValue()) {
                if (value < previousValue) {
                    LOGGER.atError().log("Wrong order in {}", entry.getKey());
                    allCommitsOrdered = false;
                    break;
                } else {
                    previousValue = value;
                }
            }
        }
        if (allCommitsOrdered) {
            LOGGER.atInfo().log("All commits look good");
        }
    }

    private static final String EMPTY_PADDING_SPACES = "                  ";
    private static final int SEGMENT_PADDING_SIZE = 14;

    private static String getPadding(int length) {
        return EMPTY_PADDING_SPACES.substring(0, SEGMENT_PADDING_SIZE - length);
    }

    /**
     * Prepares a repository for testing purposes by creating a test directory,
     * setting up repository configurations, and committing initial files.
     *
     * @param repo the repository URL to prepare for testing
     * @return the version identifier of the committed "omegat/project_save.tmx" file
     * @throws Exception if there is an issue creating or deleting directories,
     *                   initializing repository configurations, or committing files
     */
    static String prepareRepo(String repo, Path tempDir) throws Exception {
        Path origDir = tempDir.resolve("repo");
        if (!origDir.toFile().mkdir()) {
            throw new IOException(ERROR_CREATE_TEST_DIR);
        }

        ProjectProperties config = createConfig(repo, origDir.toFile());

        RemoteRepositoryProvider remote = new RemoteRepositoryProvider(config.getProjectRootDir(),
                config.getRepositories(), config);
        remote.switchAllToLatest();

        Path omegatDir = origDir.resolve("omegat");
        boolean result = omegatDir.toFile().mkdirs();
        if (!result) {
            throw new IOException(ERROR_CREATE_TEST_DIR);
        }

        Path f = origDir.resolve("omegat/project_save.tmx");
        TMXWriter2 wr = new TMXWriter2(f.toFile(), SRC_LANG, TRG_LANG, true, false, true);
        wr.close();
        ProjectFileStorage.writeProjectFile(config);

        remote.copyFilesFromProjectToRepos("omegat.project", null);
        remote.commitFiles("omegat.project", "Prepare for team test");
        GlossaryManager.createNewWritableGlossaryFile(config.getWritableGlossaryFile().getAsFile());
        remote.copyFilesFromProjectToRepos("glossary/glossary.txt", null);
        remote.commitFiles("glossary/glossary.txt", "Prepare for team test");
        remote.copyFilesFromProjectToRepos("omegat/project_save.tmx", null);
        remote.commitFiles("omegat/project_save.tmx", "Prepare for team test");

        return remote.getVersion("omegat/project_save.tmx");
    }

    /**
     * Creates a configuration for a project based on the given repository URL and directory.
     *
     * @param repoUrl the repository URL to be used for the project configuration
     * @param dir the directory where the project resides
     * @return a {@link ProjectProperties} instance containing the configuration details for the project
     */
    static ProjectProperties createConfig(String repoUrl, File dir) {
        ProjectProperties config = new ProjectProperties(dir);
        config.setSourceLanguage(SRC_LANG);
        config.setTargetLanguage(TRG_LANG);
        config.setRepositories(new ArrayList<>());
        if (mapRepo == null || mapFile == null) {
            config.getRepositories().add(getDef(repoUrl, predictMainType(repoUrl), "", ""));
        } else {
            config.getRepositories().add(getDef(repoUrl, predictMainType(repoUrl), "/", "/"));
            config.getRepositories().add(getDef(mapRepo, mapRepoType, mapFile, "source/" + mapFile));
        }
        config.setWriteableGlossary("glossary/glossary.txt");
        config.setGlossaryRoot("glossary");
        return config;
    }

    static String predictMainType(String repoUrl) {
        if (repoUrl.startsWith("git") || repoUrl.endsWith(".git")) {
            return "git";
        } else if (repoUrl.startsWith("svn") || repoUrl.startsWith("http") || repoUrl.endsWith(".svn")) {
            return "svn";
        } else {
            throw new RuntimeException("Unknown repo");
        }
    }

    static RepositoryDefinition getDef(String repoUrl, String type, String remote, String local) {
        RepositoryDefinition def = new RepositoryDefinition();
        RepositoryMapping m = new RepositoryMapping();
        def.setType(type);
        if (type.equals("git") && repoUrl.contains("@")) {
            Matcher matcher = URL_PATTERN.matcher(repoUrl);
            if (matcher.find()) {
                String username = matcher.group("username");
                if (!StringUtils.isEmpty(username)) {
                    def.getOtherAttributes().put(new QName("gitUsername"), username);
                }
                String password = matcher.group("password");
                if (!StringUtils.isEmpty(password)) {
                    def.getOtherAttributes().put(new QName("gitPassword"), password);
                }
            }
        }
        if (type.equals("svn") && repoUrl.contains("@")) {
            Matcher matcher = URL_PATTERN.matcher(repoUrl);
            if (matcher.find()) {
                String username = matcher.group("username");
                if (!StringUtils.isEmpty(username)) {
                    def.getOtherAttributes().put(new QName("svnUsername"), username);
                }
                String password = matcher.group("password");
                if (!StringUtils.isEmpty(password)) {
                    def.getOtherAttributes().put(new QName("svnPassword"), password);
                }
            }
        }
        def.setUrl(repoUrl);
        m.setLocal(local);
        m.setRepository(remote);
        def.getMapping().add(m);
        return def;
    }

    static boolean isProjectDir(File file) {
        return Arrays.stream(file.listFiles()).anyMatch(f -> f.getName().equals("omegat.project"));
    }

    static Team createRepo2(String url, File dir) throws Exception {
        File repoDir = Stream
                .of(Objects.requireNonNull(new File(dir, RemoteRepositoryProvider.REPO_SUBDIR).listFiles()))
                .filter(File::isDirectory).filter(TestTeamIntegration::isProjectDir).findFirst().get();
        if (url.startsWith("git") || url.endsWith(".git")) {
            return new GitTeam(repoDir);
        } else if (url.startsWith("svn") || url.startsWith("http") || url.endsWith(".svn")) {
            return new SvnTeam(repoDir, url);
        } else {
            throw new Exception("Unknown repo");
        }
    }

    /**
     * Child process handling.
     */
    static class Run extends Thread {
        volatile Process p;
        volatile int result;
        volatile boolean finished;
        String source;

        Run(String source, File dir, int delay, final String repo, final String logConfig) throws Exception {
            this.source = source;
            String cp = ManagementFactory.getRuntimeMXBean().getClassPath();

            // Get `java` command path from java.home
            Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java");
            List<String> cmd = new ArrayList<>();
            cmd.add(javaBin.toString());
            cmd.add("-Duser.name=" + source);
            if (logConfig != null) {
                cmd.add("-Djava.util.logging.config.file=" + logConfig);
            }
            cmd.add("-cp");
            cmd.add(cp);
            cmd.add(TestTeamIntegrationChild.class.getName());
            cmd.add(source);
            cmd.add(Long.toString(processSeconds * 1000L));
            cmd.add(dir.getAbsolutePath());
            cmd.add(repo);
            cmd.add(Integer.toString(delay));
            cmd.add(Integer.toString(SEG_COUNT));

            LOGGER.atInfo().log("Execute: " + source + " " + processSeconds * 1000 + " " + dir.getAbsolutePath() + " " + repo + " " + delay + " " + SEG_COUNT);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
            pb.redirectErrorStream(true);
            p = pb.start();
        }

        @Override
        public void run() {
            try {
                result = p.waitFor();
            } catch (Exception ex) {
                result = -1;
            }
            if (result != 200) {
                LOGGER.atError().log("Error result from " + source);
            } else {
                LOGGER.atInfo().log("==================== " + source + " finished OK ====================");
            }
            finished = true;
        }

        public void end() {
            if (p.isAlive()) {
                p.destroyForcibly();
            }
        }
    }

    static CheckOrphanedCallback checkOrphanedCallback = new CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return true;
        }

        public boolean existEntryInProject(EntryKey key) {
            return true;
        }
    };

    interface Team {
        List<String> listRevisions(String from) throws Exception;

        void checkout(String rev) throws Exception;

        void update() throws Exception;

        File getDir();
    }

    public static class GitTeam implements Team {
        final Repository repository;
        final File dir;

        public GitTeam(File dir) throws Exception {
            this.dir = dir;
            repository = Git.open(dir).getRepository();
        }

        public List<String> listRevisions(String from) throws Exception {
            try (Git git = new Git(repository)) {
                LogCommand cmd = git.log();
                List<String> result = new ArrayList<>();
                for (RevCommit commit : cmd.call()) {
                    if (commit.getName().equals(from)) {
                        break;
                    }
                    result.add(commit.getName());
                }
                Collections.reverse(result);
                return result;
            }
        }

        public void update() throws Exception {
            try (Git git = new Git(repository)) {
                git.fetch().call();
                git.checkout().setName(GITRemoteRepository2.getDefaultBranchName(repository)).call();
            }
        }

        public void checkout(String rev) throws Exception {
            try (Git git = new Git(repository)) {
                git.checkout().setName(rev).call();
            }
        }

        public File getDir() {
            return dir;
        }
    }

    public static class SvnTeam implements Team {
        SVNClientManager ourClientManager;
        File dir;

        public SvnTeam(File dir, String url) throws Exception {
            this.dir = dir;
            RepositoryDefinition def = getDef(url, "svn", "/", "/");
            String predefinedUser = def.getOtherAttributes().get(new QName("svnUsername"));
            String predefinedPass = def.getOtherAttributes().get(new QName("svnPassword"));
            ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
            ISVNAuthenticationManager authManager = new SVNAuthenticationManager(predefinedUser,
                    predefinedPass);
            ourClientManager = SVNClientManager.newInstance(options, authManager);
        }

        public List<String> listRevisions(String from) throws Exception {
            final List<String> result = new ArrayList<>();
            ourClientManager.getLogClient().doLog(
                    new File[] { new File(dir, "omegat/project_save.tmx") },
                    SVNRevision.create(Long.parseLong(from)), SVNRevision.HEAD, false, false,
                    Integer.MAX_VALUE, new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry en) throws SVNException {
                            result.add("" + en.getRevision());
                        }
                    });
            return result;
        }

        public void update() throws Exception {
            ourClientManager.getUpdateClient().doUpdate(dir, SVNRevision.HEAD, SVNDepth.INFINITY, false,
                    false);
        }

        public void checkout(String rev) throws Exception {
            ourClientManager.getUpdateClient().doUpdate(dir, SVNRevision.create(Long.parseLong(rev)),
                    SVNDepth.INFINITY, false, false);
        }

        public File getDir() {
            return dir;
        }
    }
}
