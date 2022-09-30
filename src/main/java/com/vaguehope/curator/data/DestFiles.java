package com.vaguehope.curator.data;

import java.io.File;
import java.util.Collection;

public class DestFiles {

	private final Collection<File> inSrc;
	private final Collection<File> notInSrc;

	public DestFiles(final Collection<File> inSrc, final Collection<File> notInSrc) {
		this.inSrc = inSrc;
		this.notInSrc = notInSrc;
	}

	public Collection<File> getInSrc() {
		return this.inSrc;
	}

	public Collection<File> getNotInSrc() {
		return this.notInSrc;
	}

}
