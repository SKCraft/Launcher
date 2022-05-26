package com.skcraft.launcher.launch.runtime;

import com.skcraft.launcher.util.EnvironmentParser;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

@Log
public class JavaReleaseFile {
	private Map<String, String> backingMap;

	private JavaReleaseFile(Map<String, String> releaseDetails) {
		this.backingMap = releaseDetails;
	}

	public String getVersion() {
		return backingMap.get("JAVA_VERSION");
	}

	public String getArch() {
		return backingMap.get("OS_ARCH");
	}

	public boolean isArch64Bit() {
		return getArch() == null || getArch().matches("x64|x86_64|amd64|aarch64");
	}

	public static JavaReleaseFile parseFromRelease(File javaPath) {
		File releaseFile = new File(javaPath, "release");

		if (releaseFile.exists()) {
			try {
				Map<String, String> releaseDetails = EnvironmentParser.parse(releaseFile);

				return new JavaReleaseFile(releaseDetails);
			} catch (IOException e) {
				log.log(Level.WARNING, "Failed to read release file " + releaseFile.getAbsolutePath(), e);
				return null;
			}
		}

		return null;
	}
}
