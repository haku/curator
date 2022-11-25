package com.vaguehope.curator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.data.DestFiles;
import com.vaguehope.curator.data.DupeAndCanonical;
import com.vaguehope.curator.data.FileAndData;

public class FindAndRemoveFilesInDestThatHaveMoved {

	private static final Logger LOG = LoggerFactory.getLogger(FindAndRemoveFilesInDestThatHaveMoved.class);

	private final File srcDir;
	private final File destDir;
	private final SuffixFileFilter fileFilter;
	private final FileHasher fileHasher;
	private final FileRemover fileRemover;

	public FindAndRemoveFilesInDestThatHaveMoved(
			final File srcDir,
			final File destDir,
			final SuffixFileFilter fileFilter,
			final FileHasher fileHasher, final FileRemover fileRemover) {
		this.srcDir = srcDir;
		this.destDir = destDir;
		this.fileFilter = fileFilter;
		this.fileHasher = fileHasher;
		this.fileRemover = fileRemover;
	}

	public void run() throws IOException, SQLException {
		final Collection<File> destFiles = FileUtils.listFiles(this.destDir, this.fileFilter, TrueFileFilter.INSTANCE);
		LOG.info("dest files: {}", destFiles.size());
		final DestFiles destFilesInAndNotInSrc = destFilesInAndNotInSrc(destFiles);
		LOG.info("dest files in src: {}", destFilesInAndNotInSrc.getInSrc().size());
		LOG.info("dest files not in src: {}", destFilesInAndNotInSrc.getNotInSrc().size());

		final Collection<DupeAndCanonical> toRemove = findDuplicateFiles(destFilesInAndNotInSrc.getNotInSrc(), destFilesInAndNotInSrc.getInSrc());
		LOG.info("files to remove: {}", toRemove.size());
		for (final DupeAndCanonical dac : toRemove) {
			this.fileRemover.remove(dac);
		}
	}

	private DestFiles destFilesInAndNotInSrc(final Collection<File> destFiles) {
		final Collection<File> inSrc = new ArrayList<>();
		final Collection<File> notInSrc = new ArrayList<>();

		final Path srcPath = this.srcDir.toPath();
		final Path destPath = this.destDir.toPath();

		for (final File destf : destFiles) {
			final Path relf = destPath.relativize(destf.toPath());
			final File srcf = srcPath.resolve(relf).toFile();
			if (srcf.exists()) {
				inSrc.add(destf);
			}
			else {
				notInSrc.add(destf);
			}
		}
		return new DestFiles(inSrc, notInSrc);
	}

	private Collection<DupeAndCanonical> findDuplicateFiles(final Collection<File> needles, final Collection<File> haystack)
			throws IOException, SQLException {
		final Collection<FileAndData> needleData = this.fileHasher.dataForFiles(needles);
		final Map<String, File> haystackSha1s = sha1Map(haystack);

		final Collection<DupeAndCanonical> ret = new ArrayList<>();
		for (final FileAndData needle : needleData) {
			final File inHaystack = haystackSha1s.get(needle.getData().getSha1());
			if (inHaystack != null && FileUtils.contentEquals(needle.getFile(), inHaystack)) {
				ret.add(new DupeAndCanonical(needle.getFile(), inHaystack));
			}
		}
		return ret;
	}

	private Map<String, File> sha1Map(final Collection<File> files) throws IOException, SQLException {
		final Collection<FileAndData> data = this.fileHasher.dataForFiles(files);
		final Map<String, File> ret = new HashMap<>();
		for (final FileAndData datum : data) {
			ret.put(datum.getData().getSha1(), datum.getFile());
		}
		return ret;
	}

}
