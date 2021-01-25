package com.skcraft.launcher.builder;

import com.google.common.io.Files;
import com.skcraft.launcher.util.FileUtils;
import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
public class FileUrlScanner extends DirectoryWalker {
	public static final String URL_FILE_SUFFIX = ".url.txt";

	public static boolean isEnabled() {
		return !System.getProperty("com.skcraft.builder.ignoreURLOverrides", "false")
				.equalsIgnoreCase("true");
	}

	@Override
	protected void onFile(File file, String relPath) throws IOException {
		if (!file.getName().endsWith(URL_FILE_SUFFIX)) return;

		log.info("Found URL file " + file.getName());

		File targetFile = new File(file.getAbsoluteFile().getParentFile(),
				file.getName().replace(URL_FILE_SUFFIX, ""));
		FileUrlRedirect info = FileUrlRedirect.fromFile(file);

		if (targetFile.exists()) {
			String localHash = FileUtils.getShaHash(targetFile);
			if (info.getHash() == null) {
				// Disabled for now, let's not touch source files
//				info.setHash(localHash);
//				info.writeToFile(file);
				return;
			}

			// If everything matches, skip this file
			if (info.getHash().equals(localHash)) return;
		}

		File tempFile = File.createTempFile("launcherlib", null);

		try {
			log.info("Downloading file " + targetFile.getName() + " from " + info.getUrl() + "...");
			HttpRequest.get(info.getUrl())
					.execute()
					.expectResponseCode(200)
					.saveContent(tempFile);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}

		Files.move(tempFile, targetFile);
		log.info("Updated " + targetFile.getName() + " from " + file.getName());
	}
}
