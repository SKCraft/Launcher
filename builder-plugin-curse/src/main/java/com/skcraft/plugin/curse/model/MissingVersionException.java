package com.skcraft.plugin.curse.model;

public class MissingVersionException extends Exception {
	public MissingVersionException() {
		super();
	}

	public MissingVersionException(String message) {
		super(message);
	}
}
