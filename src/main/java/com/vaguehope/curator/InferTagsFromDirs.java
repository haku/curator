package com.vaguehope.curator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vaguehope.curator.data.FileAndData;
import com.vaguehope.curator.data.HashAndTags;
import com.vaguehope.curator.data.HashAndTags.ImporterTag;

public class InferTagsFromDirs {

	private static final String TAG_CLS = "Curator";
	private static final Logger LOG = LoggerFactory.getLogger(InferTagsFromDirs.class);

	private final File srcDir;
	private final IOFileFilter fileFilter;
	private final FileHasher fileHasher;
	private final File tagMap;
	private final File outputTags;

	public InferTagsFromDirs(
			final File srcDir,
			final IOFileFilter fileFilter,
			final FileHasher fileHasher,
			final File tagMap,
			final File outputTags) {
		this.srcDir = srcDir;
		this.fileFilter = fileFilter;
		this.fileHasher = fileHasher;
		this.tagMap = tagMap;
		this.outputTags = outputTags;
	}

	public void run() throws IOException, SQLException {
		if (this.tagMap == null && this.outputTags == null) return;
		if (this.tagMap == null ^ this.outputTags == null) throw new IllegalArgumentException("Must specify both tagMap and outputTags or neither.");

		final Multimap<String, String> dirToTags = DirToTagsMapping.readFile(this.tagMap).asMap();
		if (dirToTags.size() < 1) {
			LOG.warn("tagMap is empty.");
			return;
		}

		final Collection<File> srcFiles = FileUtils.listFiles(this.srcDir, this.fileFilter, TrueFileFilter.INSTANCE);
		final List<File> filesToTag = new ArrayList<>();
		for (final File file : srcFiles) {
			final String dirName = file.getParentFile().getName();
			final Collection<String> tags = dirToTags.get(dirName);
			if (tags == null || tags.size() < 1) continue;
			filesToTag.add(file);
		}
		LOG.info("Files matching tagmap: {}", filesToTag.size());

		final Collection<FileAndData> fileDatas = this.fileHasher.dataForFiles(filesToTag);
		final List<HashAndTags> hashesAndTags = new ArrayList<>();

		for (final FileAndData fad : fileDatas) {
			final String dirName = fad.getFile().getParentFile().getName();
			final Collection<String> tagStrs = dirToTags.get(dirName);
			if (tagStrs == null || tagStrs.size() < 1) throw new IllegalStateException("File previously matched a tag but does not any more: " + fad.getFile());

			final List<ImporterTag> tags = tagStrs.stream().map(t -> new ImporterTag(t, TAG_CLS)).collect(Collectors.toList());
			final HashAndTags hat = new HashAndTags(new BigInteger(fad.getData().getSha1(), 16), tags);
			hashesAndTags.add(hat);
		}

		if (hashesAndTags.size() < filesToTag.size()) {
			throw new IllegalStateException("Expected " + filesToTag.size() + " files with tags but only got " + hashesAndTags.size() + ".");
		}

		final Gson gson = new GsonBuilder()
				.registerTypeAdapter(BigInteger.class, new BigIntegerSerializer())
				.create();
		try (final FileWriter w = new FileWriter(this.outputTags)) {
			gson.toJson(hashesAndTags, w);
		}
		LOG.info("Wrote {} tags to: {}", hashesAndTags.size(), this.outputTags.getAbsolutePath());
	}

	private static class BigIntegerSerializer implements JsonSerializer<BigInteger> {
		@Override
		public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString(16));
		}
	}

}
