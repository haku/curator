package com.vaguehope.curator;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class HashAndTags {

	private final BigInteger md5;
	private final List<ImporterTag> tags;

	public HashAndTags(final BigInteger md5, final List<ImporterTag> tags) {
		this.md5 = md5;
		this.tags = tags;
	}

	public BigInteger getMd5() {
		return this.md5;
	}

	public Collection<ImporterTag> getTags() {
		return this.tags;
	}

	@Override
	public String toString() {
		return String.format("HashAndTags{%s, %s}", this.md5, this.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.md5, this.tags);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof HashAndTags)) return false;
		final HashAndTags that = (HashAndTags) obj;
		return Objects.equals(this.md5, that.md5)
				&& Objects.equals(this.tags, that.tags);
	}

	public static class ImporterTag {
		private final String tag;
		private final String cls;

		public ImporterTag(final String tag, String cls) {
			this.tag = tag;
			this.cls = cls;
		}

		public String getTag() {
			return this.tag;
		}

		public String getCls() {
			return this.cls;
		}

		@Override
		public String toString() {
			return String.format("Tag{%s, %s}", this.tag, this.cls);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.tag, this.cls);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) return false;
			if (this == obj) return true;
			if (!(obj instanceof ImporterTag)) return false;
			final ImporterTag that = (ImporterTag) obj;
			return Objects.equals(this.tag, that.tag)
					&& Objects.equals(this.cls, that.cls);
		}
	}

}