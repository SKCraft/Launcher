package com.skcraft.launcher.launch.runtime;

import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Scans Minecraft bundled Java directories
 */
public class MinecraftJavaFinder {
	public static List<JavaRuntime> scanLauncherDirectories(Environment env, Collection<File> launcherDirs) {
		ArrayList<JavaRuntime> entries = new ArrayList<>();

		for (File install : launcherDirs) {
			File runtimes = new File(install, "runtime");
			File[] runtimeList = runtimes.listFiles();
			if (runtimeList != null) {
				for (File potential : runtimeList) {
					JavaRuntime runtime = scanPotentialRuntime(env, potential);

					if (runtime != null) {
						entries.add(runtime);
					}
				}
			}
		}

		return entries;
	}

	private static JavaRuntime scanPotentialRuntime(Environment env, File potential) {
		String runtimeName = potential.getName();
		if (runtimeName.startsWith("jre-x")) {
			boolean is64Bit = runtimeName.equals("jre-x64");

			JavaReleaseFile release = JavaReleaseFile.parseFromRelease(potential);
			String version = release != null ? release.getVersion() : null;

			JavaRuntime runtime = new JavaRuntime(potential.getAbsoluteFile(), version, is64Bit);
			runtime.setMinecraftBundled(true);
			return runtime;
		} else {
			String[] children = potential.list((dir, name) -> new File(dir, name).isDirectory());
			if (children == null || children.length != 1) return null;
			String platformName = children[0];

			File javaDir = new File(potential, String.format("%s/%s", platformName, runtimeName));
			if (env.getPlatform() == Platform.MAC_OS_X) {
				javaDir = new File(javaDir, "jre.bundle/Contents/Home");
			}

			JavaReleaseFile release = JavaReleaseFile.parseFromRelease(javaDir);
			if (release == null) {
				return null;
			}

			JavaRuntime runtime = new JavaRuntime(javaDir.getAbsoluteFile(), release.getVersion(), release.isArch64Bit());
			runtime.setMinecraftBundled(true);
			return runtime;
		}
	}
}
