package org.omegat.core.team2;

import static org.junit.Assert.assertEquals;

import gen.core.project.RepositoryDefinition;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.omegat.core.team2.impl.SVNRemoteRepository2;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

@RunWith(Parameterized.class)
public class SVNRemoteRepository2IT {

	Path tempDir;
	Path tempSVNDir;
	SVNRemoteRepository2 rr2;
	RepositoryDefinition repositoryDefinition;
	ProjectTeamSettings projectTeamSettings;
	
	String svnSubPath;
	File svnCheckoutDir;

	@Parameterized.Parameters
	public static Collection<String> subPath() {
		return Arrays.asList("", "/asubrepo");
	}

	public SVNRemoteRepository2IT(String subPath) {
		this.svnSubPath = subPath;
	}

	SVNURL tgtURL;

	@Before
	public void setUp() throws Exception {

		tempSVNDir = Files.createTempDirectory("omegat-team-svn");

		SVNRepositoryFactoryImpl.setup();
		tgtURL = SVNRepositoryFactory.createLocalRepository( tempSVNDir.toFile(), true , false );
		prepareFilesInLocalRepository(tgtURL.toString());


		tempDir = Files.createTempDirectory("omegat-team-it");
		rr2 = new SVNRemoteRepository2();

		repositoryDefinition = new RepositoryDefinition();
		repositoryDefinition.setType("SVN");
		repositoryDefinition.setUrl(tgtURL.toString()+svnSubPath);

		projectTeamSettings = new ProjectTeamSettings(tempDir.toFile());

		svnCheckoutDir = new File(this.tempDir.toFile(), "mysvnrepo");
		rr2.init(repositoryDefinition, svnCheckoutDir, projectTeamSettings);
		rr2.switchToVersion(null);

		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(0, deletedFiles.length);

	}

	private void prepareFilesInLocalRepository(String url) throws Exception {
		Path tempSVNClientDir = Files.createTempDirectory("omegat-team-svnc");

		SVNRemoteRepository2 rr2 = new SVNRemoteRepository2();

		RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
		repositoryDefinition.setType("SVN");
		repositoryDefinition.setUrl(url);

		ProjectTeamSettings projectTeamSettings = new ProjectTeamSettings(tempSVNClientDir.toFile());

		File svnCheckoutDir = new File(tempSVNClientDir.toFile(), "mysvnrepo");
		rr2.init(repositoryDefinition, svnCheckoutDir, projectTeamSettings);
		rr2.switchToVersion(null);

		String newFile = createFileInSubdir(svnCheckoutDir, "asubrepo");
		rr2.addForCommit(newFile);
		rr2.commit(null, "init");

		FileUtils.deleteDirectory(tempSVNClientDir.toFile());
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir.toFile());
		FileUtils.deleteDirectory(tempSVNDir.toFile());
	}


	@Test
	public void test() throws Exception {

		String newFile = createFile();
		String newFile2 = createFile();

		rr2.addForCommit(newFile);
		rr2.commit(null, "test add");

		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("Add is not a delete", 0, deletedFiles.length);

		rr2.addForDeletion(newFile);
		rr2.commit(null, "test delete");
		rr2.addForCommit(newFile2);
		rr2.commit(null, "test add2");

		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("In list of commits, the delete is found", 1, deletedFiles.length);
		assertEquals("In list of commits, the delete is found", newFile, deletedFiles[0]);

		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("calling method second time gives empty list", 0, deletedFiles.length);

		String newFile3 = createFile();
		rr2.addForCommit(newFile3);
		rr2.commit(null, "test add");
		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("Add is not a delete", 0, deletedFiles.length);

		rr2.addForDeletion(newFile3);
		rr2.commit(null, "test delete");

		FileWriter myWriter = new FileWriter(new File(svnCheckoutDir, newFile3));
		myWriter.write("Files in Java might be tricky, but it is fun enough!");
		myWriter.close();
		rr2.addForCommit(newFile3);
		rr2.commit(null, "test add deleted file");

		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("delete not in list if added later", 0, deletedFiles.length);

		testDelSubdir();
		testDelSubfile();
	}

	@NotNull
	private String createFile() throws IOException {
		Random random = new Random();
		String newFile = "file"+random.nextInt();
		FileWriter myWriter = new FileWriter(new File(svnCheckoutDir, newFile));
		myWriter.write("Files in Java might be tricky, but it is fun enough!");
		myWriter.close();
		return newFile;
	}

	private void testDelSubdir() throws Exception {
		String dirToDelete = "subdir";
		String newFile2 = createFileInSubdir(svnCheckoutDir, dirToDelete);

		rr2.addForCommit(newFile2);
		rr2.commit(null, "add so we can delete");

		rr2.addForDeletion(dirToDelete);
		rr2.commit(null, "test delete dir");

		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(1, deletedFiles.length);
		assertEquals("the directory itself is deleted", dirToDelete, deletedFiles[0]);
	}

	private void testDelSubfile() throws Exception {
		String subdir = "subdir2";
		String fileToDelete = createFileInSubdir(svnCheckoutDir, subdir);

		rr2.addForCommit(fileToDelete);
		rr2.commit(null, "add so we can delete");

		rr2.addForDeletion(fileToDelete);
		rr2.commit(null, "test delete file");

		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(1, deletedFiles.length);
		assertEquals("the file is deleted", fileToDelete, deletedFiles[0]);
	}

	@NotNull
	private String createFileInSubdir(File basedir, String subdir) throws IOException {
		String newFile = subdir + File.separator + "fileinsubdir";
		Path path = Paths.get(basedir.getAbsolutePath() + File.separator + subdir);
		Files.createDirectories(path);
		FileWriter myWriter2 = new FileWriter(new File(basedir, newFile));
		myWriter2.write("This file is in a dir");
		myWriter2.close();
		return newFile;
	}
}
