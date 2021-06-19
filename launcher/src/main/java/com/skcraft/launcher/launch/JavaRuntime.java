package com.skcraft.launcher.launch;

import lombok.Data;

import java.io.File;

@Data
public class JavaRuntime implements Comparable<JavaRuntime> {
	private final File dir;
	private final String version;
	private final boolean is64Bit;
	private boolean isMinecraftBundled = false;

	public int getMajorVersion() {
		if (version == null) {
			return 0; //
		}

		String[] parts = version.split("\\.");

		if (parts.length < 2) {
			throw new IllegalArgumentException("Invalid Java runtime version: " + version);
		}

		if (parts[0].equals("1")) {
			return Integer.parseInt(parts[1]);
		} else {
			return Integer.parseInt(parts[0]);
		}
	}

	@Override
	public int compareTo(JavaRuntime o) {
		if (isMinecraftBundled && !o.isMinecraftBundled) {
			return -1;
		} else if (!isMinecraftBundled && o.isMinecraftBundled) {
			return 1;
		}

		if (is64Bit && !o.is64Bit) {
			return -1;
		} else if (!is64Bit && o.is64Bit) {
			return 1;
		}

		if (version == null) {
			return 1;
		} else if (o.version == null) {
			return -1;
		}

		String[] a = version.split("[\\._]");
		String[] b = o.version.split("[\\._]");
		int min = Math.min(a.length, b.length);

		for (int i = 0; i < min; i++) {
			int first, second;

			try {
				first = Integer.parseInt(a[i]);
			} catch (NumberFormatException e) {
				return -1;
			}

			try {
				second = Integer.parseInt(b[i]);
			} catch (NumberFormatException e) {
				return 1;
			}

			if (first > second) {
				return -1;
			} else if (first < second) {
				return 1;
			}
		}

		if (a.length == b.length) {
			return 0; // Same
		}

		return a.length > b.length ? -1 : 1;
	}

	@Override
	public String toString() {
		return String.format("Java %s (%s) (%s)", version, is64Bit ? "64-bit" : "32-bit", dir);
	}
}
