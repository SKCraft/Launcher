package com.skcraft.launcher.launch.runtime;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.skcraft.launcher.util.Environment;
import lombok.extern.java.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@Log
public class MacRuntimeFinder implements PlatformRuntimeFinder {
	@Override
	public Set<File> getLauncherDirectories(Environment env) {
		return ImmutableSet.of(new File(System.getenv("HOME"), "Library/Application Support/minecraft"));
	}

	@Override
	public List<File> getCandidateJavaLocations() {
		return Collections.emptyList();
	}

	@Override
	public List<JavaRuntime> getExtraRuntimes() {
		ArrayList<JavaRuntime> entries = Lists.newArrayList();

		try {
			Process p = Runtime.getRuntime().exec("/usr/libexec/java_home -X");
			NSArray root = (NSArray) PropertyListParser.parse(p.getInputStream());
			NSObject[] arr = root.getArray();
			for (NSObject obj : arr) {
				NSDictionary dict = (NSDictionary) obj;
				entries.add(new JavaRuntime(
						new File(dict.objectForKey("JVMHomePath").toString()).getAbsoluteFile(),
						dict.objectForKey("JVMVersion").toString(),
						isArch64Bit(dict.objectForKey("JVMArch").toString())
				));
			}
		} catch (Throwable err) {
			log.log(Level.WARNING, "Failed to parse java_home command", err);
		}

		return entries;
	}

	private static boolean isArch64Bit(String string) {
		return string == null || string.matches("x64|x86_64|amd64|aarch64");
	}
}
