package com.skcraft.launcher.launch.runtime;

import java.io.File;

/**
 * Special sentinel subclass used to represent the "add custom runtime" entry in the dropdown
 */
public class AddJavaRuntime extends JavaRuntime {
	public AddJavaRuntime() {
		super(new File(""), "", false);
	}

	@Override
	public String toString() {
		return "Add undetected Java runtime...";
	}

	public static final AddJavaRuntime ADD_RUNTIME_SENTINEL = new AddJavaRuntime();
}
