package com.skcraft.launcher.creator.plugin;

import com.skcraft.launcher.builder.plugin.BuilderPlugin;

public abstract class CreatorToolsPlugin {
	public Class<? extends BuilderPlugin> getBuilderPlugin() {
		return null;
	}
}
