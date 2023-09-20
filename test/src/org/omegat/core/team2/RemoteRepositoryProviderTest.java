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

package org.omegat.core.team2;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.impl.FileRepository;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

@RunWith(Parameterized.class)
public final class RemoteRepositoryProviderTest {
    String testProjectRoot;
    String testProjectRepositoryDir, testProjectRepositoryDir2;

    String repoUrlDir = "url";
    String repoUrlDir2 = "otherurl";

    List<RepositoryDefinition> repos;
    List<String> files;
    VirtualRemoteRepositoryProvider provider;

    List<String> copyFrom = new ArrayList<>();
    List<String> copyTo = new ArrayList<>();
    int copyCheckedIndex;

    // User can write mapping with or without leading and trailing directory separators.
    // Lets test these variants, and test mappings to different levels of subdirectories
    @Parameterized.Parameters(name = "{index}: remote subdir={0}, local path prefix={1}, remote path prefix={2}," +
            " local path postfix={3}, remote path postfix={4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"", "", "", "", ""}, {"", "/", "", "", ""}, {"", "", "/", "", ""}, {"", "", "", "/", ""},
                {"", "", "", "", "/"}, {"remoteSubdir/", "", "", "", ""}, {"sub/subsub/", "/", "", "", ""},
                {"sub/subsub/", "", "/", "", ""}
        });
    }

    String remoteSubdir;
    String localMappingPrefix, remoteMappingPrefix, localMappingPostfix, remoteMappingPostfix;

    public RemoteRepositoryProviderTest(String subdir, String localMappingPrefix, String remoteMappingPrefix,
                                        String localMappingPostfix, String remoteMappingPostfix) {
        this.remoteSubdir = subdir;
        this.localMappingPrefix = localMappingPrefix;
        this.remoteMappingPrefix = remoteMappingPrefix;
        this.localMappingPostfix = localMappingPostfix;
        this.remoteMappingPostfix = remoteMappingPostfix;
    }

    void createRemoteRepoFiles() throws IOException {
        createFile(testProjectRepositoryDir + remoteSubdir + "omegat.project");
        createFile(testProjectRepositoryDir + remoteSubdir + ".git/gitstuff");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/file1.txt");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/file1.txt.bak");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt.bak");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/subdir/3.jpg");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/subdir/4.png");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/asubdir/subdir/3.jpg");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/3.jpg");
        createFile(testProjectRepositoryDir + remoteSubdir + "source/4.png");
        createFile(testProjectRepositoryDir + remoteSubdir + "omegat/project_save.tmx");
        createFile(testProjectRepositoryDir + remoteSubdir + "glossary/sub/myglossary.txt");
        createFile(testProjectRepositoryDir2 + "otherprojectfile.txt");
    }

    void createLocalRepoFiles() throws IOException {
        createFile(testProjectRoot + "omegat.project");
        createFile(testProjectRoot + "source/file1.txt");
        createFile(testProjectRoot + "source/file1.txt.bak");
        createFile(testProjectRoot + "source/subdir/file2.txt");
        createFile(testProjectRoot + "source/subdir/file2.txt.bak");
        createFile(testProjectRoot + "source/subdir/3.jpg");
        createFile(testProjectRoot + "source/subdir/4.png");
        createFile(testProjectRoot + "source/asubdir/subdir/3.jpg");
        createFile(testProjectRoot + "source/3.jpg");
        createFile(testProjectRoot + "source/4.png");
        createFile(testProjectRoot + "source/otherproject/file.txt");
        createFile(testProjectRoot + "omegat/project_save.tmx");
        createFile(testProjectRoot + "glossary/sub/myglossary.txt");
    }

    void map_normalRemoteRepoAndExtraRemoteRepoWithExcludesWithDirectorySeparatorPrefix() {
        addRepo(localMappingPrefix + "" + localMappingPostfix, repoUrlDir,
                remoteMappingPrefix + remoteSubdir + remoteMappingPostfix, "**/*.bak", "/*.png", "/subdir/3.jpg");
        addRepo(localMappingPrefix + "source/otherproject", repoUrlDir2,
                remoteMappingPrefix + "" + remoteMappingPostfix, "**/*.bak", "/*.png", "/subdir/3.jpg");
    }

    void map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix() {
        addRepo(localMappingPrefix + "" + localMappingPostfix, repoUrlDir,
                remoteMappingPrefix + remoteSubdir + remoteMappingPostfix, "**/*.bak", "*.png", "subdir/3.jpg");
        addRepo(localMappingPrefix + "source/otherproject" + localMappingPostfix, repoUrlDir2,
                remoteMappingPrefix + "" + remoteMappingPostfix, "**/*.bak", "*.png", "subdir/3.jpg");
    }

    void map_SingleFileRemoteRepo() {
        addRepo(localMappingPrefix + "source/otherproject/file.txt" + localMappingPostfix, repoUrlDir2,
                remoteMappingPrefix + "otherprojectfile.txt" + remoteMappingPostfix);
    }

    @Test
    public void testCopyFileFromReposToProject() throws Exception {
        //test normal case when OmegaT downloads team project and only a few project files are copied (one at a time)
        //omegat.project (forceExcludes) should not be filtered in this case!
        createRemoteRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromReposToProject("omegat.project");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "omegat.project", testProjectRoot + "omegat.project");
        checkCopyEnd();
    }

    @Test
    public void testCopyAllFromReposToProjectWithExcludes() throws Exception {
        //test normal case when OmegaT syncs team project
        //'**/*.bak' == all .bak, '*.png' == all png, 'subdir/3.jpg' == '*/subdir/3.jpg' filtered.
        //When localPath="", then also forceExludes active!
        createRemoteRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromReposToProject("");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "glossary/sub/myglossary.txt",
                testProjectRoot + "glossary/sub/myglossary.txt");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/3.jpg", testProjectRoot + "source/3.jpg");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/file1.txt", testProjectRoot + "source/file1.txt");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt",
                testProjectRoot + "source/subdir/file2.txt");
        checkCopy(testProjectRepositoryDir2 + "otherprojectfile.txt",
                testProjectRoot + "source/otherproject/otherprojectfile.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyAllFromReposToProjectWithSExcludes() throws Exception {
        //test normal case when OmegaT syncs team project
        //'**/*.bak' == all .bak, '/*.png' == first level png, '/subdir/3.jpg' == '/subdir/3.jpg' filtered.
        //When localPath="", then also forceExludes active!
        createRemoteRepoFiles();
        map_normalRemoteRepoAndExtraRemoteRepoWithExcludesWithDirectorySeparatorPrefix();
        provider.copyFilesFromReposToProject("");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "glossary/sub/myglossary.txt",
                testProjectRoot + "glossary/sub/myglossary.txt");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/3.jpg", testProjectRoot + "source/3.jpg");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/4.png", testProjectRoot + "source/4.png");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/asubdir/subdir/3.jpg",
                testProjectRoot + "source/asubdir/subdir/3.jpg");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/file1.txt", testProjectRoot + "source/file1.txt");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/subdir/3.jpg",
                testProjectRoot + "source/subdir/3.jpg"); //not filtered since /source is not matched
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/subdir/4.png",
                testProjectRoot + "source/subdir/4.png");
        checkCopy(testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt",
                testProjectRoot + "source/subdir/file2.txt");
        checkCopy(testProjectRepositoryDir2 + "otherprojectfile.txt",
                testProjectRoot + "source/otherproject/otherprojectfile.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyRenamedFileFromRepoToProject() throws Exception {
        createRemoteRepoFiles();
        map_SingleFileRemoteRepo();
        provider.copyFilesFromReposToProject("");
        checkCopy(testProjectRepositoryDir2 + "otherprojectfile.txt", testProjectRoot + "source/otherproject/file.txt");
        checkCopyEnd();
    }

    //usages of copyFilesFromProjectToRepos in OmegaT:
    //example1: compileProjectAndCommit: copy target files to project; can be null if target outside project root,
    //          but in that case the option to commit is disabled.
    //example2: commitSourceFiles: copy source files to project; can be null if sources outside project root,
    //          but in that case the option to commit is disabled.
    //example3: project_save.tmx (rebase and commit project)+ EOL conversion
    //example4: project_save.tmx or glossaries (commitPrepared)
    //example5: IntegrationTest preparing remote repo: omegat.project and omegat/project_save.tmx

    @Test
    public void testCopyDirFromProjectToReposWithExcludes() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("source", null);
        checkCopy(testProjectRoot + "source/3.jpg", testProjectRepositoryDir + remoteSubdir + "source/3.jpg");
        checkCopy(testProjectRoot + "source/file1.txt", testProjectRepositoryDir + remoteSubdir + "source/file1.txt");
        //since source/otherproject/file.txt matches both mappings, it is copied to both projects!
        checkCopy(testProjectRoot + "source/otherproject/file.txt",
                testProjectRepositoryDir + remoteSubdir + "source/otherproject/file.txt");
        checkCopy(testProjectRoot + "source/subdir/file2.txt",
                testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt");
        checkCopy(testProjectRoot + "source/otherproject/file.txt", testProjectRepositoryDir2 + "file.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyDirFromProjectToReposWithExcludesWithDirectorySeparatorPrefix() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraRemoteRepoWithExcludesWithDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("source", null);
        checkCopy(testProjectRoot + "source/3.jpg", testProjectRepositoryDir + remoteSubdir + "source/3.jpg");
        checkCopy(testProjectRoot + "source/4.png", testProjectRepositoryDir + remoteSubdir + "source/4.png");
        checkCopy(testProjectRoot + "source/asubdir/subdir/3.jpg",
                testProjectRepositoryDir + remoteSubdir + "source/asubdir/subdir/3.jpg");
        checkCopy(testProjectRoot + "source/file1.txt", testProjectRepositoryDir + remoteSubdir + "source/file1.txt");
        //since source/otherproject/file.txt matches both mappings, it is copied to both projects!
        checkCopy(testProjectRoot + "source/otherproject/file.txt",
                testProjectRepositoryDir + remoteSubdir + "source/otherproject/file.txt");
        //not filtered since /source is not matched
        checkCopy(testProjectRoot + "source/subdir/3.jpg",
                testProjectRepositoryDir + remoteSubdir + "source/subdir/3.jpg");
        checkCopy(testProjectRoot + "source/subdir/4.png",
                testProjectRepositoryDir + remoteSubdir + "source/subdir/4.png");
        checkCopy(testProjectRoot + "source/subdir/file2.txt",
                testProjectRepositoryDir + remoteSubdir + "source/subdir/file2.txt");
        checkCopy(testProjectRoot + "source/otherproject/file.txt", testProjectRepositoryDir2 + "file.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("omegat.project", null);
        checkCopy(testProjectRoot + "omegat.project", testProjectRepositoryDir + remoteSubdir + "omegat.project");
        checkCopyEnd();
    }

    @Test
    public void testCopySubFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("omegat/project_save.tmx", null);
        checkCopy(testProjectRoot + "omegat/project_save.tmx",
                testProjectRepositoryDir + remoteSubdir + "omegat/project_save.tmx");
        checkCopyEnd();
    }

    @Test
    public void testCopyRenamedFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_SingleFileRemoteRepo();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(testProjectRoot + "source/otherproject/file.txt", testProjectRepositoryDir2 + "otherprojectfile.txt");
        checkCopyEnd();
    }

    /**
     * Test a case when remote omegat.project is removed, and OmegaT downloads team project.
     * Only a few project files are copied.
     * @throws Exception
     */
    @Test
    public void testCopyAndDeletePropagateReposToProject() throws Exception {
        createRemoteRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromReposToProject("omegat.project", ".NEW", false);
        checkCopy(testProjectRepositoryDir + remoteSubdir + "omegat.project", testProjectRoot + "omegat.project.NEW");
        checkCopyEnd();
    }

    @Before
    public void setUp() throws Exception {
        File dir = new File("build/testdata/repotest");
        FileUtils.deleteDirectory(dir);
        dir.mkdirs();
        testProjectRoot = dir.getAbsolutePath() + "/";
        testProjectRepositoryDir = dir.getAbsolutePath() + "/.repositories/" + repoUrlDir + "/";
        //every repository, whether file, http, git or svn, gets its own directory.
        testProjectRepositoryDir2 = dir.getAbsolutePath() + "/.repositories/" + repoUrlDir2 + "/";

        repos = new ArrayList<>();
        provider = new VirtualRemoteRepositoryProvider(repos, new ProjectProperties(new File(testProjectRoot)));
        files = new ArrayList<>();
    }

    void addRepo(String localPath, String repositoryUrlDir, String repoPath, String... excludes) {
        RepositoryMapping m = new RepositoryMapping();
        m.setLocal(localPath);
        m.setRepository(repoPath);
        m.getExcludes().addAll(Arrays.asList(excludes));
        RepositoryDefinition def = new RepositoryDefinition();
        def.setUrl(repositoryUrlDir);
        def.getMapping().add(m);
        repos.add(def);
        provider.repositories.add(new FileRepository());
    }

    void createFile(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
    }

    /**
     * Asserts if the given from -> to files are present on index copyCheckedIndex in the copy-lists that were filled by
     * our VirtualRemoteRepositoryProvider (so we're asserting if the copy-commands are called, not if the actual copy
     * has been performed, but that is almost the same.) and increases the index afterwards.
     *
     * @param from full path/filename of source file
     * @param to   full path/filename of target file
     */
    void checkCopy(String from, String to) {
        assertEquals("Wrong copy file from2", from.replace('\\', '/'),
                copyFrom.get(copyCheckedIndex).replace('\\', '/'));
        assertEquals("Wrong copy file to2", to.replace('\\', '/'),
                copyTo.get(copyCheckedIndex).replace('\\', '/'));
        copyCheckedIndex++;
    }

    /**
     * Asserts that there are no other files copied than the ones that we tested with checkCopy().
     */
    void checkCopyEnd() {
        assertEquals("Wrong copy list", copyCheckedIndex, copyFrom.size());
    }

    /**
     * Adapted RemoteRepositoryProvider that doesn't really copy files, but tracks copy commands in a list that can be
     * used for testing which files are copied.
     */
    public final class VirtualRemoteRepositoryProvider extends RemoteRepositoryProvider {
        public VirtualRemoteRepositoryProvider(List<RepositoryDefinition> repositoriesDefinitions,
                                               ProjectProperties projectProperties) throws Exception {
            super(new File(testProjectRoot), repositoriesDefinitions, projectProperties);
        }

        @Override
        protected void initializeRepositories() {
            // disable initialize for testing
        }

        @Override
        protected void copyFile(File from, File to, String eolConversionCharset) {
            copyFrom.add(from.getAbsolutePath());
            copyTo.add(to.getAbsolutePath());
        }

        @Override
        protected void addForCommit(IRemoteRepository2 repo, String path) {
        }
    }

    /**
     * ProjectProperties successor for create project on the virtual directory with specific repositories
     * definitions.
     */
    protected class ProjectPropertiesTest extends ProjectProperties {
        public ProjectPropertiesTest(List<RepositoryDefinition> repositoriesDefinitions) {
            setProjectRoot(testProjectRoot);
            setRepositories(repositoriesDefinitions);
        }
    }
}
