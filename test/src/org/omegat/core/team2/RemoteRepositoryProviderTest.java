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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.data.ProjectProperties;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

public class RemoteRepositoryProviderTest {
    String V;
    String VR;

    List<RepositoryDefinition> repos;
    List<String> files;
    VirtualRemoteRepositoryProvider provider;

    List<String> copyFrom = new ArrayList<>();
    List<String> copyTo = new ArrayList<>();
    int copyCheckedIndex;

    @Before
    public final void setUp() throws Exception {
        File dir = new File("build/testdata/repotest");
        FileUtils.deleteDirectory(dir);
        dir.mkdirs();
        V = dir.getAbsolutePath() + "/";
        VR = dir.getAbsolutePath() + "/.repositories/url/";

        repos = new ArrayList<RepositoryDefinition>();
        provider = new VirtualRemoteRepositoryProvider(repos);
        files = new ArrayList<String>();
    }

    void filesLocal() throws IOException {
        addFile(V + "dir/localfile");
        addFile(V + "dir/local/1.txt");
        addFile(V + "dir/local/1.txt.bak");
        addFile(V + "dir/local/1.jpg");
        addFile(V + "dir/local/2.xml");
        addFile(V + "dir/local/subdir/3.png");
        addFile(V + "otherdir/local/4.file");
    }

    void filesRemote() throws IOException {
        addFile(VR + "remotefile");
        addFile(VR + "remote/1.txt");
        addFile(VR + "remote/1.txt.bak");
        addFile(VR + "remote/1.jpg");
        addFile(VR + "remote/2.xml");
        addFile(VR + "remote/subdir/3.png");
        addFile(VR + "otherremote/4.file");
    }

    void mapping1() {
        addRepo("dir/localfile", "remotefile");
        // bak should be excluded, but png - no
        addRepo("dir/local/", "remote/", "/*.bak", "/*.png", "/1.jpg");
    }

    void mapping1a() {
        addRepo("/dir/localfile", "/remotefile");
        // bak should be excluded, but png - no
        addRepo("/dir/local", "/remote", "*.bak", "*.png", "1.jpg");
    }

    void mapping2() {
        addRepo("", "", "**/*.bak", "/*.png", "/dir/local/1.jpg", "/remote/1.jpg");
    }

    void mapping2a() {
        addRepo("/", "/", "**/*.bak", "*.png", "dir/local/1.jpg", "remote/1.jpg");
    }

    void mapping3() {
        addRepo("dir/", "", "**/*.bak", "/*.png", "/local/1.jpg", "/remote/1.jpg");
    }

    void mapping3a() {
        addRepo("/dir", "/", "**/*.bak", "*.png", "local/1.jpg/", "remote/1.jpg/");
    }

    void mapping4() {
        addRepo("", "remote/", "**/*.bak", "/*.png", "/dir/local/1.jpg", "/1.jpg");
    }

    void mapping4a() {
        addRepo("/", "/remote", "**/*.bak", "*.png", "dir/local/1.jpg", "1.jpg");
    }

    @Test
    public void testNames() throws Exception {
        provider.copyFilesFromReposToProject("/dir");
        provider.copyFilesFromReposToProject("dir/");
        provider.copyFilesFromReposToProject("file");
        provider.copyFilesFromProjectToRepos("/dir", null);
        provider.copyFilesFromProjectToRepos("dir/", null);
        provider.copyFilesFromProjectToRepos("file", null);
    }

    @Test
    public void testCopyFilesFromRepoToProject11() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/localfile");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject11a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir/localfile");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject12() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/local/1.txt");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject12a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir/local/1.txt");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject13() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/local/2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject13a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/local/2.xml");
        // Unlike 13, this time *.png on all levels is excluded
        // checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject14() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/local/subdir/");
        checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject14a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir/local/subdir");
        // Unlike 14, this time *.png on all levels is excluded
        // checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject15() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/lo");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject15a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir/lo/");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject16() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("dir/lo/");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject16a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/dir/lo");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject17() throws Exception {
        filesRemote();
        mapping1();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/local/2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject17a() throws Exception {
        filesRemote();
        mapping1a();
        provider.copyFilesFromReposToProject("/");
        checkCopy(VR + "remotefile", V + "dir/localfile");
        checkCopy(VR + "remote/1.txt", V + "dir/local/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/local/2.xml");
        // Unlike 14, this time *.png on all levels is excluded
        // checkCopy(VR + "remote/subdir/3.png", V + "dir/local/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject21() throws Exception {
        filesRemote();
        mapping2();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR + "otherremote/4.file", V + "otherremote/4.file");
        checkCopy(VR + "remote/1.txt", V + "remote/1.txt");
        checkCopy(VR + "remote/2.xml", V + "remote/2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "remote/subdir/3.png");
        checkCopy(VR + "remotefile", V + "remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject21a() throws Exception {
        filesRemote();
        mapping2a();
        provider.copyFilesFromReposToProject("/");
        checkCopy(VR + "otherremote/4.file", V + "otherremote/4.file");
        checkCopy(VR + "remote/1.txt", V + "remote/1.txt");
        checkCopy(VR + "remote/2.xml", V + "remote/2.xml");
        // Unlike 21, this time *.png on all levels is excluded
        // checkCopy(VR + "remote/subdir/3.png", V + "remote/subdir/3.png");
        checkCopy(VR + "remotefile", V + "remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject22() throws Exception {
        filesRemote();
        mapping2();
        provider.copyFilesFromReposToProject("otherremote/4.file");
        checkCopy(VR + "otherremote/4.file", V + "otherremote/4.file");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject22a() throws Exception {
        filesRemote();
        mapping2a();
        provider.copyFilesFromReposToProject("/otherremote/4.file/");
        checkCopy(VR + "otherremote/4.file", V + "otherremote/4.file");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject31() throws Exception {
        filesRemote();
        mapping3();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR + "otherremote/4.file", V + "dir/otherremote/4.file");
        checkCopy(VR + "remote/1.txt", V + "dir/remote/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/remote/2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "dir/remote/subdir/3.png");
        checkCopy(VR + "remotefile", V + "dir/remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject31a() throws Exception {
        filesRemote();
        mapping3a();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR + "otherremote/4.file", V + "dir/otherremote/4.file");
        checkCopy(VR + "remote/1.txt", V + "dir/remote/1.txt");
        checkCopy(VR + "remote/2.xml", V + "dir/remote/2.xml");
        // checkCopy(VR + "remote/subdir/3.png", V + "dir/remote/subdir/3.png");
        checkCopy(VR + "remotefile", V + "dir/remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject41() throws Exception {
        filesRemote();
        mapping4();
        provider.copyFilesFromReposToProject("");
        checkCopy(VR + "remote/1.txt", V + "1.txt");
        checkCopy(VR + "remote/2.xml", V + "2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject41a() throws Exception {
        filesRemote();
        mapping4a();
        provider.copyFilesFromReposToProject("/");
        checkCopy(VR + "remote/1.txt", V + "1.txt");
        checkCopy(VR + "remote/2.xml", V + "2.xml");
        // checkCopy(VR + "remote/subdir/3.png", V + "subdir/3.png");
        checkCopyEnd();
    }

/**    @Test
    public void testCopyFilesFromRepoToProject51() throws Exception {
        filesRemote();
        mapping4();
        provider.copyFilesFromReposToProject("", "/1.txt");
        checkCopy(VR + "remote/2.xml", V + "2.xml");
        checkCopy(VR + "remote/subdir/3.png", V + "subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromRepoToProject51a() throws Exception {
        filesRemote();
        mapping4a();
        provider.copyFilesFromReposToProject("/", "1.txt/");
        checkCopy(VR + "remote/2.xml", V + "2.xml");
        // checkCopy(VR + "remote/subdir/3.png", V + "subdir/3.png");
        checkCopyEnd();
    }
*/

    
    @Test
    public void testCopyFilesFromProjectToRepo11() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/localfile", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo11a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir/localfile/", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo12() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/local/1.txt", null);
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo12a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir/local/1.txt/", null);
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo13() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/2.xml");
        checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo13a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/2.xml");
        // checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo14() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/local/subdir/", null);
        checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo14a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir/local/subdir", null);
        // checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo15() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/lo", null);
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo15a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir/lo/", null);
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo16() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("dir/lo/", null);
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo16a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/dir/lo", null);
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo17() throws Exception {
        filesLocal();
        mapping1();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/2.xml");
        checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo17a() throws Exception {
        filesLocal();
        mapping1a();
        provider.copyFilesFromProjectToRepos("/", null);
        checkCopy(V + "dir/localfile", VR + "remotefile");
        checkCopy(V + "dir/local/1.txt", VR + "remote/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/2.xml");
        // checkCopy(V + "dir/local/subdir/3.png", VR + "remote/subdir/3.png");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo21() throws Exception {
        filesLocal();
        mapping2();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(V + "dir/local/1.txt", VR + "dir/local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "dir/local/2.xml");
        checkCopy(V + "dir/local/subdir/3.png", VR + "dir/local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "dir/localfile");
        checkCopy(V + "otherdir/local/4.file", VR + "otherdir/local/4.file");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo21a() throws Exception {
        filesLocal();
        mapping2a();
        provider.copyFilesFromProjectToRepos("/", null);
        checkCopy(V + "dir/local/1.txt", VR + "dir/local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "dir/local/2.xml");
        //checkCopy(V + "dir/local/subdir/3.png", VR + "dir/local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "dir/localfile");
        checkCopy(V + "otherdir/local/4.file", VR + "otherdir/local/4.file");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo22() throws Exception {
        filesLocal();
        mapping2();
        provider.copyFilesFromProjectToRepos("dir/localfile", null);
        checkCopy(V + "dir/localfile", VR + "dir/localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo22a() throws Exception {
        filesLocal();
        mapping2a();
        provider.copyFilesFromProjectToRepos("/dir/localfile/", null);
        checkCopy(V + "dir/localfile", VR + "dir/localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo31() throws Exception {
        filesLocal();
        mapping3();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(V + "dir/local/1.txt", VR + "local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "local/2.xml");
        checkCopy(V + "dir/local/subdir/3.png", VR + "local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo31a() throws Exception {
        filesLocal();
        mapping3a();
        provider.copyFilesFromProjectToRepos("/", null);
        checkCopy(V + "dir/local/1.txt", VR + "local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "local/2.xml");
        // checkCopy(V + "dir/local/subdir/3.png", VR + "local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "localfile");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo41() throws Exception {
        filesLocal();
        mapping4();
        provider.copyFilesFromProjectToRepos("", null);
        checkCopy(V + "dir/local/1.txt", VR + "remote/dir/local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/dir/local/2.xml");
        checkCopy(V + "dir/local/subdir/3.png", VR + "remote/dir/local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "remote/dir/localfile");
        checkCopy(V + "otherdir/local/4.file", VR + "remote/otherdir/local/4.file");
        checkCopyEnd();
    }

    @Test
    public void testCopyFilesFromProjectToRepo41a() throws Exception {
        filesLocal();
        mapping4a();
        provider.copyFilesFromProjectToRepos("/", null);
        checkCopy(V + "dir/local/1.txt", VR + "remote/dir/local/1.txt");
        checkCopy(V + "dir/local/2.xml", VR + "remote/dir/local/2.xml");
        // checkCopy(V + "dir/local/subdir/3.png", VR + "remote/dir/local/subdir/3.png");
        checkCopy(V + "dir/localfile", VR + "remote/dir/localfile");
        checkCopy(V + "otherdir/local/4.file", VR + "remote/otherdir/local/4.file");
        checkCopyEnd();
    }

    void addRepo(String localPath, String repoPath, String... excludes) {
        RepositoryMapping m = new RepositoryMapping();
        m.setLocal(localPath);
        m.setRepository(repoPath);
        m.getExcludes().addAll(Arrays.asList(excludes));
        RepositoryDefinition def = new RepositoryDefinition();
        def.setUrl("url");
        def.getMapping().add(m);
        repos.add(def);
        provider.repositories.add(null);
    }

    void addFile(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
    }

    void checkCopy(String from, String to) {
        assertEquals("Wrong copy file from2", from.replace('\\', '/'),
                copyFrom.get(copyCheckedIndex).replace('\\', '/'));
        assertEquals("Wrong copy file to2", to.replace('\\', '/'),
                copyTo.get(copyCheckedIndex).replace('\\', '/'));
        copyCheckedIndex++;
    }

    void checkCopyEnd() {
        assertEquals("Wrong copy list", copyCheckedIndex, copyFrom.size());
    }

    public class VirtualRemoteRepositoryProvider extends RemoteRepositoryProvider {
        public VirtualRemoteRepositoryProvider(List<RepositoryDefinition> repositoriesDefinitions)
                throws Exception {
            super(new File(V), repositoriesDefinitions);
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
