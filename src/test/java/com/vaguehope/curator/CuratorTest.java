package com.vaguehope.curator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaguehope.curator.data.DupeAndCanonical;
import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.FileData;
import com.vaguehope.curator.data.SrcAndDest;

public class CuratorTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private FileCopier fileCopier;
	private FileHasher fileHasher;
	private FileRemover fileRemover;
	private Args args;
	private Curator undertest;

	private File srcDir;
	private File destDir;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		this.fileCopier = spy(new FileCopier(false));
		this.fileHasher = mock(FileHasher.class);
		this.fileRemover = mock(FileRemover.class);

		this.args = mock(Args.class);
		this.srcDir = this.tmp.newFolder("src");
		this.destDir = this.tmp.newFolder("dest");
		when(this.args.getSrc()).thenReturn(this.srcDir);
		when(this.args.getDest()).thenReturn(this.destDir);

		this.undertest = new Curator(this.fileCopier, this.fileHasher, this.fileRemover, this.args);

		doAnswer((i) -> ((Collection<File>) i.getArgument(0)).stream().map(f -> {
			try {
				return new FileAndData(f, new FileData(0, 0, "sha1:" + FileUtils.readFileToString(f, StandardCharsets.UTF_8), null));
			}
			catch (final IOException e) {
				throw new IllegalStateException(e);
			}
		}).collect(Collectors.toList())).when(this.fileHasher).dataForFiles(any(Collection.class));
	}

	@Test
	public void itCopiesSingleFile() throws Exception {
		final File sd1 = mkDir(this.srcDir, "dir1");
		final File sf1 = mkFileWithContent(sd1, "foo.jpg", "abcdef");

		this.undertest.run();

		verify(this.fileCopier).copy(new SrcAndDest(sf1, new File(this.destDir, "dir1/foo.jpg")));
		verifyNoMoreInteractions(this.fileCopier);
		verifyNoInteractions(this.fileRemover);

		assertFilesEqual(sf1, new File(this.destDir, "dir1/foo.jpg"));
	}

	@Test
	public void itUpdatesSingleFile() throws Exception {
		final File dd1 = mkDir(this.destDir, "dir1");
		final File df1 = mkFileWithContent(dd1, "foo.jpg", "old");

		final File sd1 = mkDir(this.srcDir, "dir1");
		final File sf1 = mkFileWithContent(sd1, "foo.jpg", "new");
		sf1.setLastModified(df1.lastModified() + 1);

		this.undertest.run();

		verify(this.fileCopier).copy(new SrcAndDest(sf1, df1));
		verifyNoMoreInteractions(this.fileCopier);
		verifyNoInteractions(this.fileRemover);

		assertFilesEqual(sf1, df1);
	}

	@Test
	public void itDoesNotCopyUnchangedFile() throws Exception {
		final File dd1 = mkDir(this.destDir, "dir1");
		final File df1 = mkFileWithContent(dd1, "foo.jpg", "old");

		final File sd1 = mkDir(this.srcDir, "dir1");
		final File sf1 = mkFileWithContent(sd1, "foo.jpg", "old");
		sf1.setLastModified(df1.lastModified());

		this.undertest.run();

		verifyNoInteractions(this.fileCopier);
		verifyNoInteractions(this.fileRemover);

		assertFilesEqual(sf1, df1);
	}

	@Test
	public void itDoesNotRemoveFileThatIsOnlyInDest() throws Exception {
		final File dd1 = mkDir(this.destDir, "dir1");
		final File df1 = mkFileWithContent(dd1, "foo.jpg", "abcdef");

		this.undertest.run();

		verifyNoInteractions(this.fileCopier);
		verifyNoInteractions(this.fileRemover);
		assertFilesContains(df1, "abcdef");
	}

	@Test
	public void itRemovesDestFileThatExistsSomewhereElseInDest() throws Exception {
		final File dd1 = mkDir(this.destDir, "dir1");
		final File df1 = mkFileWithContent(dd1, "f1.jpg", "abcdef");

		final File dd2 = mkDir(this.destDir, "dir2");
		final File df2 = mkFileWithContent(dd2, "f2.jpg", "abcdef");
		mkFileWithContent(dd2, "f2.gif", "ghijkl");

		// the src file is needed to inform which dest file to keep and which to remove.
		final File sd2 = mkDir(this.srcDir, "dir2");
		final File sf2 = mkFileWithContent(sd2, "f2.jpg", "abcdef");
		sf2.setLastModified(df2.lastModified());

		this.undertest.run();

		verifyNoMoreInteractions(this.fileCopier);

		verify(this.fileRemover).remove(new DupeAndCanonical(df1, df2));
		verifyNoMoreInteractions(this.fileRemover);
	}

	private static File mkDir(final File parent, final String name) throws IOException {
		final File sd1 = new File(parent, name);
		FileUtils.forceMkdir(sd1);
		return sd1;
	}

	private static File mkFileWithContent(final File parent, final String name, final String content) throws IOException {
		final File sf1 = new File(parent, name);
		FileUtils.write(sf1, content, StandardCharsets.UTF_8);
		return sf1;
	}

	private static void assertFilesEqual(final File expected, final File actual) throws IOException {
		assertTrue(actual.exists());
		assertTrue(FileUtils.contentEquals(expected, actual));
	}

	private static void assertFilesContains(final File file, final String expected) throws IOException {
		assertTrue(file.exists());
		assertEquals(expected, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
	}

}
