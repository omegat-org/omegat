package org.omegat.core.team2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.omegat.core.team2.impl.GITRemoteRepository2;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GitRemoteRepository2IT extends AbstractRemoteRepository2IT {

	@Override
	void prepareLocalRepo() throws IOException, GitAPIException {
		Git git = Git.init().setDirectory( tempRepoDir.toFile() ).call();
		String originalFile = createFile(tempRepoDir.toFile());
		git.add().addFilepattern(originalFile).call();
		git.commit().setMessage("init").setAuthor("OmegaT unit test", "test@test.nl").call();
	}

	@Override
	IRemoteRepository2 getRr2() {
		return new GITRemoteRepository2();
	}

	@Override
	void configureRepositoryDefinition() {
		repositoryDefinition.setType("GIT");
		repositoryDefinition.setUrl("file://" + tempRepoDir.toString());
	}

	void testDelSubdir() throws Exception {
		String dirToDelete = "subdir";
		String newFile2 = createFileInSubdir(localCheckoutDir, dirToDelete);

		rr2.addForCommit(toRr2Notation(newFile2));
		rr2.commit(null, "add so we can delete");
		String[] deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(0, deletedFiles.length);

		rr2.addForDeletion(toRr2Notation(dirToDelete));
		rr2.commit(null, "test delete dir");
		deletedFiles = rr2.getRecentlyDeletedFiles();
		assertEquals(1, deletedFiles.length);

	}



	String toRr2Notation(String file) {
		//on windows, we still have to use '/' as separator, because jgit requires that.
		return file.replace(File.separator, "/");
	}

	@Override
	void assertFileOrDirDeleted(String dir, String fileInDir, String actual) {
		assertEquals("the file itself is deleted", fileInDir, actual);
	}

}
