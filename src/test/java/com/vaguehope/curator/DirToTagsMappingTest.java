package com.vaguehope.curator;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class DirToTagsMappingTest {

	@Test
	public void itDecodesJson() throws Exception {
		final String mapping = "{\n"
				+ "\"cute-things\": [\"cute\"],\n"
				+ "\"fluffy-things\": \"fluffy\",\n"
				+ "\"other-things\": [\"also\", \"other\"]\n"
				+ "}\n";

		final DirToTagsMapping actual = DirToTagsMapping.readReader(new StringReader(mapping));
		final Multimap<String, String> expected = ImmutableMultimap.<String, String>builder()
				.put("cute-things", "cute")
				.put("fluffy-things", "fluffy")
				.put("other-things", "also")
				.put("other-things", "other")
				.build();

		assertEquals(expected, actual.asMap());
	}

}
