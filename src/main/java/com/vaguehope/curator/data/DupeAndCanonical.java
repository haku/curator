package com.vaguehope.curator.data;

import java.io.File;

public class DupeAndCanonical {

	private File dupe;
	private File canonical;

	public DupeAndCanonical(File dupe, File canonical) {
		this.dupe = dupe;
		this.canonical = canonical;
	}

	public File getDupe() {
		return this.dupe;
	}

	public File getCanonical() {
		return this.canonical;
	}

}
