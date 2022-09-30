package com.vaguehope.curator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface C {

	public Set<String> MEDIA_FILE_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			".gif",
			".jpeg",
			".jpg",
			".png",
			".webp"
			)));
}
