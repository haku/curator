package com.vaguehope.curator;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.hamcrest.MockitoHamcrest;

import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.FileData;

public class InferTagsFromDirsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File srcDir;
	private FileHasher fileHasher;
	private File outputTags;
	private InferTagsFromDirs undertest;

	private final String mapping = "{\n"
			+ "\"cute-things\": \"cute\",\n"
			+ "\"other-things\": [\"also\", \"other\"]\n"
			+ "}\n";

	@Before
	public void before() throws Exception {
		this.srcDir = this.tmp.newFolder();
		this.fileHasher = mock(FileHasher.class);
		this.outputTags = new File(this.tmp.getRoot(), "output-tags.json");

		final File tagMap = this.tmp.newFile();
		FileUtils.writeStringToFile(tagMap, this.mapping, StandardCharsets.UTF_8);

		this.undertest = new InferTagsFromDirs(this.srcDir, Curator.FILE_FILTER, this.fileHasher, tagMap, this.outputTags);
	}

	@Test
	public void itDoesNothingWithNoOutputFile() throws Exception {
		new InferTagsFromDirs(this.srcDir, Curator.FILE_FILTER, this.fileHasher, null, null).run();
	}

	@Test
	public void itErrorsOnInvalidArgs() throws Exception {
		try {
			new InferTagsFromDirs(this.srcDir, Curator.FILE_FILTER, this.fileHasher, new File(""), null).run();
			fail("Expected exception.");
		}
		catch (final IllegalArgumentException e) {
			assertEquals("Must specify both tagMap and outputTags or neither.", e.getMessage());
		}

		try {
			new InferTagsFromDirs(this.srcDir, Curator.FILE_FILTER, this.fileHasher, null, this.outputTags).run();
			fail("Expected exception.");
		}
		catch (final IllegalArgumentException e) {
			assertEquals("Must specify both tagMap and outputTags or neither.", e.getMessage());
		}
	}

	@Test
	public void itDoesNothingOnEmptyTagMap() throws Exception {
		final File tagMap = this.tmp.newFile();
		FileUtils.writeStringToFile(tagMap, "{}", StandardCharsets.UTF_8);
		new InferTagsFromDirs(this.srcDir, Curator.FILE_FILTER, this.fileHasher, tagMap, this.outputTags).run();
	}

	@Test
	public void itAssertsAllFiledataWasFound() throws Exception {
		final File src = dir(this.srcDir, "some-dir");
		final File cuteThings = dir(src, "cute-things");
		file(cuteThings);

		try {
			this.undertest.run();
			fail("Expected exception.");
		}
		catch (final IllegalStateException e) {
			assertEquals("Expected 1 files with tags but only got 0.", e.getMessage());
		}
	}

	@Test
	public void itInfertsTagsFromDir() throws Exception {
		final File src = dir(this.srcDir, "some-dir");

		final File cuteThings = dir(src, "cute-things");
		final File f1 = file(cuteThings);
		final File f2 = file(cuteThings);

		final File otherThings = dir(src, "other-things");
		final File f3 = file(otherThings);

		final File notMatching = dir(src, "not-matching");
		file(notMatching);

		@SuppressWarnings("unchecked") // I do not know why the compiler allows this cast but i am not complaining.
		final Collection<File> matcher = (Collection<File>) MockitoHamcrest.argThat(containsInAnyOrder(f1, f2, f3));
		when(this.fileHasher.dataForFiles(matcher)).thenReturn(Arrays.asList(
				new FileAndData(f1, new FileData(123, 456, "abc", "def")),
				new FileAndData(f2, new FileData(321, 654, "cba", "fed")),
				new FileAndData(f3, new FileData(111, 222, "aaa", "bbb"))));

		this.undertest.run();

		assertEquals("["
				+ "{\"sha1\":\"abc\",\"tags\":[{\"tag\":\"cute\",\"cls\":\"Curator\"}]},"
				+ "{\"sha1\":\"cba\",\"tags\":[{\"tag\":\"cute\",\"cls\":\"Curator\"}]},"
				+ "{\"sha1\":\"aaa\",\"tags\":[{\"tag\":\"also\",\"cls\":\"Curator\"},{\"tag\":\"other\",\"cls\":\"Curator\"}]}"
				+ "]",
				FileUtils.readFileToString(this.outputTags, StandardCharsets.UTF_8));
	}

	private static File dir(final File parent, final String name) {
		final File d = new File(parent, name);
		d.mkdir();
		return d;
	}

	private static File file(final File dir) throws IOException {
		final File f = new File(dir, RandomStringUtils.randomAlphabetic(16) + ".jpeg");
		FileUtils.touch(f);
		return f;
	}

}
