package com.vaguehope.curator.data;

import java.io.File;
import java.util.Objects;

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

	@Override
	public String toString() {
		return String.format("DupeAndCanonical{%s, %s}", this.dupe, this.canonical);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.dupe, this.canonical);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof DupeAndCanonical)) return false;
		final DupeAndCanonical that = (DupeAndCanonical) obj;
		return Objects.equals(this.dupe, that.dupe)
				&& Objects.equals(this.canonical, that.canonical);
	}

}
