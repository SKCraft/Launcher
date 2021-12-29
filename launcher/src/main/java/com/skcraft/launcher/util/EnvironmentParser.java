package com.skcraft.launcher.util;

import com.google.common.io.CharSource;
import com.google.common.io.Files;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses dotenv-style files.
 */
@RequiredArgsConstructor
public class EnvironmentParser {
	private final BufferedReader reader;

	private char read() throws IOException {
		int c = reader.read();

		if (c == -1) {
			throw new EOFException("End of stream reached unexpectedly!");
		}

		return (char) c;
	}

	public Map<String, String> parse() throws IOException {
		HashMap<String, String> result = new HashMap<>();

		while (reader.ready()) {
			KeyValue entry = parseLine();

			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public KeyValue parseLine() throws IOException {
		String key = parseKey();
		String value = parseValue();

		try {
			reader.mark(1);
			char newline = read();
			if (newline == '\r') {
				reader.mark(1);
				if (read() != '\n') {
					throw new IOException("Expected CRLF but only got CR");
				}
				reader.reset();
			} else if (newline != '\n') {
				reader.reset();
			}
		} catch (EOFException ignored) {
		}

		return new KeyValue(key, value);
	}

	private String parseKey() throws IOException {
		StringBuilder buffer = new StringBuilder();

		// Very lenient key parsing.
		while (true) {
			char c = read();

			switch (c) {
				case '=':
				case '\r':
				case '\n':
					return buffer.toString();
				default:
					buffer.append(c);
			}
		}
	}

	private String parseValue() throws IOException {
		StringBuilder buffer = new StringBuilder();

		try {
			while (true) {
				char c = read();

				switch (c) {
					case '\r':
					case '\n':
						return buffer.toString();
					case '"':
						buffer.append(parseQuotedPhrase());
						break;
					case '\\':
						char next = read();
						buffer.append(next);
						break;
					default:
						buffer.append(c);
				}
			}
		} catch (EOFException e) {
			// No terminating newline. bad!
			return buffer.toString();
		}
	}

	private String parseQuotedPhrase() throws IOException {
		StringBuilder buffer = new StringBuilder();

		while (true) {
			char c = read();

			switch (c) {
				case '"':
					return buffer.toString();
				case '\\':
					char next = read();
					buffer.append(next);
					break;
				default:
					buffer.append(c);
			}
		}
	}

	public static Map<String, String> parse(File target) throws IOException {
		CharSource charSource = Files.asCharSource(target, StandardCharsets.UTF_8);

		EnvironmentParser parser = new EnvironmentParser(charSource.openBufferedStream());
		return parser.parse();
	}

	@Data
	private static class KeyValue {
		private final String key;
		private final String value;
	}
}
