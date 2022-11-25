package com.vaguehope.curator;

import java.io.IOException;
import java.nio.file.LinkOption;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.data.DupeAndCanonical;

public class FileRemover {

	private static final Logger LOG = LoggerFactory.getLogger(FileRemover.class);

	private final boolean dryRun;

	public FileRemover(final boolean dryRun) {
		this.dryRun = dryRun;
	}

	public void remove(final DupeAndCanonical dac) throws IOException {
		// Multiple checks just to be realllly sure.
		// Also cos some of these return false on errors, so are not reliable negative tests.

		if (!dac.getCanonical().exists()) {
			throw new IllegalStateException("Canonical file does not exist: " + dac.getCanonical().getAbsolutePath());
		}

		if (FileUtils.isSymlink(dac.getCanonical())) {
			throw new IllegalStateException("Canonical file is a symlink: " + dac.getCanonical().getAbsolutePath());
		}

		if (!FileUtils.isRegularFile(dac.getCanonical(), LinkOption.NOFOLLOW_LINKS)) {
			throw new IllegalStateException("Canonical file is not a regular file: " + dac.getCanonical().getAbsolutePath());
		}

		if (!FileUtils.contentEquals(dac.getDupe(), dac.getCanonical())) {
			throw new IllegalStateException("Duplicate and canonical files to do not have equal content: " + dac);
		}

		if (this.dryRun) {
			LOG.info("[dryrun] rm \"{}\"  (canonical: \"{}\")", dac.getDupe(), dac.getCanonical());
		}
		else {
			FileUtils.delete(dac.getDupe());
			LOG.info("rm \"{}\"  (canonical: \"{}\")", dac.getDupe(), dac.getCanonical());
		}
	}

}
