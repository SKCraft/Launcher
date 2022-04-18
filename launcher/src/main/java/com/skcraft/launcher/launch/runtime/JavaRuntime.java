package com.skcraft.launcher.launch.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;
import lombok.Data;

import java.io.File;

@Data
public class JavaRuntime implements Comparable<JavaRuntime> {
	private final File dir;
	private final String version;
	private final boolean is64Bit;
	private boolean isMinecraftBundled = false; // Used only in list sorting & not serialized.

	@JsonValue
	public File getDir() {
		return dir;
	}

	@JsonCreator
	public static JavaRuntime fromDir(String dir) {
		return JavaRuntimeFinder.getRuntimeFromPath(dir);
	}

	@JsonIgnore
	public int getMajorVersion() {
		if (version == null) {
			return 0; // uhh make this an error?
		}

		String[] parts = version.split("\\.");

		if (parts[0].equals("1") && parts.length > 1) {
			return Integer.parseInt(parts[1]);
		} else {
			return Integer.parseInt(parts[0]);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JavaRuntime that = (JavaRuntime) o;
		return Objects.equal(dir, that.dir);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(dir);
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
		String version = this.version != null ? this.version : "unknown";

		return String.format("Java %s (%s) (%s)", version, is64Bit ? "64-bit" : "32-bit", dir);
	}
}
