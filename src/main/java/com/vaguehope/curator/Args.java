package com.vaguehope.curator;

import java.io.File;

import org.kohsuke.args4j.Option;

public class Args {

	@Option(name = "-h", aliases = { "--help" }, usage = "Print this help text.") private boolean help;
	@Option(name = "--src", usage = "Dir path to read files from.", required = true) private String src;
	@Option(name = "--dest", usage = "Dir path to manage files in.", required = true) private String dest;
	@Option(name = "--dryrun", usage = "Do not modify file system.", required = false) private boolean dryRun;
	@Option(name = "--db", usage = "File path to metadata DB.", required = true) private String db;
	@Option(name = "--tagsmap", usage = "JSON file of tag mappings.") private String tagmap;
	@Option(name = "--tagsout", usage = "File path to write sha1tags file to.") private String tagsout;

	public static class ArgsException extends Exception {
		private static final long serialVersionUID = 4160594293982918286L;

		public ArgsException(final String msg) {
			super(msg);
		}
	}

	public boolean isHelp() {
		return this.help;
	}

	public File getSrc() throws ArgsException {
		return dirMustExist(this.src);
	}

	public File getDest() throws ArgsException {
		return dirMustExist(this.dest);
	}

	public boolean isDryRun() {
		return this.dryRun;
	}

	public File getDb() {
		return new File(this.db);
	}

	public File getTagsmap() {
		return this.tagmap != null ? new File(this.tagmap) : null;
	}

	public File getTagsout() {
		return this.tagsout != null ? new File(this.tagsout) : null;
	}

	private static File dirMustExist(final String path) throws ArgsException {
		final File f = new File(path);
		if (!f.exists() || !f.isDirectory()) throw new ArgsException("Directory does not exist: " + path);
		return f;
	}

}
