package com.skcraft.launcher.model.minecraft;

public enum Side {
	CLIENT("client"),
	SERVER("server");

	private String name;

	Side(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
