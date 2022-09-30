package com.vaguehope.curator.data;

import java.io.File;

public class SrcAndDest {

	private final File src;
	private final File dest;

	public SrcAndDest(final File src, final File dest) {
		this.src = src;
		this.dest = dest;
	}

	public File getSrc() {
		return this.src;
	}

	public File getDest() {
		return this.dest;
	}

	@Override
	public String toString() {
		return String.format("%s --> %s", this.src, this.dest);
	}

}
