package com.skcraft.launcher.launch.runtime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.skcraft.launcher.util.Environment;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LinuxRuntimeFinder implements PlatformRuntimeFinder {
	@Override
	public Set<File> getLauncherDirectories(Environment env) {
		return ImmutableSet.of(new File(System.getenv("HOME"), ".minecraft"));
	}

	@Override
	public List<File> getCandidateJavaLocations() {
		ArrayList<File> entries = Lists.newArrayList();

		String javaHome = System.getenv("JAVA_HOME");
		if (javaHome != null) {
			entries.add(new File(javaHome));
		}

		File[] runtimesList = new File("/usr/lib/jvm").listFiles();
		if (runtimesList != null) {
			Arrays.stream(runtimesList).map(file -> {
				try {
					return file.getCanonicalFile();
				} catch (IOException exception) {
					return file;
				}
			}).distinct().forEach(entries::add);
		}

		return entries;
	}

	@Override
	public List<JavaRuntime> getExtraRuntimes() {
		return Collections.emptyList();
	}
}
