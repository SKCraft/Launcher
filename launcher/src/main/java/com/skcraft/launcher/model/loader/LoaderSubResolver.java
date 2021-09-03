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
	private final File libraryDir;
	private final HashMap<String, DownloadableFile.LocalFile> localFiles;

	public String getPathOf(String... rest) {
		File file = libraryDir;
		for (String part : rest) {
			file = new File(file, part);
		}

		return file.getAbsolutePath();
	}

	@Override
	public String apply(String arg) {
		if (arg == null) return null;

		arg = replaceTokens(arg);

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
					arg = getPathOf(library.getPath(env));
				} else {
					arg = getPathOf(Library.mavenNameToPath(libraryName));
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

	private String replaceTokens(String arg) {
		StringBuilder buf = new StringBuilder();

		int length = arg.length();
		for (int i = 0; i < length; i++) {
			char c = arg.charAt(i);

			if (c == '\\') {
				buf.append(arg.charAt(i + 1));
				i++;
			} else if (c == '{' || c == '\'') {
				StringBuilder keyBuf = new StringBuilder();

				for (int j = i + 1; j <= length; j++) {
					if (j == length) {
						throw new IllegalArgumentException("Illegal pattern: unclosed " + c);
					}

					char d = arg.charAt(j);

					if (d == '\\') {
						keyBuf.append(arg.charAt(j + 1));
						j++;
					} else if (c == '{' && d == '}') {
						String key = keyBuf.toString();
						SidedData<String> sidedData = loader.getSidedData().get(key);

						if (sidedData != null) {
							buf.append(sidedData.resolveFor(side));
						} else {
							throw new IllegalArgumentException("Missing key: " + key);
						}

						i = j;
						break;
					} else if (c == '\'' && d == '\'') {
						buf.append(keyBuf.toString());
						i = j;
						break;
					} else {
						keyBuf.append(d);
					}
				}
			} else {
				buf.append(c);
			}
		}

		return buf.toString();
	}
}
