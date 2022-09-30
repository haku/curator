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
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.curator.Args.ArgsException;
import com.vaguehope.curator.data.DestFiles;
import com.vaguehope.curator.data.DupeAndCanonical;
import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.SrcAndDest;

public class Curator {

	private static final SuffixFileFilter FILE_FILTER = new SuffixFileFilter(new ArrayList<>(C.MEDIA_FILE_EXTENSIONS), IOCase.INSENSITIVE);
	private static final Logger LOG = LoggerFactory.getLogger(Curator.class);

	private final FileCopier fileCopier;
	private final FileHasher fileHasher;
	private final File srcDir;
	private final File destDir;

	public Curator(final FileCopier fileCopier, final FileHasher fileHasher, final Args args) throws ArgsException {
		this.fileCopier = fileCopier;
		this.fileHasher = fileHasher;
		this.srcDir = args.getSrc();
		this.destDir = args.getDest();
	}

	public void run() throws IOException, SQLException {
		final Collection<File> srcFiles = FileUtils.listFiles(this.srcDir, FILE_FILTER, TrueFileFilter.INSTANCE);
		LOG.info("src files: {} files", srcFiles.size());
		final Collection<SrcAndDest> toCopy = filesToCopy(srcFiles);
		LOG.info("to copy: {} files", toCopy.size());
		for (final SrcAndDest sd : toCopy) {
			this.fileCopier.copy(sd);
		}

		final Collection<File> destFiles = FileUtils.listFiles(this.destDir, FILE_FILTER, TrueFileFilter.INSTANCE);
		LOG.info("dest files: {} files", srcFiles.size());
		final DestFiles destFilesInAndNotInSrc = destFilesInAndNotInSrc(destFiles);
		LOG.info("dest files in src: {} files", destFilesInAndNotInSrc.getInSrc().size());
		LOG.info("dest files not in src: {} files", destFilesInAndNotInSrc.getNotInSrc().size());

		final Collection<DupeAndCanonical> toRemove = findDuplicateFiles(destFilesInAndNotInSrc.getNotInSrc(), destFilesInAndNotInSrc.getInSrc());
		LOG.info("to remove: {} files", toRemove.size());
		for (final DupeAndCanonical dac : toRemove) {
			LOG.info("TODO Remove: {}  (canonical: {})", dac.getDupe(), dac.getCanonical());
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

	private Collection<DupeAndCanonical> findDuplicateFiles(final Collection<File> needles, final Collection<File> haystack) throws IOException, SQLException {
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
