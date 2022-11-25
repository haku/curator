package com.vaguehope.curator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaguehope.curator.data.DupeAndCanonical;

public class FileRemoverTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void itDoesNotDeleteIfDryRun() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abc"));
		new FileRemover(true).remove(dac);
		assertTrue(dac.getDupe().exists());
		assertTrue(dac.getCanonical().exists());
	}

	@Test
	public void itDoesNotDeleteIfCanonicalDoesNotExist() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abc"));
		dac.getCanonical().delete();
		try {
			new FileRemover(false).remove(dac);
			fail("did not throw");
		}
		catch (final IllegalStateException e) {
			assertThat(e.getMessage(), startsWith("Canonical file does not exist:"));
		}
		assertTrue(dac.getDupe().exists());
	}

	@Test
	public void itDoesNotDeleteIfCanonicalIsSymLink() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abc"));
		dac.getCanonical().delete();
		Files.createSymbolicLink(dac.getCanonical().toPath(), dac.getDupe().toPath());
		try {
			new FileRemover(false).remove(dac);
			fail("did not throw");
		}
		catch (final IllegalStateException e) {
			assertThat(e.getMessage(), startsWith("Canonical file is a symlink:"));
		}
		assertTrue(dac.getDupe().exists());
	}

	@Test
	public void itDoesNotDeleteIfCanonicalIsDir() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abc"));
		dac.getCanonical().delete();
		dac.getCanonical().mkdir();
		try {
			new FileRemover(false).remove(dac);
			fail("did not throw");
		}
		catch (final IllegalStateException e) {
			assertThat(e.getMessage(), startsWith("Canonical file is not a regular file:"));
		}
		assertTrue(dac.getDupe().exists());
	}

	@Test
	public void itDoesNotDeleteIfCanonicalIsDifferentFromDupe() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abd"));
		try {
			new FileRemover(false).remove(dac);
			fail("did not throw");
		}
		catch (final IllegalStateException e) {
			assertThat(e.getMessage(), startsWith("Duplicate and canonical files to do not have equal content:"));
		}
		assertTrue(dac.getDupe().exists());
	}

	@Test
	public void itDeletesAFile() throws Exception {
		final DupeAndCanonical dac = new DupeAndCanonical(
				fileWithContent("abc"),
				fileWithContent("abc"));
		new FileRemover(false).remove(dac);
		assertTrue(dac.getCanonical().exists());
		assertFalse(dac.getDupe().exists());
	}

	private File fileWithContent(final String content) throws IOException {
		final File f = this.tmp.newFile();
		FileUtils.writeStringToFile(f, content, StandardCharsets.UTF_8);
		return f;
	}

}
