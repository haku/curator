package com.vaguehope.curator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class DirToTagsMapping {

	public static DirToTagsMapping readFile(final File file) throws IOException {
		try (final InputStream s = new FileInputStream(file)) {
			return readInputStream(s);
		}
	}

	public static DirToTagsMapping readInputStream(final InputStream is) throws IOException {
		return readReader(new BufferedReader(new InputStreamReader(is)));
	}

	public static DirToTagsMapping readReader(final Reader reader) throws IOException {
		final Gson gson = new GsonBuilder()
				.registerTypeAdapter(Multimap.class, new MultimapDeserializer())
				.registerTypeAdapter(MyList.class, new ListOrPrimitiveDeserializer())
				.create();

		final Type mapType = new TypeToken<Multimap<String, String>>() {}.getType();
		final Multimap<String, String> decoded = gson.fromJson(reader, mapType);
		return new DirToTagsMapping(decoded);
	}

	private static class MultimapDeserializer implements JsonDeserializer<Multimap<String, String>> {
		@Override
		public Multimap<String, String> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
				throws JsonParseException {
			final Type mapType = new TypeToken<Map<String, MyList<String>>>() {}.getType();
			final Map<String, MyList<String>> decoded = context.deserialize(json, mapType);

			final SetMultimap<String, String> smm = HashMultimap.create();
			decoded.forEach(smm::putAll);
			return smm;
		}
	}

	private static class ListOrPrimitiveDeserializer implements JsonDeserializer<MyList<String>> {
		@Override
		public MyList<String> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
				throws JsonParseException {
			if (json.isJsonArray()) {
				final Type mapType = new TypeToken<List<String>>() {}.getType();
				final List<String> decoded = context.deserialize(json, mapType);
				return new MyList<>(decoded);
			}
			else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
				return new MyList<>(json.getAsString());
			}
			else {
				throw new JsonParseException("Not an array or primitive: " + json);
			}
		}
	}

	// To prevent infinite loops when decoding arrays.
	private static class MyList<T> extends ArrayList<T> {
		private static final long serialVersionUID = 6375691309389279006L;

		public MyList(Collection<? extends T> collection) {
			super(collection);
		}

		public MyList(T single) {
			super();
			add(single);
		}
	}

	private final Multimap<String, String> dirToTags;

	public DirToTagsMapping(final Multimap<String, String> dirToTag) {
		this.dirToTags = ImmutableMultimap.copyOf(dirToTag);
	}

	public Multimap<String, String> asMap() {
		return this.dirToTags;
	}

}
