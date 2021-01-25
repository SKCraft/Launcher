package com.skcraft.launcher.builder;

import com.google.common.io.Files;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static com.skcraft.launcher.util.HttpRequest.url;

@Data
public class FileUrlRedirect {
	private URL url;
	private String hash;

	public void readFromFile(File file) throws IOException {
		List<String> lines = Files.readLines(file, Charset.defaultCharset());
		this.url = url(lines.get(0));

		if (lines.size() > 1) {
			String hash = lines.get(1);

			if (!hash.isEmpty()) {
				this.hash = hash;
			}
		}
	}

	public void writeToFile(File file) throws IOException {
		String entry = url.toString() + '\n' + hash;

		Files.write(entry, file, Charset.defaultCharset());
	}

	public static FileUrlRedirect fromFile(File file) throws IOException {
		FileUrlRedirect entry = new FileUrlRedirect();
		entry.readFromFile(file);
		return entry;
	}
}
