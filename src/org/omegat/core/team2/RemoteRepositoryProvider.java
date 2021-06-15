/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.IRemoteRepository2.NetworkException;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.StringUtil;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Class for process some repository commands.
 *
 * Path, local path, repository path can be directory or one file only. Directory should be declared like
 * 'source/', file should be declared like 'source/text.po'.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RemoteRepositoryProvider {
    public static final String REPO_SUBDIR = ".repositories/";
    public static final String REPO_PREPARE_SUBDIR = ".repositories/prep/";
    public static final String REPO_GIT_SUBDIR = ".git/";
    public static final String REPO_SVN_SUBDIR = ".svn/";

    final File projectRoot;
    final ProjectTeamSettings teamSettings;
    final List<RepositoryDefinition> repositoriesDefinitions;
    final List<IRemoteRepository2> repositories = new ArrayList<>();
    /**
     * exclude some path like .git, .svn, project_save.tmx and glossary.txt when copying files from repo to project.
     */
    private String[] forceExcludes = {};
    private List<Mapping> mappingsWithFileListSnapshot;

    public RemoteRepositoryProvider(File projectRoot, List<RepositoryDefinition> repositoriesDefinitions, ProjectProperties props)
            throws Exception {
        this(projectRoot, repositoriesDefinitions);
        setForceExcludesFromProjectProperties(props);

    }
    public RemoteRepositoryProvider(File projectRoot, List<RepositoryDefinition> repositoriesDefinitions) throws Exception {
        this.projectRoot = projectRoot;
        teamSettings = new ProjectTeamSettings(new File(projectRoot, REPO_SUBDIR));
        this.repositoriesDefinitions = repositoriesDefinitions;

        checkDefinitions();
        initializeRepositories();
    }

    public void setForceExcludesFromProjectProperties(ProjectProperties props) {
        this.forceExcludes = new String[]{
                '/' + RemoteRepositoryProvider.REPO_SUBDIR,
                '/' + RemoteRepositoryProvider.REPO_GIT_SUBDIR, '/' + RemoteRepositoryProvider.REPO_SVN_SUBDIR,
                '/' + OConsts.FILE_PROJECT,
                '/' + props.getProjectInternalRelative() + OConsts.STATUS_EXTENSION,
                '/' + props.getWritableGlossaryFile().getUnderRoot(),
                '/' + props.getTargetDir().getUnderRoot()};
    }

    public ProjectTeamSettings getTeamSettings() {
        return teamSettings;
    }

    /**
     * Check repository definitions in the project. TODO: define messages for user
     */
    protected void checkDefinitions() {
        Set<String> dirs = new TreeSet<>();
        for (RepositoryDefinition r : repositoriesDefinitions) {
            if (StringUtil.isEmpty(r.getUrl())) {
                throw new RuntimeException("There is no repository url");
            }
            if (!dirs.add(getRepositoryDir(r).getAbsolutePath())) {
                throw new RuntimeException("Duplicate repository URL");
            }
            for (RepositoryMapping m : r.getMapping()) {
                if (m.getLocal() == null || m.getRepository() == null) {
                    throw new RuntimeException("Invalid mapping: local and/or remote is not set.");
                }
            }
        }
    }

    /**
     * Initialize repositories instances.
     */
    protected void initializeRepositories() throws Exception {
        for (RepositoryDefinition r : repositoriesDefinitions) {
            IRemoteRepository2 repo = RemoteRepositoryFactory.create(r.getType());
            repo.init(r, getRepositoryDir(r), teamSettings);
            repositories.add(repo);
        }
    }

    /**
     * Find mappings for specified path.
     */
    protected List<Mapping> getMappings(String path, String... forceExcludes) {
        List<Mapping> result = new ArrayList<>();
        for (int i = 0; i < repositoriesDefinitions.size(); i++) {
            RepositoryDefinition rd = repositoriesDefinitions.get(i);
            for (RepositoryMapping repoMapping : rd.getMapping()) {
                Mapping m = new Mapping(path, repositories.get(i), rd, repoMapping, forceExcludes);
                if (m.matches()) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * Found mapping must be one.
     */
    protected Mapping oneMapping(String path) {
        List<Mapping> mappings = getMappings(path);
        if (mappings.size() > 1) {
            throw new RuntimeException("Multiple mappings for file '" + path + "'");
        } else if (mappings.isEmpty()) {
            throw new RuntimeException("There is no mapping for file '" + path + "'");
        }

        return mappings.get(0);
    }

    /**
     * Checks if path is under mapping.
     */
    public boolean isUnderMapping(String path) {
        return !getMappings(path).isEmpty();
    }

    public void cleanPrepared() throws IOException {
        FileUtils.deleteDirectory(new File(projectRoot, REPO_PREPARE_SUBDIR));
    }

    /**
     * Saves file into 'prepared' dir.
     */
    public File toPrepared(File inFile) throws IOException {
        File dir = new File(projectRoot, REPO_PREPARE_SUBDIR);
        dir.mkdirs();
        File out = File.createTempFile("prepared", "", dir);
        FileUtils.copyFile(inFile, out);
        return out;
    }

    /**
     * Switch all repositories into latest version. Will continue in the event of an error, unless that error is a
     * {@link NetworkException} in which case it will throw immediately. If any other exceptions occur while updating
     * the repos, the first will be thrown after all repos have been processed.
     */
    public void switchAllToLatest() throws Exception {
        List<Exception> errors = new ArrayList<>();

        for (IRemoteRepository2 r : repositories) {
            try {
                r.switchToVersion(null);
            } catch (NetworkException e) {
                throw e;
            } catch (Exception e) {
                errors.add(e);
                Log.logErrorRB("TEAM_UPDATE_REPO_ERROR", e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
           throw errors.get(0);
        }
    }

    /**
     * Switch repository that contains path to specified version. If version is null, need to switch to latest
     * version. Returns the path in the remote repository ( /path/to/omegatproject/.repositories/url/filepath
     */
    public File switchToVersion(String filePath, String version) throws Exception {
        return oneMapping(filePath).switchToVersion(version);
    }

    /**
     * Commit specific file after rebase. Used for omegat/project_save.tmx, glossaries, etc.
     */
    public String commitFileAfterVersion(String path, String commentText, String... onVersions) throws Exception {
        return oneMapping(path).repo.commit(onVersions, commentText);
    }

    /**
     * Commit set of files without rebase - just local version. Used for target/*, etc.
     */
    public void commitFiles(String path, String commentText) throws Exception {
        Map<String, IRemoteRepository2> repos = new TreeMap<>();
        // collect repositories one for one mapping
        for (Mapping m : getMappings(path)) {
            repos.put(m.repoDefinition.getUrl(), m.repo);
        }
        // commit only unique repositories
        for (IRemoteRepository2 repo : repos.values()) {
            repo.commit(null, commentText);
        }
    }

    /**
     * Copies all files under specified path that are mapped to local directory.
     * If path is empty (i.e. full project), also file deletions since last copy are propagated, i.e. if remote repo has
     * file deletions, those files are deleted locally as well.
     *
     * @param localPath
     *            directory name or file name
     */
    public void copyFilesFromReposToProject(String localPath) throws IOException {
        String[] myForceExcludes = "".equals(localPath) ? forceExcludes : new String[]{};
        for (Mapping m : getMappings(localPath, myForceExcludes)) {
            m.copyFromRepoToProject();
            if ("".equals(localPath)) {
                m.propagateDeletes();
            }
        }
    }

    /**
     * Copy all mappings that under specified directory path into repository directory.
     *
     * @param localPath
     *            directory name or file name
     * @param eolConversionCharset
     *            not null if EOL conversion required. EOL will be converted to repository-specific for
     *            existing files, and to platform-specific for new files
     */
    public void copyFilesFromProjectToRepos(String localPath, String eolConversionCharset) throws Exception {
        for (Mapping m : getMappings(localPath)) {
            m.copyFromProjectToRepo(eolConversionCharset);
        }
    }

    /**
     * Get version of specified file.
     */
    public String getVersion(String file) throws Exception {
        return oneMapping(file).getVersion();
    }

    protected void copyFile(File from, File to, String eolConversionCharset) throws IOException {
        if (eolConversionCharset != null) {
            // charset defined - text file for EOL conversion
            FileUtil.copyFileWithEolConversion(from, to, Charset.forName(eolConversionCharset));
        } else {
            // charset not defined - binary file
            FileUtils.copyFile(from, to);
        }
    }

    protected void addForCommit(IRemoteRepository2 repo, String path) throws Exception {
        repo.addForCommit(path);
    }

    protected File getRepositoryDir(RepositoryDefinition repo) {
        String path = repo.getUrl().replaceAll("[^A-Za-z0-9\\.]", "_").replaceAll("__+", "_");
        return new File(new File(projectRoot, REPO_SUBDIR), path);
    }

    static String withLeadingSlash(String s) {
        return s.startsWith("/") ? s : "/" + s;
    }

    static String withTrailingSlash(String s) {
        return s.endsWith("/") ? s : s + "/";
    }

    static String withoutLeadingSlash(String s) {
        return s.startsWith("/") ? s.substring(1) : s;
    }

    static String withoutTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    static String withSlashes(String s) {
        return withTrailingSlash(withLeadingSlash(s));
    }

    static String withoutSlashes(String s) {
        return withoutTrailingSlash(withoutLeadingSlash(s));
    }

    /**
     * Heper function for file deletions. Gives the absolute local path to a file that is to be deleted, based on a
     * relative file, the project root and the mapping.
     * @param relativeFile a file relative to repository root, as returned by the IRemoterepository.getRecentlyDeletedFiles
     * @param projectRoot local project root
     * @param repoPath mapped path in repo
     * @param localPath mapped path in local project
     * @return the file to be deleted locally
     */
    static File relativeRemoteToAbsoluteLocal(String relativeFile, File projectRoot, String repoPath, String localPath) {
        repoPath = withSlashes(repoPath);
        localPath = withSlashes(localPath);

        Path p = new File(repoPath).toPath();
        Path rp = new File(withLeadingSlash(relativeFile)).toPath();

        Path newRelativeFile = p.relativize(rp);

        File root = new File(projectRoot, withoutLeadingSlash(localPath));
        return new File(root, withoutLeadingSlash(newRelativeFile.toString()));
    }


    /**
     * Class for mapping by specified local path.
     */
    class Mapping {
        final String filterPrefix;
        final IRemoteRepository2 repo;
        final RepositoryDefinition repoDefinition;
        final RepositoryMapping repoMapping;
        final List<String> forceExcludes;

        Mapping(String path, IRemoteRepository2 repo, RepositoryDefinition repoDefinition,
                RepositoryMapping repoMapping, String... forceExcludes) {
            this.repo = repo;
            this.repoDefinition = repoDefinition;
            this.repoMapping = repoMapping;
            this.forceExcludes = new ArrayList<>();
            /*
             * Find common part - it should be one of path or local. If path and local have only common begin,
             * they will not be mapped. I.e. path=source/ and local=source/one - it's okay, path=source/one/
             * and local=source/ - also okay, but path=source/one/ and local=source/two - wrong.
             */
            path = withSlashes(path);
            String local = withSlashes(repoMapping.getLocal());
            if (path.equals("/")) {
                // root(full project path) mapping
                filterPrefix = "/";
                this.forceExcludes.addAll(Arrays.asList(forceExcludes));
            } else if (local.equals(path)) {
                // path equals mapping (path="source/" for "source/"=>"...")
                filterPrefix = "/";
                this.forceExcludes.addAll(getTruncatedExclusions(local, forceExcludes));
            } else if (local.startsWith(path)) {
                // path shorter than local and is directory (path="source/" for "source/first/..."=>"...")
                filterPrefix = "/";
                this.forceExcludes.addAll(getTruncatedExclusions(local, forceExcludes));
            } else if (path.startsWith(local)) {
                // local is shorter than path and is directory (path="omegat/project_save" for
                // "omegat/"=>"...")
                filterPrefix = withSlashes(path.substring(local.length()));
                this.forceExcludes.addAll(Arrays.asList(forceExcludes));
            } else if (local.equals("/")) {
                // root(full project path) mapping (""=>"...")
                filterPrefix = path;
                this.forceExcludes.addAll(Arrays.asList(forceExcludes));
            } else {
                // otherwise path doesn't correspond with repoMapping
                filterPrefix = null;
            }
        }

        List<String> getTruncatedExclusions(String prefix, String... excludes) {
            String normalizedPrefix = withSlashes(prefix);
            return Stream.of(excludes).map(RemoteRepositoryProvider::withLeadingSlash)
                    .filter(e -> e.startsWith(normalizedPrefix))
                    .map(e -> withLeadingSlash(e.substring(normalizedPrefix.length())))
                    .collect(Collectors.toList());
        }

        /**
         * Is path matched with mapping ?
         */
        public boolean matches() {
            return filterPrefix != null;
        }

        public void copyFromRepoToProject() throws IOException {
            if (!matches()) {
                throw new RuntimeException("Path doesn't match with mapping");
            }
            // Remove leading slashes on child args to avoid doing `new
            // File("foo", "/")` which treats the "/" as an actual child element
            // name and prevents proper slash normalization later on.
            File from = new File(getRepositoryDir(repoDefinition), withoutLeadingSlash(repoMapping.getRepository()));
            File to = new File(projectRoot, withoutLeadingSlash(repoMapping.getLocal()));
            if (from.isDirectory()) {
                // directory mapping
                List<String> excludes = new ArrayList<>(repoMapping.getExcludes());
                excludes.addAll(forceExcludes);
                copy(from, to, filterPrefix, repoMapping.getIncludes(), excludes, null);
            } else if (!from.exists()) {
                //e.g. you opened an omegat.properties to download a team project, but it refers to a remote repo location that doesn't exist.
                throw new RuntimeException("Location '" + withoutLeadingSlash(repoMapping.getRepository()) + "' does not exist in repository " + repoDefinition.getUrl());
            } else {
                // file mapping
                if (!filterPrefix.equals("/")) {
                    throw new RuntimeException(
                            "Filter prefix should have been / for file mapping, but was " + filterPrefix);
                }
                copyFile(from, to, null);
            }
        }

        public void copyFromProjectToRepo(String eolConversionCharset) throws Exception {
            if (!matches()) {
                throw new RuntimeException("Path doesn't match with mapping");
            }
            // Remove leading slashes on child args to avoid doing `new
            // File("foo", "/")` which treats the "/" as an actual child element
            // name and prevents proper slash normalization later on.
            File from = new File(projectRoot, withoutLeadingSlash(repoMapping.getLocal()));
            File to = new File(getRepositoryDir(repoDefinition), withoutLeadingSlash(repoMapping.getRepository()));
            if (from.isDirectory()) {
                // directory mapping or full mapping
                List<String> files = copy(from, to, filterPrefix, repoMapping.getIncludes(), repoMapping.getExcludes(),
                        eolConversionCharset);
                String repoSubdir = withoutLeadingSlash(repoMapping.getRepository());
                if (!"".equals(repoSubdir)) {
                    repoSubdir = withTrailingSlash(repoSubdir);
                }
                for (String f : files) {
                    addForCommit(repo, repoSubdir + withoutSlashes(f));
                }
            } else {
                // file mapping
                if (!filterPrefix.equals("/")) {
                    throw new RuntimeException(
                            "Filter prefix should have been '/' for file mapping, but was '" + filterPrefix + "'");
                }
                copyFile(from, to, eolConversionCharset);
                addForCommit(repo, withoutSlashes(repoMapping.getRepository()));
            }
        }

        public File switchToVersion(String version) throws Exception {
            repo.switchToVersion(version);
            File to = new File(getRepositoryDir(repoDefinition), repoMapping.getRepository());
            return new File(to, filterPrefix);
        }

        public String getVersion() throws Exception {
            return repo.getFileVersion(new File(repoMapping.getRepository(), filterPrefix).getPath());
        }

        /**
         * @return Relative paths of copied files, <em>with <code>/</code> at
         *         start and end</em>
         */
        protected List<String> copy(File from, File to, String prefix, List<String> includes,
                List<String> excludes, String eolConversionCharset) throws IOException {
            prefix = withSlashes(prefix);
            List<String> relativeFiles = FileUtil.buildRelativeFilesList(from, includes, excludes);
            List<String> copied = new ArrayList<>();
            for (String rf : relativeFiles) {
                rf = withSlashes(rf);
                if (rf.startsWith("/.repositories/")) {
                    continue; // list from root - shouldn't travel to .repositories/
                }
                if (prefix.isEmpty() || prefix.equals("/") || rf.startsWith(prefix)) {
                    copyFile(new File(from, rf), new File(to, rf), eolConversionCharset);
                    copied.add(rf);
                }
            }
            return copied;
        }

        /**
         * Delete files locally that have been deleted in the remote repository since the last call to this function.
         * Local checkout must be a full checkout. Don't call this method when doing checkouts of individual files.
         */
        public void propagateDeletes() {
            try {
                String[] toDeleteFiles = repo.getRecentlyDeletedFiles();

                for (String relativeFile : toDeleteFiles) {
                    if (FileUtil.checkFileInclude(relativeFile, FileUtil.compileFileMasks(repoMapping.getIncludes()), FileUtil.compileFileMasks(repoMapping.getExcludes()))) {
                        File fileToDelete = RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal(relativeFile, projectRoot, repoMapping.getRepository(), repoMapping.getLocal());
                        deleteFile(fileToDelete);
                    }
                }
            } catch (Exception e) {
                Log.log(e);
            }
        }

        private void deleteFile(File f) {
            if (!f.exists()) {
                return;
            }
            try {
                if (f.isFile()) {
                    f.delete();
                } else {
                    FileUtils.deleteDirectory(f);
                }
            } catch (Exception e) {
                Log.logInfoRB("LOG_ERROR_DELETE_FILE", f.toString());
            }
        }
    }
}
