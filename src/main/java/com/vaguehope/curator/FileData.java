package com.vaguehope.curator;

import java.io.File;
import java.io.IOException;

import com.vaguehope.curator.util.HashHelper;
import com.vaguehope.curator.util.HashHelper.Md5AndSha1;

public class FileData {

	private final long size;
	private final long modified;
	private final String sha1;
	private final String md5;

	public FileData (final long size, final long modified, final String sha1, final String md5) {
		if (size < 0) throw new IllegalArgumentException("Invalid size: " + size);
		if (sha1 == null || sha1.length() < 1) throw new IllegalArgumentException("Invalid sha1: " + sha1);
		this.size = size;
		this.modified = modified;
		this.sha1 = sha1;
		this.md5 = md5;
	}

	public long getSize () {
		return this.size;
	}

	public long getModified () {
		return this.modified;
	}

	public String getSha1 () {
		return this.sha1;
	}

	public String getMd5() {
		return this.md5;
	}

	public boolean upToDate (final File file) {
		return file.length() == this.size && file.lastModified() == this.modified;
	}

	public static FileData forFile (final File file) throws IOException {
		final Md5AndSha1 hashes = HashHelper.generateMd5AndSha1(file);
		return new FileData(file.length(), file.lastModified(), hashes.getSha1().toString(16), hashes.getMd5().toString(16));
	}

	@Override
	public String toString() {
		return String.format("FileData{%s, %s, %s, %s}", this.size, this.modified, this.sha1, this.md5);
	}

}
