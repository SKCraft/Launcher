package com.skcraft.launcher.builder.plugin;

import com.skcraft.launcher.model.modpack.Manifest;

/**
 * Class that all builder plugins should extend.
 */
public abstract class BuilderPlugin {
	public void acceptOptions(String[] args) {}
	public void onManifestCreated(Manifest manifest) {}
	public void onBuilderFinished(Builder builder) {}
}
