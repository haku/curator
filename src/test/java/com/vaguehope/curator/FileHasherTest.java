package com.vaguehope.curator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.FileData;

public class FileHasherTest {

	private static final String FOOBAR_SHA1 = "8843d7f92416211de9ebb963ff4ce28125932878";
	private static final String BARFOO_SHA1 = "60518c1c11dc0452be71a7118a43ab68e3451b82";

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	private InMemoryFileDb filedb;
	private FileHasher undertest;

	@Before
	public void before() throws Exception {
		this.filedb = new InMemoryFileDb();
		this.undertest = new FileHasher(this.filedb);
	}

	@Test
	public void itHashesSomeFiles() throws Exception {
		final File f1 = this.tmp.newFile();
		final File f2 = this.tmp.newFile();
		FileUtils.write(f1, "foobar", StandardCharsets.UTF_8);
		FileUtils.write(f2, "barfoo", StandardCharsets.UTF_8);

		final List<FileAndData> actual = new ArrayList<>(this.undertest.dataForFiles(Arrays.asList(f1, f2)));

		assertEquals(FOOBAR_SHA1, actual.get(0).getData().getSha1());
		assertEquals(BARFOO_SHA1, actual.get(1).getData().getSha1());
	}

	@Test
	public void itDoesSomething() throws Exception {
		final File f1 = this.tmp.newFile();
		FileUtils.write(f1, "123", StandardCharsets.UTF_8);
		final FileData fd = new FileData(3, f1.lastModified(), "abc", "def");
		try (final WritableFileDb w = this.filedb.getWritable()) {
			w.storeFileData(f1, fd);
		}

		assertEquals(fd, this.undertest.dataForFiles(Arrays.asList(f1)).iterator().next().getData());
	}

}
