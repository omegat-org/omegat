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
import org.junit.runners.Parameterized;
import org.omegat.core.data.ProjectProperties;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

public class RemoteRepositoryProviderTest {
    String V;
    String VR, VR2;

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
    @Parameterized.Parameters(name= "{index}: remote subdir={0}, local path prefix={1}, remote path prefix={2}, local path postfix={3}, remote path postfix={4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "", "", "", "", ""}, { "", "/", "", "", "" }, { "", "", "/", "", "" }, { "", "", "", "/", "" }, { "", "", "", "", "/" }, { "remoteSubdir/", "", "", "", "" }, { "sub/subsub/","/", "", "", "" }, { "sub/subsub/", "", "/", "", "" }
        });
    }
    String remoteSubdir;
    String localMappingPrefix, remoteMappingPrefix, localMappingPostfix, remoteMappingPostfix;

    public RemoteRepositoryProviderTest(String subdir, String localMappingPrefix, String remoteMappingPrefix, String localMappingPostfix, String remoteMappingPostfix) {
        this.remoteSubdir= subdir;
        this.localMappingPrefix = localMappingPrefix;
        this.remoteMappingPrefix= remoteMappingPrefix;
        this.localMappingPostfix = localMappingPostfix;
        this.remoteMappingPostfix = remoteMappingPostfix;
    }

    void createRemoteRepoFiles() throws IOException {
        createFile(VR + remoteSubdir + "omegat.project");
        createFile(VR + remoteSubdir + ".git/gitstuff");
        createFile(VR + remoteSubdir + "source/file1.txt");
        createFile(VR + remoteSubdir + "source/file1.txt.bak");
        createFile(VR + remoteSubdir + "source/subdir/file2.txt");
        createFile(VR + remoteSubdir + "source/subdir/file2.txt.bak");
        createFile(VR + remoteSubdir + "source/subdir/3.jpg");
        createFile(VR + remoteSubdir + "source/subdir/4.png");
        createFile(VR + remoteSubdir + "source/asubdir/subdir/3.jpg");
        createFile(VR + remoteSubdir + "source/3.jpg");
        createFile(VR + remoteSubdir + "source/4.png");
        createFile(VR + remoteSubdir + "omegat/project_save.tmx");
        createFile(VR + remoteSubdir + "glossary/sub/myglossary.txt");
        createFile(VR2 + "otherprojectfile.txt");
    }

    void createLocalRepoFiles() throws IOException {
        createFile(V + "omegat.project");
        createFile(V + "source/file1.txt");
        createFile(V + "source/file1.txt.bak");
        createFile(V + "source/subdir/file2.txt");
        createFile(V + "source/subdir/file2.txt.bak");
        createFile(V + "source/subdir/3.jpg");
        createFile(V + "source/subdir/4.png");
        createFile(V + "source/asubdir/subdir/3.jpg");
        createFile(V + "source/3.jpg");
        createFile(V + "source/4.png");
        createFile(V + "source/otherproject/file.txt");
        createFile(V + "omegat/project_save.tmx");
        createFile(V + "glossary/sub/myglossary.txt");
    }

    void map_normalRemoteRepoAndExtraRemoteRepoWithExcludesWithDirectorySeparatorPrefix() {
        addRepo(localMappingPrefix+""+localMappingPostfix, repoUrlDir, remoteMappingPrefix+remoteSubdir+remoteMappingPostfix, "**/*.bak", "/*.png", "/subdir/3.jpg");
        addRepo(localMappingPrefix+"source/otherproject", repoUrlDir2, remoteMappingPrefix+""+remoteMappingPostfix, "**/*.bak", "/*.png", "/subdir/3.jpg");
    }

    void map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix() {
        addRepo(localMappingPrefix+""+localMappingPostfix, repoUrlDir, remoteMappingPrefix+remoteSubdir+remoteMappingPostfix, "**/*.bak", "*.png", "subdir/3.jpg");
        addRepo(localMappingPrefix+"source/otherproject"+localMappingPostfix, repoUrlDir2, remoteMappingPrefix+""+remoteMappingPostfix, "**/*.bak", "*.png", "subdir/3.jpg");
    }

    void map_SingleFileRemoteRepo() {
        addRepo(localMappingPrefix+"source/otherproject/file.txt"+localMappingPostfix, repoUrlDir2, remoteMappingPrefix+"otherprojectfile.txt"+remoteMappingPostfix);
    }

    @Test
    public void testCopyFileFromReposToProject() throws Exception {
        //test normal case when OmegaT downloads team project and only a few project files are copied (one at a time)
        //omegat.project (forceExcludes) should not be filtered in this case!
        createRemoteRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromReposToProject("omegat.project");
        checkCopy(VR + remoteSubdir+"omegat.project", V + "omegat.project");
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
        checkCopy(VR + remoteSubdir + "glossary/sub/myglossary.txt", V + "glossary/sub/myglossary.txt");
        checkCopy(VR + remoteSubdir + "source/3.jpg", V + "source/3.jpg");
        checkCopy(VR + remoteSubdir + "source/file1.txt", V + "source/file1.txt");
        checkCopy(VR + remoteSubdir + "source/subdir/file2.txt", V + "source/subdir/file2.txt");
        checkCopy(VR2 + "otherprojectfile.txt", V + "source/otherproject/otherprojectfile.txt");
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
        checkCopy(VR + remoteSubdir + "glossary/sub/myglossary.txt", V + "glossary/sub/myglossary.txt");
        checkCopy(VR + remoteSubdir + "source/3.jpg", V + "source/3.jpg");
        checkCopy(VR + remoteSubdir + "source/4.png", V + "source/4.png");
        checkCopy(VR + remoteSubdir + "source/asubdir/subdir/3.jpg", V + "source/asubdir/subdir/3.jpg");
        checkCopy(VR + remoteSubdir + "source/file1.txt", V + "source/file1.txt");
        checkCopy(VR + remoteSubdir + "source/subdir/3.jpg", V + "source/subdir/3.jpg"); //not filtered since /source is not matched
        checkCopy(VR + remoteSubdir + "source/subdir/4.png", V + "source/subdir/4.png");
        checkCopy(VR + remoteSubdir + "source/subdir/file2.txt", V + "source/subdir/file2.txt");
        checkCopy(VR2 + "otherprojectfile.txt", V + "source/otherproject/otherprojectfile.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyRenamedFileFromRepoToProject() throws Exception {
        createLocalRepoFiles();
        map_SingleFileRemoteRepo();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR2 + "otherprojectfile.txt", V + "source/otherproject/file.txt");
        checkCopyEnd();
    }

    //usages of copyFilesFromProjectToRepos in OmegaT:
    //example1: compileProjectAndCommit: copy target files to project; can be null if target outside project root, but in that case the option to commit is disabled.
    //example2: commitSourceFiles: copy source files to project; can be null if sources outside project root, but in that case the option to commit is disabled.
    //example3: project_save.tmx (rebase and commit project)+ EOL conversion
    //example4: project_save.tmx or glossaries (commitPrepared)
    //example5: IntegrationTest preparing remote repo: omegat.project and omegat/project_save.tmx

    @Test
    public void testCopyDirFromProjectToReposWithExcludes() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("source", null);
        checkCopy(V + "source/3.jpg", VR + remoteSubdir+"source/3.jpg");
        checkCopy(V + "source/file1.txt", VR + remoteSubdir+"source/file1.txt");
        //since source/otherproject/file.txt matches both mappings, it is copied to both projects!
        checkCopy(V + "source/otherproject/file.txt", VR + remoteSubdir + "source/otherproject/file.txt");
        checkCopy(V + "source/subdir/file2.txt", VR + remoteSubdir+"source/subdir/file2.txt");
        checkCopy(V + "source/otherproject/file.txt", VR2 + "file.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyDirFromProjectToReposWithExcludesWithDirectorySeparatorPrefix() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraRemoteRepoWithExcludesWithDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("source", null);
        checkCopy(V + "source/3.jpg", VR + remoteSubdir+"source/3.jpg");
        checkCopy(V + "source/4.png", VR + remoteSubdir + "source/4.png");
        checkCopy(V + "source/asubdir/subdir/3.jpg", VR + remoteSubdir + "source/asubdir/subdir/3.jpg");
        checkCopy(V + "source/file1.txt", VR + remoteSubdir+"source/file1.txt");
        //since source/otherproject/file.txt matches both mappings, it is copied to both projects!
        checkCopy(V + "source/otherproject/file.txt", VR + remoteSubdir + "source/otherproject/file.txt");
        checkCopy(V + "source/subdir/3.jpg", VR + remoteSubdir + "source/subdir/3.jpg"); //not filtered since /source is not matched
        checkCopy(V + "source/subdir/4.png", VR + remoteSubdir + "source/subdir/4.png");
        checkCopy(V + "source/subdir/file2.txt", VR + remoteSubdir+"source/subdir/file2.txt");
        checkCopy(V + "source/otherproject/file.txt", VR2 + "file.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("omegat.project", null);
        checkCopy(V + "omegat.project", VR + remoteSubdir+"omegat.project");
        checkCopyEnd();
    }

    @Test
    public void testCopySubFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_normalRemoteRepoAndExtraremoteRepoWithExcludesWithoutDirectorySeparatorPrefix();
        provider.copyFilesFromProjectToRepos("omegat/project_save.tmx", null);
        checkCopy(V + "omegat/project_save.tmx", VR + remoteSubdir+"omegat/project_save.tmx");
        checkCopyEnd();
    }

    @Test
    public void testCopyRenamedFileFromProjectToRepos() throws Exception {
        createLocalRepoFiles();
        map_SingleFileRemoteRepo();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(V + "source/otherproject/file.txt", VR2 + "otherprojectfile.txt");
        checkCopyEnd();
    }

    @Before
    public final void setUp() throws Exception {
        File dir = new File("build/testdata/repotest");
        FileUtils.deleteDirectory(dir);
        dir.mkdirs();
        V = dir.getAbsolutePath() + "/";
        VR = dir.getAbsolutePath() + "/.repositories/"+repoUrlDir+"/";
        VR2 = dir.getAbsolutePath() + "/.repositories/"+repoUrlDir2+"/"; //every repository, whether file, http, git or svn, gets its own directory.

        repos = new ArrayList<>();
        provider = new VirtualRemoteRepositoryProvider(repos, new ProjectProperties(new File(V)));
        files = new ArrayList<>();
    }

    void addRepo(String localPath, String repoUrlDir, String repoPath, String... excludes) {
        RepositoryMapping m = new RepositoryMapping();
        m.setLocal(localPath);
        m.setRepository(repoPath);
        m.getExcludes().addAll(Arrays.asList(excludes));
        RepositoryDefinition def = new RepositoryDefinition();
        def.setUrl(repoUrlDir);
        def.getMapping().add(m);
        repos.add(def);
        provider.repositories.add(null);
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
     * @param from full path/filename of source file
     * @param to full path/filename of target file
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
    public class VirtualRemoteRepositoryProvider extends RemoteRepositoryProvider {
        public VirtualRemoteRepositoryProvider(List<RepositoryDefinition> repositoriesDefinitions, ProjectProperties projectProperties)
                throws Exception {
            super(new File(V), repositoriesDefinitions, projectProperties);
        }

        @Override
        protected void initializeRepositories() throws Exception {
            // disable initialize for testing
        }

        @Override
        protected void copyFile(File from, File to, String eolConversionCharset) throws IOException {
            copyFrom.add(from.getAbsolutePath());
            copyTo.add(to.getAbsolutePath());
        }

        @Override
        protected void addForCommit(IRemoteRepository2 repo, String path) throws Exception {
        }
    }

    /**
     * ProjectProperties successor for create project on the virtual directory with specific repositories
     * definitions.
     */
    protected class ProjectPropertiesTest extends ProjectProperties {
        public ProjectPropertiesTest(List<RepositoryDefinition> repositoriesDefinitions) {
            setProjectRoot(V);
            setRepositories(repositoriesDefinitions);
        }
    }
}
