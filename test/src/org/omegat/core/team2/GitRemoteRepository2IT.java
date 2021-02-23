package org.omegat.core.team2;

import gen.core.project.RepositoryDefinition;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.team2.impl.GITRemoteRepository2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class GitRemoteRepository2IT {

	Path gitDirofRepoToClone;
	Path tempDir;
	GITRemoteRepository2 rr2;
	RepositoryDefinition repositoryDefinition;
	ProjectTeamSettings projectTeamSettings;

	File gitCheckoutDir;

	@Before
	public void setUp() throws Exception {

		gitDirofRepoToClone = createAGitRepo();

		tempDir = Files.createTempDirectory("omegat-team-it");
		rr2 = new GITRemoteRepository2();

		repositoryDefinition = new RepositoryDefinition();
		repositoryDefinition.setType("GIT");

		repositoryDefinition.setUrl("file://" + gitDirofRepoToClone.toString());

		projectTeamSettings = new ProjectTeamSettings(tempDir.toFile());

		gitCheckoutDir = new File(this.tempDir.toFile(), "mygitrepo");
		rr2.init(repositoryDefinition, gitCheckoutDir, projectTeamSettings);
		rr2.switchToVersion(null);

		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(0, deletedFiles.length);

	}

	@NotNull
	private Path createAGitRepo() throws IOException, GitAPIException {
		Path gitDirofRepoToClone = Files.createTempDirectory("omegat-team-git");
		Git git = Git.init().setDirectory( gitDirofRepoToClone.toFile() ).call();
		String originalFile = createFile(gitDirofRepoToClone.toFile());
		git.add().addFilepattern(originalFile).call();
		git.commit().setMessage("init").setAuthor("OmegaT unit test", "test@test.nl").call();
		return gitDirofRepoToClone;
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir.toFile());
		FileUtils.deleteDirectory(gitDirofRepoToClone.toFile());
	}


	@Test
	public void test() throws Exception {

		String newFile = createFile(gitCheckoutDir);
		String newFile2 = createFile(gitCheckoutDir);

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

		String newFile3 = createFile(gitCheckoutDir);
		rr2.addForCommit(newFile3);
		rr2.commit(null, "test add");
		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("Add is not a delete", 0, deletedFiles.length);

		rr2.addForDeletion(newFile3);
		rr2.commit(null, "test delete");

		FileWriter myWriter = new FileWriter(new File(gitCheckoutDir, newFile3));
		myWriter.write("Files in Java might be tricky, but it is fun enough!");
		myWriter.close();
		rr2.addForCommit(newFile3);
		rr2.commit(null, "test add deleted file");

		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals("delete not in list if added later", 0, deletedFiles.length);

		testDelSubdir();
	}

	@NotNull
	private String createFile(File basedir) throws IOException {
		Random random = new Random();
		String newFile = "file"+random.nextInt();
		FileWriter myWriter = new FileWriter(new File(basedir, newFile));
		myWriter.write("Files in Java might be tricky, but it is fun enough!");
		myWriter.close();
		return newFile;
	}

	private void testDelSubdir() throws Exception {
		String dirToDelete = "subdir";
		String newFile2 = createFileInSubdir(dirToDelete);

		rr2.addForCommit(newFile2.replace(File.separator, "/")); //on windows, we still have to use '/' as separator, because jgit requires that.
		rr2.commit(null, "add so we can delete");
		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(0, deletedFiles.length);

		rr2.addForDeletion(dirToDelete);
		rr2.commit(null, "test delete dir");
		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(1, deletedFiles.length);
		assertEquals("the file itself is deleted", newFile2, deletedFiles[0]);
	}

	@NotNull
	private String createFileInSubdir(String subdir) throws IOException {
		String newFile = subdir + File.separator + "fileinsubdir";
		Path path = Paths.get(gitCheckoutDir.getAbsolutePath() + File.separator + "subdir");
		Files.createDirectories(path);
		FileWriter myWriter2 = new FileWriter(new File(gitCheckoutDir, newFile));
		myWriter2.write("This file is in a dir");
		myWriter2.close();
		return newFile;
	}
}
