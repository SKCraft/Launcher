package com.skcraft.launcher.builder.plugin;

import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.model.modpack.Manifest;

/**
 * Class that all builder plugins should extend.
 */
public abstract class BuilderPlugin {
	public void acceptOptions(BuilderOptions options, String[] args) {}
	public void onManifestCreated(Manifest manifest) {}
	public void onBuild(Builder builder) {}
}
