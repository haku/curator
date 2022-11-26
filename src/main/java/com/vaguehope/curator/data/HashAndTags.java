package com.vaguehope.curator.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HashAndTags {

	private final BigInteger sha1;
	private final List<ImporterTag> tags;

	public HashAndTags(final BigInteger sha1, final List<ImporterTag> tags) {
		this.sha1 = sha1;
		this.tags = tags;
		Collections.sort(tags, ImporterTag.Order.TAG);
	}

	@Override
	public String toString() {
		return String.format("HashAndTags{%s, %s}", this.sha1, this.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.sha1, this.tags);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof HashAndTags)) return false;
		final HashAndTags that = (HashAndTags) obj;
		return Objects.equals(this.sha1, that.sha1)
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

		public enum Order implements Comparator<ImporterTag> {
			TAG {
				@Override
				public int compare(final ImporterTag a, final ImporterTag b) {
					return a.tag.compareTo(b.tag);
				}
			};

			@Override
			public abstract int compare (ImporterTag a, ImporterTag b);
		}
	}

	public enum Order implements Comparator<HashAndTags> {
		SHA1 {
			@Override
			public int compare(final HashAndTags a, final HashAndTags b) {
				return a.sha1.compareTo(b.sha1);
			}
		};

		@Override
		public abstract int compare (HashAndTags a, HashAndTags b);
	}

}