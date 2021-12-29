package com.skcraft.launcher.launch.runtime;

import com.skcraft.launcher.util.Environment;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface PlatformRuntimeFinder {
	/**
	 * Get the list of possible launcher locations for this platform
	 * @return List of possible launcher locations
	 */
	Set<File> getLauncherDirectories(Environment env);

	/**
	 * Get a list of candidate folders to check for Java runtimes.
	 * The returned folders will be checked for "release" files which describe the version and architecture.
	 *
	 * @return List of folders that may contain Java runtimes
	 */
	List<File> getCandidateJavaLocations();

	/**
	 * Get a list of extra runtimes obtained using platform-specific logic.
	 * e.g. on Windows, registry entries are returned
	 *
	 * @return List of extra Java runtimes
	 */
	List<JavaRuntime> getExtraRuntimes();
}
