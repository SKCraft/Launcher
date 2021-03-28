package com.skcraft.launcher.creator.plugin;

import lombok.Data;

@Data
public class CreatorPluginWrapper<T extends CreatorToolsPlugin> {
	private final CreatorPluginInfo info;
	private final T instance;
}
