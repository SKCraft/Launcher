package com.skcraft.launcher.creator.plugin;

import com.beust.jcommander.internal.Lists;
import com.skcraft.launcher.builder.plugin.BuilderPlugin;

import java.util.List;

public abstract class CreatorToolsPlugin {
	public abstract String getName();

	public List<PluginMenu> getPluginMenus() {
		return Lists.newArrayList();
	}

	public Class<? extends BuilderPlugin> getBuilderPlugin() {
		return null;
	}
}
