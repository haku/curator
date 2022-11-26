package com.vaguehope.curator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.vaguehope.curator.Args.ArgsException;

public class Curator {

	static final SuffixFileFilter FILE_FILTER = new SuffixFileFilter(new ArrayList<>(C.MEDIA_FILE_EXTENSIONS), IOCase.INSENSITIVE);

	private final FileCopier fileCopier;
	private final FileHasher fileHasher;
	private final FileRemover fileRemover;
	private final File srcDir;
	private final File destDir;
	private File tagMap;
	private final File outputTags;

	public Curator(final FileCopier fileCopier, final FileHasher fileHasher, final FileRemover fileRemover, final Args args) throws ArgsException {
		this.fileCopier = fileCopier;
		this.fileHasher = fileHasher;
		this.fileRemover = fileRemover;
		this.srcDir = args.getSrc();
		this.destDir = args.getDest();
		this.tagMap = args.getTagsmap();
		this.outputTags = args.getTagsout();
	}

	public void run() throws IOException, SQLException {
		new CopyNewFilesFromSrcToDest(this.srcDir, this.destDir, FILE_FILTER, this.fileCopier).run();
		new FindAndRemoveFilesInDestThatHaveMoved(this.srcDir, this.destDir, FILE_FILTER, this.fileHasher, this.fileRemover).run();
		new InferTagsFromDirs(this.srcDir, FILE_FILTER, this.fileHasher, this.tagMap, this.outputTags).run();
	}

}
