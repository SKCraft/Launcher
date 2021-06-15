package com.skcraft.launcher.util;

import com.google.common.base.Splitter;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Parses dotenv-style files.
 */
public class EnvironmentParser {
	public static Map<String, String> parse(File target) throws IOException {
		CharSource charSource = Files.asCharSource(target, StandardCharsets.UTF_8);
		Map<String, String> values = Splitter.onPattern("\r?\n").withKeyValueSeparator('=')
				.split(charSource.read());

		// Remove quotes
		// TODO do this better. it works fine for the release file, though
		values.replaceAll((key, value) -> value.substring(1, value.length() - 1));

		return values;
	}
}
