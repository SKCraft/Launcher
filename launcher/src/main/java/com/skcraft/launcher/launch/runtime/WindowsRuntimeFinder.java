package com.skcraft.launcher.launch.runtime;

import com.google.common.collect.Lists;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.WinRegistry;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@Log
public class WindowsRuntimeFinder implements PlatformRuntimeFinder {
	@Override
	public Set<File> getLauncherDirectories(Environment env) {
		HashSet<File> launcherDirs = new HashSet<>();

		try {
			String launcherPath = WinRegistry.readString(WinReg.HKEY_CURRENT_USER,
					"SOFTWARE\\Mojang\\InstalledProducts\\Minecraft Launcher", "InstallLocation");

			launcherDirs.add(new File(launcherPath));
		} catch (Throwable err) {
			log.log(Level.WARNING, "Failed to read launcher location from registry", err);
		}

		String programFiles = Objects.equals(env.getArchBits(), "64")
				? System.getenv("ProgramFiles(x86)")
				: System.getenv("ProgramFiles");

		// Mojang likes to move the java runtime directory
		launcherDirs.add(new File(programFiles, "Minecraft"));
		launcherDirs.add(new File(programFiles, "Minecraft Launcher"));
		launcherDirs.add(new File(System.getenv("LOCALAPPDATA"), "Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local"));

		return launcherDirs;
	}

	@Override
	public List<File> getCandidateJavaLocations() {
		return Collections.emptyList();
	}

	@Override
	public List<JavaRuntime> getExtraRuntimes() {
		ArrayList<JavaRuntime> entries = Lists.newArrayList();

		getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Runtime Environment");
		getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Development Kit");
		getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\JDK");

		return entries;
	}

	private static void getEntriesFromRegistry(Collection<JavaRuntime> entries, String basePath)
			throws IllegalArgumentException {
		try {
			List<String> subKeys = WinRegistry.readStringSubKeys(WinReg.HKEY_LOCAL_MACHINE, basePath);
			for (String subKey : subKeys) {
				JavaRuntime entry = getEntryFromRegistry(basePath, subKey);
				if (entry != null) {
					entries.add(entry);
				}
			}
		} catch (Throwable err) {
			log.log(Level.INFO, "Failed to read Java locations from registry in " + basePath);
		}
	}

	private static JavaRuntime getEntryFromRegistry(String basePath, String version) {
		String regPath = basePath + "\\" + version;
		String path = WinRegistry.readString(WinReg.HKEY_LOCAL_MACHINE, regPath, "JavaHome");
		File dir = new File(path);
		if (dir.exists() && new File(dir, "bin/java.exe").exists()) {
			return new JavaRuntime(dir, version, guessIf64BitWindows(dir));
		} else {
			return null;
		}
	}

	private static boolean guessIf64BitWindows(File path) {
		try {
			String programFilesX86 = System.getenv("ProgramFiles(x86)");
			return programFilesX86 == null || !path.getCanonicalPath().startsWith(new File(programFilesX86).getCanonicalPath());
		} catch (IOException ignored) {
			return false;
		}
	}
}
