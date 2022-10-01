package com.vaguehope.curator.data;

import java.io.File;
import java.util.Objects;

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
		return String.format("\"%s\" --> \"%s\"", this.src, this.dest);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.src, this.dest);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof SrcAndDest)) return false;
		final SrcAndDest that = (SrcAndDest) obj;
		return Objects.equals(this.src, that.src)
				&& Objects.equals(this.dest, that.dest);
	}

}
