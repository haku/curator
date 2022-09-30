package com.vaguehope.curator;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.data.SrcAndDest;

public class FileCopier {

	private static final Logger LOG = LoggerFactory.getLogger(FileCopier.class);

	private final boolean dryRun;

	public FileCopier(final boolean dryRun) {
		this.dryRun = dryRun;
	}

	public void copy(final SrcAndDest tc) throws IOException {
		if (this.dryRun) {
			LOG.info("[dryrun] cp {}", tc);
		}
		else {
			LOG.info("cp {}", tc);
			FileUtils.copyFile(tc.getSrc(), tc.getDest(), true);
		}
	}

}
