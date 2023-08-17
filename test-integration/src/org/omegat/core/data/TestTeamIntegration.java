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
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
 * <p>
 * TODO: "svn: E160028: Commit failed" during commit
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 *
 */
public final class TestTeamIntegration {

    private TestTeamIntegration() {
    }

    private final static Pattern URL_PATTERN = Pattern
            .compile("(http(s)?|svn(\\+ssh)?)" + "://(?<username>.+?)(:(?<password>.+?))?@.+");

    static final String DIR = "/tmp/teamtest";
    static final List<String> REPO = new ArrayList<>();
    static final String MAP_REPO = System.getProperty("omegat.test.map.repo", null);
    static final String MAP_REPO_TYPE = System.getProperty("omegat.test.map.type", "http");
    static final String MAP_FILE = System.getProperty("omegat.test.map.file", null);
    static final int PROCESS_SECONDS = Optional.ofNullable(System.getProperty("omegat.test.duration"))
            .map(Integer::parseInt).orElse(4 * 60 * 60);
    static final int MAX_DELAY_SECONDS = 15;
    static final int SEG_COUNT = 4;

    static final Language SRC_LANG = new Language("en");
    static final Language TRG_LANG = new Language("be");

    static final String[] THREADS = new String[] { "s1", "s2", "s3" };

    static Team repo;

    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("java.util.logging.config.file", null);
        String repository = System.getProperty("omegat.test.repo", null);
        if (repository == null) {
            System.err.println("Property omegat.test.repo is mandatory.");
            System.exit(1);
        }
        REPO.add(repository);
        String altRepo = System.getProperty("omegat.test.repo.alt", null);
        if (altRepo != null) {
            REPO.add(altRepo);
        }
        String startVersion = prepareRepo();

        Run[] runs = new Run[THREADS.length];
        for (int i = 0; i < THREADS.length; i++) {
            runs[i] = new Run(THREADS[i], new File(DIR, THREADS[i]), MAX_DELAY_SECONDS,
                    REPO.get(i % REPO.size()), logConfig);
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
                        System.err.println("==================== EXIT BY ERROR ====================");
                        System.exit(1);
                    }
                } else {
                    alive = true;
                }
            }
            Thread.sleep(500);
        } while (alive);

        repo = createRepo2(REPO.get(0), new File(DIR, "repo"));
        repo.update();

        System.err.println("Check repo");

        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        checkRepo(startVersion);

        System.err.println("Processed successfully");
    }

    /**
     * Check repository after children processed.
     */
    static void checkRepo(String startVersion) throws Exception {
        List<String> segments = new ArrayList<String>();
        for (String th : THREADS) {
            for (int c = 0; c < SEG_COUNT; c++) {
                segments.add(th + "/" + c);
            }
        }

        Map<String, List<Long>> data = new TreeMap<String, List<Long>>();
        for (String th : segments) {
            data.put(th, new ArrayList<Long>());
            data.get(th).add(0L);
        }
        data.put(TestTeamIntegrationChild.CONCURRENT_NAME, new ArrayList<Long>());
        data.get(TestTeamIntegrationChild.CONCURRENT_NAME).add(0L);

        ProjectTMX tmx = null;
        int tmxCount = 0;
        for (String rev : repo.listRevisions(startVersion)) {
            repo.checkout(rev);
            tmx = new ProjectTMX(SRC_LANG, TRG_LANG, false,
                    new File(repo.getDir(), "omegat/project_save.tmx"), checkOrphanedCallback);

            for (String th : data.keySet()) {
                TMXEntry en = tmx.getDefaultTranslation(th);
                long value = en == null ? 0 : Long.parseLong(en.translation);
                data.get(th).add(value);
            }
            tmxCount++;
        }

        System.err.println("Values :");
        for (String th : segments) {
            out(th);
        }
        System.err.println();
        for (int i = 0; i < tmxCount; i++) {
            for (String th : segments) {
                out(data.get(th).get(i));
            }
            System.err.println();
        }
        boolean ok = true;
        for (String th : data.keySet()) {
            long prev = 0;
            for (long v : data.get(th)) {
                if (v < prev) {
                    System.err.println("Wrong order in " + th);
                    ok = false;
                    break;
                } else {
                    prev = v;
                }
            }
        }
        if (ok) {
            System.err.println("All commits look good");
        }
    }

    static void out(Object v) {
        String s = v.toString();
        System.err.print("                  ".substring(0, 14 - s.length()) + s + " ");
    }

    /**
     * Prepare repository.
     */
    static String prepareRepo() throws Exception {
        File tmp = new File(DIR);
        FileUtils.deleteDirectory(tmp);
        if (tmp.exists()) {
            throw new Exception("Impossible to delete test dir");
        }
        if (!tmp.mkdirs()) {
            throw new Exception("Impossible to create test dir");
        }
        File origDir = new File(tmp, "repo");
        if (!origDir.mkdir()) {
            throw new Exception("Impossible to create test dir");
        }

        ProjectProperties config = createConfig(REPO.get(0), origDir);

        RemoteRepositoryProvider remote = new RemoteRepositoryProvider(config.getProjectRootDir(),
                config.getRepositories(), config);
        remote.switchAllToLatest();

        new File(origDir, "omegat").mkdirs();
        File f = new File(origDir, "omegat/project_save.tmx");
        TMXWriter2 wr = new TMXWriter2(f, SRC_LANG, TRG_LANG, true, false, true);
        wr.close();

        ProjectFileStorage.writeProjectFile(config);

        remote.copyFilesFromProjectToRepos("omegat.project", null);
        remote.commitFiles("omegat.project", "Prepare for team test");
        remote.copyFilesFromProjectToRepos("omegat/project_save.tmx", null);
        remote.commitFiles("omegat/project_save.tmx", "Prepare for team test");

        return remote.getVersion("omegat/project_save.tmx");
    }

    static ProjectProperties createConfig(String repoUrl, File dir) throws Exception {
        ProjectProperties config = new ProjectProperties(dir);
        config.setSourceLanguage(SRC_LANG);
        config.setTargetLanguage(TRG_LANG);
        config.setRepositories(new ArrayList<>());
        if (MAP_REPO == null || MAP_FILE == null) {
            config.getRepositories().add(getDef(repoUrl, predictMainType(repoUrl), "", ""));
        } else {
            config.getRepositories().add(getDef(repoUrl, predictMainType(repoUrl), "/", "/"));
            config.getRepositories().add(getDef(MAP_REPO, MAP_REPO_TYPE, MAP_FILE, "source/" + MAP_FILE));
        }
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
            FileUtils.copyFile(new File(DIR + "/repo/omegat.project"),
                    new File(DIR + "/" + source + "/omegat.project"));
            if (!new File(DIR + "/" + source + "/omegat/").mkdirs()) {
                throw new Exception("Impossible to create test dir");
            }
            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.add("-Duser.name=" + source);
            if (logConfig != null) {
                cmd.add("-Djava.util.logging.config.file=" + logConfig);
            }
            cmd.add("-cp");
            cmd.add(cp);
            cmd.add(TestTeamIntegrationChild.class.getName());
            cmd.add(source);
            cmd.add(Long.toString(PROCESS_SECONDS * 1000L));
            cmd.add(dir.getAbsolutePath());
            cmd.add(repo);
            cmd.add(Integer.toString(delay));
            cmd.add(Integer.toString(SEG_COUNT));

            System.err.println("Execute: " + source + " " + (PROCESS_SECONDS * 1000) + " "
                    + dir.getAbsolutePath() + " " + repo + " " + delay + " " + SEG_COUNT);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
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
                System.err.println("Error result from " + source);
            } else {
                System.err.println("==================== " + source + " finished OK ====================");
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
                List<String> result = new ArrayList<String>();
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
            ISVNAuthenticationManager authManager = new SVNAuthenticationManager(def, predefinedUser,
                    predefinedPass, null);
            ourClientManager = SVNClientManager.newInstance(options, authManager);
        }

        public List<String> listRevisions(String from) throws Exception {
            final List<String> result = new ArrayList<String>();
            ourClientManager.getLogClient().doLog(
                    new File[] { new File(repo.getDir(), "omegat/project_save.tmx") },
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
