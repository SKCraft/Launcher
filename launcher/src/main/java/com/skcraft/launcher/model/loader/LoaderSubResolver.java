package com.skcraft.launcher.model.loader;

import com.google.common.base.Function;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.Side;
import com.skcraft.launcher.model.modpack.DownloadableFile;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.Environment;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashMap;

@RequiredArgsConstructor
public class LoaderSubResolver implements Function<String, String> {
	private final Manifest manifest;
	private final LoaderManifest loader;
	private final Environment env;
	private final Side side;
	private final File baseDir;
	private final HashMap<String, DownloadableFile.LocalFile> localFiles;

	public String getPathOf(String... rest) {
		File file = baseDir;
		for (String part : rest) {
			file = new File(file, part);
		}

		return file.getAbsolutePath();
	}

	@Override
	public String apply(String arg) {
		if (arg == null) return null;

		while (true) {
			char start = arg.charAt(0);
			int bound = arg.length() - 1;
			char end = arg.charAt(bound);

			if (start == '{' && end == '}') {
				SidedData<String> sidedData = loader.getSidedData().get(arg.substring(1, bound));
				if (sidedData != null) {
					arg = sidedData.resolveFor(side);
				}
			} else if (start == '[' && end == ']') {
				String libraryName = arg.substring(1, bound);
				Library library = loader.findLibrary(libraryName);
				if (library != null) {
					arg = getPathOf(manifest.getLibrariesLocation(), library.getPath(env));
				} else {
					arg = getPathOf(manifest.getLibrariesLocation(), Library.mavenNameToPath(libraryName));
				}
			} else if (start == '&' && end == '&') {
				String localFileName = arg.substring(1, bound);

				if (localFiles.containsKey(localFileName)) {
					arg = localFiles.get(localFileName).getLocation().getAbsolutePath();
				} else {
					arg = localFileName;
				}
			} else if (start == '\'' && end == '\'') {
				arg = arg.substring(1, bound);
			} else {
				return arg;
			}
		}
	}
}
