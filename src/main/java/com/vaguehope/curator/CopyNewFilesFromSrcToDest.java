package com.vaguehope.curator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.data.SrcAndDest;

public class CopyNewFilesFromSrcToDest {

	private static final Logger LOG = LoggerFactory.getLogger(CopyNewFilesFromSrcToDest.class);

	private final File srcDir;
	private final File destDir;
	private final IOFileFilter fileFilter;
	private final FileCopier fileCopier;

	public CopyNewFilesFromSrcToDest(final File srcDir, final File destDir, final IOFileFilter fileFilter, final FileCopier fileCopier) {
		this.srcDir = srcDir;
		this.destDir = destDir;
		this.fileFilter = fileFilter;
		this.fileCopier = fileCopier;
	}

	public void run() throws IOException {
		final Collection<File> srcFiles = FileUtils.listFiles(this.srcDir, this.fileFilter, HiddenFileFilter.VISIBLE);
		LOG.info("src files: {}", srcFiles.size());
		final Collection<SrcAndDest> toCopy = filesToCopy(srcFiles);
		LOG.info("files to copy: {}", toCopy.size());
		for (final SrcAndDest sd : toCopy) {
			this.fileCopier.copy(sd);
		}
	}

	private Collection<SrcAndDest> filesToCopy(final Collection<File> srcFiles) {
		final Path srcPath = this.srcDir.toPath();
		final Path destPath = this.destDir.toPath();

		final Collection<SrcAndDest> ret = new ArrayList<>();
		for (final File srcf : srcFiles) {
			final Path relf = srcPath.relativize(srcf.toPath());
			final File destf = destPath.resolve(relf).toFile();
			if (!destf.exists()
					|| srcf.lastModified() != destf.lastModified()
					|| srcf.length() != destf.length()) {
				ret.add(new SrcAndDest(srcf, destf));
			}
		}
		return ret;
	}

}
