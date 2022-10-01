package com.vaguehope.curator;

import java.io.IOException;

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
		if (this.dryRun) {
			LOG.info("[dryrun] rm \"{}\"  (canonical: \"{}\")", dac.getDupe(), dac.getCanonical());
		}
		else {
			LOG.info("TODO rm \"{}\"  (canonical: \"{}\")", dac.getDupe(), dac.getCanonical());
		}
	}

}
